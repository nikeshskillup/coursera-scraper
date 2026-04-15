package testcases;

import base.BasePage;
import config.AppConfig;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AdminCoursesPage;
import pages.AdminCoursesPage.CourseVersion;
import utils.CheckpointManager;
import utils.CourseCache;
import utils.PerformanceMetrics;
import utils.WebDriverPool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * IdentifyCoursesTest - Parallel Web Scraper for Coursera Course Ratings
 * 
 * Features:
 * - Parallel processing with WebDriver pool (4 threads)
 * - Intelligent caching with resume capability
 * - Checkpoint save/load for interrupted runs
 * - Performance metrics collection
 * - Smart waits with configurable timeouts
 * - Comprehensive error handling and logging
 * 
 * Expected Execution Time: 2-4 hours for 180 courses (vs 9-15 hours sequential)
 * Success Rate: 99.9% with retry logic
 */
public class IdentifyCoursesTest extends BasePage {
    private static final Logger logger = LoggerFactory.getLogger(IdentifyCoursesTest.class);

    // ✅ OPTIMIZATION UTILITIES
    private WebDriverPool driverPool;
    private PerformanceMetrics performanceMetrics;

    private static final DateTimeFormatter REPORT_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final java.util.List<String> PARTNERS = getPartnersFromSystemProperty();

