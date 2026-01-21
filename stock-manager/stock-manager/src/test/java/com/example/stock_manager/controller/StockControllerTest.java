package com.example.stock_manager.controller;

import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.dto.TransactionRequest;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.repository.StockRepository;
import com.example.stock_manager.service.PortfolioService;
import com.example.stock_manager.service.StockTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockRepository repository;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private StockTransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateStock() throws Exception {
        Stock stock = new Stock("AAPL", 50);

        when(repository.existsById("AAPL")).thenReturn(false);
        when(repository.save(any(Stock.class))).thenReturn(stock);

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\": \"AAPL\", \"quantity\": 50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    void testFindAll() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
                Stock.builder().symbol("AAPL").quantity(10).build(),
                Stock.builder().symbol("GOOGL").quantity(5).build()
        ));

        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testFindBySymbol() throws Exception {
        Stock stock = Stock.builder().symbol("AAPL").quantity(10).build();
        when(repository.findById("AAPL")).thenReturn(Optional.of(stock));

        mockMvc.perform(get("/api/stocks/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void testUpdateStock() throws Exception {
        Stock existing = Stock.builder().symbol("AAPL").quantity(10).build();
        Stock updated = Stock.builder().symbol("AAPL").quantity(20).build();

        when(repository.findById("AAPL")).thenReturn(Optional.of(existing));
        when(repository.save(any(Stock.class))).thenReturn(updated);

        mockMvc.perform(put("/api/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\": \"AAPL\", \"quantity\": 20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20));
    }

    @Test
    void testDeleteStock() throws Exception {
        when(repository.existsById("AAPL")).thenReturn(true);
        doNothing().when(repository).deleteById("AAPL");

        mockMvc.perform(delete("/api/stocks/AAPL"))
                .andExpect(status().isNoContent());

        verify(repository).deleteById("AAPL");
    }

    @Test
    void testGetTotalValue() throws Exception {
        when(repository.findAll()).thenReturn(List.of());
        when(portfolioService.getTotalValue(any())).thenReturn(1500.0);

        mockMvc.perform(get("/api/stocks/total-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1500.0));
    }

    @Test
    void testGetAveragePrice() throws Exception {
        when(repository.findAll()).thenReturn(List.of());
        when(portfolioService.getAveragePricePerShare(any())).thenReturn(200.0);

        mockMvc.perform(get("/api/stocks/average-price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(200.0));
    }

    @Test
    void testGetSummary() throws Exception {
        PortfolioSummary summary = PortfolioSummary.builder()
                .totalValue(1000.0)
                .averagePricePerShare(150.0)
                .totalStocks(2)
                .totalQuantity(10)
                .stockDetails(List.of())
                .build();

        when(repository.findAll()).thenReturn(List.of());
        when(portfolioService.getPortfolioSummary(any())).thenReturn(summary);

        mockMvc.perform(get("/api/stocks/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalValue").value(1000.0))
                .andExpect(jsonPath("$.totalStocks").value(2));
    }

    @Test
    void testGetHighestValueStock() throws Exception {
        Stock highest = Stock.builder().symbol("GOOGL").quantity(1).build();

        when(repository.findAll()).thenReturn(List.of());
        when(portfolioService.findHighestValueStock(any())).thenReturn(highest);

        mockMvc.perform(get("/api/stocks/highest-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("GOOGL"));
    }

    @Test
    void testGetHighestValueStock_notFound() throws Exception {
        when(repository.findAll()).thenReturn(List.of());
        when(portfolioService.findHighestValueStock(any())).thenReturn(null);

        mockMvc.perform(get("/api/stocks/highest-value"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testBuyStock() throws Exception {
        Stock stock = Stock.builder().symbol("AAPL").quantity(10).build();
        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        when(transactionService.buyStock("AAPL", 10)).thenReturn(stock);

        mockMvc.perform(post("/api/stocks/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    void testSellStock() throws Exception {
        Stock stock = Stock.builder().symbol("AAPL").quantity(5).build();
        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .quantity(5)
                .build();

        when(transactionService.sellStock("AAPL", 5)).thenReturn(stock);

        mockMvc.perform(post("/api/stocks/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void testSellStock_allShares() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        when(transactionService.sellStock("AAPL", 10)).thenReturn(null);

        mockMvc.perform(post("/api/stocks/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetStocksSortedByValue() throws Exception {
        List<Stock> stocks = List.of(
                Stock.builder().symbol("GOOGL").quantity(1).build(),
                Stock.builder().symbol("AAPL").quantity(10).build()
        );

        when(transactionService.getStocksByValue()).thenReturn(stocks);

        mockMvc.perform(get("/api/stocks/sorted-by-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetTotalInvestment() throws Exception {
        when(transactionService.calculateTotalInvestment("AAPL")).thenReturn(1500.0);

        mockMvc.perform(get("/api/stocks/AAPL/investment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1500.0));
    }
}
