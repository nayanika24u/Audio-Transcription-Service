package com.audiotranscriptionservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class JobManager {
    // Key: jobId, Value: userId
    private ConcurrentHashMap<Long, String> jobToUserMap = new ConcurrentHashMap<>();
    private Logger logger = LoggerFactory.getLogger(JobManager.class);
    private static String logFileName = "job_log.txt";
    private static String logFilePath = StorageUtil.getFilePath(logFileName);
    //private Set<Long> jobIds = jobToUserMap.keySet();

    public JobManager() {
        // Replay the log file at startup to rebuild the in-memory set
        replayLogFile();
    }

    /**
     * Reads entire log file. Gets slower as more logs are added but needed to maintain consistency with inmemory data structure.
     * This is similar to how it is important to invalidate cache when write operations occur in source of truth ie database or a disk file in this case.
     * this can be replaced by an efficient change data capture mechanism but logging the info in memory and then persisting it to disk periodically may be faster approach.
     *
     * Also, this can be a single point of failure if the job_log.txt file is deleted or corrupted.
     * */
    private void replayLogFile() {
        // first call may result in no file found for log status
        try (Stream<String> lines = Files.lines(Paths.get(logFilePath))) {
            lines.forEach(line -> {
                    String[] keyValuePair = line.split(" ");
                    jobToUserMap.put(Long.parseLong(keyValuePair[0]), keyValuePair[1]);
            });
        } catch (IOException e) {
            logger.info("Log file either doesn't exist or not found. Check only if this error repeats as it doesn't exist on 1st call.");
        }
    }

    public boolean checkJobExists(Long jobId) {
        return jobToUserMap.containsKey(jobId);
    }

    public String getUserId(Long jobId) {
        return checkJobExists(jobId) ? jobToUserMap.get(jobId): null;
    }

    public void logJobCreation(Long jobId, String userId) {
        //simple version of write through cache like logic - update both in-memory and disk
        jobToUserMap.put(jobId, userId);
        // Append to WAL
        try {
            Files.writeString(Paths.get(logFilePath),  jobId + " "+ userId+ "\n", StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            // slow but important as we need a fresh copy of log file in memory else we may reject GET calls for jobs added after last replay
        } catch (IOException e) {
            // log error for now; add retry logic as this can be erroneous
            logger.error("Writing entry for " + jobId+ " and" +userId + " to log file failed.");
        }
    }

}

