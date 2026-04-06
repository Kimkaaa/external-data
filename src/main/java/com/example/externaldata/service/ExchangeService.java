package com.example.externaldata.service;

import com.example.externaldata.dto.ExchangeRateDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeService {

    private static final String API_SCHEME = "https";
    private static final String API_HOST = "oapi.koreaexim.go.kr";
    private static final String API_PATH = "/site/program/financial/exchangeJSON";
    private static final String DATA_TYPE = "AP01";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final List<String> TARGET_CURRENCIES = List.of("USD", "JPY(100)", "EUR");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${external.exchange.api-key}")
    private String apiKey;

    public List<ExchangeRateDto> getExchangeRates() {
        List<ExchangeRateDto> result = new ArrayList<>();

        try {
            LocalDate today = LocalDate.now();
            LocalDate latestBusinessDay = getLatestBusinessDay(today);
            LocalDate previousBusinessDay = getPreviousBusinessDay(latestBusinessDay);

            log.info("환율 조회 시작. latestBusinessDay={}, previousBusinessDay={}",
                    latestBusinessDay, previousBusinessDay);

            JsonNode latestRates = getExchangeJson(latestBusinessDay);
            JsonNode previousRates = getExchangeJson(previousBusinessDay);

            if (latestRates == null || previousRates == null || !latestRates.isArray() || !previousRates.isArray()) {
                log.warn("환율 데이터를 정상적으로 가져오지 못했습니다. latestRates={}, previousRates={}",
                        latestRates != null, previousRates != null);
                return result;
            }

            for (String currency : TARGET_CURRENCIES) {
                result.add(createExchangeRateDto(currency, latestRates, previousRates));
            }

        } catch (Exception e) {
            log.error("환율 데이터 조회 중 오류가 발생했습니다.", e);
        }

        return result;
    }

    private JsonNode getExchangeJson(LocalDate date) {
        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme(API_SCHEME)
                            .host(API_HOST)
                            .path(API_PATH)
                            .queryParam("authkey", apiKey)
                            .queryParam("searchdate", date.format(DATE_FORMATTER))
                            .queryParam("data", DATA_TYPE)
                            .build())
                    .retrieve()
                    .body(String.class);

            return objectMapper.readTree(response);
        } catch (Exception e) {
            log.error("환율 API 호출 또는 JSON 파싱 실패. date={}", date, e);
            return null;
        }
    }

    private ExchangeRateDto createExchangeRateDto(String currency, JsonNode latestRates, JsonNode previousRates) {
        BigDecimal latestRate = findRateByCurrency(latestRates, currency);
        BigDecimal previousRate = findRateByCurrency(previousRates, currency);

        if (latestRate == null || latestRate.compareTo(BigDecimal.ZERO) == 0) {
            return new ExchangeRateDto(currency, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "NONE");
        }

        if (previousRate == null || previousRate.compareTo(BigDecimal.ZERO) == 0) {
            return new ExchangeRateDto(currency, latestRate, BigDecimal.ZERO, BigDecimal.ZERO, "NONE");
        }

        BigDecimal change = latestRate.subtract(previousRate);
        BigDecimal changeRate = change.divide(previousRate, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        String direction = "SAME";
        if (change.compareTo(BigDecimal.ZERO) > 0) {
            direction = "UP";
        } else if (change.compareTo(BigDecimal.ZERO) < 0) {
            direction = "DOWN";
        }

        return new ExchangeRateDto(currency, latestRate, change, changeRate, direction);
    }

    private BigDecimal findRateByCurrency(JsonNode rates, String currency) {
        for (JsonNode node : rates) {
            String curUnit = node.path("cur_unit").asText();
            if (currency.equals(curUnit)) {
                return parseBigDecimal(node.path("deal_bas_r").asText());
            }
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate getLatestBusinessDay(LocalDate date) {
        LocalDate target = date;
        if (target.getDayOfWeek() == DayOfWeek.SATURDAY) {
            target = target.minusDays(1);
        }
        if (target.getDayOfWeek() == DayOfWeek.SUNDAY) {
            target = target.minusDays(2);
        }
        return target;
    }

    private LocalDate getPreviousBusinessDay(LocalDate date) {
        LocalDate target = date.minusDays(1);
        while (target.getDayOfWeek() == DayOfWeek.SATURDAY || target.getDayOfWeek() == DayOfWeek.SUNDAY) {
            target = target.minusDays(1);
        }
        return target;
    }
}