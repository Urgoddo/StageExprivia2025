package com.example.stock_manager.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String symbol, int available, int requested) {
        super(String.format("Insufficient stock for '%s': available %d, requested %d", 
                symbol, available, requested));
    }
}
