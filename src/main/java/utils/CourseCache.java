package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.AdminCoursesPage.CourseVersion;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CourseCache {
    private static final Logger logger = LoggerFactory.getLogger(CourseCache.class);
    
    private final Map<String, CourseVersion> cache = new ConcurrentHashMap<>();
    private final File cacheFile;
    
    public CourseCache(String cacheFilePath) {
        this.cacheFile = new File(cacheFilePath);
        loadFromDisk();
    }
    
    public CourseCache() {
        this("course_cache.json");
    }
    
    public boolean isCached(String courseName) {
        return cache.containsKey(courseName.toLowerCase());
    }
    
    public CourseVersion get(String courseName) {
        return cache.get(courseName.toLowerCase());
    }
    
    public void put(String courseName, CourseVersion version) {
        cache.put(courseName.toLowerCase(), version);
        saveToDisk();
    }
    
    public int size() {
        return cache.size();
    }
    
    public void clear() {
        cache.clear();
        logger.info("Cache cleared");
    }
    
    private void saveToDisk() {
        try (FileWriter writer = new FileWriter(cacheFile)) {
            new Gson().toJson(cache, writer);
            logger.debug("Cache saved to disk: {} entries", cache.size());
        } catch (IOException e) {
            logger.error("Failed to save cache: {}", e.getMessage());
        }
    }
    
    private void loadFromDisk() {
        if (!cacheFile.exists()) {
            logger.info("Cache file not found. Starting with empty cache");
            return;
        }
        
        try (FileReader reader = new FileReader(cacheFile)) {
            Map<String, CourseVersion> loaded = new Gson().fromJson(reader, 
                new TypeToken<Map<String, CourseVersion>>(){}.getType());
            
            if (loaded != null && !loaded.isEmpty()) {
                cache.putAll(loaded);
                logger.info("Cache loaded from disk: {} entries", cache.size());
            }
        } catch (IOException e) {
            logger.warn("Failed to load cache: {}", e.getMessage());
        }
    }
}