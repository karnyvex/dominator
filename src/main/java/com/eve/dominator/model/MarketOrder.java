package com.eve.dominator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketOrder {

    @JsonProperty("order_id")
    private long orderId;

    @JsonProperty("type_id")
    private int typeId;

    @JsonProperty("location_id")
    private long locationId;

    @JsonProperty("volume_total")
    private int volumeTotal;

    @JsonProperty("volume_remain")
    private int volumeRemain;

    @JsonProperty("min_volume")
    private int minVolume;

    private double price;

    @JsonProperty("is_buy_order")
    private boolean isBuyOrder;

    private String duration;
    private String issued;
    private String range;

    // Constructors
    public MarketOrder() {}

    // Getters and setters
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public long getLocationId() { return locationId; }
    public void setLocationId(long locationId) { this.locationId = locationId; }

    public int getVolumeTotal() { return volumeTotal; }
    public void setVolumeTotal(int volumeTotal) { this.volumeTotal = volumeTotal; }

    public int getVolumeRemain() { return volumeRemain; }
    public void setVolumeRemain(int volumeRemain) { this.volumeRemain = volumeRemain; }

    public int getMinVolume() { return minVolume; }
    public void setMinVolume(int minVolume) { this.minVolume = minVolume; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isBuyOrder() { return isBuyOrder; }
    public void setBuyOrder(boolean buyOrder) { isBuyOrder = buyOrder; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getIssued() { return issued; }
    public void setIssued(String issued) { this.issued = issued; }

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
}
