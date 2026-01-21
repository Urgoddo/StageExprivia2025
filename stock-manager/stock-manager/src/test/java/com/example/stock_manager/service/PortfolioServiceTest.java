package com.example.stock_manager.service;

import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.model.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private StockPriceService stockPriceService;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void testGetTotalValue_singleStock() {
        // Uso del Builder di Lombok per creare uno Stock
        Stock apple = Stock.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        List<Stock> stocks = List.of(apple);

        when(stockPriceService.getPrice("AAPL")).thenReturn(150.0);

        double total = portfolioService.getTotalValue(stocks);

        assertEquals(1500.0, total);
    }

    @Test
    void testGetTotalValue_multipleStocks() {
        Stock apple = Stock.builder()
                .symbol("AAPL")
                .quantity(5)
                .build();

        Stock google = Stock.builder()
                .symbol("GOOGL")
                .quantity(2)
                .build();

        List<Stock> stocks = List.of(apple, google);

        when(stockPriceService.getPrice("AAPL")).thenReturn(200.0);
        when(stockPriceService.getPrice("GOOGL")).thenReturn(1000.0);

        double total = portfolioService.getTotalValue(stocks);

        // 5 * 200 + 2 * 1000 = 1000 + 2000 = 3000
        assertEquals(3000.0, total);
    }

    @Test
    void testGetAveragePricePerShare() {
        Stock apple = Stock.builder()
                .symbol("AAPL")
                .quantity(5)
                .build();

        Stock google = Stock.builder()
                .symbol("GOOGL")
                .quantity(5)
                .build();

        List<Stock> stocks = List.of(apple, google);

        when(stockPriceService.getPrice("AAPL")).thenReturn(100.0);
        when(stockPriceService.getPrice("GOOGL")).thenReturn(300.0);

        double average = portfolioService.getAveragePricePerShare(stocks);

        // Valore totale = 5*100 + 5*300 = 2000; quantità totale = 10; media = 200
        assertEquals(200.0, average);
    }

    @Test
    void testGetTotalValue_withNullList() {
        double total = portfolioService.getTotalValue(null);
        assertEquals(0.0, total);
    }

    @Test
    void testGetTotalValue_withEmptyList() {
        double total = portfolioService.getTotalValue(List.of());
        assertEquals(0.0, total);
    }

    @Test
    void testGetAveragePricePerShare_withNullList() {
        double average = portfolioService.getAveragePricePerShare(null);
        assertEquals(0.0, average);
    }

    @Test
    void testGetAveragePricePerShare_withEmptyList() {
        double average = portfolioService.getAveragePricePerShare(List.of());
        assertEquals(0.0, average);
    }

    @Test
    void testGetAveragePricePerShare_withZeroQuantity() {
        Stock stock = Stock.builder()
                .symbol("AAPL")
                .quantity(0)
                .build();

        List<Stock> stocks = List.of(stock);
        double average = portfolioService.getAveragePricePerShare(stocks);
        assertEquals(0.0, average);
    }

    @Test
    void testGetPortfolioSummary() {
        Stock apple = Stock.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        Stock google = Stock.builder()
                .symbol("GOOGL")
                .quantity(5)
                .build();

        List<Stock> stocks = List.of(apple, google);

        when(stockPriceService.getPrice("AAPL")).thenReturn(150.0);
        when(stockPriceService.getPrice("GOOGL")).thenReturn(2800.0);

        PortfolioSummary summary = portfolioService.getPortfolioSummary(stocks);

        assertEquals(2, summary.getTotalStocks());
        assertEquals(15, summary.getTotalQuantity());
        assertEquals(2, summary.getStockDetails().size());
        assertEquals(15000.0, summary.getTotalValue(), 0.01);
    }

    @Test
    void testGetPortfolioSummary_withNullList() {
        PortfolioSummary summary = portfolioService.getPortfolioSummary(null);

        assertEquals(0, summary.getTotalStocks());
        assertEquals(0, summary.getTotalQuantity());
        assertEquals(0.0, summary.getTotalValue());
        assertEquals(0.0, summary.getAveragePricePerShare());
        assertEquals(0, summary.getStockDetails().size());
    }

    @Test
    void testGetPortfolioSummary_withEmptyList() {
        PortfolioSummary summary = portfolioService.getPortfolioSummary(List.of());

        assertEquals(0, summary.getTotalStocks());
        assertEquals(0, summary.getTotalQuantity());
        assertEquals(0.0, summary.getTotalValue());
        assertEquals(0.0, summary.getAveragePricePerShare());
        assertEquals(0, summary.getStockDetails().size());
    }

    @Test
    void testFindHighestValueStock() {
        Stock apple = Stock.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        Stock google = Stock.builder()
                .symbol("GOOGL")
                .quantity(1)
                .build();

        List<Stock> stocks = List.of(apple, google);

        when(stockPriceService.getPrice("AAPL")).thenReturn(150.0);  // 10 * 150 = 1500
        when(stockPriceService.getPrice("GOOGL")).thenReturn(2800.0); // 1 * 2800 = 2800

        Stock highest = portfolioService.findHighestValueStock(stocks);

        assertEquals("GOOGL", highest.getSymbol());
    }

    @Test
    void testFindHighestValueStock_withNullList() {
        Stock highest = portfolioService.findHighestValueStock(null);
        assertEquals(null, highest);
    }

    @Test
    void testFindHighestValueStock_withEmptyList() {
        Stock highest = portfolioService.findHighestValueStock(List.of());
        assertEquals(null, highest);
    }

    @Test
    void testFindHighestValueStock_singleStock() {
        Stock apple = Stock.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        when(stockPriceService.getPrice("AAPL")).thenReturn(150.0);

        Stock highest = portfolioService.findHighestValueStock(List.of(apple));

        assertEquals("AAPL", highest.getSymbol());
    }
}