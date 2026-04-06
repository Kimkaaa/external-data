package com.example.externaldata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardDto {
    private List<NewsItemDto> newsList;
    private List<ExchangeRateDto> exchangeRates;
    private StockIndexDto kospi;
    private StockIndexDto kosdaq;
}