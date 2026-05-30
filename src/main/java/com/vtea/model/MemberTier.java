package com.vtea.model;

public class MemberTier {
    private int tierId;
    private String tierName;;
    private int requiredPoints;
    private String description;

    public MemberTier() {

    }

    public MemberTier(String description, int requiredPoints, int tierId, String tierName) {
        this.description = description;
        this.requiredPoints = requiredPoints;
        this.tierId = tierId;
        this.tierName = tierName;
    }

    public String getDescription() {
        return description;
    }

    public int getRequiredPoints() {
        return requiredPoints;
    }

    public int getTierId() {
        return tierId;
    }

    public String getTierName() {
        return tierName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequiredPoints(int requiredPoints) {
        this.requiredPoints = requiredPoints;
    }

    public void setTierId(int tierId) {
        this.tierId = tierId;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }
}
