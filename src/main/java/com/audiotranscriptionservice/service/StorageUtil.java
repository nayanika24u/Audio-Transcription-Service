package com.audiotranscriptionservice.service;
import java.io.*;

public class StorageUtil {

    private static final String STORAGE_DIR = "/tmp/ats/";

    public static void saveJob(Job job) throws IOException {
        File file = new File(STORAGE_DIR + "job-" + job.getJobId());
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

    public static void saveFile(String fileName) throws IOException {
        File file = new File(STORAGE_DIR + fileName);
        // Ensure the directories exist.
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); // Extra check- Create the directory or directories if they do not exist.
        }
        // Ensure the file itself exists.
        if (!file.exists()) {
            file.createNewFile(); // Create the file if it does not exist.
        }
    }

    public static Job loadJob(Long jobId) throws IOException, ClassNotFoundException {
        File file = new File(STORAGE_DIR + "job-" + jobId);
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
}

