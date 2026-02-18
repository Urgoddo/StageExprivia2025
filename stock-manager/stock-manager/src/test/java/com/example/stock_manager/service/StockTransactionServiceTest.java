package com.example.stock_manager.service;

import com.example.stock_manager.dto.CreateStockRequest;
import com.example.stock_manager.dto.UpdateStockRequest;
import com.example.stock_manager.exception.DuplicateStockException;
import com.example.stock_manager.exception.InsufficientStockException;
import com.example.stock_manager.exception.StockNotFoundException;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockTransactionServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockPriceService stockPriceService;

    @InjectMocks
    private StockTransactionService transactionService;

    @Test
    void testCreateStock_uppercasesSymbolAndSaves() {
        CreateStockRequest request = CreateStockRequest.builder()
                .symbol("aapl")
                .quantity(10)
                .build();

        when(stockRepository.existsById("AAPL")).thenReturn(false);
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stock created = transactionService.createStock(request);

        assertNotNull(created);
        assertEquals("AAPL", created.getSymbol());
        assertEquals(10, created.getQuantity());
    }

    @Test
    void testCreateStock_throwsOnDuplicate() {
        CreateStockRequest request = CreateStockRequest.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        when(stockRepository.existsById("AAPL")).thenReturn(true);

        assertThrows(DuplicateStockException.class, () -> transactionService.createStock(request));
    }

    @Test
    void testGetAllStocks_delegatesToRepository() {
        when(stockRepository.findAll()).thenReturn(List.of(Stock.builder().symbol("AAPL").quantity(1).build()));

        List<Stock> stocks = transactionService.getAllStocks();

        assertEquals(1, stocks.size());
        verify(stockRepository).findAll();
    }

    @Test
    void testGetStockBySymbol_uppercasesAndReturns() {
        when(stockRepository.findById("AAPL")).thenReturn(Optional.of(Stock.builder().symbol("AAPL").quantity(1).build()));

        Stock stock = transactionService.getStockBySymbol("aapl");

        assertEquals("AAPL", stock.getSymbol());
    }

    @Test
    void testGetStockBySymbol_throwsWhenMissing() {
        when(stockRepository.findById("AAPL")).thenReturn(Optional.empty());
        assertThrows(StockNotFoundException.class, () -> transactionService.getStockBySymbol("AAPL"));
    }

    @Test
    void testUpdateStock_updatesQuantity() {
        when(stockRepository.findById("AAPL")).thenReturn(Optional.of(Stock.builder().symbol("AAPL").quantity(1).build()));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateStockRequest request = UpdateStockRequest.builder().quantity(99).build();
        Stock updated = transactionService.updateStock("AAPL", request);

        assertEquals(99, updated.getQuantity());
    }

    @Test
    void testDeleteStock_deletesWhenExists() {
        when(stockRepository.existsById("AAPL")).thenReturn(true);

        transactionService.deleteStock("aapl");

        verify(stockRepository).deleteById("AAPL");
    }

    @Test
    void testDeleteStock_throwsWhenMissing() {
        when(stockRepository.existsById("AAPL")).thenReturn(false);
        assertThrows(StockNotFoundException.class, () -> transactionService.deleteStock("AAPL"));
    }

    @Test
    void testBuyStock_newStock() {
        String symbol = "AAPL";
        int quantity = 10;

        when(stockRepository.findById(symbol)).thenReturn(Optional.empty());
        when(stockPriceService.getPrice(symbol)).thenReturn(150.0);
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stock result = transactionService.buyStock(symbol, quantity);

        assertNotNull(result);
        assertEquals(symbol, result.getSymbol());
        assertEquals(quantity, result.getQuantity());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void testBuyStock_existingStock() {
        String symbol = "AAPL";
        int existingQuantity = 5;
        int buyQuantity = 10;

        Stock existing = Stock.builder()
                .symbol(symbol)
                .quantity(existingQuantity)
                .build();

        when(stockRepository.findById(symbol)).thenReturn(Optional.of(existing));
        when(stockPriceService.getPrice(symbol)).thenReturn(150.0);
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stock result = transactionService.buyStock(symbol, buyQuantity);

        assertNotNull(result);
        assertEquals(existingQuantity + buyQuantity, result.getQuantity());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void testBuyStock_invalidQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buyStock("AAPL", 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.buyStock("AAPL", -1);
        });
    }

    @Test
    void testSellStock_success() {
        String symbol = "AAPL";
        int existingQuantity = 20;
        int sellQuantity = 5;

        Stock existing = Stock.builder()
                .symbol(symbol)
                .quantity(existingQuantity)
                .build();

        when(stockRepository.findById(symbol)).thenReturn(Optional.of(existing));
        when(stockPriceService.getPrice(symbol)).thenReturn(150.0);
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stock result = transactionService.sellStock(symbol, sellQuantity);

        assertNotNull(result);
        assertEquals(existingQuantity - sellQuantity, result.getQuantity());
        verify(stockRepository).save(any(Stock.class));
        verify(stockRepository, never()).delete(any(Stock.class));
    }

    @Test
    void testSellStock_allShares() {
        String symbol = "AAPL";
        int quantity = 10;

        Stock existing = Stock.builder()
                .symbol(symbol)
                .quantity(quantity)
                .build();

        when(stockRepository.findById(symbol)).thenReturn(Optional.of(existing));

        Stock result = transactionService.sellStock(symbol, quantity);

        assertNull(result);
        verify(stockRepository).delete(existing);
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    void testSellStock_notFound() {
        when(stockRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () -> {
            transactionService.sellStock("UNKNOWN", 10);
        });
    }

    @Test
    void testSellStock_insufficientStock() {
        String symbol = "AAPL";
        int existingQuantity = 5;
        int sellQuantity = 10;

        Stock existing = Stock.builder()
                .symbol(symbol)
                .quantity(existingQuantity)
                .build();

        when(stockRepository.findById(symbol)).thenReturn(Optional.of(existing));

        assertThrows(InsufficientStockException.class, () -> {
            transactionService.sellStock(symbol, sellQuantity);
        });
    }

    @Test
    void testSellStock_invalidQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.sellStock("AAPL", 0);
        });
    }

    @Test
    void testCalculateTotalInvestment() {
        String symbol = "AAPL";
        int quantity = 10;
        double price = 150.0;

        Stock stock = Stock.builder()
                .symbol(symbol)
                .quantity(quantity)
                .build();

        when(stockRepository.findById(symbol)).thenReturn(Optional.of(stock));
        when(stockPriceService.getPrice(symbol)).thenReturn(price);

        double investment = transactionService.calculateTotalInvestment(symbol);

        assertEquals(quantity * price, investment, 0.01);
    }

    @Test
    void testCalculateTotalInvestment_notFound() {
        when(stockRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () -> {
            transactionService.calculateTotalInvestment("UNKNOWN");
        });
    }

    @Test
    void testGetStocksByValue() {
        Stock stock1 = Stock.builder().symbol("AAPL").quantity(10).build();
        Stock stock2 = Stock.builder().symbol("GOOGL").quantity(2).build();

        when(stockRepository.findAll()).thenReturn(List.of(stock1, stock2));
        when(stockPriceService.getPrice("AAPL")).thenReturn(150.0);  // 10 * 150 = 1500
        when(stockPriceService.getPrice("GOOGL")).thenReturn(2800.0); // 2 * 2800 = 5600

        List<Stock> sorted = transactionService.getStocksByValue();

        assertNotNull(sorted);
        assertEquals(2, sorted.size());
        // GOOGL dovrebbe essere primo perché ha valore più alto
        assertEquals("GOOGL", sorted.get(0).getSymbol());
        assertEquals("AAPL", sorted.get(1).getSymbol());
    }

    @Test
    void testGetStocksByValue_emptyList() {
        when(stockRepository.findAll()).thenReturn(List.of());

        List<Stock> sorted = transactionService.getStocksByValue();

        assertNotNull(sorted);
        assertTrue(sorted.isEmpty());
    }
}
