package com.example.externaldata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewsItemDto {
    private String title;
    private String link;
    private String imageUrl;
}