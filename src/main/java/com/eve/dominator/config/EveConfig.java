package com.eve.dominator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "eve")
public class EveConfig {

    private List<Long> regions;
    private List<Long> importRegions;
    private Map<Long, Long> stations;
    private Monopoly monopoly = new Monopoly();
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
