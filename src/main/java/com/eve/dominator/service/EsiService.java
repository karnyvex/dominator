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
        List<MarketOrder> allOrders = new ArrayList<>();

        return Flux.range(1, 100) // Max 100 pages to prevent infinite loops
                .concatMap(page ->
                        webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                        .path("/markets/{region_id}/orders/")
                                        .queryParam("page", page)
                                        .build(regionId))
                                .retrieve()
                                .bodyToMono(MarketOrder[].class)
                                .map(Arrays::asList)
                                .onErrorReturn(new ArrayList<>())
                )
                .takeWhile(orders -> !orders.isEmpty())
                .collectList()
                .map(listOfLists -> {
                    List<MarketOrder> result = new ArrayList<>();
                    for (List<MarketOrder> orderList : listOfLists) {
                        result.addAll(orderList);
                    }
                    return result;
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
