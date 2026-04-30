package com.vtea.model;

public class Customer {
    private int customerId;
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

    public int getCustomerId() {
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
}
