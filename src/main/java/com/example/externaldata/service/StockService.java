package com.example.externaldata.service;

import com.example.externaldata.dto.StockIndexDto;
import com.example.externaldata.dto.StockPointDto;
import com.example.externaldata.dto.StockSummaryDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private static final String API_SCHEME = "https";
    private static final String API_HOST = "apis.data.go.kr";
    private static final String API_PATH = "/1160100/service/GetMarketIndexInfoService/getStockMarketIndex";

    private static final String RESULT_TYPE = "json";
    private static final String PAGE_NO = "1";
    private static final String NUM_OF_ROWS = "10";

    private static final String KOSPI = "코스피";
    private static final String KOSDAQ = "코스닥";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${external.stock.api-key}")
    private String apiKey;

    public StockIndexDto getKospi() {
        return getStockIndex(KOSPI);
    }

    public StockIndexDto getKosdaq() {
        return getStockIndex(KOSDAQ);
    }

    private StockIndexDto getStockIndex(String indexName) {
        try {
            JsonNode items = getStockItems(indexName);

            if (items == null || !items.isArray() || items.size() == 0) {
                log.warn("{} 데이터가 비어 있습니다.", indexName);
                return new StockIndexDto(
                        Collections.emptyList(),
                        new StockSummaryDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
                );
            }

            List<StockPointDto> points = new ArrayList<>();

            for (JsonNode item : items) {
                String date = item.path("basDt").asText();
                BigDecimal closePrice = parseBigDecimal(item.path("clpr").asText());
                points.add(new StockPointDto(date, closePrice));
            }

            Collections.reverse(points);

            JsonNode latest = items.get(0);
            StockSummaryDto summary = new StockSummaryDto(
                    parseBigDecimal(latest.path("clpr").asText()),
                    parseBigDecimal(latest.path("vs").asText()),
                    parseBigDecimal(latest.path("fltRt").asText())
            );

            return new StockIndexDto(points, summary);

        } catch (Exception e) {
            log.error("{} 지수 데이터 조회 중 오류가 발생했습니다.", indexName, e);
            return new StockIndexDto(
                    Collections.emptyList(),
                    new StockSummaryDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
            );
        }
    }

    private JsonNode getStockItems(String indexName) throws Exception {
        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(API_SCHEME)
                        .host(API_HOST)
                        .path(API_PATH)
                        .queryParam("serviceKey", apiKey)
                        .queryParam("resultType", RESULT_TYPE)
                        .queryParam("pageNo", PAGE_NO)
                        .queryParam("numOfRows", NUM_OF_ROWS)
                        .queryParam("idxNm", indexName)
                        .build())
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);

        return root.path("response")
                .path("body")
                .path("items")
                .path("item");
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}