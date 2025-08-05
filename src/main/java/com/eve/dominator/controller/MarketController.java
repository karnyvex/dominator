package com.eve.dominator.controller;

import com.eve.dominator.config.EveConfig;
import com.eve.dominator.model.ItemName;
import com.eve.dominator.model.MarketAnalysisResult;
import com.eve.dominator.model.MarketStatistics;
import com.eve.dominator.model.TradehubComparisonResult;
import com.eve.dominator.service.ItemNameService;
import com.eve.dominator.service.MarketAnalysisService;
import com.eve.dominator.service.MokaamService;
import com.eve.dominator.service.TradehubAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class MarketController {

    private static final Logger logger = LoggerFactory.getLogger(MarketController.class);

    private final MarketAnalysisService marketAnalysisService;
    private final MokaamService mokaamService;
    private final EveConfig eveConfig;
    private final ItemNameService itemNameService;
    private final TradehubAnalysisService tradehubAnalysisService;

    @Autowired
    public MarketController(MarketAnalysisService marketAnalysisService, MokaamService mokaamService, EveConfig eveConfig, ItemNameService itemNameService, TradehubAnalysisService tradehubAnalysisService) {
        this.marketAnalysisService = marketAnalysisService;
        this.mokaamService = mokaamService;
        this.eveConfig = eveConfig;
        this.itemNameService = itemNameService;
        this.tradehubAnalysisService = tradehubAnalysisService;
        logger.info("MarketController initialized with config: {}", eveConfig);
    }

    @GetMapping("/")
    public String index(Model model) {
        logger.info("Index page requested");

        return "index";
    }

    @GetMapping("/monopoly")
    public String monopolyPage(Model model) {
        logger.info("Monopoly scan page requested");
        logger.info("Regions: {}", eveConfig.getRegions());

        model.addAttribute("regions", eveConfig.getRegions());
        model.addAttribute("maxInvestment", eveConfig.getMonopoly().getMaxInvestmentMillions());
        model.addAttribute("roiPercentage", eveConfig.getMonopoly().getTargetRoiPercentage());
        model.addAttribute("taxPercentage", eveConfig.getMonopoly().getTaxPercentage());

        return "monopoly";
    }

    @GetMapping("/market-data")
    public String marketDataPage(Model model) {
        logger.info("Market data landing page requested");

        // Add item names count for display
        long itemNamesCount = itemNameService.getItemNamesCount();
        model.addAttribute("itemNamesCount", itemNamesCount);

        // Add statistics count for each import region
        for (Long regionId : eveConfig.getImportRegions()) {
            long count = mokaamService.getStatisticsCount(regionId);
            model.addAttribute("statsCount_" + regionId, count);
        }

        return "market-data";
    }

    @GetMapping("/market-data/manage")
    public String marketDataManagePage(Model model) {
        logger.info("Market data management page requested");
        logger.info("Import Regions: {}", eveConfig.getImportRegions());

        model.addAttribute("importRegions", eveConfig.getImportRegions());

        // Add statistics count and last refresh date for each import region
        for (Long regionId : eveConfig.getImportRegions()) {
            long count = mokaamService.getStatisticsCount(regionId);
            model.addAttribute("statsCount_" + regionId, count);
            
            java.time.LocalDate lastRefresh = mokaamService.getLastRefreshDate(regionId);
            model.addAttribute("lastRefresh_" + regionId, lastRefresh);
        }

        return "market-data-manage";
    }

    @GetMapping("/market-data/search")
    public String marketDataSearchPage(Model model) {
        logger.info("Market data search page requested");

        long itemNamesCount = itemNameService.getItemNamesCount();
        model.addAttribute("itemNamesCount", itemNamesCount);
        model.addAttribute("importRegions", eveConfig.getImportRegions());

        return "market-data-search";
    }

    @PostMapping("/market-data/search")
    public String searchMarketData(@RequestParam String searchTerm, Model model) {
        logger.info("Market data search requested for: {}", searchTerm);

        try {
            List<ItemName> items = itemNameService.searchItemsByName(searchTerm);

            // Check which items have market data available
            Map<Integer, Boolean> itemDataAvailability = new HashMap<>();
            for (ItemName item : items) {
                boolean hasData = false;
                for (Long regionId : eveConfig.getImportRegions()) {
                    MarketStatistics stats = mokaamService.getLatestStatisticsForItem(item.getTypeId(), regionId);
                    if (stats != null) {
                        hasData = true;
                        break; // Found data in at least one region
                    }
                }
                itemDataAvailability.put(item.getTypeId(), hasData);
            }

            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("searchResults", items);
            model.addAttribute("itemDataAvailability", itemDataAvailability);
            model.addAttribute("importRegions", eveConfig.getImportRegions());

            long itemNamesCount = itemNameService.getItemNamesCount();
            model.addAttribute("itemNamesCount", itemNamesCount);

            return "market-data-search";
        } catch (Exception e) {
            logger.error("Failed to search market data for term {}: ", searchTerm, e);
            model.addAttribute("error", "Failed to search market data: " + e.getMessage());
            return "market-data-search";
        }
    }

    @PostMapping("/analyze")
    public String analyzeMarket(@RequestParam Long regionId, Model model) {
        logger.info("Monopoly scan requested for region: {}", regionId);

        try {
            List<MarketAnalysisResult> results = marketAnalysisService.analyzeMarkets(regionId).block();
            logger.info("Analysis completed. Found {} opportunities", results != null ? results.size() : 0);

            model.addAttribute("results", results);
            model.addAttribute("regionId", regionId);
            model.addAttribute("regionName", getRegionName(regionId));
            model.addAttribute("analysisTime", java.time.LocalDateTime.now());

            return "results";
        } catch (Exception e) {
            logger.error("Failed to analyze market for region {}: ", regionId, e);
            model.addAttribute("error", "Failed to analyze market: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/import-mokaam")
    public String importMokaamData(@RequestParam Long regionId, Model model) {
        logger.info("Mokaam data import requested for region: {}", regionId);

        try {
            String result = mokaamService.importHistoricalData(regionId).block();
            logger.info("Mokaam import completed: {}", result);

            model.addAttribute("message", result);
            model.addAttribute("importRegions", eveConfig.getImportRegions());

            // Refresh statistics count and last refresh date for import regions
            for (Long regId : eveConfig.getImportRegions()) {
                long count = mokaamService.getStatisticsCount(regId);
                model.addAttribute("statsCount_" + regId, count);

                java.time.LocalDate lastRefresh = mokaamService.getLastRefreshDate(regId);
                model.addAttribute("lastRefresh_" + regId, lastRefresh);
            }

            return "market-data-manage";
        } catch (Exception e) {
            logger.error("Failed to import Mokaam data for region {}: ", regionId, e);
            model.addAttribute("error", "Failed to import Mokaam data: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/import-mokaam-all")
    public String importMokaamDataAll(Model model) {
        logger.info("Mokaam data import requested for ALL regions");

        try {
            List<String> results = new ArrayList<>();
            
            for (Long regionId : eveConfig.getImportRegions()) {
                logger.info("Starting import for region: {}", regionId);
                String result = mokaamService.importHistoricalData(regionId).block();
                results.add(getRegionName(regionId) + ": " + result);
                logger.info("Completed import for region {}: {}", regionId, result);
            }

            String combinedMessage = "All regions imported successfully:\n" + String.join("\n", results);
            model.addAttribute("message", combinedMessage);
            model.addAttribute("importRegions", eveConfig.getImportRegions());

            // Refresh statistics count and last refresh date for all import regions
            for (Long regId : eveConfig.getImportRegions()) {
                long count = mokaamService.getStatisticsCount(regId);
                model.addAttribute("statsCount_" + regId, count);

                java.time.LocalDate lastRefresh = mokaamService.getLastRefreshDate(regId);
                model.addAttribute("lastRefresh_" + regId, lastRefresh);
            }

            return "market-data-manage";
        } catch (Exception e) {
            logger.error("Failed to import Mokaam data for all regions: ", e);
            model.addAttribute("error", "Failed to import Mokaam data for all regions: " + e.getMessage());
            return "market-data-manage";
        }
    }

    @GetMapping("/market-data/item/{typeId}")
    public String marketDataItemDetails(@PathVariable Integer typeId, Model model) {
        logger.info("Market data item details requested for type ID: {}", typeId);

        try {
            // Get item name
            ItemName itemName = itemNameService.getItemByTypeId(typeId);
            if (itemName == null) {
                logger.warn("Item not found for type ID: {}", typeId);
                model.addAttribute("error", "Item not found");
                return "market-data-search";
            }

            // Get market statistics for this item across all regions
            Map<Long, MarketStatistics> regionData = new HashMap<>();
            for (Long regionId : eveConfig.getImportRegions()) {
                logger.debug("Searching for statistics: typeId={}, regionId={}", typeId, regionId);
                MarketStatistics stats = mokaamService.getLatestStatisticsForItem(typeId, regionId);
                if (stats != null) {
                    logger.info("Found statistics for typeId={}, regionId={}: date={}, avgPrice={}", 
                               typeId, regionId, stats.getDate(), stats.getAveragePrice());
                    regionData.put(regionId, stats);
                } else {
                    logger.warn("No statistics found for typeId={}, regionId={}", typeId, regionId);
                }
            }

            logger.info("Total regions with data for typeId {}: {}", typeId, regionData.size());

            model.addAttribute("item", itemName);
            model.addAttribute("regionData", regionData);
            model.addAttribute("importRegions", eveConfig.getImportRegions());

            return "market-data-item-details";
        } catch (Exception e) {
            logger.error("Failed to load item details for type {}: ", typeId, e);
            model.addAttribute("error", "Failed to load item details: " + e.getMessage());
            return "market-data-search";
        }
    }

    @GetMapping("/debug/market-data")
    public String debugMarketData(Model model) {
        logger.info("Debug market data endpoint requested");

        Map<String, Object> debugInfo = new HashMap<>();

        // Check item names count
        long itemNamesCount = itemNameService.getItemNamesCount();
        debugInfo.put("itemNamesCount", itemNamesCount);

        // Check statistics count per region
        Map<Long, Long> regionCounts = new HashMap<>();
        for (Long regionId : eveConfig.getImportRegions()) {
            long count = mokaamService.getStatisticsCount(regionId);
            regionCounts.put(regionId, count);
        }
        debugInfo.put("regionCounts", regionCounts);

        // Get sample type IDs from database
        Map<Long, List<Integer>> sampleTypeIds = new HashMap<>();
        Set<Integer> allTypeIdsWithData = new java.util.HashSet<>();
        for (Long regionId : eveConfig.getImportRegions()) {
            List<Integer> typeIds = mokaamService.getSampleTypeIds(regionId, 10);
            sampleTypeIds.put(regionId, typeIds);
            allTypeIdsWithData.addAll(typeIds);
        }
        debugInfo.put("sampleTypeIds", sampleTypeIds);
        debugInfo.put("typeIdsWithData", allTypeIdsWithData);

        // Get item names for these type IDs to show what items actually have market data
        Map<Integer, String> itemNamesForSampleTypes = new HashMap<>();
        for (Integer typeId : allTypeIdsWithData) {
            ItemName itemName = itemNameService.getItemByTypeId(typeId);
            if (itemName != null) {
                itemNamesForSampleTypes.put(typeId, itemName.getName());
            } else {
                itemNamesForSampleTypes.put(typeId, null);
            }
        }
        debugInfo.put("itemNamesForSampleTypes", itemNamesForSampleTypes);

        model.addAttribute("debugInfo", debugInfo);
        return "debug-market-data";
    }

    @GetMapping("/tradehub")
    public String tradehubPage(Model model) {
        logger.info("Tradehub scanner page requested");

        model.addAttribute("minPriceDifference", eveConfig.getTradehub().getMinPriceDifferencePercentage());
        model.addAttribute("minMarketSize", eveConfig.getTradehub().getMinMarketSizeMillions());
        model.addAttribute("importRegions", eveConfig.getImportRegions());

        return "tradehub";
    }

    @PostMapping("/tradehub/scan")
    public String scanTradehub(@RequestParam String timePeriod, Model model) {
        logger.info("Tradehub scan requested for period: {}", timePeriod);

        try {
            List<TradehubComparisonResult> results = tradehubAnalysisService.scanTradehubDifferences(timePeriod);
            logger.info("Tradehub scan completed. Found {} price differences", results.size());

            model.addAttribute("results", results);
            model.addAttribute("timePeriod", timePeriod);
            model.addAttribute("minPriceDifference", eveConfig.getTradehub().getMinPriceDifferencePercentage());
            model.addAttribute("minMarketSize", eveConfig.getTradehub().getMinMarketSizeMillions());
            model.addAttribute("scanTime", java.time.LocalDateTime.now());

            return "tradehub-results";
        } catch (Exception e) {
            logger.error("Failed to scan tradehub differences for period {}: ", timePeriod, e);
            model.addAttribute("error", "Failed to scan tradehub differences: " + e.getMessage());
            model.addAttribute("minPriceDifference", eveConfig.getTradehub().getMinPriceDifferencePercentage());
            model.addAttribute("minMarketSize", eveConfig.getTradehub().getMinMarketSizeMillions());
            model.addAttribute("importRegions", eveConfig.getImportRegions());
            return "tradehub";
        }
    }

    private String getRegionName(Long regionId) {
        switch (regionId.intValue()) {
            case 10000002: return "The Forge (Jita)";
            case 10000043: return "Domain (Amarr)";
            case 10000032: return "Sinq Laison (Dodixie)";
            case 10000030: return "Heimatar (Rens)";
            case 10000042: return "Metropolis";
            default: return "Region " + regionId;
        }
    }
}
