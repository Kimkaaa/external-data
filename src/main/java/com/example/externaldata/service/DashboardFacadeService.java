package com.example.externaldata.service;

import com.example.externaldata.dto.DashboardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardFacadeService {

    private final NewsService newsService;
    private final ExchangeService exchangeService;
    private final StockService stockService;

    public DashboardDto getDashboard() {
        return new DashboardDto(
                newsService.getTopNews(),
                exchangeService.getExchangeRates(),
                stockService.getKospi(),
                stockService.getKosdaq()
        );
    }
}