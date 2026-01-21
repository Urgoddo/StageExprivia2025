package com.example.stock_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummary {
    private double totalValue;
    private double averagePricePerShare;
    private int totalStocks;
    private int totalQuantity;
    private List<StockDetail> stockDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockDetail {
        private String symbol;
        private int quantity;
        private double currentPrice;
        private double totalValue;
    }
}
