package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PerformanceMetrics {

    private static final Logger logger =
            LoggerFactory.getLogger(PerformanceMetrics.class);

    private final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    private final List<String> logs = new CopyOnWriteArrayList<>();

    public void startTimer(String operation) {
        startTimes.put(operation, System.currentTimeMillis());
    }

    public long endTimer(String operation) {
        long elapsed = System.currentTimeMillis()
                - startTimes.getOrDefault(operation, 0L);

        String log = "METRIC | " + operation + " | " + elapsed + " ms";
        logger.info(log);
        logs.add(log);

        return elapsed;
    }

    public void printSummary() {
        logger.info("===== PERFORMANCE SUMMARY =====");
        for (String log : logs) {
            logger.info(log);
        }
    }
}