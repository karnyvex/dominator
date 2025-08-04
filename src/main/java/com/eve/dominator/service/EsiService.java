package com.eve.dominator.service;

import com.eve.dominator.config.EveConfig;
import com.eve.dominator.model.MarketOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EsiService {

    private final WebClient webClient;
    private final EveConfig eveConfig;

    @Autowired
    public EsiService(EveConfig eveConfig) {
        this.eveConfig = eveConfig;
        this.webClient = WebClient.builder()
                .baseUrl(eveConfig.getEsi().getBaseUrl())
                .defaultHeader("User-Agent", eveConfig.getEsi().getUserAgent())
                .build();
    }

    public Mono<List<MarketOrder>> getMarketOrders(long regionId) {
        return fetchAllPages(regionId);
    }

    private Mono<List<MarketOrder>> fetchAllPages(long regionId) {
        return fetchPage(regionId, 1, new ArrayList<>());
    }

    private Mono<List<MarketOrder>> fetchPage(long regionId, int page, List<MarketOrder> accumulator) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/markets/{region_id}/orders/")
                        .queryParam("page", page)
                        .build(regionId))
                .retrieve()
                .toEntity(MarketOrder[].class)
                .flatMap(response -> {
                    MarketOrder[] orders = response.getBody();
                    List<MarketOrder> orderList = orders != null ? Arrays.asList(orders) : new ArrayList<>();

                    System.out.println("ESI Page " + page + ": " + orderList.size() + " orders");

                    // Add current page orders to accumulator
                    accumulator.addAll(orderList);

                    // Check if this page was empty or less than full (indicating last page)
                    if (orderList.isEmpty()) {
                        System.out.println("ESI PAGINATION COMPLETE:");
                        System.out.println("  Total pages fetched: " + (page - 1));
                        System.out.println("  Total orders: " + accumulator.size());
                        System.out.println("  Stopped because: Empty page received");
                        return Mono.just(accumulator);
                    }

                    // ESI typically returns 1000 orders per page, if we get less, we're likely at the end
                    if (orderList.size() < 1000) {
                        System.out.println("ESI PAGINATION COMPLETE:");
                        System.out.println("  Total pages fetched: " + page);
                        System.out.println("  Total orders: " + accumulator.size());
                        System.out.println("  Stopped because: Partial page received (" + orderList.size() + " orders)");
                        return Mono.just(accumulator);
                    }

                    // Continue to next page
                    return fetchPage(regionId, page + 1, accumulator);
                })
                .onErrorResume(error -> {
                    // If we get a 404 or similar error, it likely means no more pages
                    System.out.println("ESI PAGINATION COMPLETE:");
                    System.out.println("  Total pages fetched: " + (page - 1));
                    System.out.println("  Total orders: " + accumulator.size());
                    System.out.println("  Stopped because: Error fetching page " + page + " - " + error.getMessage());
                    return Mono.just(accumulator);
                });
    }

    public Mono<String> getTypeName(int typeId) {
        return webClient.get()
                .uri("/universe/types/{type_id}/", typeId)
                .retrieve()
                .bodyToMono(TypeInfo.class)
                .map(typeInfo -> typeInfo != null && typeInfo.getName() != null ? typeInfo.getName() : "Unknown Item")
                .onErrorReturn("Unknown Item")
                .defaultIfEmpty("Unknown Item");
    }

    private static class TypeInfo {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
