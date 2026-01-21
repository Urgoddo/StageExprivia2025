package com.example.stock_manager.exception;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String symbol) {
        super("Stock with symbol '" + symbol + "' not found");
    }
}
