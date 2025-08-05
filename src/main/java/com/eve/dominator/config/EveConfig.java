package com.eve.dominator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "eve")
public class EveConfig {

    private List<Long> regions;
    private List<Long> importRegions;
    private Map<Long, Long> stations;
    private Monopoly monopoly = new Monopoly();
    private Tradehub tradehub = new Tradehub();
    private MarketData marketData = new MarketData();
    private Esi esi = new Esi();
    private Mokaam mokaam = new Mokaam();

    // Getters and setters
    public List<Long> getRegions() { return regions; }
    public void setRegions(List<Long> regions) { this.regions = regions; }

    public List<Long> getImportRegions() { return importRegions; }
    public void setImportRegions(List<Long> importRegions) { this.importRegions = importRegions; }

    public Map<Long, Long> getStations() { return stations; }
    public void setStations(Map<Long, Long> stations) { this.stations = stations; }

    public Monopoly getMonopoly() { return monopoly; }
    public void setMonopoly(Monopoly monopoly) { this.monopoly = monopoly; }

    public Tradehub getTradehub() { return tradehub; }
    public void setTradehub(Tradehub tradehub) { this.tradehub = tradehub; }

    public MarketData getMarketData() { return marketData; }
    public void setMarketData(MarketData marketData) { this.marketData = marketData; }

    public Esi getEsi() { return esi; }
    public void setEsi(Esi esi) { this.esi = esi; }

    public Mokaam getMokaam() { return mokaam; }
    public void setMokaam(Mokaam mokaam) { this.mokaam = mokaam; }

    public static class Monopoly {
        private double maxInvestmentMillions;
        private double targetRoiPercentage;
        private double taxPercentage;
        private boolean enableNpcFiltering;
        private double npcConfidenceThreshold;
        private long minVolumeThreshold;
        private int maxCompetitors;
        private double minMarginPercentage;
        private boolean excludeStationTrading;
        private int minDailyTrades;

        // Historical data filters (region-specific)
        private long minVolumeMonth;
        private long minVolumeQuarter;
        private long minVolumeYear;

        // Market size filters (in millions of ISK)
        private long minMarketSizeMonth;
        private long minMarketSizeQuarter;
        private long minMarketSizeYear;

        // Region-specific volume filters
        private Map<String, Long> minVolumeMonthByRegion = new HashMap<>();
        private Map<String, Long> minVolumeQuarterByRegion = new HashMap<>();
        private Map<String, Long> minVolumeYearByRegion = new HashMap<>();

        // Region-specific market size filters
        private Map<String, Long> minMarketSizeMonthByRegion = new HashMap<>();
        private Map<String, Long> minMarketSizeQuarterByRegion = new HashMap<>();
        private Map<String, Long> minMarketSizeYearByRegion = new HashMap<>();

        public double getMaxInvestmentMillions() { return maxInvestmentMillions; }
        public void setMaxInvestmentMillions(double maxInvestmentMillions) { this.maxInvestmentMillions = maxInvestmentMillions; }

        public double getTargetRoiPercentage() { return targetRoiPercentage; }
        public void setTargetRoiPercentage(double targetRoiPercentage) { this.targetRoiPercentage = targetRoiPercentage; }

        public double getTaxPercentage() { return taxPercentage; }
        public void setTaxPercentage(double taxPercentage) { this.taxPercentage = taxPercentage; }

        public boolean isEnableNpcFiltering() { return enableNpcFiltering; }
        public void setEnableNpcFiltering(boolean enableNpcFiltering) { this.enableNpcFiltering = enableNpcFiltering; }

        public double getNpcConfidenceThreshold() { return npcConfidenceThreshold; }
        public void setNpcConfidenceThreshold(double npcConfidenceThreshold) { this.npcConfidenceThreshold = npcConfidenceThreshold; }

        public long getMinVolumeThreshold() { return minVolumeThreshold; }
        public void setMinVolumeThreshold(long minVolumeThreshold) { this.minVolumeThreshold = minVolumeThreshold; }

        public int getMaxCompetitors() { return maxCompetitors; }
        public void setMaxCompetitors(int maxCompetitors) { this.maxCompetitors = maxCompetitors; }

        public double getMinMarginPercentage() { return minMarginPercentage; }
        public void setMinMarginPercentage(double minMarginPercentage) { this.minMarginPercentage = minMarginPercentage; }

        public boolean isExcludeStationTrading() { return excludeStationTrading; }
        public void setExcludeStationTrading(boolean excludeStationTrading) { this.excludeStationTrading = excludeStationTrading; }

        public int getMinDailyTrades() { return minDailyTrades; }
        public void setMinDailyTrades(int minDailyTrades) { this.minDailyTrades = minDailyTrades; }

        public long getMinVolumeMonth() { return minVolumeMonth; }
        public void setMinVolumeMonth(long minVolumeMonth) { this.minVolumeMonth = minVolumeMonth; }

        public long getMinVolumeQuarter() { return minVolumeQuarter; }
        public void setMinVolumeQuarter(long minVolumeQuarter) { this.minVolumeQuarter = minVolumeQuarter; }

