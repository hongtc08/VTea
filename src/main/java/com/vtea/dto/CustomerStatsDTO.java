package com.vtea.dto;

public class CustomerStatsDTO {
    private int totalCustomers;
    private int diamondCount;
    private int goldCount;
    private int avgPoints;

    public CustomerStatsDTO(int totalCustomers, int diamondCount, int goldCount, int avgPoints) {
        this.totalCustomers = totalCustomers;
        this.diamondCount = diamondCount;
        this.goldCount = goldCount;
        this.avgPoints = avgPoints;
    }

    public int getTotalCustomers() { return totalCustomers; }
    public int getDiamondCount() { return diamondCount; }
    public int getGoldCount() { return goldCount; }
    public int getAvgPoints() { return avgPoints; }
}
