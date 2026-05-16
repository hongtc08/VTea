package com.vtea.model;

public class Customer {
    private Integer customerId;
    private String phoneNumber;
    private String fullName;
    private int rewardPoints;

    public Customer() {

    }

    public Customer(int customerId, String fullName, String phoneNumber, int rewardPoints) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.rewardPoints = rewardPoints;
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
}
