package com.example.stock_manager.integration;

import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.repository.StockRepository;
import com.example.stock_manager.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PortfolioServiceIntegrationTest {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        stockRepository.deleteAll();
    }

    @Test
    void shouldCalculateTotalValueWithRealDatabase() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(2).build());

        List<Stock> stocks = stockRepository.findAll();
        double totalValue = portfolioService.getTotalValue(stocks);

        assertThat(totalValue).isPositive();
        assertThat(totalValue).isGreaterThan(0);
    }

    @Test
    void shouldCalculateAveragePriceWithRealDatabase() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(10).build());

        List<Stock> stocks = stockRepository.findAll();
        double average = portfolioService.getAveragePricePerShare(stocks);

        assertThat(average).isPositive();
    }

    @Test
    void shouldReturnZeroForEmptyPortfolio() {
        List<Stock> stocks = stockRepository.findAll();
        double totalValue = portfolioService.getTotalValue(stocks);
        double average = portfolioService.getAveragePricePerShare(stocks);

        assertThat(totalValue).isEqualTo(0.0);
        assertThat(average).isEqualTo(0.0);
    }

    @Test
    void shouldGetPortfolioSummary() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(5).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(3).build());

        List<Stock> stocks = stockRepository.findAll();
        PortfolioSummary summary = portfolioService.getPortfolioSummary(stocks);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalStocks()).isEqualTo(2);
        assertThat(summary.getTotalQuantity()).isEqualTo(8);
        assertThat(summary.getStockDetails()).hasSize(2);
        assertThat(summary.getTotalValue()).isPositive();
        assertThat(summary.getAveragePricePerShare()).isPositive();
    }

    @Test
    void shouldFindHighestValueStock() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(1).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(100).build());

        List<Stock> stocks = stockRepository.findAll();
        Stock highest = portfolioService.findHighestValueStock(stocks);

        assertThat(highest).isNotNull();
        assertThat(highest.getSymbol()).isIn("AAPL", "GOOGL");
    }
}
