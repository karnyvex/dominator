package com.eve.dominator.controller;

import com.eve.dominator.config.EveConfig;
import com.eve.dominator.model.MarketAnalysisResult;
import com.eve.dominator.service.MarketAnalysisService;
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
    private final EveConfig eveConfig;

    @Autowired
    public MarketController(MarketAnalysisService marketAnalysisService, EveConfig eveConfig) {
        this.marketAnalysisService = marketAnalysisService;
        this.eveConfig = eveConfig;
        logger.info("MarketController initialized with config: {}", eveConfig);
    }

    @GetMapping("/")
    public String index(Model model) {
        logger.info("Index page requested");
        logger.info("Regions: {}", eveConfig.getRegions());
        logger.info("Max Investment: {}", eveConfig.getInvestment().getMaxMillions());

        model.addAttribute("regions", eveConfig.getRegions());
        model.addAttribute("maxInvestment", eveConfig.getInvestment().getMaxMillions());
        model.addAttribute("roiPercentage", eveConfig.getProfit().getRoiPercentage());
        model.addAttribute("taxPercentage", eveConfig.getProfit().getTaxPercentage());
        return "index";
    }

    @PostMapping("/analyze")
    public String analyzeMarket(@RequestParam Long regionId, Model model) {
        logger.info("Market analysis requested for region: {}", regionId);

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

    private String getRegionName(Long regionId) {
        // For now, just handle The Forge
        if (regionId == 10000002L) {
            return "The Forge";
        }
        return "Region " + regionId;
    }
}
