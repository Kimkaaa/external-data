package com.example.externaldata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StockPointDto {
    private String date;
    private BigDecimal closePrice;
}