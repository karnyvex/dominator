package com.eve.dominator.controller;

import com.eve.dominator.config.EveConfig;
import com.eve.dominator.model.MarketAnalysisResult;
import com.eve.dominator.service.MarketAnalysisService;
import com.eve.dominator.service.MokaamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MarketController {

    private static final Logger logger = LoggerFactory.getLogger(MarketController.class);

    private final MarketAnalysisService marketAnalysisService;
    private final MokaamService mokaamService;
    private final EveConfig eveConfig;

    @Autowired
    public MarketController(MarketAnalysisService marketAnalysisService, MokaamService mokaamService, EveConfig eveConfig) {
        this.marketAnalysisService = marketAnalysisService;
        this.mokaamService = mokaamService;
        this.eveConfig = eveConfig;
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
        logger.info("Market data page requested");
        logger.info("Import Regions: {}", eveConfig.getImportRegions());

        model.addAttribute("importRegions", eveConfig.getImportRegions());

        // Add statistics count for each import region
        for (Long regionId : eveConfig.getImportRegions()) {
            long count = mokaamService.getStatisticsCount(regionId);
            model.addAttribute("statsCount_" + regionId, count);
        }

        return "market-data";
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

            // Refresh statistics count for import regions
            for (Long regId : eveConfig.getImportRegions()) {
                long count = mokaamService.getStatisticsCount(regId);
                model.addAttribute("statsCount_" + regId, count);
            }

            return "market-data";
        } catch (Exception e) {
            logger.error("Failed to import Mokaam data for region {}: ", regionId, e);
            model.addAttribute("error", "Failed to import Mokaam data: " + e.getMessage());
            return "error";
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
