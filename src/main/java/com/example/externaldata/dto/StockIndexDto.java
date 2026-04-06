package com.example.externaldata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StockIndexDto {
    private List<StockPointDto> points;
    private StockSummaryDto summary;
}