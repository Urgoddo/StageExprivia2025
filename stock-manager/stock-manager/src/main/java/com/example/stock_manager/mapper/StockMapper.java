package com.example.stock_manager.mapper;

import com.example.stock_manager.dto.StockResponse;
import com.example.stock_manager.dto.StockValueResponse;
import com.example.stock_manager.model.Stock;

public final class StockMapper {

    private StockMapper() {
        // utility class
    }

    public static StockResponse toResponse(Stock stock) {
        if (stock == null) return null;
        return StockResponse.builder()
                .symbol(stock.getSymbol())
                .quantity(stock.getQuantity())
                .build();
    }

    public static StockValueResponse toValueResponse(Stock stock, double currentPrice) {
        if (stock == null) return null;
        return StockValueResponse.builder()
                .symbol(stock.getSymbol())
                .quantity(stock.getQuantity())
                .currentPrice(currentPrice)
                .totalValue(currentPrice * stock.getQuantity())
                .build();
    }
}
