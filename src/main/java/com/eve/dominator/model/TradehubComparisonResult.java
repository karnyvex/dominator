package com.eve.dominator.model;

public class TradehubComparisonResult {
    private int typeId;
    private String itemName;
    private long lowRegionId;
    private String lowRegionName;
    private double lowPrice;
    private long highRegionId;
    private String highRegionName;
    private double highPrice;
    private double priceDifferencePercentage;

    public TradehubComparisonResult() {}

    public TradehubComparisonResult(int typeId, String itemName, long lowRegionId, String lowRegionName,
                                  double lowPrice, long highRegionId, String highRegionName, double highPrice) {
        this.typeId = typeId;
        this.itemName = itemName;
        this.lowRegionId = lowRegionId;
        this.lowRegionName = lowRegionName;
        this.lowPrice = lowPrice;
        this.highRegionId = highRegionId;
        this.highRegionName = highRegionName;
        this.highPrice = highPrice;
        this.priceDifferencePercentage = ((highPrice - lowPrice) / lowPrice) * 100;
    }

    // Getters and setters
    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public long getLowRegionId() { return lowRegionId; }
    public void setLowRegionId(long lowRegionId) { this.lowRegionId = lowRegionId; }

    public String getLowRegionName() { return lowRegionName; }
    public void setLowRegionName(String lowRegionName) { this.lowRegionName = lowRegionName; }

    public double getLowPrice() { return lowPrice; }
    public void setLowPrice(double lowPrice) { this.lowPrice = lowPrice; }

    public long getHighRegionId() { return highRegionId; }
    public void setHighRegionId(long highRegionId) { this.highRegionId = highRegionId; }

    public String getHighRegionName() { return highRegionName; }
    public void setHighRegionName(String highRegionName) { this.highRegionName = highRegionName; }

    public double getHighPrice() { return highPrice; }
    public void setHighPrice(double highPrice) { this.highPrice = highPrice; }

    public double getPriceDifferencePercentage() { return priceDifferencePercentage; }
    public void setPriceDifferencePercentage(double priceDifferencePercentage) { this.priceDifferencePercentage = priceDifferencePercentage; }

    public String getFormattedLowPrice() {
        return String.format("%,.2f", lowPrice);
    }

    public String getFormattedHighPrice() {
        return String.format("%,.2f", highPrice);
    }

    public String getFormattedPriceDifference() {
        return String.format("%.1f%%", priceDifferencePercentage);
    }
}
