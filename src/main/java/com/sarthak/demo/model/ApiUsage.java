package com.sarthak.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "api_usage")
public class ApiUsage {

    @Id
    private String id;

    private String apiKey;

    private int totalCalls;

    private int successCalls;

    private int failedCalls;

    private double successPercent;

    private double failedPercent;

    // Required by MongoDB / Spring Data
    public ApiUsage() {
    }

    // Constructor used when reading Excel data
    public ApiUsage(String apiKey, int totalCalls, int successCalls, int failedCalls) {

        this.apiKey = apiKey;
        this.totalCalls = totalCalls;
        this.successCalls = successCalls;
        this.failedCalls = failedCalls;

        calculatePercentages();
    }

    private void calculatePercentages() {

        if (totalCalls > 0) {

            this.successPercent =
                    (successCalls * 100.0) / totalCalls;

            this.failedPercent =
                    (failedCalls * 100.0) / totalCalls;

        } else {

            this.successPercent = 0;
            this.failedPercent = 0;
        }
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public int getTotalCalls() {
        return totalCalls;
    }

    public int getSuccessCalls() {
        return successCalls;
    }

    public int getFailedCalls() {
        return failedCalls;
    }

    public double getSuccessPercent() {
        return successPercent;
    }

    public double getFailedPercent() {
        return failedPercent;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setTotalCalls(int totalCalls) {
        this.totalCalls = totalCalls;
    }

    public void setSuccessCalls(int successCalls) {
        this.successCalls = successCalls;
    }

    public void setFailedCalls(int failedCalls) {
        this.failedCalls = failedCalls;
    }

    public void setSuccessPercent(double successPercent) {
        this.successPercent = successPercent;
    }

    public void setFailedPercent(double failedPercent) {
        this.failedPercent = failedPercent;
    }
}