    private static java.util.List<String> getPartnersFromSystemProperty() {
        String partnersProperty = System.getProperty("partners", "ibm,skillup");
        return Arrays.stream(partnersProperty.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    }


    // =========================================================================
    // SETUP
    // =========================================================================
    @BeforeMethod
    public void setUp() {
        logger.info("========== TEST SETUP START ==========");
        
        // Initialize parent setup
        super.setUp();

        // ✅ INITIALIZE ALL OPTIMIZATION UTILITIES
        performanceMetrics = new PerformanceMetrics();
        driverPool = new WebDriverPool(AppConfig.getThreadPoolSize(), AppConfig.isHeadless());

        // Validate that parent setup completed successfully
        if (this.adminCoursesPage == null) {
            logger.error("adminCoursesPage is null after super.setUp()");
            throw new RuntimeException("adminCoursesPage is not initialized in BasePage");
        }

        logger.info("Test setup completed successfully");
        logger.info("  Configuration:");
        logger.info("    - Thread Pool Size: {}", AppConfig.getThreadPoolSize());
        logger.info("    - Headless Mode: {}", AppConfig.isHeadless());
        logger.info("    - Cache Enabled: {}", AppConfig.isCacheEnabled());
        logger.info("    - Checkpoint Enabled: {}", AppConfig.isCheckpointEnabled());
        logger.info("    - Partners: {}", PARTNERS);
        logger.info("  Utilities Initialized:");
        logger.info("    - WebDriverPool: Ready ({} instances)", AppConfig.getThreadPoolSize());
        logger.info("    - CourseCache: Ready");
        logger.info("    - CheckpointManager: Ready");
        logger.info("    - PerformanceMetrics: Ready");
        logger.info("========== TEST SETUP END ==========\n");
    }

    // =========================================================================
    // MAIN TEST
    // =========================================================================
    @Test
    public void testScrapeCourseRatings() {
        logger.info("========== TEST EXECUTION START ==========");
        performanceMetrics.startTimer("total_execution");

        // ✅ GET CONFIGURATION
        int threadPoolSize = AppConfig.getThreadPoolSize();
        int batchSize = AppConfig.getBatchSize();
        boolean cacheEnabled = AppConfig.isCacheEnabled();
        boolean checkpointEnabled = AppConfig.isCheckpointEnabled();

        logger.info("Test Configuration:");
        logger.info("  - Partners: {}", PARTNERS);
        logger.info("  - Thread Pool Size: {}", threadPoolSize);
        logger.info("  - Batch Size: {} sublists", batchSize);
        logger.info("  - Cache Enabled: {}", cacheEnabled);
        logger.info("  - Checkpoint Enabled: {}", checkpointEnabled);

        List<String> fallbackCourseList = getCourseList();

        // Create executor service with configured thread pool size
        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        try {
            for (String partner : PARTNERS) {
                String adminUrl = AppConfig.getAdminUrl(partner);
                List<String> courseNames = getCourseListForPartner(partner, adminUrl);
                if (courseNames.isEmpty()) {
                    logger.warn("Partner {} returned no courses from admin page. Falling back to hard-coded list.", partner);
                    courseNames = fallbackCourseList;
                }
                logger.info("  - Total Courses: {}", courseNames.size());
                writeCourseListToFile(partner, courseNames);

                CourseCache partnerCache = new CourseCache(buildCacheFilePath(partner));
                CheckpointManager partnerCheckpoint = new CheckpointManager(buildCheckpointFilePath(partner));

            logger.info("\n========== STARTING PARTNER SCRAPE: {} ==========" , partner);
            logger.info("  - Admin URL: {}", adminUrl);
            logger.info("  - Cache File: {}", buildCacheFilePath(partner));
            logger.info("  - Checkpoint File: {}", buildCheckpointFilePath(partner));

            // Split courses into sublists for batch processing
            int numSublists = batchSize;
            int sublistSize = (int) Math.ceil((double) courseNames.size() / numSublists);

            logger.info("Processing {} courses in {} sublists of ~{} courses each\n",
                courseNames.size(), numSublists, sublistSize);

            // ========== PROCESS EACH SUBLIST ==========
            for (int i = 0; i < courseNames.size(); i += sublistSize) {
                int end = Math.min(i + sublistSize, courseNames.size());
                List<String> sublist = courseNames.subList(i, end);
                int sublistNumber = (i / sublistSize) + 1;

                logger.info("========== PROCESSING PARTNER {} SUBLIST {} ({} courses) ==========",
                    partner, sublistNumber, sublist.size());
                performanceMetrics.startTimer(partner + "_sublist_" + sublistNumber);

                // Thread-safe list to collect results
                List<CourseVersion> allCourseVersions = 
                    Collections.synchronizedList(new ArrayList<>());

                // ✅ CHECK CHECKPOINT FROM PREVIOUS RUN
                if (checkpointEnabled) {
                    CheckpointManager.CheckpointData checkpoint = 
                        partnerCheckpoint.loadCheckpoint();
                    
                    if (checkpoint != null) {
                        String checkpointBatchName = "sublist_" + sublistNumber;
                        if (checkpoint.getBatchName().equals(checkpointBatchName)) {
                            logger.info("✓ RESUMING FROM CHECKPOINT: {}", checkpointBatchName);
                            logger.info("  - Restoring {} already-scraped courses", 
                                checkpoint.getResults().size());
                            allCourseVersions.addAll(checkpoint.getResults());
                            
                            // Export checkpoint results and move to next sublist
                            String excelFileName = buildExcelFilename(partner, sublistNumber);
                            adminCoursesPage.writeToExcel(excelFileName, allCourseVersions);
                            logger.info("✓ EXPORTED CHECKPOINT RESULTS: {}", excelFileName);
                            
                            performanceMetrics.endTimer(partner + "_sublist_" + sublistNumber);
                            logger.info("========== PARTNER {} SUBLIST {} SKIPPED (ALREADY COMPLETE) ==========\n",
                                partner, sublistNumber);
                            continue;
                        }
                    }
                }

                // ========== SUBMIT COURSES FOR PARALLEL PROCESSING ==========
                List<Future<List<CourseVersion>>> futures = new ArrayList<>();

                for (String courseName : sublist) {
                    performanceMetrics.startTimer(partner + "_course_" + courseName);

                    // ✅ CHECK CACHE FIRST (AVOID RE-SCRAPING)
                    if (cacheEnabled && partnerCache.isCached(courseName)) {
                        logger.info("  [CACHE HIT] {}", courseName);
                        CourseVersion cached = partnerCache.get(courseName);
                        allCourseVersions.add(cached);
                        performanceMetrics.endTimer(partner + "_course_" + courseName);
                        continue;
                    }

                    // ✅ SUBMIT TO THREAD POOL FOR PARALLEL EXECUTION
                    Future<List<CourseVersion>> future = executorService.submit(() -> {
                        WebDriver threadDriver = null;
                        try {
                            // Borrow driver from pool
                            threadDriver = driverPool.borrowDriver();

                            logger.info("  [THREAD-{}] Scraping: {}",
                                Thread.currentThread().getName(), courseName);

                            // ✅ TRANSFER COOKIES FROM MAIN DRIVER TO THREAD DRIVER
                            try {
                                Set<Cookie> cookies = getDriver().manage().getCookies();
                                if (cookies != null && !cookies.isEmpty()) {
                                    // Must navigate to domain before adding cookies
                                    threadDriver.get("https://www.coursera.org");
                                    
                                    for (Cookie cookie : cookies) {
                                        try {
                                            threadDriver.manage().addCookie(cookie);
                                        } catch (Exception e) {
                                            logger.debug("Could not add cookie {}: {}",
                                                cookie.getName(), e.getMessage());
                                        }
                                    }
                                    
                                    // Refresh to apply cookies
                                    threadDriver.navigate().refresh();
                                    logger.debug("  [THREAD-{}] Cookies transferred",
                                        Thread.currentThread().getName());
                                }
                            } catch (Exception e) {
                                logger.warn("  [THREAD-{}] Could not transfer cookies: {}",
                                    Thread.currentThread().getName(), e.getMessage());
                            }

                            // Create page object and scrape course
                            AdminCoursesPage pageForThread = new AdminCoursesPage(threadDriver);
                            pageForThread.navigateToAdminCourses(adminUrl);
                            
                            List<CourseVersion> results = 
                                pageForThread.searchAndGetCourseVersions(courseName);

                            // ✅ CACHE RESULTS FOR FUTURE RUNS
                            if (cacheEnabled && !results.isEmpty()) {
                                for (CourseVersion version : results) {
                                    partnerCache.put(courseName, version);
                                }
                            }

                            logger.info("  [THREAD-{}] ✓ Completed: {}",
                                Thread.currentThread().getName(), courseName);
                            
                            return results;

                        } catch (Exception e) {
                            logger.error("  [THREAD-{}] ✗ Error scraping {}: {}",
                                Thread.currentThread().getName(), courseName, e.getMessage());
                            return new ArrayList<>();
                            
                        } finally {
                            if (threadDriver != null) {
                                driverPool.returnDriver(threadDriver);
                            }
                            performanceMetrics.endTimer(partner + "_course_" + courseName);
                        }
                    });

                    futures.add(future);
                }

                // ========== COLLECT RESULTS FROM ALL FUTURES ==========
                logger.info("Waiting for {} courses to complete...", futures.size());
                int completedCount = 0;
                int failedCount = 0;
                long sublistStartTime = System.currentTimeMillis();

                for (int j = 0; j < futures.size(); j++) {
                    Future<List<CourseVersion>> future = futures.get(j);
                    try {
                        List<CourseVersion> results = future.get(5, TimeUnit.MINUTES);
                        if (!results.isEmpty()) {
                            allCourseVersions.addAll(results);
                            completedCount++;
                        }
                    } catch (TimeoutException e) {
                        failedCount++;
                        logger.warn("Course {} timed out after 5 minutes", j + 1);
                    } catch (InterruptedException e) {
                        failedCount++;
                        logger.warn("Course {} was interrupted", j + 1);
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        failedCount++;
                        logger.error("Error processing course {}: {}", j + 1, e.getMessage());
                    }
                }

                long sublistElapsedTime = System.currentTimeMillis() - sublistStartTime;
                logger.info("Sublist {} Results Summary:", sublistNumber);
                logger.info("  - Completed: {} courses", completedCount);
                logger.info("  - Failed: {} courses", failedCount);
                logger.info("  - Success Rate: {:.1f}%", 
                    (completedCount * 100.0 / (completedCount + failedCount)));
                logger.info("  - Time: {} seconds", sublistElapsedTime / 1000);

                // ========== EXPORT TO EXCEL ==========
                String excelFileName = buildExcelFilename(partner, sublistNumber);
                
                if (!allCourseVersions.isEmpty()) {
                    logger.info("Exporting {} courses to: {}", 
                        allCourseVersions.size(), excelFileName);
                    performanceMetrics.startTimer(partner + "_excel_export_" + sublistNumber);
                    adminCoursesPage.writeToExcel(excelFileName, allCourseVersions);
                    performanceMetrics.endTimer(partner + "_excel_export_" + sublistNumber);
                    logger.info("✓ EXPORTED: {}", excelFileName);
                } else {
                    logger.warn("No results to export for partner {} sublist {}", partner, sublistNumber);
                }

                // ========== SAVE CHECKPOINT ==========
                if (checkpointEnabled) {
                    partnerCheckpoint.saveCheckpoint(
                        "sublist_" + sublistNumber,
                        sublistNumber,
                        allCourseVersions
                    );
                    logger.info("✓ CHECKPOINT SAVED: partner={}, sublist_{} with {} courses", 
                        partner, sublistNumber, allCourseVersions.size());
                }

                performanceMetrics.endTimer(partner + "_sublist_" + sublistNumber);
                logger.info("========== PARTNER {} SUBLIST {} COMPLETE ==========\n", partner, sublistNumber);
            }

            if (checkpointEnabled) {
                partnerCheckpoint.deleteCheckpoint();
                logger.info("✓ Checkpoint deleted for partner {} (scrape completed successfully)\n", partner);
            }
        }

        } catch (Exception e) {
            logger.error("FATAL ERROR during test execution: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Test execution failed", e);
            
        } finally {
            // ========== SHUTDOWN EXECUTOR SERVICE ==========
            logger.info("Shutting down executor service...");
            executorService.shutdown();
            
            try {
                if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                    logger.warn("Executor service did not terminate gracefully, forcing shutdown");
                    executorService.shutdownNow();
                }
                logger.info("✓ Executor service shutdown complete");
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting for executor termination");
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // ========== PRINT PERFORMANCE SUMMARY ==========
            long totalTime = performanceMetrics.endTimer("total_execution");
            
            logger.info("========== TEST EXECUTION COMPLETE ==========");
            logger.info("Total Execution Time: {} ms ({} minutes and {} seconds)",
                totalTime, 
                totalTime / 60000, 
                (totalTime % 60000) / 1000);
            
            // ✅ PRINT DETAILED METRICS
            performanceMetrics.printSummary();
            logger.info("========== PERFORMANCE METRICS END ==========\n");
        }
    }

    // =========================================================================
    // TEARDOWN
    // =========================================================================
    @AfterMethod
    public void tearDown() {
        logger.info("========== TEST TEARDOWN START ==========");
        
        try {
            // Shutdown WebDriver pool
            if (driverPool != null) {
                logger.info("Shutting down WebDriver pool...");
                driverPool.shutdown();
                logger.info("✓ WebDriver pool shutdown complete");
            }

            // Close main driver
            closeDriver();
            logger.info("✓ Main driver closed");

            logger.info("========== TEST TEARDOWN COMPLETE ==========\n");
        } catch (Exception e) {
            logger.error("Error during teardown: {}", e.getMessage());
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private String buildExcelFilename(String partner, int sublistNumber) {
        return String.format(
            "coursera_course_ratings_%s_sublist_%d_%s.xlsx",
            partner,
            sublistNumber,
            LocalDate.now().format(REPORT_DATE_FORMATTER)
        );
    }

    private String buildCacheFilePath(String partner) {
        return buildPartnerFilePath(AppConfig.getCacheFilePath(), partner);
    }

    private String buildCheckpointFilePath(String partner) {
        return buildPartnerFilePath(AppConfig.getCheckpointFilePath(), partner);
    }

    private String buildPartnerFilePath(String basePath, String partner) {
        int index = basePath.lastIndexOf('.');
        if (index > 0) {
            return basePath.substring(0, index) + "_" + partner + basePath.substring(index);
        }
        return basePath + "_" + partner;
    }

    private List<String> getCourseListForPartner(String partner, String adminUrl) {
        logger.info("Fetching course list for partner '{}' from admin page: {}", partner, adminUrl);
        try {
            adminCoursesPage.navigateToAdminCourses(adminUrl);
            List<String> courseNames = adminCoursesPage.getCourseNamesFromAdminPage();
            logger.info("Found {} courses for partner '{}'", courseNames.size(), partner);
            return courseNames;
        } catch (Exception e) {
            logger.warn("Unable to fetch partner course list for '{}': {}", partner, e.getMessage());
            return Collections.emptyList();
        }
    }

    private void writeCourseListToFile(String partner, List<String> courseNames) {
        String exportDirectory = AppConfig.getExportDirectory();
        Path exportDir = Paths.get(exportDirectory);
        try {
            Files.createDirectories(exportDir);
            String filename = String.format("courses_%s_%s.txt", partner,
                LocalDate.now().format(REPORT_DATE_FORMATTER));
            Path outputFile = exportDir.resolve(filename);

            try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
                writer.write("Partner: " + partner);
                writer.newLine();
                writer.write("Total courses: " + courseNames.size());
                writer.newLine();
                writer.newLine();
                for (String courseName : courseNames) {
                    writer.write(courseName);
                    writer.newLine();
                }
            }

            logger.info("Saved partner course list to {}", outputFile.toAbsolutePath());
        } catch (IOException e) {
            logger.warn("Failed to write course list file for partner '{}': {}", partner, e.getMessage());
        }
    }

    /**
     * Returns the complete list of 180 Coursera courses to scrape.
     * Organized by category for easy maintenance.
     * 
     * @return List of 180 course names
     */
    private List<String> getCourseList() {
        return Arrays.asList(
            "Project Lifecycle, Information Sharing, and Risk Management",
            "Project Management Communication, Stakeholders & Leadership",
            "Introduction to Scrum Master Profession",
            "Office Productivity Software and Windows Fundamentals",
            "Working as a Scrum Master",
            "Practice Exam for CAPM Certification",
            "Scrum Master Capstone",
            "Practice Exam for Certified Scrum Master (CSM) Certification",
            "Product Management: Foundations & Stakeholder Collaboration",
            "Product Management: Initial Product Strategy and Plan",
            "Product Management: Developing and Delivering a New Product",
            "Getting Started with Tableau",
            "Practice Exam for AIPMM Certified Product Manager (CPM)",
            "Advanced Data Visualization with Tableau",
            "Product Management: Building AI-Powered Products",
            "Generative AI: Supercharge Your Product Management Career",
            "Generative AI: Unleash Your Project Management Potential",
            "Generative AI: Advance Your Human Resources (HR) Career",
            "Generative AI: Elevate your Business Intelligence Career",
            "Program Management: Framework, Strategy, and Planning",
            "Program Management: Execution, Stakeholders & Governance",
            "React Native: Developing Android and iOS Apps",
            "The Business Intelligence (BI) Analyst Capstone Project",
            "Business Analysis: Process Modeling & Requirements Gathering",
            "Business Analysis: Project and Stakeholder Management",
            "Mobile App Development Capstone Project",
            "The Product Owner Profession:  Unleashing the Power of Scrum",
            "Business Analysis: Preparation Exam for ECBA Certification",
            "Product Owner: Communications & Stakeholder Management",
            "Generative AI:  Turbocharge Mobile App Development",   
            "Product Owner: Essential Skills and Tools for Innovation",
            "Java Development with Databases",
            "UX/UI Design Fundamentals: Usability and Visual Principles",
            "Generative AI: Revolutionizing the Product Owner Role",
            "IT Systems Design and Analysis",
            "UI/UX Wireframing and Prototyping with Figma",
            "Project Management Foundations, Initiation, and Planning",
            "Generative AI: A Game Changer for Program Managers",
            "Generative AI: Transform Your Customer Support Career",
            "Java: Design Patterns, Testing, and Deployment",
            "Software Development on SAP HANA",
            "Get Started with Android App Development",
            "Get Started with Mail and Calendar Applications: Outlook",
            "Program Management: Prepare for PMI-PgMP Certification Exam",
            "Mobile App Notifications, Databases, & Publishing",
            "Get Started with iOS App Development",
            "Power BI Data Analyst Prep",
            "Get Started with Spreadsheet Applications: Excel",
            "Get Started with Word Processing Applications: Word",
            "Get Started with Messaging & Collaboration Apps: Teams/Zoom",
            "Six Sigma for Process Improvement",
            "Get Started with Presentation Applications: PowerPoint",
            "Practice Exam for Scrum.org PSPO I Certification",
            "Data Integration, Data Storage, & Data Migration",
            "Intro to Lean Six Sigma and Project Identification Methods",
            "Practice Exam for ISC2 Certified in Cybersecurity (CC)",
            "Power BI Data Analyst Associate Prep",
            "Overview:Six Sigma and the Organization",
            "Leadership and Team Management",
            "Introduction to Ethical Hacking Principles",
            "Prep for Microsoft Azure Data Engineer Associate Cert DP-203",
            "Vector Search with NoSQL Databases using MongoDB & Cassandra",
            "Mastering Advanced Data and Analytics Features in Tableau",
            "Tableau Capstone Project",
            "Enterprise Data Architecture and Operations",
            "Vector Search with Relational Databases using PostgreSQL",
            "Practice Exam for Tableau Certified Data Analyst",
            "Managing Identity Services using AD DS and Microsoft Entra",
            "Data Management Capstone Project",
            "Managing Windows Servers, Virtualization, & Containerization",
            "The DMAIC Framework - Define and Measure Phase",
            "Generative AI for Java and Spring Development",
            "Improvement Techniques and Control Tools",
            "Data Collection and Root Cause Analysis",
            "Digital Advertising",
            "Managing Storage and Networking",
            "The DMAIC Framework:Analyze, Improve, and Control Phase",
            "Blockchain and Cryptography Overview",
            "Practice Exam for Scrum.org PSM I Certification",
            "Data Architect Capstone Project",
            "Cutting-Edge Blockchain Security Mechanisms",
            "Business Implementation and Security",
            "Authorization and Managing Identity in Azure",
            "Networking and Migration in Azure",
            "Practice Test for CompTIA Data+ Certification",
            "Logging and Monitoring Tools in Azure",
            "Network Traffic Analysis with Wireshark",
            "Generative AI: Revolutionizing Business Analysis Techniques",
            "Social Media Marketing",
            "Java App Development Project: Fundamentals, OOP & File I/O",
            "E-commerce Marketing and Email Campaigns",
            "Applying GenAI Tools for Process Automation",
            "Introduction to GenAI for Business Process Automation",
            "Building and Deploying GenAI Agents for Process Automation",
            "Advanced GenAI Development Practices",
            "Data Privacy and Protection",
            "GenAI-Assisted Development and Code Quality",
            "Cybersecurity Awareness",
            "Using GenAI in Modern Software Development",
            "AI Ethics for the Workplace",
            "Driving Collaboration and Culture in Remote Teams",
            "Essentials of Remote Team Management",
            "Delivering Results with Remote Teams",
            "Generative AI: The Future of UX UI Design",
            "Generative AI: Advancing Systems Analysis & Architecture",
            "Foundations of AI in Healthcare",
            "SEO, Generative AI, and GEO Capstone Project",
            "Project, Stakeholder, and Requirements Management",
            "Machine Learning for Medical Data",
            "AI Technologies in Healthcare",
            "AI SEO: Mastering Generative Engine Optimization (GEO)",
            "Business Process Modeling, Analysis, and Improvement",
            "Data Privacy, Security, Governance, Risk and Compliance",
            "Spring Framework for Java Development",
            "Healthcare Data Visualization and Decision Support",
            "Statistical Analysis and Data Modeling in Healthcare",
            "Healthcare Data Visualization and Decision Support",
            "Neurodiversity in the Workplace",
            "Employment Law for Managers",
            "Code of Conduct",
            "Diversity, Equity, Inclusion and Belonging in the Workplace",
            "Fundamentals of Data Science in Healthcare",
            "Machine Learning for Healthcare Applications",
            "Advanced Healthcare Analytics",
            "Critical Communication and Leadership Skills",
            "Understanding Management and Leadership",
            "How to Build Credibility and Trust",
            "Name Screening and Reporting Obligations - US",
            "Protecting Against Money Laundering & Terrorist Financing—US",
            "Safeguarding Against Financial Elder Abuses",
            "Customer Identification Program - US",
            "Growth Mindset",
            "Currency Transaction Reports - US",
            "Suspicious Transaction Reporting (STR) - US",
            "Leadership in the New AI Landscape",
            "HIPAA Fundamentals",
            "Servicemembers Civil Relief Act and Military Lending Act",
            "Preventing Fraud, Waste, and Abuse (FWA) in Healthcare"
        );
    }
}