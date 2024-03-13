package com.spamdetector.service;

import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.Response;

@Path("/spam")
public class SpamResource {
    //SpamDetector Class responsible for all the SpamDetecting logic
    SpamDetector detector = new SpamDetector();
    List<TestFile> spamFileList;

    SpamResource() {
        //load resources, train and test to improve performance on the endpoint calls
        System.out.println("Training and testing the model, please wait");

        //call this.trainAndTest();
        this.spamFileList = this.trainAndTest();
    }

    @GET
    @Produces("application/json")
    public Response getSpamResults() {
        //return the test results list of TestFile, return in a Response object
        Response response = null;

        response = Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "application/json")
                .entity(spamFileList)
                .build();

        return response;


    }

    @GET
    @Path("/accuracy")
    @Produces("application/json")
    public Response getAccuracy() {
        //return the accuracy of the detector, return in a Response object
        Response response = null;
        double accurateValue = 0;
        double rightGuesses = 0;

        for (int i = 0; i < spamFileList.size(); i++) {
            // Checks that the spam probability is low, and that the actual class is in fact ham
            if (spamFileList.get(i).getActualClass().equals("ham") && spamFileList.get(i).getSpamProbability() < 0.5) {
                rightGuesses++;
            } else if (spamFileList.get(i).getActualClass().equals("spam") && spamFileList.get(i).getSpamProbability() > 0.5) {
                rightGuesses++;
            }
        }

        accurateValue = rightGuesses / spamFileList.size();

        response = Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "application/json")
                .entity(accurateValue)
                .build();

        return response;
    }

    @GET
    @Path("/precision")
    @Produces("application/json")
    public Response getPrecision() {
        //return the precision of the detector, return in a Response object
        Response response = null;
        double truePositives = 0;
        double falsePositives = 0;
        double precisionValue;

        for (TestFile file : spamFileList) {
            if (file.getSpamProbability() > 0.5) {
                if (file.getActualClass().equals("spam")) {
                    truePositives++;
                }
                else if (file.getActualClass().equals("ham")) {
                    falsePositives++;
                }
            }
        }

        precisionValue = truePositives / (falsePositives + truePositives);

        response = Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "application/json")
                .entity(precisionValue)
                .build();

        return response;
    }


    private List<TestFile> trainAndTest() {
        if (this.detector == null) {
            this.detector = new SpamDetector();
        }

        //load the main directory "data" here from the Resources folder
        URL url = this.getClass().getClassLoader().getResource("/data");
        File mainDirectory = null;
        try {
            mainDirectory = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return this.detector.trainAndTest(mainDirectory);
    }
}