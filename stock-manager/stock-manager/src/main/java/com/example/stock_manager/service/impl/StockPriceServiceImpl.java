package com.example.stock_manager.service.impl;

import com.example.stock_manager.service.StockPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class StockPriceServiceImpl implements StockPriceService {

    private final Map<String, Double> priceCache = new HashMap<>();
    private final Random random = new Random();

    public StockPriceServiceImpl() {
        // Inizializza alcuni prezzi mock
        priceCache.put("AAPL", 150.0);
        priceCache.put("GOOGL", 2800.0);
        priceCache.put("MSFT", 350.0);
        priceCache.put("AMZN", 3200.0);
        priceCache.put("TSLA", 800.0);
    }

    @Override
    public double getPrice(String symbol) {
        return priceCache.computeIfAbsent(symbol.toUpperCase(), s -> {
            // Genera un prezzo random per simboli non conosciuti
            double randomPrice = 50.0 + random.nextDouble() * 500.0;
            log.info("Generated random price for {}: {}", symbol, randomPrice);
            return randomPrice;
        });
    }

    public void updatePrice(String symbol, double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        priceCache.put(symbol.toUpperCase(), price);
        log.info("Updated price for {} to {}", symbol, price);
    }
}
