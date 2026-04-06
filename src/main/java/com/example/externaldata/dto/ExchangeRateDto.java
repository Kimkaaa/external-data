package com.example.externaldata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ExchangeRateDto {
    private String currency;
    private BigDecimal baseRate;
    private BigDecimal change;
    private BigDecimal changeRate;
    private String direction;
}