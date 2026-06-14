package com.vtea.dto;

public class CustomerDTO {
    private Integer customerId;
    private String phoneNumber;
    private String fullName;
    private int rewardPoints;
    private int totalAccumulatedPoints;
    private int tierId;
    private String tierName;
    private int discountPercent;
    
    private int currentPoints;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime lastPurchase;

    public CustomerDTO(){

    }

    public CustomerDTO(Integer customerId, String fullName, String phoneNumber, int rewardPoints, int tierId, String tierName, int totalAccumulatedPoints, int discountPercent) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.rewardPoints = rewardPoints;
        this.tierId = tierId;
        this.tierName = tierName;
        this.totalAccumulatedPoints = totalAccumulatedPoints;
        this.discountPercent = discountPercent;
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

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(int currentPoints) {
        this.currentPoints = currentPoints;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getLastPurchase() {
        return lastPurchase;
    }

    public void setLastPurchase(java.time.LocalDateTime lastPurchase) {
        this.lastPurchase = lastPurchase;
    }
}
