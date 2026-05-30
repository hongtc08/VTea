package com.vtea.dto;

public class CustomerDTO {
    private Integer customerId;
    private String phoneNumber;
    private String fullName;
    private int rewardPoints;
    private int totalAccumulatedPoints;
    private int tierId;
    private String tierName;

    public CustomerDTO(){

    }

    public CustomerDTO(Integer customerId, String fullName, String phoneNumber, int rewardPoints, int tierId, String tierName, int totalAccumulatedPoints) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.rewardPoints = rewardPoints;
        this.tierId = tierId;
        this.tierName = tierName;
        this.totalAccumulatedPoints = totalAccumulatedPoints;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public int getTierId() {
        return tierId;
    }

    public String getTierName() {
        return tierName;
    }

    public int getTotalAccumulatedPoints() {
        return totalAccumulatedPoints;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public void setTierId(int tierId) {
        this.tierId = tierId;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public void setTotalAccumulatedPoints(int totalAccumulatedPoints) {
        this.totalAccumulatedPoints = totalAccumulatedPoints;
    }
}
