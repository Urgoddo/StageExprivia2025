package com.example.stock_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockValueResponse {
    private String symbol;
    private int quantity;
    private double currentPrice;
    private double totalValue;
}
