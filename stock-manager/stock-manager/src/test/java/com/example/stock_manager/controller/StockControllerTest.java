package com.example.stock_manager.controller;

import com.example.stock_manager.dto.CreateStockRequest;
import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.dto.StockResponse;
import com.example.stock_manager.dto.StockValueResponse;
import com.example.stock_manager.dto.TransactionRequest;
import com.example.stock_manager.dto.UpdateStockRequest;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.service.PortfolioService;
import com.example.stock_manager.service.StockPriceService;
import com.example.stock_manager.service.StockTransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private StockTransactionService transactionService;

    @Mock
    private StockPriceService stockPriceService;

    @InjectMocks
    private StockController controller;

    @Test
    void create_returnsCreatedStockResponse() {
        CreateStockRequest request = CreateStockRequest.builder().symbol("aapl").quantity(50).build();
        Stock created = Stock.builder().symbol("AAPL").quantity(50).build();
        when(transactionService.createStock(any(CreateStockRequest.class))).thenReturn(created);

        ResponseEntity<StockResponse> response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AAPL", response.getBody().getSymbol());
        assertEquals(50, response.getBody().getQuantity());
    }

    @Test
    void findAll_mapsEntitiesToResponses() {
        when(transactionService.getAllStocks()).thenReturn(List.of(
                Stock.builder().symbol("AAPL").quantity(10).build(),
                Stock.builder().symbol("GOOGL").quantity(5).build()
        ));

        ResponseEntity<List<StockResponse>> response = controller.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("AAPL", response.getBody().get(0).getSymbol());
    }

    @Test
    void update_usesPathSymbolAndQuantityOnly() {
        UpdateStockRequest request = UpdateStockRequest.builder().quantity(20).build();
        Stock updated = Stock.builder().symbol("AAPL").quantity(20).build();
        when(transactionService.updateStock(eq("AAPL"), any(UpdateStockRequest.class))).thenReturn(updated);

        ResponseEntity<StockResponse> response = controller.update("AAPL", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AAPL", response.getBody().getSymbol());
        assertEquals(20, response.getBody().getQuantity());
    }

    @Test
    void findBySymbol_mapsEntityToResponse() {
        Stock stock = Stock.builder().symbol("MSFT").quantity(3).build();
        when(transactionService.getStockBySymbol("MSFT")).thenReturn(stock);

        ResponseEntity<StockResponse> response = controller.findBySymbol("MSFT");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MSFT", response.getBody().getSymbol());
        assertEquals(3, response.getBody().getQuantity());
    }

    @Test
    void delete_returnsNoContent() {
        doNothing().when(transactionService).deleteStock("AAPL");

        ResponseEntity<Void> response = controller.delete("AAPL");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(transactionService).deleteStock("AAPL");
    }

    @Test
    void getTotalValue_delegatesToPortfolioService() {
        when(portfolioService.getTotalValue()).thenReturn(123.45);

        ResponseEntity<Double> response = controller.getTotalValue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(123.45, response.getBody());
    }

    @Test
    void getAveragePricePerShare_delegatesToPortfolioService() {
        when(portfolioService.getAveragePricePerShare()).thenReturn(10.0);

        ResponseEntity<Double> response = controller.getAveragePricePerShare();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10.0, response.getBody());
    }

    @Test
    void getSummary_delegatesToPortfolioService() {
        PortfolioSummary summary = PortfolioSummary.builder()
                .totalValue(1.0)
                .averagePricePerShare(2.0)
                .totalStocks(1)
                .totalQuantity(3)
                .stockDetails(List.of())
                .build();
        when(portfolioService.getPortfolioSummary()).thenReturn(summary);

        ResponseEntity<PortfolioSummary> response = controller.getSummary();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalStocks());
    }

    @Test
    void buyStock_returnsMappedResponse() {
        TransactionRequest request = TransactionRequest.builder().symbol("AAPL").quantity(10).build();
        when(transactionService.buyStock("AAPL", 10)).thenReturn(Stock.builder().symbol("AAPL").quantity(10).build());

        ResponseEntity<StockResponse> response = controller.buyStock(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AAPL", response.getBody().getSymbol());
        assertEquals(10, response.getBody().getQuantity());
    }

    @Test
    void sell_returnsMappedResponseWhenNotAllSharesSold() {
        TransactionRequest request = TransactionRequest.builder().symbol("AAPL").quantity(5).build();
        when(transactionService.sellStock("AAPL", 5)).thenReturn(Stock.builder().symbol("AAPL").quantity(5).build());

        ResponseEntity<StockResponse> response = controller.sellStock(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getQuantity());
    }

    @Test
    void getHighestValueStock_returnsValueDto() {
        Stock highest = Stock.builder().symbol("GOOGL").quantity(2).build();
        when(portfolioService.findHighestValueStock()).thenReturn(highest);
        when(stockPriceService.getPrice("GOOGL")).thenReturn(100.0);

        ResponseEntity<StockValueResponse> response = controller.getHighestValueStock();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("GOOGL", response.getBody().getSymbol());
        assertEquals(2, response.getBody().getQuantity());
        assertEquals(100.0, response.getBody().getCurrentPrice(), 0.0001);
        assertEquals(200.0, response.getBody().getTotalValue(), 0.0001);
    }

    @Test
    void getHighestValueStock_returns404WhenNull() {
        when(portfolioService.findHighestValueStock()).thenReturn(null);

        ResponseEntity<StockValueResponse> response = controller.getHighestValueStock();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void sell_returnsNoContentWhenServiceReturnsNull() {
        TransactionRequest request = TransactionRequest.builder().symbol("AAPL").quantity(10).build();
        when(transactionService.sellStock("AAPL", 10)).thenReturn(null);

        ResponseEntity<StockResponse> response = controller.sellStock(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getStocksSortedByValue_sortsUsingComputedTotalValue() {
        when(transactionService.getAllStocks()).thenReturn(List.of(
                Stock.builder().symbol("AAPL").quantity(10).build(),   // price 10 -> total 100
                Stock.builder().symbol("GOOGL").quantity(1).build()   // price 1000 -> total 1000
        ));
        when(stockPriceService.getPrice("AAPL")).thenReturn(10.0);
        when(stockPriceService.getPrice("GOOGL")).thenReturn(1000.0);

        ResponseEntity<List<StockValueResponse>> response = controller.getStocksSortedByValue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("GOOGL", response.getBody().get(0).getSymbol());
        assertEquals("AAPL", response.getBody().get(1).getSymbol());
    }

    @Test
    void getTotalInvestment_delegatesToTransactionService() {
        when(transactionService.calculateTotalInvestment("AAPL")).thenReturn(999.0);

        ResponseEntity<Double> response = controller.getTotalInvestment("AAPL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(999.0, response.getBody());
    }
}

