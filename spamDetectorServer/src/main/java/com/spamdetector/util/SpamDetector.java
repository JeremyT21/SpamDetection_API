package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.nio.*;
import java.util.stream.Collectors;

public class SpamDetector {
    public List<TestFile> trainAndTest(File mainDirectory) {
        //main method of loading the directories and files, training and testing the model

        // ******************************************************
        //                      TRAINING
        // ******************************************************

        // Initialize all arrays of emails and frequency maps
        File[] emails = mainDirectory.listFiles();
        File[] testEmails = emails[0].listFiles();
        File[] testHamEmails = testEmails[0].listFiles();
        File[] testSpamEmails = testEmails[1].listFiles();
        File[] trainEmails = emails[1].listFiles();
        File[] trainHamEmails = trainEmails[0].listFiles();
        File[] trainHam2Emails = trainEmails[1].listFiles();
        File[] trainSpamEmails = trainEmails[2].listFiles();
        Map<String, Integer> trainHamFreq = new TreeMap<>();
        Map<String, Integer> trainSpamFreq = new TreeMap<>();

        // Calculate frequencies of every word in every email and store them in corresponding maps
        trainHamFreq = calculateFrequency(trainHamEmails, trainHamFreq);
        trainHamFreq = calculateFrequency(trainHam2Emails, trainHamFreq);
        trainSpamFreq = calculateFrequency(trainSpamEmails, trainSpamFreq);

        // Get amount of ham and spam emails
        int hamCount = trainHamEmails.length + trainHam2Emails.length;
        int spamCount = trainSpamEmails.length;

        // Probabilities of words appearing in a spam email
        Map<String, Double> prWS = calculateProbabilities(trainSpamFreq, spamCount);

        // Probabilities of words appearing in a ham email
        Map<String, Double> prWH = calculateProbabilities(trainHamFreq, hamCount);

        // Stores probabilities of spam given words
        Map<String, Double> prSW = new TreeMap<>();

        // Get set of all words appearing in all emails
        Set<String> hamWords = trainHamFreq.keySet();
        Set<String> spamWords = trainSpamFreq.keySet();
        Set<String> allWords = new HashSet<>(hamWords);
        allWords.addAll(spamWords);

        // Calculate probabilities of spam given words
        Iterator<String> wordIterator = allWords.iterator();
        while (wordIterator.hasNext()) {
            String word = wordIterator.next();

            // Skip any words that don't appear in both email types
            if (!trainHamFreq.containsKey(word) || !trainSpamFreq.containsKey(word)) {
                continue;
            }

            double prob = (prWS.get(word)) / (prWS.get(word) + (prWH.get(word)));
            prSW.put(word, prob);
        }

        // ******************************************************
        //                       TESTING
        // ******************************************************

        // Initialize variables
        ArrayList<TestFile> testFiles = new ArrayList<>();
        double prSF; // Probability of file being spam
        double eta = 0;

        // Adds a TestFile object for every spam email
        for (File email_spam : testSpamEmails)
        {
            // Store every word in this email in a map and then turn it into a set to iterate through
            Map<String, Integer> spam_wordsInFile = storeWords(email_spam);
            Set<String> spam_words = spam_wordsInFile.keySet();
            Iterator<String> spam_wordIterator = spam_words.iterator();

            // Calculate eta for file
            while (spam_wordIterator.hasNext()) {
                String word = spam_wordIterator.next();

                // Can't have a probability greater than 1
                if(prSW.get(word) != null && prSW.get(word) <= 1)
                    eta += Math.log(1-prSW.get(word)) - Math.log(prSW.get(word));
            }

            // Calculate probability of file being spam and add it to a new TestFile object in testFiles
            prSF = (1/(1+Math.pow(Math.E,eta)));
            testFiles.add(new TestFile(email_spam.getName(), prSF, "spam"));
            eta = 0;
        }

        // Adds a TestFile object for every ham email
        for (File email_ham : testHamEmails)
        {
            // Store every word in this email in a map and then turn it into a set to iterate through
            Map<String, Integer> ham_wordsInFile = storeWords(email_ham);
            Set<String> ham_words = ham_wordsInFile.keySet();
            Iterator<String> ham_wordIterator = ham_words.iterator();

            // Calculate eta for file
            while (ham_wordIterator.hasNext()) {
                String word = ham_wordIterator.next();

                // Can't have a probability greater than 1
                if(prSW.get(word) != null && prSW.get(word) <= 1)
                    eta += Math.log(1-prSW.get(word)) - Math.log(prSW.get(word));
            }

            // Calculate probability of file being spam and add it to a new TestFile object in testFiles
            prSF = (1/(1+Math.pow(Math.E,eta)));
            testFiles.add(new TestFile(email_ham.getName(), prSF, "ham"));
            eta = 0;
        }

        // Returns an array list of all TestFile objects
        return testFiles;
    }

    public Map<String, Double> calculateProbabilities(Map<String, Integer> wordFreq, int emailCount) {
        Map<String, Double> probabilities = new TreeMap<>();
        Set<String> words = wordFreq.keySet();
        Iterator<String> wordIterator = words.iterator();

        while (wordIterator.hasNext()) {
            String word = wordIterator.next();
            probabilities.put(word, (double)(wordFreq.get(word)) / emailCount);
        }

        return probabilities;
    }

    public Map<String, Integer> calculateFrequency(File[] emails, Map<String, Integer> trainFreq) {
        // Store the words found in each email in the training ham frequency map
        for (File email : emails) {
            // Store every word in this email in a map and then turn it into a set to iterate through
            Map<String, Integer> wordsInFile = storeWords(email);
            Set<String> words = wordsInFile.keySet();
            Iterator<String> wordIterator = words.iterator();

            // Put each word in the corresponding frequency map if it does not already exist there, otherwise
            // add one to its count
            while (wordIterator.hasNext()) {
                String word = wordIterator.next();

                if (!trainFreq.containsKey(word)) {
                    trainFreq.put(word, 1);
                }
                else {
                    int count = trainFreq.get(word);
                    trainFreq.put(word, count + 1);
                }
            }
        }

        return trainFreq;
    }

    // Determine what words are in each email
    public Map<String, Integer> storeWords(File email) {
        // Initialize map
        Map<String, Integer> wordsInFile = new TreeMap<>();

        try {
            // Scan every "word" in the email and add it to the map if it's an actual word and is not already there
            Scanner emailScanner = new Scanner(email);
            while (emailScanner.hasNext()) {
                String word = emailScanner.next();
                word = word.toLowerCase();
                if (isWord(word)) {
                    if (!wordsInFile.containsKey(word)) {
                        wordsInFile.put(word, 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return wordsInFile;
    }

    // Determine if string is a valid word
    private boolean isWord(String word) {
        if (word == null | "".equals(word)) {
            return false;
        }
        String wordPattern = "^[a-z]*$";
        if (word.matches(wordPattern)) {
            return true;
        }
        return false;
    }
}

