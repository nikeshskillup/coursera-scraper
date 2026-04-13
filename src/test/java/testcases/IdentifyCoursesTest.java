package testcases;

import base.BasePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AdminCoursesPage.CourseVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdentifyCoursesTest extends BasePage {
    private static final Logger logger = LoggerFactory.getLogger(IdentifyCoursesTest.class);

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up test...");
        super.setUp();
        if (this.adminCoursesPage == null) {
            logger.error("adminCoursesPage is null after super.setUp(). Check BasePage initialization.");
            throw new RuntimeException("adminCoursesPage is not initialized in BasePage.");
        }
    }

    @Test
    public void testScrapeCourseRatings() {
        logger.info("Starting test to scrape course ratings for multiple courses...");
 String adminUrl = "https://www.coursera.org/admin/ibm-skills-network/home/courses";
//String adminUrl =  "https://www.coursera.org/admin/skillup-ed-tech/home/courses";
      
   List<String> courseNames = Arrays.asList(
      
//    "Project Lifecycle, Information Sharing, and Risk Management",
//     "Project Management Communication, Stakeholders & Leadership",
//     "Introduction to Scrum Master Profession",
//     "Office Productivity Software and Windows Fundamentals",
//     "Working as a Scrum Master",
//     "Practice Exam for CAPM Certification",
//     "Scrum Master Capstone",
//     "Practice Exam for Certified Scrum Master (CSM) Certification",
//     "Product Management: Foundations & Stakeholder Collaboration",
//     "Product Management: Initial Product Strategy and Plan",
//     "Product Management: Developing and Delivering a New Product",
//     "Getting Started with Tableau",
//     "Practice Exam for AIPMM Certified Product Manager (CPM)",
//     "Advanced Data Visualization with Tableau",
//     "Product Management: Building AI-Powered Products",
//     "Generative AI: Supercharge Your Product Management Career",
//     "Generative AI: Unleash Your Project Management Potential",
//     "Generative AI: Advance Your Human Resources (HR) Career",
//     "Generative AI: Elevate your Business Intelligence Career",
//     "Program Management: Framework, Strategy, and Planning",
//     "Program Management: Execution, Stakeholders & Governance",
//     "React Native: Developing Android and iOS Apps",
//     "The Business Intelligence (BI) Analyst Capstone Project",
//     "Business Analysis: Process Modeling & Requirements Gathering",
//     "Business Analysis: Project and Stakeholder Management",
//     "Mobile App Development Capstone Project",
//     "The Product Owner Profession:  Unleashing the Power of Scrum",
//     "Business Analysis: Preparation Exam for ECBA Certification",
//     "Product Owner: Communications & Stakeholder Management",
//     "Generative AI:  Turbocharge Mobile App Development",   
//     "Product Owner: Essential Skills and Tools for Innovation",
//     "Java Development with Databases",
//     "UX/UI Design Fundamentals: Usability and Visual Principles",
//     "Generative AI: Revolutionizing the Product Owner Role",
//     "IT Systems Design and Analysis",
//     "UI/UX Wireframing and Prototyping with Figma",
//     "Project Management Foundations, Initiation, and Planning",
//     "Generative AI: A Game Changer for Program Managers",
//     "Generative AI: Transform Your Customer Support Career",
//     "Java: Design Patterns, Testing, and Deployment",
//     "Software Development on SAP HANA",
//     "Get Started with Android App Development",
//     "Get Started with Mail and Calendar Applications: Outlook",
//     "Program Management: Prepare for PMI-PgMP Certification Exam",
//     "Mobile App Notifications, Databases, & Publishing",
//     "Get Started with iOS App Development",
//     "Power BI Data Analyst Prep",
//     "Get Started with Spreadsheet Applications: Excel",
//     "Get Started with Word Processing Applications: Word",
//     "Get Started with Messaging & Collaboration Apps: Teams/Zoom",
//     "Six Sigma for Process Improvement",
//     "Get Started with Presentation Applications: PowerPoint",
//     "Practice Exam for Scrum.org PSPO I Certification",
//     "Data Integration, Data Storage, & Data Migration",
//     "Intro to Lean Six Sigma and Project Identification Methods",
//     "Practice Exam for ISC2 Certified in Cybersecurity (CC)",
//     "Power BI Data Analyst Associate Prep",
//     "Overview:Six Sigma and the Organization",
//     "Leadership and Team Management",
//     "Introduction to Ethical Hacking Principles",
//     "Prep for Microsoft Azure Data Engineer Associate Cert DP-203",
//     "Vector Search with NoSQL Databases using MongoDB & Cassandra",
//     "Mastering Advanced Data and Analytics Features in Tableau",
//     "Tableau Capstone Project",
//     "Enterprise Data Architecture and Operations",
//     "Vector Search with Relational Databases using PostgreSQL",
//     "Practice Exam for Tableau Certified Data Analyst",
//     "Managing Identity Services using AD DS and Microsoft Entra",
//     "Data Management Capstone Project",
//     "Managing Windows Servers, Virtualization, & Containerization",
//     "The DMAIC Framework - Define and Measure Phase",
//     "Generative AI for Java and Spring Development",
//     "Improvement Techniques and Control Tools",
//     "Data Collection and Root Cause Analysis",
//     "Digital Advertising",
//     "Managing Storage and Networking",
//     "The DMAIC Framework:Analyze, Improve, and Control Phase",
//     "Blockchain and Cryptography Overview",
//     "Practice Exam for Scrum.org PSM I Certification",
//     "Data Architect Capstone Project",
//     "Cutting-Edge Blockchain Security Mechanisms",
//     "Business Implementation and Security",
//     "Authorization and Managing Identity in Azure",
//     "Networking and Migration in Azure",
//     "Practice Test for CompTIA Data+ Certification",
//     "Logging and Monitoring Tools in Azure",
//     "Network Traffic Analysis with Wireshark",
//     "Generative AI: Revolutionizing Business Analysis Techniques",
//     "Social Media Marketing",
//     "Java App Development Project: Fundamentals, OOP & File I/O",
//     "E-commerce Marketing and Email Campaigns",
//     "Applying GenAI Tools for Process Automation",
//     "Introduction to GenAI for Business Process Automation",
//     "Building and Deploying GenAI Agents for Process Automation",
//     "Advanced GenAI Development Practices",
//     "Data Privacy and Protection",
//     "GenAI-Assisted Development and Code Quality",
//     "Cybersecurity Awareness",
//     "Using GenAI in Modern Software Development",
//     "AI Ethics for the Workplace",
//     "Driving Collaboration and Culture in Remote Teams",
//     "Essentials of Remote Team Management",
//     "Delivering Results with Remote Teams",
//     "Generative AI: The Future of UX UI Design",
//     "Generative AI: Advancing Systems Analysis & Architecture",
//     "Foundations of AI in Healthcare",
//     "SEO, Generative AI, and GEO Capstone Project",
//     "Project, Stakeholder, and Requirements Management",
//     "Machine Learning for Medical Data",
//     "AI Technologies in Healthcare",
//     "AI SEO: Mastering Generative Engine Optimization (GEO)",
//     "Business Process Modeling, Analysis, and Improvement",
//     "Data Privacy, Security, Governance, Risk and Compliance",
//     "Spring Framework for Java Development",
//    "Healthcare Data Visualization and Decision Support",
//    "Statistical Analysis and Data Modeling in Healthcare",
//    "Healthcare Data Visualization and Decision Support",
//    "Neurodiversity in the Workplace",
//    "Employment Law for Managers",
//    "Code of Conduct",
//    "Diversity, Equity, Inclusion and Belonging in the Workplace",
//    "Fundamentals of Data Science in Healthcare",
//    "Machine Learning for Healthcare Applications",
//    "Advanced Healthcare Analytics",
//   "Critical Communication and Leadership Skills",
//   "Understanding Management and Leadership",
//   "How to Build Credibility and Trust",
//   "Name Screening and Reporting Obligations - US",
//   "Protecting Against Money Laundering & Terrorist Financing—US",
//   "Safeguarding Against Financial Elder Abuses",
//   "Customer Identification Program - US",
//   "Growth Mindset",
//   "Currency Transaction Reports - US",
//   "Suspicious Transaction Reporting (STR) - US",
//   "Leadership in the New AI Landscape",
//   "HIPAA Fundamentals",
// "Servicemembers Civil Relief Act and Military Lending Act",
// "Preventing Fraud, Waste, and Abuse (FWA) in Healthcare"

     //  Full list of 180 courses

// //List<String> courseNames = Arrays.asList(
    // "What is Data Science?",
    // "SQL for Data Science with R",
    // "Python for Data Science, AI & Development",
    // "Tools for Data Science",
    // "Databases and SQL for Data Science with Python",
    // "Data Analysis with Python",
    // "Software Testing, Deployment, and Maintenance Strategies",
    // "UX Research and Information Architecture",
    // "Advanced RAG with Vector Databases and Retrievers",
    // "Data Science Methodology",
    // "Machine Learning with Python",
    // "Introduction to Data Analytics",
    // "Data Visualization with Python",
    // "Python Project for Data Science",
    // "Excel Basics for Data Analysis",
    // "Applied Data Science Capstone",
    // "Introduction to Cybersecurity Tools & Cyberattacks",
    // "Introduction to Cloud Computing",
    // "Data Visualization and Dashboards with Excel and Cognos",
    // "Introduction to Artificial Intelligence (AI)",
    // "Introduction to Data Engineering",
    // "Operating Systems: Overview, Administration, and Security",
    // "Introduction to Web Development with HTML, CSS, JavaScript",
    // "IBM Data Analyst Capstone Project",
    // "Getting Started with Git and GitHub",
    // "Cybersecurity Compliance Framework, Standards & Regulations",
    // "Developing AI Applications with Python and Flask",
    // "Hands-on Introduction to Linux Commands and Shell Scripting",
    // "Introduction to Software Engineering",
    // "Introduction to DevOps",
    // "Computer Networks and Network Security",
    // "Penetration Testing, Threat Hunting, and Cryptography",
    // "Introduction to Containers w/ Docker, Kubernetes & OpenShift",
    // "Generative AI: Introduction and Applications",
    // "Incident Response and Digital Forensics",
    // "Introduction to Agile Development and Scrum",
    // "Exploratory Data Analysis for Machine Learning",
    // "Building AI Powered Chatbots Without Programming",
    // "Cybersecurity Case Studies and Capstone Project",
    // "Introduction to Deep Learning & Neural Networks with Keras",
    // "Generative AI: Prompt Engineering Basics",
    // "Introduction to Neural Networks and PyTorch",
    // "Developing Front-End Apps with React",
    // "Introduction to Relational Databases (RDBMS)",
    // "Python Project for Data Engineering",
    // "Introduction to Computer Vision and Image Processing",
    // "Application Development using Microservices and Serverless",
    // "Cybersecurity Assessment: CompTIA Security+ & CYSA+",
    // "ETL and Data Pipelines with Shell, Airflow and Kafka",
    // "Developing Back-End Apps with Node.js and Express",
    // "Introduction to Project Management",
    // "Django Application Development with SQL and Databases",
    // "Deep Learning with Keras and Tensorflow",
    // "Introduction to Big Data with Spark and Hadoop",
    // "Introduction to Hardware and Operating Systems",
    // "Relational Database Administration (DBA)",
    // "Supervised Machine Learning: Regression",
    // "AI Capstone Project with Deep Learning",
    // "Data Warehouse Fundamentals",
    // "Introduction to NoSQL Databases",
    // "Introduction to R Programming for Data Science",
    // "Introduction to Technical Support",
    // "Full Stack Application Development Capstone Project",
    // "Product Management: An Introduction",
    // "Supervised Machine Learning: Classification",
    // "Full Stack Software Developer Assessment",
    // "Introduction to Cybersecurity Essentials",
    // "Scalable Machine Learning on Big Data using Apache Spark",
    // "Unsupervised Machine Learning",
    // "Deep Learning and Reinforcement Learning",
    // "Introduction to Software, Programming, and Databases",
    // "Data Engineering Capstone Project",
    // "Generative AI: Elevate Your Data Science Career",
    // "Introduction to Networking and Storage",
    // "Continuous Integration and Continuous Delivery (CI/CD)",
    // "Statistics for Data Science with Python",
    // "Introduction to Test and Behavior Driven Development",
    // "Introduction to Cybersecurity Careers",
    // "Data Scientist Career Guide and Interview Preparation",
    // "Application Security for Developers and DevOps Professionals",
    // "Assessment for Data Analysis and Visualization Foundations",
    // "SQL: A Practical Introduction for Querying Databases",
    // "Data Analysis with R",
    // "Generative AI: Enhance your Data Analytics Career",
    // "DevOps Capstone Project",
    // "Machine Learning with Apache Spark",
    // "Software Developer Career Guide and Interview Preparation",
    // "Generative AI: Elevate your Software Development Career",
    // "Machine Learning Capstone",
    // "Data Analyst Career Guide and Interview Preparation",
    // "Data Visualization with R",
    // "Collaborate Effectively for Professional Success",
    // "Building Generative AI-Powered Applications with Python",
    // "Information Technology (IT) Fundamentals for Everyone",
    // "Getting Started with Front-End and Web Development",
    // "Specialized Models: Time Series and Survival Analysis",
    // "Project Management Capstone",
    // "Designing User Interfaces and Experiences (UI/UX)",
    // "Technical Support (IT) Case Studies and Capstone",
    // "Monitoring and Observability for Development and DevOps",
    // "Developing Interpersonal Skills",
    // "Generative AI: Foundation Models and Platforms",
    // "Data Science with R - Capstone Project",
    // "Business Intelligence (BI) Essentials",
    // "Present with Purpose: Create/Deliver Effective Presentations",
    // "Generative AI and LLMs: Architecture and Data Preparation",
    // "Solving Problems with Creative and Critical Thinking",
    // "GenAI for Executives & Business Leaders: An Introduction",
    // "People and Soft Skills Assessment",
    // "Introduction to Business Analysis",
    // "Core 1: Hardware and Network Troubleshooting",
    // "Gen AI Foundational Models for NLP & Language Understanding",
    // "Delivering Quality Work with Agility",
    // "Artificial Intelligence (AI) Education for Teachers",
    // "Generative AI Language Modeling with Transformers",
    // "Fundamentals of AI Agents Using RAG and LangChain",
    // "JavaScript Programming Essentials",
    // "Product Management: Capstone Project",
    // "Generative AI Engineering and Fine-Tuning Transformers",
    // "Generative AI: Impact, Considerations, and Ethical Issues",
    "Project: Generative AI Applications with RAG and LangChain",
    "BI Dashboards with IBM Cognos Analytics and Google Looker",
    "Get Started with Cloud Native, DevOps, Agile, and NoSQL",
    "Practice Exam for CompTIA ITF+ Certification",
    "Generative AI Advance Fine-Tuning for LLMs",
    "Machine Learning Introduction for Everyone",
    "Generative AI: Business Transformation and Career Growth",
    "Deep Learning with PyTorch",
    "Project Management Job Search, Resume, and Interview Prep",
    "Developing Websites and Front-Ends with Bootstrap",
    "Statistical Analysis Fundamentals using Excel",
    "Generative AI: Elevate your Data Engineering Career",
    "Back-end Application Development Capstone Project",
    "Intermediate Web and Front-End Development",
    "Node.js & MongoDB: Developing Back-end Database Applications",
    "Cybersecurity Architecture",
    "Data Engineering Career Guide and Interview Preparation",
    "Tech Support Career Guide and Interview Preparation",
    "Generative AI: Boost Your Cybersecurity Career",
    "Core 2: OS, Software, Security and Operational Procedures",
    "Front-End Development Capstone Project",
    "Flutter and Dart: Developing iOS, Android, and Mobile Apps",
    "Database Essentials and Vulnerabilities",
    "Practice Exams for CompTIA A+ Certification: Core 1 & Core 2",
    "DataOps Methodology",
    "Introduction to Mobile App Development",
    "Introduction to Program Management",
    "Cybersecurity Job Search, Resume, and Interview Prep",
    "GenAI for Execs & Business Leaders: Integration Strategy",
    "Introduction to the Threat Intelligence Lifecycle",
    "JavaScript Full Stack Capstone Project",
    "Data Warehousing Capstone Project",
    "GenAI for Execs & Business Leaders: Formulate Your Use Case",
    "Java Programming for Beginners",
    "Program Manager Capstone",
    "Capstone Project: Applying Business Analysis Skills",
    "Develop Generative AI Applications: Get Started",
    "Object Oriented Programming in Java",
    "JavaScript Back-end Capstone Project",
    "Statistics and Clustering in Python",
    "Cloud Native, Microservices, Containers, DevOps and Agile",
    "Vector Databases: An Introduction with Chroma DB",
    "Build RAG Applications: Get Started",
    "Encryption and Cryptography Essentials",
    "Generative AI: Accelerate your Digital Marketing Career",
    "Introduction to Systems Analysis",
    "Java Development Capstone Project",
    "Introduction to Digital Marketing",
    "Build Multimodal Generative AI Applications",
    "Ethical Hacking with Kali Linux",
    "Generative AI: Empowering Modern Education",
    "Fundamentals of Building AI Agents",
    "Product Owner Capstone",
    "Introduction to UX/UI Design",
    "Vector Database Projects: AI Recommendation Systems",
    "Exploitation and Penetration Testing with Metasploit",
    "Relational Database Administration Capstone Project",
    "Agentic AI with LangChain and LangGraph",
    "Vector Databases for RAG: An Introduction",
    "Search Engine Optimization and Content Marketing",
    "Incident Response and Defense with OpenVAS",
    "Capstone Project: Applying UI/UX Design in the Real World",
    "Agentic AI with LangGraph, CrewAI, AutoGen and BeeAI",
    "Systems Analyst Capstone Project",
    "Introduction to HTML, CSS, & JavaScript",
    "Capstone Project: Digital Marketing and Growth Hacking",
    "Introduction to Systems Architecture",
    "Ethical Hacking Capstone Project: Breach, Response, AI",
    "Generative AI: Boost Your Sales Career",
    "GenAI for SEO: A Hands-On Playbook",
    "Systems and Solutions Architect Capstone Project",
    "Hybrid Cloud: Networking, Storage and Data Management",
    "Build AI Agents using MCP",
     "Cloud Operations, Monitoring, Security, and Compliance",
    "Designing Hybrid and Multicloud Architectures",
    "RAG and Agentic AI Capstone Project",
    "Hybrid Cloud Capstone Project"
   

);


        // Split the course list into 5 sublists
        int numSublists = 5;
        int sublistSize = (int) Math.ceil((double) courseNames.size() / numSublists);
        List<List<String>> courseSublists = new ArrayList<>();
        for (int i = 0; i < courseNames.size(); i += sublistSize) {
            int end = Math.min(i + sublistSize, courseNames.size());
            courseSublists.add(courseNames.subList(i, end));
        }

        // Process each sublist
        for (int i = 0; i < courseSublists.size(); i++) {
            List<String> sublist = courseSublists.get(i);
            List<CourseVersion> allCourseVersions = new ArrayList<>();
            logger.info("Processing sublist {} with {} courses...", i + 1, sublist.size());

            for (String courseName : sublist) {
                logger.info("Processing course: {}", courseName);
                adminCoursesPage.navigateToAdminCourses(adminUrl);
                List<CourseVersion> courseVersions = adminCoursesPage.searchAndGetCourseVersions(courseName);

                if (courseVersions.isEmpty()) {
                    logger.warn("No versions found for course: {}. Check the course name, logs, and screenshots for details.", courseName);
                    continue;
                }

                allCourseVersions.addAll(courseVersions);

                logger.info("Scraped ratings data for course: {}", courseName);
                for (CourseVersion courseVersion : courseVersions) {
                    logger.info("Course Name: {}, Version: {}, Link: {}, Rating: {}", 
                        courseVersion.getCourseName(), courseVersion.getVersion(), courseVersion.getLink(), courseVersion.getRating());
                }
            }

            if (allCourseVersions.isEmpty()) {
                logger.warn("No course versions were found for sublist {}.", i + 1);
                continue;
            }

            // Export results for this sublist to a separate Excel file
            String excelFileName = String.format("coursera_course_ratings_sublist_%d.xlsx", i + 1);
            adminCoursesPage.writeToExcel(excelFileName, allCourseVersions);
            logger.info("Exported sublist {} to {}", i + 1, excelFileName);
        }

        logger.info("Test completed successfully.");
    }

    @AfterMethod
    public void tearDown() {
        logger.info("Tearing down test...");
        closeDriver();
        logger.info("Test cleanup completed.");
    }
}