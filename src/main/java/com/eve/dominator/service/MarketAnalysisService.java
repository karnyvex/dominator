package com.eve.dominator.service;

import com.eve.dominator.config.EveConfig;
import com.eve.dominator.model.MarketAnalysisResult;
import com.eve.dominator.model.MarketOrder;
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

    @Autowired
    public MarketAnalysisService(EsiService esiService, EveConfig eveConfig) {
        this.esiService = esiService;
        this.eveConfig = eveConfig;
    }

    public Mono<List<MarketAnalysisResult>> analyzeMarkets(long regionId) {
        long stationId = eveConfig.getStations().get(regionId);

        return esiService.getMarketOrders(regionId)
                .map(orders -> filterOrdersByStation(orders, stationId))
                .flatMap(this::analyzeOrdersByType);
    }

    private List<MarketOrder> filterOrdersByStation(List<MarketOrder> orders, long stationId) {
        return orders.stream()
                .filter(order -> order.getLocationId() == stationId)
                .filter(order -> !order.isBuyOrder()) // Only sell orders
                .sorted(Comparator.comparing(MarketOrder::getPrice))
                .collect(Collectors.toList());
    }

    private Mono<List<MarketAnalysisResult>> analyzeOrdersByType(List<MarketOrder> orders) {
        Map<Integer, List<MarketOrder>> ordersByType = orders.stream()
                .collect(Collectors.groupingBy(MarketOrder::getTypeId));

        return Flux.fromIterable(ordersByType.entrySet())
                .flatMap(entry -> {
                    MarketAnalysisResult result = calculateDominationOpportunity(entry.getKey(), entry.getValue());
                    if (result != null) {
                        return enrichWithTypeName(result);
                    } else {
                        return Mono.empty();
                    }
                })
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

    private MarketAnalysisResult calculateDominationOpportunity(int typeId, List<MarketOrder> orders) {
        double maxInvestment = eveConfig.getMonopoly().getMaxInvestmentMillions() * 1_000_000;
        double requiredRoi = eveConfig.getMonopoly().getTargetRoiPercentage();
        double taxRate = eveConfig.getMonopoly().getTaxPercentage() / 100.0;

        double totalCost = 0;
        int totalItems = 0;
        int ordersCleared = 0;

        // Sort orders by price (lowest first)
        orders.sort(Comparator.comparing(MarketOrder::getPrice));

        for (MarketOrder order : orders) {
            double orderCost = order.getPrice() * order.getVolumeRemain();

            if (totalCost + orderCost > maxInvestment) {
                break;
            }

            totalCost += orderCost;
            totalItems += order.getVolumeRemain();
            ordersCleared++;
        }

        if (ordersCleared == 0 || ordersCleared >= orders.size()) {
            return null; // No profitable opportunity or would clear entire market
        }

        // The target sell price is just below the first order we don't clear
        double nextOrderPrice = orders.get(ordersCleared).getPrice();
        double targetSellPrice = nextOrderPrice - 0.01; // 1 ISK below

        // Calculate required minimum sell price for desired ROI
        double avgBuyPrice = totalCost / totalItems;
        double minSellPriceForRoi = avgBuyPrice * (1 + (requiredRoi / 100.0)) / (1 - taxRate);

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

        return result;
    }
}
