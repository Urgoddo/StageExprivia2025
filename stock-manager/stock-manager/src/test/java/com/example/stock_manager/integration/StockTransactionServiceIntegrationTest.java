package com.example.stock_manager.integration;

import com.example.stock_manager.exception.InsufficientStockException;
import com.example.stock_manager.exception.StockNotFoundException;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.repository.StockRepository;
import com.example.stock_manager.service.StockTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockTransactionServiceIntegrationTest {

    @Autowired
    private StockTransactionService transactionService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        stockRepository.deleteAll();
    }

    @Test
    void shouldBuyStockAndCreateNewEntry() {
        Stock result = transactionService.buyStock("AAPL", 10);

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        assertEquals(10, result.getQuantity());
        assertTrue(stockRepository.existsById("AAPL"));
    }

    @Test
    void shouldBuyStockAndUpdateExisting() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(5).build());

        Stock result = transactionService.buyStock("AAPL", 10);

        assertNotNull(result);
        assertEquals(15, result.getQuantity());
        
        Stock saved = stockRepository.findById("AAPL").orElseThrow();
        assertEquals(15, saved.getQuantity());
    }

    @Test
    void shouldSellStockAndUpdateQuantity() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(20).build());

        Stock result = transactionService.sellStock("AAPL", 5);

        assertNotNull(result);
        assertEquals(15, result.getQuantity());
        
        Stock saved = stockRepository.findById("AAPL").orElseThrow();
        assertEquals(15, saved.getQuantity());
    }

    @Test
    void shouldSellAllStockAndRemoveEntry() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());

        Stock result = transactionService.sellStock("AAPL", 10);

        assertNull(result);
        assertFalse(stockRepository.existsById("AAPL"));
    }

    @Test
    void shouldThrowExceptionWhenSellingNonExistentStock() {
        assertThrows(StockNotFoundException.class, () -> {
            transactionService.sellStock("UNKNOWN", 10);
        });
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(5).build());

        assertThrows(InsufficientStockException.class, () -> {
            transactionService.sellStock("AAPL", 10);
        });
    }

    @Test
    void shouldCalculateTotalInvestment() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());

        double investment = transactionService.calculateTotalInvestment("AAPL");

        assertThat(investment).isPositive();
    }

    @Test
    void shouldThrowExceptionWhenCalculatingInvestmentForNonExistentStock() {
        assertThrows(StockNotFoundException.class, () -> {
            transactionService.calculateTotalInvestment("UNKNOWN");
        });
    }

    @Test
    void shouldGetStocksSortedByValue() {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(1).build());

        List<Stock> sorted = transactionService.getStocksByValue();

        assertThat(sorted).hasSize(2);
        assertThat(sorted.get(0).getSymbol()).isNotNull();
        assertThat(sorted.get(1).getSymbol()).isNotNull();
    }

    @Test
    void shouldHandleInvalidQuantityInBuy() {
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buyStock("AAPL", 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buyStock("AAPL", -1);
        });
    }

    @Test
    void shouldHandleInvalidQuantityInSell() {
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.sellStock("AAPL", 0);
        });
    }
}
