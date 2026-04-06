package com.example.externaldata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StockSummaryDto {
    private BigDecimal current;
    private BigDecimal change;
    private BigDecimal changeRate;
}