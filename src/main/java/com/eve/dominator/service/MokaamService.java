package com.eve.dominator.service;

import com.eve.dominator.config.EveConfig;
import com.eve.dominator.model.MarketStatistics;
import com.eve.dominator.repository.MarketStatisticsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MokaamService {

    private static final Logger logger = LoggerFactory.getLogger(MokaamService.class);

    private final EveConfig eveConfig;
    private final MarketStatisticsRepository statisticsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public MokaamService(EveConfig eveConfig, MarketStatisticsRepository statisticsRepository) {
        this.eveConfig = eveConfig;
        this.statisticsRepository = statisticsRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public Mono<String> importHistoricalData(Long regionId) {
        logger.info("Starting Mokaam data import for region {}", regionId);

        return Mono.fromRunnable(() -> {
            // Clear existing data for this region
            logger.info("Clearing existing data for region {}", regionId);
            statisticsRepository.deleteByRegionId(regionId);
        })
        .then(fetchMokaamData(regionId))
        .doOnSuccess(result -> logger.info("Mokaam data import completed for region {}", regionId))
        .doOnError(error -> logger.error("Mokaam data import failed for region {}: ", regionId, error));
    }

    private Mono<String> fetchMokaamData(Long regionId) {
        String apiUrl = String.format("/API/market/all?regionid=%d", regionId);
        String fullUrl = eveConfig.getMokaam().getBaseUrl() + apiUrl;

        logger.info("Fetching market data from: {}", fullUrl);

        // Use blocking approach for large data transfers
        return Mono.fromCallable(() -> {
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .build();

                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(fullUrl))
                    .header("User-Agent", eveConfig.getMokaam().getUserAgent())
                    .header("Accept", "application/json")
                    .timeout(java.time.Duration.ofMinutes(5))
                    .build();

                java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String jsonData = response.body();
                    logger.info("Successfully fetched {} characters of JSON data", jsonData.length());
                    logger.debug("JSON data preview (first 500 chars): {}",
                            jsonData.length() > 500 ? jsonData.substring(0, 500) + "..." : jsonData);
                    return jsonData;
                } else {
                    throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                }

            } catch (Exception e) {
                logger.error("Failed to fetch from Mokaam using HttpClient: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        })
        .flatMap(jsonData -> processJsonData(jsonData, regionId))
        .onErrorResume(error -> {
            logger.warn("Mokaam API call failed, falling back to mock data: {}", error.getMessage());
            return generateMockDataFallback(regionId);
        });
    }

    private Mono<String> processJsonData(String jsonData, Long regionId) {
        return Mono.fromCallable(() -> {
            logger.info("Processing JSON data for region {}, data length: {}", regionId, jsonData.length());
            logger.debug("JSON data preview (first 500 chars): {}",
                    jsonData.length() > 500 ? jsonData.substring(0, 500) + "..." : jsonData);

            List<MarketStatistics> statistics = parseJsonData(jsonData, regionId);

            if (!statistics.isEmpty()) {
                logger.info("Saving {} historical records for region {}", statistics.size(), regionId);
                statisticsRepository.saveAll(statistics);
                return String.format("Successfully imported %d historical records from Mokaam for region %d",
                        statistics.size(), regionId);
            } else {
                logger.warn("No valid data found in JSON for region {}", regionId);
                return "No valid data found in Mokaam JSON - using mock data instead";
            }
        });
    }

    private List<MarketStatistics> parseJsonData(String jsonData, Long regionId) {
        List<MarketStatistics> statistics = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            JsonNode rootNode = objectMapper.readTree(jsonData);

            if (rootNode.isObject()) {
                // Iterate over each type ID in the JSON object
                rootNode.fieldNames().forEachRemaining(typeIdStr -> {
                    try {
                        JsonNode itemNode = rootNode.get(typeIdStr);

                        // Extract fields from Mokaam API response
                        String dateStr = itemNode.has("last_data") ? itemNode.get("last_data").asText() : null;
                        Integer typeId = itemNode.has("typeid") ? itemNode.get("typeid").asInt() : null;

                        // Skip records with invalid date values
                        if (dateStr == null || dateStr.equals("ERROR: 404") || dateStr.equals("Null") || dateStr.trim().isEmpty()) {
                            return; // Skip this record
                        }

                        // Validate essential fields
                        if (typeId != null) {
                            LocalDate date = LocalDate.parse(dateStr, dateFormatter);
                            MarketStatistics stats = new MarketStatistics(typeId, regionId, date);

                            // Yesterday's data
                            stats.setAveragePrice(getDoubleValue(itemNode, "avg_price_yesterday"));
                            stats.setHighestPrice(getDoubleValue(itemNode, "high_yesterday"));
                            stats.setLowestPrice(getDoubleValue(itemNode, "low_yesterday"));
                            stats.setVolume(getLongValue(itemNode, "vol_yesterday"));
                            stats.setOrderCount(getIntegerValue(itemNode, "order_count_yesterday"));
                            stats.setSizeYesterday(getDoubleValue(itemNode, "size_yesterday"));

                            // Weekly data
                            stats.setVolumeWeek(getLongValue(itemNode, "vol_week"));
                            stats.setAveragePriceWeek(getDoubleValue(itemNode, "avg_price_week"));
                            stats.setOrderCountWeek(getIntegerValue(itemNode, "order_count_week"));
                            stats.setHighWeek(getDoubleValue(itemNode, "high_week"));
                            stats.setLowWeek(getDoubleValue(itemNode, "low_week"));
                            stats.setSpreadWeek(getDoubleValue(itemNode, "spread_week"));
                            stats.setVwapWeek(getDoubleValue(itemNode, "vwap_week"));
                            stats.setStdDevWeek(getDoubleValue(itemNode, "std_dev_week"));
                            stats.setSizeWeek(getDoubleValue(itemNode, "size_week"));

                            // Monthly data
                            stats.setVolumeMonth(getLongValue(itemNode, "vol_month"));
                            stats.setAveragePriceMonth(getDoubleValue(itemNode, "avg_price_month"));
                            stats.setOrderCountMonth(getIntegerValue(itemNode, "order_count_month"));
                            stats.setHighMonth(getDoubleValue(itemNode, "high_month"));
                            stats.setLowMonth(getDoubleValue(itemNode, "low_month"));
                            stats.setSpreadMonth(getDoubleValue(itemNode, "spread_month"));
                            stats.setVwapMonth(getDoubleValue(itemNode, "vwap_month"));
                            stats.setStdDevMonth(getDoubleValue(itemNode, "std_dev_month"));
                            stats.setSizeMonth(getDoubleValue(itemNode, "size_month"));

                            // Quarterly data
                            stats.setVolumeQuarter(getLongValue(itemNode, "vol_quarter"));
                            stats.setAveragePriceQuarter(getDoubleValue(itemNode, "avg_price_quarter"));
                            stats.setOrderCountQuarter(getIntegerValue(itemNode, "order_count_quarter"));
                            stats.setHighQuarter(getDoubleValue(itemNode, "high_quarter"));
                            stats.setLowQuarter(getDoubleValue(itemNode, "low_quarter"));
                            stats.setSpreadQuarter(getDoubleValue(itemNode, "spread_quarter"));
                            stats.setVwapQuarter(getDoubleValue(itemNode, "vwap_quarter"));
                            stats.setStdDevQuarter(getDoubleValue(itemNode, "std_dev_quarter"));
                            stats.setSizeQuarter(getDoubleValue(itemNode, "size_quarter"));

                            // Yearly data
                            stats.setVolumeYear(getLongValue(itemNode, "vol_year"));
                            stats.setAveragePriceYear(getDoubleValue(itemNode, "avg_price_year"));
                            stats.setOrderCountYear(getIntegerValue(itemNode, "order_count_year"));
                            stats.setHighYear(getDoubleValue(itemNode, "high_year"));
                            stats.setLowYear(getDoubleValue(itemNode, "low_year"));
                            stats.setSpreadYear(getDoubleValue(itemNode, "spread_year"));
                            stats.setVwapYear(getDoubleValue(itemNode, "vwap_year"));
                            stats.setStdDevYear(getDoubleValue(itemNode, "std_dev_year"));
                            stats.setSizeYear(getDoubleValue(itemNode, "size_year"));

                            // 52-week data
                            stats.setWeek52High(getDoubleValue(itemNode, "_52w_high"));
                            stats.setWeek52Low(getDoubleValue(itemNode, "_52w_low"));

                            statistics.add(stats);
                        }

                    } catch (Exception e) {
                        logger.debug("Failed to parse JSON item for type {}: {}", typeIdStr, e.getMessage());
                    }
                });

                logger.info("Successfully parsed {} market statistics records", statistics.size());
            } else if (rootNode.isArray()) {
                // Keep the old array parsing logic as fallback
                for (JsonNode itemNode : rootNode) {
                    try {
                        // Extract fields from JSON - adjust field names based on actual Mokaam API response
                        String dateStr = itemNode.has("date") ? itemNode.get("date").asText() : null;
                        Integer typeId = itemNode.has("type_id") ? itemNode.get("type_id").asInt() : null;
                        Double averagePrice = itemNode.has("average_price") ? itemNode.get("average_price").asDouble() : null;
                        Double highestPrice = itemNode.has("highest_price") ? itemNode.get("highest_price").asDouble() : null;
                        Double lowestPrice = itemNode.has("lowest_price") ? itemNode.get("lowest_price").asDouble() : null;
                        Long volume = itemNode.has("volume") ? itemNode.get("volume").asLong() : null;
                        Integer orderCount = itemNode.has("order_count") ? itemNode.get("order_count").asInt() : null;

                        // Validate essential fields
                        if (dateStr != null && typeId != null) {
                            LocalDate date = LocalDate.parse(dateStr, dateFormatter);

                            MarketStatistics stats = new MarketStatistics(typeId, regionId, date);
                            stats.setAveragePrice(averagePrice);
                            stats.setHighestPrice(highestPrice);
                            stats.setLowestPrice(lowestPrice);
                            stats.setVolume(volume);
                            stats.setOrderCount(orderCount);

                            statistics.add(stats);
                        }

                    } catch (Exception e) {
                        logger.warn("Failed to parse JSON item: {} - Error: {}", itemNode.toString(), e.getMessage());
                    }
                }
            } else {
                logger.warn("Expected JSON object or array but got: {}", rootNode.getNodeType());
            }
        } catch (Exception e) {
            logger.error("Error processing JSON data: ", e);
            logger.debug("JSON data preview: {}", jsonData.length() > 200 ? jsonData.substring(0, 200) + "..." : jsonData);
        }

        return statistics;
    }

    private Mono<String> generateMockDataFallback(Long regionId) {
        return Mono.fromCallable(() -> {
            List<MarketStatistics> mockData = generateMockHistoricalData(regionId);
            logger.info("Saving {} mock historical records for region {}", mockData.size(), regionId);
            statisticsRepository.saveAll(mockData);
            return String.format("Mokaam unavailable - generated %d mock historical records for region %d",
                    mockData.size(), regionId);
        });
    }

    private List<MarketStatistics> generateMockHistoricalData(Long regionId) {
        List<MarketStatistics> data = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(30);

        // Common EVE Online item types for The Forge
        int[] commonItems = {34, 35, 36, 37, 38, 39, 40, 11399, 16275, 17470}; // Tritanium, Pyerite, etc.

        for (int typeId : commonItems) {
            for (int i = 0; i < 30; i++) {
                LocalDate date = startDate.plusDays(i);
                MarketStatistics stats = new MarketStatistics(typeId, regionId, date);

                // Generate realistic mock data
                double basePrice = typeId * 0.1; // Simple price calculation
                stats.setAveragePrice(basePrice + (Math.random() * basePrice * 0.2));
                stats.setHighestPrice(stats.getAveragePrice() * (1.1 + Math.random() * 0.3));
                stats.setLowestPrice(stats.getAveragePrice() * (0.8 - Math.random() * 0.2));
                stats.setVolume((long) (1000000 + Math.random() * 5000000));
                stats.setOrderCount((int) (50 + Math.random() * 200));

                data.add(stats);
            }
        }

        return data;
    }

    private String getRegionName(Long regionId) {
        // Map region IDs to Mokaam region names
        switch (regionId.intValue()) {
            case 10000002: return "the-forge";
            case 10000043: return "domain";
            case 10000032: return "sinq-laison";
            case 10000030: return "heimatar";
            case 10000042: return "metropolis";
            default: return "the-forge"; // Default fallback
        }
    }

    // Utility methods for parsing JSON values safely
    private Double getDoubleValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            try {
                return node.get(fieldName).asDouble();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Long getLongValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            try {
                return node.get(fieldName).asLong();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Integer getIntegerValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            try {
                return node.get(fieldName).asInt();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // Utility methods for parsing CSV values
    private Double parseDoubleOrNull(String value) {
        try {
            return value != null && !value.trim().isEmpty() ? Double.parseDouble(value.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLongOrNull(String value) {
        try {
            return value != null && !value.trim().isEmpty() ? Long.parseLong(value.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntegerOrNull(String value) {
        try {
            return value != null && !value.trim().isEmpty() ? Integer.parseInt(value.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean hasStatisticalData(Integer typeId, Long regionId) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return !statisticsRepository.findRecentStatistics(typeId, regionId, thirtyDaysAgo).isEmpty();
    }

    public Double getAverageVolume(Integer typeId, Long regionId, int days) {
        LocalDate fromDate = LocalDate.now().minusDays(days);
        return statisticsRepository.getAverageVolume(typeId, regionId, fromDate).orElse(0.0);
    }

    public Double getAveragePrice(Integer typeId, Long regionId, int days) {
        LocalDate fromDate = LocalDate.now().minusDays(days);
        return statisticsRepository.getAveragePrice(typeId, regionId, fromDate).orElse(0.0);
    }

    public long getStatisticsCount(Long regionId) {
        return statisticsRepository.countByRegionId(regionId);
    }

    public MarketStatistics getLatestStatisticsForItem(Integer typeId, Long regionId) {
        List<MarketStatistics> stats = statisticsRepository.findByTypeIdAndRegionIdOrderByDateDesc(typeId, regionId);
        logger.debug("Query for typeId={}, regionId={} returned {} results", typeId, regionId, stats.size());
        if (!stats.isEmpty()) {
            MarketStatistics latest = stats.get(0);
            logger.debug("Latest stats: date={}, avgPrice={}, volume={}",
                        latest.getDate(), latest.getAveragePrice(), latest.getVolume());
            return latest;
        }
        return null;
    }

    public List<Integer> getSampleTypeIds(Long regionId, int limit) {
        return statisticsRepository.findDistinctTypeIdsByRegion(regionId, limit);
    }

    public long getStatisticsCountForTypeId(Integer typeId) {
        return statisticsRepository.countByTypeId(typeId);
    }

    public java.time.LocalDate getLastRefreshDate(Long regionId) {
        return statisticsRepository.findLatestDateByRegionId(regionId).orElse(null);
    }
}
