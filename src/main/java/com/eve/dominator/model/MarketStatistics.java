package com.eve.dominator.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "market_statistics")
public class MarketStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_id", nullable = false)
    private Integer typeId;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    // Yesterday's data
    @Column(name = "average_price")
    private Double averagePrice;

    @Column(name = "highest_price")
    private Double highestPrice;

    @Column(name = "lowest_price")
    private Double lowestPrice;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "order_count")
    private Integer orderCount;

    // Extended Mokaam data - Weekly
    @Column(name = "vol_week")
    private Long volumeWeek;

    @Column(name = "avg_price_week")
    private Double averagePriceWeek;

    @Column(name = "order_count_week")
    private Integer orderCountWeek;

    @Column(name = "high_week")
    private Double highWeek;

    @Column(name = "low_week")
    private Double lowWeek;

    @Column(name = "spread_week")
    private Double spreadWeek;

    @Column(name = "vwap_week")
    private Double vwapWeek;

    @Column(name = "std_dev_week")
    private Double stdDevWeek;

    // Extended Mokaam data - Monthly
    @Column(name = "vol_month")
    private Long volumeMonth;

    @Column(name = "avg_price_month")
    private Double averagePriceMonth;

    @Column(name = "order_count_month")
    private Integer orderCountMonth;

    @Column(name = "high_month")
    private Double highMonth;

    @Column(name = "low_month")
    private Double lowMonth;

    @Column(name = "spread_month")
    private Double spreadMonth;

    @Column(name = "vwap_month")
    private Double vwapMonth;

    @Column(name = "std_dev_month")
    private Double stdDevMonth;

    // Extended Mokaam data - Quarterly
    @Column(name = "vol_quarter")
    private Long volumeQuarter;

    @Column(name = "avg_price_quarter")
    private Double averagePriceQuarter;

    @Column(name = "order_count_quarter")
    private Integer orderCountQuarter;

    @Column(name = "high_quarter")
    private Double highQuarter;

    @Column(name = "low_quarter")
    private Double lowQuarter;

    @Column(name = "spread_quarter")
    private Double spreadQuarter;

    @Column(name = "vwap_quarter")
    private Double vwapQuarter;

    @Column(name = "std_dev_quarter")
    private Double stdDevQuarter;

    // Extended Mokaam data - Yearly
    @Column(name = "vol_year")
    private Long volumeYear;

    @Column(name = "avg_price_year")
    private Double averagePriceYear;

    @Column(name = "order_count_year")
    private Integer orderCountYear;

    @Column(name = "high_year")
    private Double highYear;

    @Column(name = "low_year")
    private Double lowYear;

    @Column(name = "spread_year")
    private Double spreadYear;

    @Column(name = "vwap_year")
    private Double vwapYear;

    @Column(name = "std_dev_year")
    private Double stdDevYear;

    // 52-week data
    @Column(name = "week_52_high")
    private Double week52High;

    @Column(name = "week_52_low")
    private Double week52Low;

    // Market size data
    @Column(name = "size_yesterday")
    private Double sizeYesterday;

    @Column(name = "size_week")
    private Double sizeWeek;

    @Column(name = "size_month")
    private Double sizeMonth;

    @Column(name = "size_quarter")
    private Double sizeQuarter;

    @Column(name = "size_year")
    private Double sizeYear;

    // Constructors
    public MarketStatistics() {}

    public MarketStatistics(Integer typeId, Long regionId, LocalDate date) {
        this.typeId = typeId;
        this.regionId = regionId;
        this.date = date;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getAveragePrice() { return averagePrice; }
    public void setAveragePrice(Double averagePrice) { this.averagePrice = averagePrice; }

    public Double getHighestPrice() { return highestPrice; }
    public void setHighestPrice(Double highestPrice) { this.highestPrice = highestPrice; }

    public Double getLowestPrice() { return lowestPrice; }
    public void setLowestPrice(Double lowestPrice) { this.lowestPrice = lowestPrice; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }

    public Long getVolumeWeek() { return volumeWeek; }
    public void setVolumeWeek(Long volumeWeek) { this.volumeWeek = volumeWeek; }

    public Double getAveragePriceWeek() { return averagePriceWeek; }
    public void setAveragePriceWeek(Double averagePriceWeek) { this.averagePriceWeek = averagePriceWeek; }

    public Integer getOrderCountWeek() { return orderCountWeek; }
    public void setOrderCountWeek(Integer orderCountWeek) { this.orderCountWeek = orderCountWeek; }

    public Double getHighWeek() { return highWeek; }
    public void setHighWeek(Double highWeek) { this.highWeek = highWeek; }

    public Double getLowWeek() { return lowWeek; }
    public void setLowWeek(Double lowWeek) { this.lowWeek = lowWeek; }

    public Double getSpreadWeek() { return spreadWeek; }
    public void setSpreadWeek(Double spreadWeek) { this.spreadWeek = spreadWeek; }

    public Double getVwapWeek() { return vwapWeek; }
    public void setVwapWeek(Double vwapWeek) { this.vwapWeek = vwapWeek; }

    public Double getStdDevWeek() { return stdDevWeek; }
    public void setStdDevWeek(Double stdDevWeek) { this.stdDevWeek = stdDevWeek; }

    public Long getVolumeMonth() { return volumeMonth; }
    public void setVolumeMonth(Long volumeMonth) { this.volumeMonth = volumeMonth; }

    public Double getAveragePriceMonth() { return averagePriceMonth; }
    public void setAveragePriceMonth(Double averagePriceMonth) { this.averagePriceMonth = averagePriceMonth; }

    public Integer getOrderCountMonth() { return orderCountMonth; }
    public void setOrderCountMonth(Integer orderCountMonth) { this.orderCountMonth = orderCountMonth; }

    public Double getHighMonth() { return highMonth; }
    public void setHighMonth(Double highMonth) { this.highMonth = highMonth; }

    public Double getLowMonth() { return lowMonth; }
    public void setLowMonth(Double lowMonth) { this.lowMonth = lowMonth; }

    public Double getSpreadMonth() { return spreadMonth; }
    public void setSpreadMonth(Double spreadMonth) { this.spreadMonth = spreadMonth; }

    public Double getVwapMonth() { return vwapMonth; }
    public void setVwapMonth(Double vwapMonth) { this.vwapMonth = vwapMonth; }

    public Double getStdDevMonth() { return stdDevMonth; }
    public void setStdDevMonth(Double stdDevMonth) { this.stdDevMonth = stdDevMonth; }

    public Long getVolumeQuarter() { return volumeQuarter; }
    public void setVolumeQuarter(Long volumeQuarter) { this.volumeQuarter = volumeQuarter; }

    public Double getAveragePriceQuarter() { return averagePriceQuarter; }
    public void setAveragePriceQuarter(Double averagePriceQuarter) { this.averagePriceQuarter = averagePriceQuarter; }

    public Integer getOrderCountQuarter() { return orderCountQuarter; }
    public void setOrderCountQuarter(Integer orderCountQuarter) { this.orderCountQuarter = orderCountQuarter; }

    public Double getHighQuarter() { return highQuarter; }
    public void setHighQuarter(Double highQuarter) { this.highQuarter = highQuarter; }

    public Double getLowQuarter() { return lowQuarter; }
    public void setLowQuarter(Double lowQuarter) { this.lowQuarter = lowQuarter; }

    public Double getSpreadQuarter() { return spreadQuarter; }
    public void setSpreadQuarter(Double spreadQuarter) { this.spreadQuarter = spreadQuarter; }

    public Double getVwapQuarter() { return vwapQuarter; }
    public void setVwapQuarter(Double vwapQuarter) { this.vwapQuarter = vwapQuarter; }

    public Double getStdDevQuarter() { return stdDevQuarter; }
    public void setStdDevQuarter(Double stdDevQuarter) { this.stdDevQuarter = stdDevQuarter; }

    public Long getVolumeYear() { return volumeYear; }
    public void setVolumeYear(Long volumeYear) { this.volumeYear = volumeYear; }

    public Double getAveragePriceYear() { return averagePriceYear; }
    public void setAveragePriceYear(Double averagePriceYear) { this.averagePriceYear = averagePriceYear; }

    public Integer getOrderCountYear() { return orderCountYear; }
    public void setOrderCountYear(Integer orderCountYear) { this.orderCountYear = orderCountYear; }

    public Double getHighYear() { return highYear; }
    public void setHighYear(Double highYear) { this.highYear = highYear; }

    public Double getLowYear() { return lowYear; }
    public void setLowYear(Double lowYear) { this.lowYear = lowYear; }

    public Double getSpreadYear() { return spreadYear; }
    public void setSpreadYear(Double spreadYear) { this.spreadYear = spreadYear; }

    public Double getVwapYear() { return vwapYear; }
    public void setVwapYear(Double vwapYear) { this.vwapYear = vwapYear; }

    public Double getStdDevYear() { return stdDevYear; }
    public void setStdDevYear(Double stdDevYear) { this.stdDevYear = stdDevYear; }

    public Double getWeek52High() { return week52High; }
    public void setWeek52High(Double week52High) { this.week52High = week52High; }

    public Double getWeek52Low() { return week52Low; }
    public void setWeek52Low(Double week52Low) { this.week52Low = week52Low; }

    public Double getSizeYesterday() { return sizeYesterday; }
    public void setSizeYesterday(Double sizeYesterday) { this.sizeYesterday = sizeYesterday; }

    public Double getSizeWeek() { return sizeWeek; }
    public void setSizeWeek(Double sizeWeek) { this.sizeWeek = sizeWeek; }

    public Double getSizeMonth() { return sizeMonth; }
    public void setSizeMonth(Double sizeMonth) { this.sizeMonth = sizeMonth; }

    public Double getSizeQuarter() { return sizeQuarter; }
    public void setSizeQuarter(Double sizeQuarter) { this.sizeQuarter = sizeQuarter; }

    public Double getSizeYear() { return sizeYear; }
    public void setSizeYear(Double sizeYear) { this.sizeYear = sizeYear; }
}
