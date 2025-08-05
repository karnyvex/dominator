package com.eve.dominator.service;

import com.eve.dominator.config.EveConfig;
import com.eve.dominator.model.MarketStatistics;
import com.eve.dominator.model.TradehubComparisonResult;
import com.eve.dominator.model.ItemName;
import com.eve.dominator.repository.MarketStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class TradehubAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(TradehubAnalysisService.class);

    private final MokaamService mokaamService;
    private final ItemNameService itemNameService;
    private final MarketStatisticsRepository statisticsRepository;
    private final EveConfig eveConfig;

    // Thread pool for parallel processing
    private final Executor executor;

    // Region name mapping
    private static final Map<Long, String> REGION_NAMES = Map.of(
        10000002L, "The Forge (Jita)",
        10000043L, "Domain (Amarr)",
        10000032L, "Sinq Laison (Dodixie)",
        10000030L, "Heimatar (Rens)",
        10000042L, "Metropolis (Hek)"
    );

    @Autowired
    public TradehubAnalysisService(MokaamService mokaamService, ItemNameService itemNameService,
                                 MarketStatisticsRepository statisticsRepository, EveConfig eveConfig) {
        this.mokaamService = mokaamService;
        this.itemNameService = itemNameService;
        this.statisticsRepository = statisticsRepository;
        this.eveConfig = eveConfig;

        // Create a thread pool with configurable number of threads
        int threadCount = eveConfig.getTradehub().getMaxThreads() > 0
            ? eveConfig.getTradehub().getMaxThreads()
            : Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
        this.executor = Executors.newFixedThreadPool(threadCount);
        logger.info("TradehubAnalysisService initialized with {} threads", threadCount);
    }

    public List<TradehubComparisonResult> scanTradehubDifferences(String timePeriod) {
        logger.info("Starting parallel tradehub scan for period: {}", timePeriod);
        long startTime = System.currentTimeMillis();

        // Step 1: Collect all unique type IDs with VWAP data in parallel
        Set<Integer> allTypeIds = collectTypeIdsInParallel(timePeriod);
        logger.info("Found {} unique items with {} VWAP data in {}ms",
                   allTypeIds.size(), timePeriod, System.currentTimeMillis() - startTime);

        // Step 2: Process items in parallel batches
        List<TradehubComparisonResult> results = processItemsInParallel(allTypeIds, timePeriod);

        // Step 3: Sort by price difference percentage (highest first)
        results.sort((a, b) -> Double.compare(b.getPriceDifferencePercentage(), a.getPriceDifferencePercentage()));

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("Parallel tradehub scan completed in {}ms. Found {} items with significant price differences",
                   totalTime, results.size());

        return results;
    }

    private Set<Integer> collectTypeIdsInParallel(String timePeriod) {
        // Use the simple, fast approach - get distinct type IDs from each region
        // and filter them during processing instead of in the database query
        List<CompletableFuture<Set<Integer>>> futures = eveConfig.getImportRegions().stream()
            .map(regionId -> CompletableFuture.supplyAsync(() -> {
                // Use the fast, simple query that we know works
                List<Integer> typeIds = statisticsRepository.findDistinctTypeIdsByRegionId(regionId, 5000); // Limit to 5000 items per region for performance
                Set<Integer> typeIdSet = new HashSet<>(typeIds);

                logger.debug("Region {} contributed {} items (limited to 5000 for performance)",
                           regionId, typeIdSet.size());
                return typeIdSet;
            }, executor))
            .collect(Collectors.toList());

        // Combine results from all regions
        Set<Integer> allTypeIds = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        logger.info("Total unique items to process: {}", allTypeIds.size());
        return allTypeIds;
    }

    private List<TradehubComparisonResult> processItemsInParallel(Set<Integer> allTypeIds, String timePeriod) {
        // Split items into batches for parallel processing
        int batchSize = Math.max(10, allTypeIds.size() / (Runtime.getRuntime().availableProcessors() * 4));
        List<List<Integer>> batches = partitionSet(allTypeIds, batchSize);

        logger.info("Processing {} items in {} batches of ~{} items each",
                   allTypeIds.size(), batches.size(), batchSize);

        // Process each batch in parallel
        List<CompletableFuture<List<TradehubComparisonResult>>> futures = new ArrayList<>();
        for (int i = 0; i < batches.size(); i++) {
            List<Integer> batch = batches.get(i);
            final int batchIndex = i;
            CompletableFuture<List<TradehubComparisonResult>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    logger.info("Starting batch {} with {} items", batchIndex, batch.size());
                    long batchStart = System.currentTimeMillis();
                    List<TradehubComparisonResult> results = processBatch(batch, timePeriod);
                    long batchTime = System.currentTimeMillis() - batchStart;
                    logger.info("Completed batch {} in {}ms, found {} results", batchIndex, batchTime, results.size());
                    return results;
                } catch (Exception e) {
                    logger.error("Error processing batch {}: ", batchIndex, e);
                    return new ArrayList<TradehubComparisonResult>();
                }
            }, executor);
            futures.add(future);
        }

        logger.info("All {} batch futures created, waiting for completion...", futures.size());

        // Combine results from all batches with timeout handling
        List<TradehubComparisonResult> allResults = new ArrayList<>();
        int completedBatches = 0;

        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<List<TradehubComparisonResult>> future = futures.get(i);
            try {
                logger.debug("Waiting for batch {} to complete...", i);
                // Add timeout to prevent infinite waiting
                List<TradehubComparisonResult> batchResults = future.get(30, java.util.concurrent.TimeUnit.SECONDS);
                allResults.addAll(batchResults);
                completedBatches++;
                if (completedBatches % 5 == 0 || completedBatches == futures.size()) {
                    logger.info("Completed {}/{} batches, total results so far: {}",
                              completedBatches, futures.size(), allResults.size());
                }
            } catch (java.util.concurrent.TimeoutException e) {
                logger.error("Batch {} timed out after 30 seconds", i);
                completedBatches++;
            } catch (Exception e) {
                logger.error("Error waiting for batch {} to complete: ", i, e);
                completedBatches++;
            }
        }

        logger.info("All batches completed. Total results: {}", allResults.size());
        return allResults;
    }

    private List<TradehubComparisonResult> processBatch(List<Integer> typeIds, String timePeriod) {
        List<TradehubComparisonResult> batchResults = new ArrayList<>();

        for (Integer typeId : typeIds) {
            try {
                TradehubComparisonResult result = processItem(typeId, timePeriod);
                if (result != null) {
                    batchResults.add(result);
                }
            } catch (Exception e) {
                logger.debug("Error processing item {}: {}", typeId, e.getMessage());
            }
        }

        logger.debug("Batch processed {} items, found {} opportunities",
                    typeIds.size(), batchResults.size());
        return batchResults;
    }

    private TradehubComparisonResult processItem(Integer typeId, String timePeriod) {
        try {
            // Collect statistics from all regions - but do it sequentially to avoid overwhelming the DB
            Map<Long, RegionData> regionDataMap = new HashMap<>();

            for (Long regionId : eveConfig.getImportRegions()) {
                try {
                    MarketStatistics stats = mokaamService.getLatestStatisticsForItem(typeId, regionId);
                    if (stats != null) {
                        Double vwapPrice = getVwapForPeriod(stats, timePeriod);
                        Long volume = getVolumeForPeriod(stats, timePeriod);

                        if (vwapPrice != null && vwapPrice > 0 && volume != null && volume > 0) {
                            double marketSize = vwapPrice * volume;
                            regionDataMap.put(regionId, new RegionData(regionId, vwapPrice, marketSize));
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Error fetching data for item {} region {}: {}", typeId, regionId, e.getMessage());
                }
            }

            // Need at least 2 regions to compare
            if (regionDataMap.size() < 2) {
                return null;
            }

            // Extract prices for comparison
            Map<Long, Double> regionPrices = regionDataMap.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getPrice()
                ));

            // Find min and max prices
            double minPrice = Collections.min(regionPrices.values());
            double maxPrice = Collections.max(regionPrices.values());

            // Calculate percentage difference
            double priceDifference = ((maxPrice - minPrice) / minPrice) * 100;

            // Check if it meets the minimum difference threshold
            if (priceDifference < eveConfig.getTradehub().getMinPriceDifferencePercentage()) {
                return null;
            }

            // Find the regions with min and max prices
            Long minRegion = regionPrices.entrySet().stream()
                .filter(entry -> entry.getValue().equals(minPrice))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);

            Long maxRegion = regionPrices.entrySet().stream()
                .filter(entry -> entry.getValue().equals(maxPrice))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);

            if (minRegion == null || maxRegion == null) {
                return null;
            }

            // Apply market size filter using already fetched data (no additional DB calls!)
            double minMarketSizeThreshold = eveConfig.getTradehub().getMinMarketSizeMillions() * 1_000_000;
            double minRegionMarketSize = regionDataMap.get(minRegion).getMarketSize();
            double maxRegionMarketSize = regionDataMap.get(maxRegion).getMarketSize();

            if (minRegionMarketSize < minMarketSizeThreshold && maxRegionMarketSize < minMarketSizeThreshold) {
                logger.debug("Item {} filtered out: minRegion {} market size {:.0f} ISK, maxRegion {} market size {:.0f} ISK, threshold {:.0f} ISK",
                            typeId, minRegion, minRegionMarketSize, maxRegion, maxRegionMarketSize, minMarketSizeThreshold);
                return null;
            }

            // Get item name
            ItemName itemName = itemNameService.getItemByTypeId(typeId);
            String name = itemName != null ? itemName.getName() : "Unknown Item";

            return new TradehubComparisonResult(
                typeId,
                name,
                minRegion,
                REGION_NAMES.getOrDefault(minRegion, "Unknown Region"),
                minPrice,
                maxRegion,
                REGION_NAMES.getOrDefault(maxRegion, "Unknown Region"),
                maxPrice
            );
        } catch (Exception e) {
            logger.debug("Error processing item {}: {}", typeId, e.getMessage());
            return null;
        }
    }

    // Enhanced data class that includes market size calculation
    private static class RegionData {
        private final Long regionId;
        private final Double price;
        private final Double marketSize;

        public RegionData(Long regionId, Double price, Double marketSize) {
            this.regionId = regionId;
            this.price = price;
            this.marketSize = marketSize;
        }

        public Long getRegionId() { return regionId; }
        public Double getPrice() { return price; }
        public Double getMarketSize() { return marketSize; }
    }

    // Utility method to partition a set into batches
    private static <T> List<List<T>> partitionSet(Set<T> set, int batchSize) {
        List<T> list = new ArrayList<>(set);
        List<List<T>> batches = new ArrayList<>();

        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(list.size(), i + batchSize);
            batches.add(list.subList(i, end));
        }

        return batches;
    }

    private boolean hasVwapDataForPeriod(MarketStatistics stats, String timePeriod) {
        Double vwap = getVwapForPeriod(stats, timePeriod);
        return vwap != null && vwap > 0;
    }

    private Double getVwapForPeriod(MarketStatistics stats, String timePeriod) {
        switch (timePeriod.toLowerCase()) {
            case "weekly":
                return stats.getVwapWeek();
            case "monthly":
                return stats.getVwapMonth();
            case "quarterly":
                return stats.getVwapQuarter();
            case "yearly":
                return stats.getVwapYear();
            default:
                logger.warn("Unknown time period: {}, defaulting to weekly", timePeriod);
                return stats.getVwapWeek();
        }
    }

    private Long getVolumeForPeriod(MarketStatistics stats, String timePeriod) {
        switch (timePeriod.toLowerCase()) {
            case "weekly":
                return stats.getVolumeWeek();
            case "monthly":
                return stats.getVolumeMonth();
            case "quarterly":
                return stats.getVolumeQuarter();
            case "yearly":
                return stats.getVolumeYear();
            default:
                logger.warn("Unknown time period: {}, defaulting to weekly", timePeriod);
                return stats.getVolumeWeek();
        }
    }
}
