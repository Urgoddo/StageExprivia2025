package com.example.stock_manager.exception;

public class DuplicateStockException extends RuntimeException {
    public DuplicateStockException(String symbol) {
        super("Stock with symbol '" + symbol + "' already exists");
    }
}
