package com.eve.dominator.service;

import com.eve.dominator.model.MarketOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simplified NPC Order Detection Service
 * Uses only duration criteria: orders with duration >= 90 days are considered NPC
 */
@Service
public class NpcDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(NpcDetectionService.class);

    /**
     * Calculate NPC confidence score for a single order
     * @param order The market order to analyze
     * @param allOrdersForItem All orders for the same item type (unused in simplified version)
     * @return Confidence score: 1.0 if duration >= 90 days, 0.0 otherwise
     */
    public double calculateNpcScore(MarketOrder order, List<MarketOrder> allOrdersForItem) {
        return calculateDurationScore(order);
    }

    /**
     * Filter orders by removing those above the NPC confidence threshold
     * @param orders List of orders to filter
     * @param confidenceThreshold Threshold above which orders are considered NPC
     * @return Filtered list with suspected NPC orders removed
     */
    public List<MarketOrder> filterNpcOrders(List<MarketOrder> orders, double confidenceThreshold) {
        if (orders.isEmpty()) {
            return new ArrayList<>(orders);
        }

        List<MarketOrder> filteredOrders = orders.stream()
                .filter(order -> {
                    double npcScore = calculateNpcScore(order, orders);
                    boolean isNpc = npcScore >= confidenceThreshold;
                    if (isNpc) {
                        logger.debug("Filtering suspected NPC order: {} (duration: {} days, score: {})",
                                order.getOrderId(), order.getDuration(), npcScore);
                    }
                    return !isNpc;
                })
                .collect(Collectors.toList());

        int removedCount = orders.size() - filteredOrders.size();
        if (removedCount > 0) {
            logger.info("Filtered {} suspected NPC orders out of {} total orders (threshold: {})",
                    removedCount, orders.size(), confidenceThreshold);
        }

        return filteredOrders;
    }

    private double calculateDurationScore(MarketOrder order) {
        try {
            // If duration field is null or empty, treat as non-NPC
            if (order.getDuration() == null || order.getDuration().trim().isEmpty()) {
                System.out.println("DEBUG NPC: Order " + order.getOrderId() + " - No duration data, treating as non-NPC");
                return 0.0;
            }

            int duration = Integer.parseInt(order.getDuration());

            // Simple rule: original duration > 90 days = NPC order
            // Players can only create orders with max 90 days, NPCs often use 365+ days
            if (duration > 90) {
                System.out.println("DEBUG NPC: Order " + order.getOrderId() + " - Flagged as NPC: original duration " + duration + " days > 90 days (player max)");
                return 1.0; // Definitely NPC
            }

            System.out.println("DEBUG NPC: Order " + order.getOrderId() + " - Not NPC: original duration " + duration + " days <= 90 days (player range)");
            return 0.0; // Not NPC

        } catch (NumberFormatException e) {
            System.out.println("DEBUG NPC: Order " + order.getOrderId() + " - Could not parse duration '" + order.getDuration() + "', treating as non-NPC");
            return 0.0; // If we can't parse duration, assume it's not NPC
        }
    }
}
