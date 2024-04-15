package com.audiotranscriptionservice.service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class StorageUtil {

    private static final String STORAGE_DIR = "/tmp/ats/";

    public static void saveJob(Job job) throws IOException {
        if (job.getUserId() == null || job.getUserId().isEmpty()) {
            throw new IllegalArgumentException("UserId in Job found null or empty "+ job.toString());
        }
        File file = new File(STORAGE_DIR +job.getUserId() + "/" + "job-" + job.getJobId());
        // Ensure the directories exist.
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); // Extra check- Create the directory or directories if they do not exist.
        }
        // Ensure the file itself exists.
        if (!file.exists()) {
            file.createNewFile(); // Create the file if it does not exist.
        }
        // Save the job object to the file.
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(job);
        }
    }

    public static void saveJobStatus(Long jobId, String userId, String jobStatus) throws IOException {
        File file = new File(STORAGE_DIR+"jobStatus.txt");
        // Ensure the directories exist.
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); // Extra check- Create the directory or directories if they do not exist.
        }
        // Ensure the file itself exists.
        if (!file.exists()) {
            file.createNewFile(); // Create the file if it does not exist.
        }
        // Save the job status to the file.
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeBytes(jobId + " " + userId + " " + jobStatus);
        }
    }

    public static Job loadJob(Long jobId, String userId) throws IOException, ClassNotFoundException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId in Job found null or empty "+ userId);
        }
        File file = new File(STORAGE_DIR + userId + "/" + "job-" + jobId);
        // Check if the file exists before trying to read it.
        if (!file.exists()) {
            throw new FileNotFoundException("The job file for job ID " + jobId + " does not exist.");
        }

        // Attempt to read the object from the file.
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Job) ois.readObject();
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class definition changed or not found: " + e.getMessage());
        }
        return null;
    }

    public static String getFilePath(String fileName) {
        return STORAGE_DIR + fileName;
    }

//    public void saveLogFileContents(String userId, Long jobId) {
//        //Files.writeString(Paths.get("job_log.txt"),  jobId + " "+ userId+ "\n", StandardOpenOption.APPEND, StandardOpenOption.CREATE);
//        File file = new File(STORAGE_DIR+"job_log.txt");
//        // Ensure the directories exist.
//        if (!file.getParentFile().exists()) {
//            file.getParentFile().mkdirs(); // Extra check- Create the directory or directories if they do not exist.
//        }
//        // Ensure the file itself exists.
//        if (!file.exists()) {
//            file.createNewFile(); // Create the file if it does not exist.
//        }
//        // Save the job status to the file.
//        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
//            oos.writeBytes(jobId + " " + userId);
//        }
//    }
}

