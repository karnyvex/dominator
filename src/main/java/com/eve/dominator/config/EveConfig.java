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
    private Map<Long, Long> stations;
    private Investment investment = new Investment();
    private Profit profit = new Profit();
    private Esi esi = new Esi();

    // Getters and setters
    public List<Long> getRegions() { return regions; }
    public void setRegions(List<Long> regions) { this.regions = regions; }

    public Map<Long, Long> getStations() { return stations; }
    public void setStations(Map<Long, Long> stations) { this.stations = stations; }

    public Investment getInvestment() { return investment; }
    public void setInvestment(Investment investment) { this.investment = investment; }

    public Profit getProfit() { return profit; }
    public void setProfit(Profit profit) { this.profit = profit; }

    public Esi getEsi() { return esi; }
    public void setEsi(Esi esi) { this.esi = esi; }

    public static class Investment {
        private double maxMillions;

        public double getMaxMillions() { return maxMillions; }
        public void setMaxMillions(double maxMillions) { this.maxMillions = maxMillions; }
    }

    public static class Profit {
        private double roiPercentage;
        private double taxPercentage;

        public double getRoiPercentage() { return roiPercentage; }
        public void setRoiPercentage(double roiPercentage) { this.roiPercentage = roiPercentage; }

        public double getTaxPercentage() { return taxPercentage; }
        public void setTaxPercentage(double taxPercentage) { this.taxPercentage = taxPercentage; }
    }

    public static class Esi {
        private String baseUrl;
        private String userAgent;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    }
}