        public long getMinVolumeYear() { return minVolumeYear; }
        public void setMinVolumeYear(long minVolumeYear) { this.minVolumeYear = minVolumeYear; }

        public long getMinMarketSizeMonth() { return minMarketSizeMonth; }
        public void setMinMarketSizeMonth(long minMarketSizeMonth) { this.minMarketSizeMonth = minMarketSizeMonth; }

        public long getMinMarketSizeQuarter() { return minMarketSizeQuarter; }
        public void setMinMarketSizeQuarter(long minMarketSizeQuarter) { this.minMarketSizeQuarter = minMarketSizeQuarter; }

        public long getMinMarketSizeYear() { return minMarketSizeYear; }
        public void setMinMarketSizeYear(long minMarketSizeYear) { this.minMarketSizeYear = minMarketSizeYear; }

        // Region-specific getters and setters
        public Map<String, Long> getMinVolumeMonthByRegion() { return minVolumeMonthByRegion; }
        public void setMinVolumeMonthByRegion(Map<String, Long> minVolumeMonthByRegion) { this.minVolumeMonthByRegion = minVolumeMonthByRegion; }

        public Map<String, Long> getMinVolumeQuarterByRegion() { return minVolumeQuarterByRegion; }
        public void setMinVolumeQuarterByRegion(Map<String, Long> minVolumeQuarterByRegion) { this.minVolumeQuarterByRegion = minVolumeQuarterByRegion; }

        public Map<String, Long> getMinVolumeYearByRegion() { return minVolumeYearByRegion; }
        public void setMinVolumeYearByRegion(Map<String, Long> minVolumeYearByRegion) { this.minVolumeYearByRegion = minVolumeYearByRegion; }

        public Map<String, Long> getMinMarketSizeMonthByRegion() { return minMarketSizeMonthByRegion; }
        public void setMinMarketSizeMonthByRegion(Map<String, Long> minMarketSizeMonthByRegion) { this.minMarketSizeMonthByRegion = minMarketSizeMonthByRegion; }

        public Map<String, Long> getMinMarketSizeQuarterByRegion() { return minMarketSizeQuarterByRegion; }
        public void setMinMarketSizeQuarterByRegion(Map<String, Long> minMarketSizeQuarterByRegion) { this.minMarketSizeQuarterByRegion = minMarketSizeQuarterByRegion; }

        public Map<String, Long> getMinMarketSizeYearByRegion() { return minMarketSizeYearByRegion; }
        public void setMinMarketSizeYearByRegion(Map<String, Long> minMarketSizeYearByRegion) { this.minMarketSizeYearByRegion = minMarketSizeYearByRegion; }

        // Helper methods to get region-specific values with fallback to global values
        public long getMinVolumeMonth(long regionId) {
            return minVolumeMonthByRegion.getOrDefault(String.valueOf(regionId), minVolumeMonth);
        }

        public long getMinVolumeQuarter(long regionId) {
            return minVolumeQuarterByRegion.getOrDefault(String.valueOf(regionId), minVolumeQuarter);
        }

        public long getMinVolumeYear(long regionId) {
            return minVolumeYearByRegion.getOrDefault(String.valueOf(regionId), minVolumeYear);
        }

        public long getMinMarketSizeMonth(long regionId) {
            return minMarketSizeMonthByRegion.getOrDefault(String.valueOf(regionId), minMarketSizeMonth);
        }

        public long getMinMarketSizeQuarter(long regionId) {
            return minMarketSizeQuarterByRegion.getOrDefault(String.valueOf(regionId), minMarketSizeQuarter);
        }

        public long getMinMarketSizeYear(long regionId) {
            return minMarketSizeYearByRegion.getOrDefault(String.valueOf(regionId), minMarketSizeYear);
        }
    }

    public static class Tradehub {
        private double minPriceDifferencePercentage;
        private double minMarketSizeMillions;
        private int maxThreads;

        public double getMinPriceDifferencePercentage() { return minPriceDifferencePercentage; }
        public void setMinPriceDifferencePercentage(double minPriceDifferencePercentage) { this.minPriceDifferencePercentage = minPriceDifferencePercentage; }

        public double getMinMarketSizeMillions() { return minMarketSizeMillions; }
        public void setMinMarketSizeMillions(double minMarketSizeMillions) { this.minMarketSizeMillions = minMarketSizeMillions; }

        public int getMaxThreads() { return maxThreads; }
        public void setMaxThreads(int maxThreads) { this.maxThreads = maxThreads; }
    }

    public static class MarketData {
        private boolean enableItemNamesImport;

        public boolean isEnableItemNamesImport() { return enableItemNamesImport; }
        public void setEnableItemNamesImport(boolean enableItemNamesImport) { this.enableItemNamesImport = enableItemNamesImport; }
    }

    public static class Esi {
        private String baseUrl;
        private String userAgent;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    }

    public static class Mokaam {
        private String baseUrl;
        private String userAgent;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    }
}
