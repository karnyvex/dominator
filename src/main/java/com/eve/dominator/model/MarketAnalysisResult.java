package com.eve.dominator.model;

public class MarketAnalysisResult {

    private int typeId;
    private String itemName;
    private int ordersToBeCleared;
    private int totalItemsToBuy;
    private double totalInvestment;
    private double targetSellPrice;
    private double highestBuyPrice;
    private double profitPerItem;
    private double totalProfit;
    private double roiPercentage;

    // Constructors
    public MarketAnalysisResult() {}

    public MarketAnalysisResult(int typeId, String itemName, int ordersToBeCleared,
                              int totalItemsToBuy, double totalInvestment, double targetSellPrice) {
        this.typeId = typeId;
        this.itemName = itemName;
        this.ordersToBeCleared = ordersToBeCleared;
        this.totalItemsToBuy = totalItemsToBuy;
        this.totalInvestment = totalInvestment;
        this.targetSellPrice = targetSellPrice;
    }

    // Getters and setters
    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getOrdersToBeCleared() { return ordersToBeCleared; }
    public void setOrdersToBeCleared(int ordersToBeCleared) { this.ordersToBeCleared = ordersToBeCleared; }

    public int getTotalItemsToBuy() { return totalItemsToBuy; }
    public void setTotalItemsToBuy(int totalItemsToBuy) { this.totalItemsToBuy = totalItemsToBuy; }

    public double getTotalInvestment() { return totalInvestment; }
    public void setTotalInvestment(double totalInvestment) { this.totalInvestment = totalInvestment; }

    public double getTargetSellPrice() { return targetSellPrice; }
    public void setTargetSellPrice(double targetSellPrice) { this.targetSellPrice = targetSellPrice; }

    public double getHighestBuyPrice() { return highestBuyPrice; }
    public void setHighestBuyPrice(double highestBuyPrice) { this.highestBuyPrice = highestBuyPrice; }

    public double getProfitPerItem() { return profitPerItem; }
    public void setProfitPerItem(double profitPerItem) { this.profitPerItem = profitPerItem; }

    public double getTotalProfit() { return totalProfit; }
    public void setTotalProfit(double totalProfit) { this.totalProfit = totalProfit; }

    public double getRoiPercentage() { return roiPercentage; }
    public void setRoiPercentage(double roiPercentage) { this.roiPercentage = roiPercentage; }
}
