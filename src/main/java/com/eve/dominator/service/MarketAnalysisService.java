package com.eve.dominator.service;

import com.eve.dominator.config.EveConfig;
import com.eve.dominator.model.MarketAnalysisResult;
import com.eve.dominator.model.MarketOrder;
import com.eve.dominator.model.MarketStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketAnalysisService {

    private final EsiService esiService;
    private final EveConfig eveConfig;
    private final NpcDetectionService npcDetectionService;
    private final MokaamService mokaamService;

    @Autowired
    public MarketAnalysisService(EsiService esiService, EveConfig eveConfig, NpcDetectionService npcDetectionService, MokaamService mokaamService) {
        this.esiService = esiService;
        this.eveConfig = eveConfig;
        this.npcDetectionService = npcDetectionService;
        this.mokaamService = mokaamService;
    }

    public Mono<List<MarketAnalysisResult>> analyzeMarkets(long regionId) {
        long stationId = eveConfig.getStations().get(regionId);

        return esiService.getMarketOrders(regionId)
                .map(orders -> filterOrdersByStation(orders, stationId))
                .flatMap(orders -> analyzeOrdersByType(orders, regionId))
                .doOnSuccess(results -> {
                    // Show comprehensive summary at the END when everything is complete
                    System.out.println("\n" + "=".repeat(80));
                    System.out.println("🎉 MONOPOLY ANALYSIS COMPLETE 🎉");
                    System.out.println("=".repeat(80));
                    System.out.println("✅ Analysis completed. Found " + results.size() + " opportunities");
                    System.out.println("");

                    // Re-fetch orders to show the summary (this is quick since it's cached)
                    esiService.getMarketOrders(regionId).subscribe(allOrders -> {
                        List<MarketOrder> filteredOrders = filterOrdersByStation(allOrders, stationId);

                        System.out.println("📊 FINAL SUMMARY:");
                        System.out.println("   Total ESI orders fetched: " + allOrders.size());
                        System.out.println("   Orders at Jita (sell orders): " + filteredOrders.size());
                        System.out.println("   Target station ID: " + stationId);
                        System.out.println("");

                        // Show location distribution
                        Map<Long, Long> locationCounts = allOrders.stream()
                            .filter(order -> !order.isBuyOrder())
                            .collect(Collectors.groupingBy(MarketOrder::getLocationId, Collectors.counting()));

                        System.out.println("📍 Top 10 locations by order count (sell orders):");
                        locationCounts.entrySet().stream()
                            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                            .limit(10)
                            .forEach(entry -> System.out.println("   LocationID " + entry.getKey() + ": " + entry.getValue() + " orders"));

                        // === DURATION ANALYSIS SECTION - EASY TO REMOVE ===
                        System.out.println("");
                        System.out.println("⏰ ORDER DURATION ANALYSIS:");

                        // Count orders by duration
                        Map<String, Long> durationCounts = filteredOrders.stream()
                            .filter(order -> order.getDuration() != null && !order.getDuration().trim().isEmpty())
                            .collect(Collectors.groupingBy(MarketOrder::getDuration, Collectors.counting()));

                        System.out.println("   Duration distribution (Jita sell orders):");
                        durationCounts.entrySet().stream()
                            .sorted((e1, e2) -> {
                                try {
                                    return Integer.compare(Integer.parseInt(e1.getKey()), Integer.parseInt(e2.getKey()));
                                } catch (NumberFormatException ex) {
                                    return e1.getKey().compareTo(e2.getKey());
                                }
                            })
                            .forEach(entry -> {
                                String duration = entry.getKey();
                                Long count = entry.getValue();
                                String label = duration + " days";
                                if (Integer.parseInt(duration) > 90) {
                                    label += " (>90 - likely NPC)";
                                } else if (Integer.parseInt(duration) == 90) {
                                    label += " (max player duration)";
                                }
                                System.out.println("     " + label + ": " + count + " orders");
                            });

                        // Count potential NPC orders
                        long potentialNpcOrders = filteredOrders.stream()
                            .filter(order -> {
                                try {
                                    return order.getDuration() != null &&
                                           Integer.parseInt(order.getDuration()) > 90;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            })
                            .count();

                        System.out.println("   Potential NPC orders (duration > 90 days): " + potentialNpcOrders + " orders");
                        // === END DURATION ANALYSIS SECTION ===

                        System.out.println("=".repeat(80));
                    });
                });
    }

    private List<MarketOrder> filterOrdersByStation(List<MarketOrder> orders, long stationId) {
        System.out.println("=== ORDER FILTERING DEBUG ===");
        System.out.println("Total orders received from ESI: " + orders.size());
        System.out.println("Target station ID: " + stationId);

        List<MarketOrder> filteredOrders = orders.stream()
                .filter(order -> order.getLocationId() == stationId)
                .filter(order -> !order.isBuyOrder()) // Only sell orders
                .sorted(Comparator.comparing(MarketOrder::getPrice))
                .collect(Collectors.toList());

        System.out.println("Orders at target station (sell orders only): " + filteredOrders.size());

        return filteredOrders;
    }

    private Mono<List<MarketAnalysisResult>> analyzeOrdersByType(List<MarketOrder> orders, long regionId) {
        Map<Integer, List<MarketOrder>> ordersByType = orders.stream()
                .collect(Collectors.groupingBy(MarketOrder::getTypeId));

        return Flux.fromIterable(ordersByType.entrySet())
                .flatMap(entry -> {
                    return meetsVolumeRequirementsAsync(entry.getKey(), regionId)
                            .flatMap(meetsRequirements -> {
                                if (!meetsRequirements) {
                                    return Mono.empty();
                                }
                                MarketAnalysisResult result = calculateDominationOpportunity(entry.getKey(), entry.getValue(), regionId);
                                if (result != null) {
                                    return enrichWithTypeName(result);
                                } else {
                                    return Mono.empty();
                                }
                            });
                }, 10) // Limit concurrency to 10 parallel requests
                .collectList()
                .map(results -> results.stream()
                        .sorted(Comparator.comparing(MarketAnalysisResult::getRoiPercentage).reversed())
                        .collect(Collectors.toList()));
    }

    private Mono<MarketAnalysisResult> enrichWithTypeName(MarketAnalysisResult result) {
        return esiService.getTypeName(result.getTypeId())
                .map(itemName -> {
                    result.setItemName(itemName);
                    return result;
                })
                .onErrorReturn(result); // Return the result even if type name fetch fails
    }

    private MarketAnalysisResult calculateDominationOpportunity(int typeId, List<MarketOrder> orders, long regionId) {
        double maxInvestment = eveConfig.getMonopoly().getMaxInvestmentMillions() * 1_000_000;
        double requiredRoi = eveConfig.getMonopoly().getTargetRoiPercentage();
        double taxRate = eveConfig.getMonopoly().getTaxPercentage() / 100.0;

        // Apply NPC filtering if enabled - REJECT entire item if ANY NPC orders are detected
        if (eveConfig.getMonopoly().isEnableNpcFiltering()) {
            List<MarketOrder> originalOrders = new ArrayList<>(orders);
            List<MarketOrder> filteredOrders = npcDetectionService.filterNpcOrders(orders, eveConfig.getMonopoly().getNpcConfidenceThreshold());

            // If any orders were filtered out, it means NPC orders were detected
            if (filteredOrders.size() < originalOrders.size()) {
                int npcOrdersDetected = originalOrders.size() - filteredOrders.size();
                System.out.println("=== SKIPPING ITEM: NPC orders detected ===");
                System.out.println("TypeId " + typeId + ": Found " + npcOrdersDetected + " suspected NPC orders - cannot monopolize");
                return null; // Skip this entire item - can't monopolize against NPCs
            }
            // If no NPC orders detected, continue with all original orders
        }

        // Sort orders by price (lowest first)
        orders.sort(Comparator.comparing(MarketOrder::getPrice));

        // Debug logging to understand what orders we're working with
        System.out.println("=== DEBUG: Analyzing " + orders.size() + " orders for typeId " + typeId + " ===");
        for (int i = 0; i < Math.min(10, orders.size()); i++) {
            MarketOrder order = orders.get(i);
            System.out.println("Order " + (i+1) + ": " + order.getVolumeRemain() + " @ " + order.getPrice() + " ISK (locationId: " + order.getLocationId() + ")");
        }

        // Need at least 2 orders to create a monopoly opportunity
        if (orders.size() < 2) {
            return null;
        }

        MarketAnalysisResult bestOpportunity = null;
        double runningCost = 0;
        int runningItems = 0;

        // Test each possible stopping point (after buying each order)
        for (int stopIndex = 0; stopIndex < orders.size() - 1; stopIndex++) {
            MarketOrder currentOrder = orders.get(stopIndex);
            double orderCost = currentOrder.getPrice() * currentOrder.getVolumeRemain();

            // Check if we can afford this order within investment limit
            if (runningCost + orderCost > maxInvestment) {
                // Try partial purchase with remaining budget
                double remainingBudget = maxInvestment - runningCost;
                int partialItems = (int) (remainingBudget / currentOrder.getPrice());

                if (partialItems > 0) {
                    double scenarioCost = runningCost + (partialItems * currentOrder.getPrice());
                    int scenarioItems = runningItems + partialItems;
                    double targetPrice = orders.get(stopIndex + 1).getPrice() - 0.01;
                    double highestBuyPrice = currentOrder.getPrice(); // Price of the current (last) order being partially bought

                    System.out.println("Testing partial scenario: buy " + scenarioItems + " items for " + scenarioCost + " ISK, target price: " + targetPrice);

                    MarketAnalysisResult scenario = evaluateScenario(typeId, stopIndex + 1, scenarioItems, scenarioCost, targetPrice, requiredRoi, taxRate, highestBuyPrice);
                    if (scenario != null && (bestOpportunity == null || scenario.getRoiPercentage() > bestOpportunity.getRoiPercentage())) {
                        bestOpportunity = scenario;
                    }
                }
                break; // Can't afford any more within investment limit
            }

            // We can afford this entire order
            runningCost += orderCost;
            runningItems += currentOrder.getVolumeRemain();

            // Target price is just below the next order
            double targetPrice = orders.get(stopIndex + 1).getPrice() - 0.01;
            double highestBuyPrice = currentOrder.getPrice(); // Price of the current (last) order being bought

            System.out.println("Testing full scenario: buy " + runningItems + " items for " + runningCost + " ISK, target price: " + targetPrice);

            MarketAnalysisResult scenario = evaluateScenario(typeId, stopIndex + 1, runningItems, runningCost, targetPrice, requiredRoi, taxRate, highestBuyPrice);
            if (scenario != null && (bestOpportunity == null || scenario.getRoiPercentage() > bestOpportunity.getRoiPercentage())) {
                bestOpportunity = scenario;
                System.out.println("New best opportunity found with ROI: " + scenario.getRoiPercentage() + "%");
            }
        }

        if (bestOpportunity != null) {
            System.out.println("=== FINAL RESULT ===");
            System.out.println("Orders: " + bestOpportunity.getOrdersToBeCleared());
            System.out.println("Items: " + bestOpportunity.getTotalItemsToBuy());
            System.out.println("Cost: " + bestOpportunity.getTotalInvestment() + " ISK (limit: " + maxInvestment + " ISK)");
            System.out.println("Target: " + bestOpportunity.getTargetSellPrice());
            System.out.println("ROI: " + bestOpportunity.getRoiPercentage() + "% (required: " + requiredRoi + "%)");
        }

        return bestOpportunity;
    }

    private MarketAnalysisResult evaluateScenario(int typeId, int ordersCleared, int totalItems,
                                                 double totalCost, double targetSellPrice, double requiredRoi, double taxRate, double highestBuyPrice) {

        // Calculate required minimum sell price for desired ROI
        double avgBuyPrice = totalCost / totalItems;
        double minSellPriceForRoi = avgBuyPrice * (1 + (requiredRoi / 100.0)) / (1 - taxRate);

        // Check if target sell price meets our ROI requirements
        if (targetSellPrice < minSellPriceForRoi) {
            return null; // Not profitable enough
        }

        // Calculate actual profits
        double grossRevenue = targetSellPrice * totalItems;
        double netRevenue = grossRevenue * (1 - taxRate);
        double totalProfit = netRevenue - totalCost;
        double profitPerItem = totalProfit / totalItems;
        double actualRoi = (totalProfit / totalCost) * 100;

        MarketAnalysisResult result = new MarketAnalysisResult(typeId, "Calculating...", ordersCleared,
                totalItems, totalCost, targetSellPrice);
        result.setProfitPerItem(profitPerItem);
        result.setTotalProfit(totalProfit);
        result.setRoiPercentage(actualRoi);
        result.setHighestBuyPrice(highestBuyPrice); // Add the highest buy price for display

        return result;
    }

    /**
     * Check if an item meets the minimum volume and market size requirements based on historical data
     */
    private Mono<Boolean> meetsVolumeRequirementsAsync(int typeId, Long regionId) {
        return Mono.fromCallable(() -> {
            try {
                MarketStatistics latestStats = mokaamService.getLatestStatisticsForItem(typeId, regionId);
                if (latestStats == null) {
                    // No historical data available, skip filtering
                    return true;
                }

                // Check monthly volume requirement (region-specific with fallback to global)
                Long monthlyVolume = latestStats.getVolumeMonth();
                if (monthlyVolume != null && monthlyVolume < eveConfig.getMonopoly().getMinVolumeMonth(regionId)) {
                    return false;
                }

                // Check quarterly volume requirement (region-specific with fallback to global)
                Long quarterlyVolume = latestStats.getVolumeQuarter();
                if (quarterlyVolume != null && quarterlyVolume < eveConfig.getMonopoly().getMinVolumeQuarter(regionId)) {
                    return false;
                }

                // Check yearly volume requirement (region-specific with fallback to global)
                Long yearlyVolume = latestStats.getVolumeYear();
                if (yearlyVolume != null && yearlyVolume < eveConfig.getMonopoly().getMinVolumeYear(regionId)) {
                    return false;
                }

                // Check monthly market size requirement (convert millions to ISK)
                Double monthlyAvgPrice = latestStats.getAveragePriceMonth();
                if (monthlyVolume != null && monthlyAvgPrice != null) {
                    double monthlyMarketSizeISK = monthlyVolume * monthlyAvgPrice;
                    double requiredMarketSizeISK = eveConfig.getMonopoly().getMinMarketSizeMonth(regionId) * 1_000_000.0;
                    if (monthlyMarketSizeISK < requiredMarketSizeISK) {
                        return false;
                    }
                }

                // Check quarterly market size requirement (convert millions to ISK)
                Double quarterlyAvgPrice = latestStats.getAveragePriceQuarter();
                if (quarterlyVolume != null && quarterlyAvgPrice != null) {
                    double quarterlyMarketSizeISK = quarterlyVolume * quarterlyAvgPrice;
                    double requiredMarketSizeISK = eveConfig.getMonopoly().getMinMarketSizeQuarter(regionId) * 1_000_000.0;
                    if (quarterlyMarketSizeISK < requiredMarketSizeISK) {
                        return false;
                    }
                }

                // Check yearly market size requirement (convert millions to ISK)
                Double yearlyAvgPrice = latestStats.getAveragePriceYear();
                if (yearlyVolume != null && yearlyAvgPrice != null) {
                    double yearlyMarketSizeISK = yearlyVolume * yearlyAvgPrice;
                    double requiredMarketSizeISK = eveConfig.getMonopoly().getMinMarketSizeYear(regionId) * 1_000_000.0;
                    if (yearlyMarketSizeISK < requiredMarketSizeISK) {
                        return false;
                    }
                }

                return true; // All volume and market size requirements are met
            } catch (Exception e) {
                // If there's an error checking requirements, don't filter out the item
                return true;
            }
        });
    }
}
