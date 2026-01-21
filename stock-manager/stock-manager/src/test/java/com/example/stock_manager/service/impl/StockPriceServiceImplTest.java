package com.example.stock_manager.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockPriceServiceImplTest {

    @Test
    void testGetPrice_knownSymbol() {
        StockPriceServiceImpl service = new StockPriceServiceImpl();
        
        double aaplPrice = service.getPrice("AAPL");
        assertEquals(150.0, aaplPrice);
        
        double googlPrice = service.getPrice("GOOGL");
        assertEquals(2800.0, googlPrice);
    }

    @Test
    void testGetPrice_unknownSymbol() {
        StockPriceServiceImpl service = new StockPriceServiceImpl();
        
        double price = service.getPrice("UNKNOWN");
        
        // Dovrebbe generare un prezzo random tra 50 e 550
        assertTrue(price >= 50.0);
        assertTrue(price <= 550.0);
    }

    @Test
    void testGetPrice_caseInsensitive() {
        StockPriceServiceImpl service = new StockPriceServiceImpl();
        
        double price1 = service.getPrice("aapl");
        double price2 = service.getPrice("AAPL");
        
        assertEquals(price1, price2);
    }

    @Test
    void testUpdatePrice() {
        StockPriceServiceImpl service = new StockPriceServiceImpl();
        
        service.updatePrice("AAPL", 200.0);
        double price = service.getPrice("AAPL");
        
        assertEquals(200.0, price);
    }

    @Test
    void testUpdatePrice_invalidPrice() {
        StockPriceServiceImpl service = new StockPriceServiceImpl();
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.updatePrice("AAPL", -10.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.updatePrice("AAPL", 0.0);
        });
    }

    @Test
    void testUpdatePrice_newSymbol() {
        StockPriceServiceImpl service = new StockPriceServiceImpl();
        
        service.updatePrice("NEWSTOCK", 500.0);
        double price = service.getPrice("NEWSTOCK");
        
        assertEquals(500.0, price);
    }
}
