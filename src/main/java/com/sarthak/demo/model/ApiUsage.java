package com.sarthak.demo.model;

public class ApiUsage {

    private String apiKey;
    private int totalCalls;
    private int successCalls;
    private int failedCalls;

    // NEW fields
    private double successPercent;
    private double failedPercent;

    public ApiUsage(String apiKey, int totalCalls, int successCalls, int failedCalls) {
        this.apiKey = apiKey;
        this.totalCalls = totalCalls;
        this.successCalls = successCalls;
        this.failedCalls = failedCalls;

        // calculate %
        if (totalCalls > 0) {
            this.successPercent = (successCalls * 100.0) / totalCalls;
            this.failedPercent = (failedCalls * 100.0) / totalCalls;
            
        } else {
            this.successPercent = 0;
            this.failedPercent = 0;
            
        }
    }

    // Getters
    public String getApiKey() { return apiKey; }
    public int getTotalCalls() { return totalCalls; }
    public int getSuccessCalls() { return successCalls; }
    public int getFailedCalls() { return failedCalls; }

    public double getSuccessPercent() { return successPercent; }
    public double getFailedPercent() { return failedPercent; }
}