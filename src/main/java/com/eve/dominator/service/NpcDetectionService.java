package com.eve.dominator.service;

import com.eve.dominator.model.MarketOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reusable NPC Order Detection Service
 * Uses a scoring system to identify potential NPC orders based on various heuristics
 */
@Service
public class NpcDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(NpcDetectionService.class);

    // Common NPC order characteristics
    private static final Set<Integer> COMMON_NPC_DURATIONS = Set.of(90, 365); // 90 or 365 days
    private static final int MIN_NPC_VOLUME = 1000; // NPCs usually have large volumes
    private static final double MAX_NPC_PRICE_VARIANCE = 0.02; // NPCs use consistent pricing (2% variance)

    // Scoring weights
    private static final double DURATION_WEIGHT = 0.4;
    private static final double VOLUME_WEIGHT = 0.3;
    private static final double PRICE_PATTERN_WEIGHT = 0.3;

    /**
     * Calculate NPC confidence score for a single order
     * @param order The market order to analyze
     * @param allOrdersForItem All orders for the same item type (for statistical analysis)
     * @return Confidence score between 0.0 and 1.0 (higher = more likely to be NPC)
     */
    public double calculateNpcScore(MarketOrder order, List<MarketOrder> allOrdersForItem) {
        double score = 0.0;

        // Phase 1: Duration-based scoring
        score += calculateDurationScore(order) * DURATION_WEIGHT;

        // Phase 2: Volume-based scoring
        score += calculateVolumeScore(order, allOrdersForItem) * VOLUME_WEIGHT;

        // Phase 2: Price pattern scoring
        score += calculatePricePatternScore(order) * PRICE_PATTERN_WEIGHT;

        logger.debug("NPC score for order {}: {} (duration: {}, volume: {}, price: {})",
                order.getOrderId(), score,
                calculateDurationScore(order),
                calculateVolumeScore(order, allOrdersForItem),
                calculatePricePatternScore(order));

        return Math.min(1.0, score); // Cap at 1.0
    }

    /**
     * Filter orders by removing those above the NPC confidence threshold
     * @param orders List of orders to filter
     * @param confidenceThreshold Threshold above which orders are considered NPC
     * @return Filtered list with suspected NPC orders removed
     */
    public List<MarketOrder> filterNpcOrders(List<MarketOrder> orders, double confidenceThreshold) {
        if (orders.isEmpty()) {
            return new ArrayList<>(orders); // Return mutable list
        }

        List<MarketOrder> filteredOrders = orders.stream()
                .filter(order -> {
                    double npcScore = calculateNpcScore(order, orders);
                    boolean isNpc = npcScore >= confidenceThreshold;
                    if (isNpc) {
                        logger.debug("Filtering suspected NPC order: {} (score: {}, volume: {}, duration: {})",
                                order.getOrderId(), npcScore, order.getVolumeTotal(), order.getDuration());
                    }
                    return !isNpc;
                })
                .collect(Collectors.toList()); // This returns a mutable ArrayList

        int removedCount = orders.size() - filteredOrders.size();
        if (removedCount > 0) {
            logger.info("Filtered {} suspected NPC orders out of {} total orders (threshold: {})",
                    removedCount, orders.size(), confidenceThreshold);
        }

        return filteredOrders;
    }

    private double calculateDurationScore(MarketOrder order) {
        try {
            int duration = Integer.parseInt(order.getDuration());
            if (COMMON_NPC_DURATIONS.contains(duration)) {
                return 1.0; // Very likely NPC
            }
            // NPCs often use maximum duration
            if (duration >= 90) {
                return 0.7; // Likely NPC
            }
            return 0.0; // Normal duration
        } catch (NumberFormatException e) {
            logger.debug("Could not parse duration '{}' for order {}", order.getDuration(), order.getOrderId());
            return 0.0;
        }
    }

    private double calculateVolumeScore(MarketOrder order, List<MarketOrder> allOrdersForItem) {
        int volume = order.getVolumeTotal();

        // Must meet minimum NPC volume threshold
        if (volume < MIN_NPC_VOLUME) {
            return 0.0;
        }

        // Calculate statistical outlier score
        double medianVolume = calculateMedianVolume(allOrdersForItem);
        if (medianVolume > 0) {
            double volumeRatio = volume / medianVolume;
            if (volumeRatio > 10.0) {
                return 1.0; // Extreme outlier, very likely NPC
            } else if (volumeRatio > 5.0) {
                return 0.8; // Significant outlier, likely NPC
            } else if (volumeRatio > 3.0) {
                return 0.5; // Moderate outlier, possibly NPC
            }
        }

        // Large absolute volumes are suspicious
        if (volume > 100000) {
            return 0.9;
        } else if (volume > 50000) {
            return 0.6;
        } else if (volume > 10000) {
            return 0.3;
        }

        return 0.0;
    }

    private double calculatePricePatternScore(MarketOrder order) {
        double price = order.getPrice();

        // Check for perfect round numbers (common in NPC orders)
        if (isPerfectRoundNumber(price)) {
            return 0.8;
        }

        // Check for common NPC price patterns
        if (hasNpcPricePattern(price)) {
            return 0.6;
        }

        return 0.0;
    }

    private boolean isPerfectRoundNumber(double price) {
        // Check if price is a perfect round number (100.00, 1000.00, etc.)
        return price > 0 && price == Math.floor(price) && (price % 10 == 0 || price % 100 == 0);
    }

    private boolean hasNpcPricePattern(double price) {
        String priceStr = String.format("%.2f", price);
        // Common NPC patterns: .00, .01, .99 endings
        return priceStr.endsWith(".00") || priceStr.endsWith(".01") || priceStr.endsWith(".99");
    }

    private double calculateMedianVolume(List<MarketOrder> orders) {
        if (orders.isEmpty()) {
            return 0.0;
        }

        List<Integer> volumes = orders.stream()
                .map(MarketOrder::getVolumeTotal)
                .sorted()
                .toList();

        int size = volumes.size();
        if (size % 2 == 0) {
            return (volumes.get(size / 2 - 1) + volumes.get(size / 2)) / 2.0;
        } else {
            return volumes.get(size / 2);
        }
    }
}
