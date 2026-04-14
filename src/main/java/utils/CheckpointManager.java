package utils;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.AdminCoursesPage.CourseVersion;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CheckpointManager {
    private static final Logger logger = LoggerFactory.getLogger(CheckpointManager.class);
    
    private final File checkpointFile;
    
    public CheckpointManager(String checkpointFilePath) {
        this.checkpointFile = new File(checkpointFilePath);
    }
    
    public CheckpointManager() {
        this("scraper_checkpoint.json");
    }
    
    public void saveCheckpoint(String batchName, int processedCount, 
                              List<CourseVersion> results) {
        CheckpointData data = new CheckpointData(batchName, processedCount, results);
        try (FileWriter writer = new FileWriter(checkpointFile)) {
            new Gson().toJson(data, writer);
            logger.info("Checkpoint saved: batch={}, processed={}, results={}", 
                batchName, processedCount, results.size());
        } catch (IOException e) {
            logger.error("Failed to save checkpoint: {}", e.getMessage());
        }
    }
    
    public CheckpointData loadCheckpoint() {
        if (!checkpointFile.exists()) {
            logger.info("No checkpoint file found. Starting fresh");
            return null;
        }
        
        try (FileReader reader = new FileReader(checkpointFile)) {
            CheckpointData data = new Gson().fromJson(reader, CheckpointData.class);
            if (data != null) {
                logger.info("Checkpoint loaded: batch={}, processed={}, results={}", 
                    data.batchName, data.processedCount, data.results.size());
            }
            return data;
        } catch (IOException e) {
            logger.warn("Failed to load checkpoint: {}", e.getMessage());
            return null;
        }
    }
    
    public void deleteCheckpoint() {
        if (checkpointFile.exists() && checkpointFile.delete()) {
            logger.info("Checkpoint deleted");
        }
    }
    
    public static class CheckpointData {
        private final String batchName;
        private final int processedCount;
        private final List<CourseVersion> results;
        private final long timestamp;
        
        public CheckpointData(String batchName, int processedCount, 
                            List<CourseVersion> results) {
            this.batchName = batchName;
            this.processedCount = processedCount;
            this.results = new ArrayList<>(results);
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getBatchName() { return batchName; }
        public int getProcessedCount() { return processedCount; }
        public List<CourseVersion> getResults() { return results; }
        public long getTimestamp() { return timestamp; }
    }
}