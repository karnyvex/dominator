package com.eve.dominator.service;

import com.eve.dominator.config.EveConfig;
import com.eve.dominator.model.ItemName;
import com.eve.dominator.repository.ItemNameRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemNameService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ItemNameService.class);

    private final EveConfig eveConfig;
    private final ItemNameRepository itemNameRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ItemNameService(EveConfig eveConfig, ItemNameRepository itemNameRepository) {
        this.eveConfig = eveConfig;
        this.itemNameRepository = itemNameRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (eveConfig.getMarketData().isEnableItemNamesImport()) {
            logger.info("Item names import is enabled, starting import process...");
            importItemNames();
        } else {
            logger.info("Item names import is disabled");
        }
    }

    public void importItemNames() {
        try {
            logger.info("Importing item names from Mokaam API...");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://mokaam.dk/API/market/type_ids"))
                    .header("User-Agent", eveConfig.getMokaam().getUserAgent())
                    .header("Accept", "application/json")
                    .timeout(Duration.ofMinutes(2))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String jsonData = response.body();
                logger.info("Successfully fetched {} characters of type_ids data", jsonData.length());

                List<ItemName> itemNames = parseItemNames(jsonData);

                if (!itemNames.isEmpty()) {
                    logger.info("Saving {} item names to database...", itemNames.size());
                    itemNameRepository.saveAll(itemNames);
                    logger.info("Successfully imported {} item names", itemNames.size());
                } else {
                    logger.warn("No item names found in response");
                }
            } else {
                logger.error("Failed to fetch item names: HTTP {}", response.statusCode());
            }
        } catch (Exception e) {
            logger.error("Error importing item names: ", e);
        }
    }

    private List<ItemName> parseItemNames(String jsonData) throws Exception {
        List<ItemName> itemNames = new ArrayList<>();

        JsonNode rootNode = objectMapper.readTree(jsonData);

        if (rootNode.isObject()) {
            rootNode.fieldNames().forEachRemaining(typeIdStr -> {
                try {
                    Integer typeId = Integer.parseInt(typeIdStr);
                    JsonNode itemNode = rootNode.get(typeIdStr);

                    if (itemNode.has("name")) {
                        String name = itemNode.get("name").asText();
                        itemNames.add(new ItemName(typeId, name));
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Invalid type ID: {}", typeIdStr);
                }
            });
        }

        return itemNames;
    }

    public long getItemNamesCount() {
        return itemNameRepository.countAll();
    }

    public List<ItemName> searchItemsByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return itemNameRepository.findByNameContainingIgnoreCase(searchTerm.trim());
    }

    public ItemName getItemByTypeId(Integer typeId) {
        return itemNameRepository.findByTypeId(typeId).orElse(null);
    }
}
