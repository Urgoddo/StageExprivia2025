package com.example.stock_manager.integration;

import com.example.stock_manager.dto.TransactionRequest;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.repository.StockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StockControllerAdvancedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        stockRepository.deleteAll();
    }

    @Test
    void shouldBuyStock() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        mockMvc.perform(post("/api/stocks/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(10));

        assertThat(stockRepository.existsById("AAPL")).isTrue();
    }

    @Test
    void shouldBuyStockAndUpdateExisting() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(5).build());

        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        mockMvc.perform(post("/api/stocks/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));

        Stock saved = stockRepository.findById("AAPL").orElseThrow();
        assertThat(saved.getQuantity()).isEqualTo(15);
    }

    @Test
    void shouldSellStock() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(20).build());

        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .quantity(5)
                .build();

        mockMvc.perform(post("/api/stocks/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));

        Stock saved = stockRepository.findById("AAPL").orElseThrow();
        assertThat(saved.getQuantity()).isEqualTo(15);
    }

    @Test
    void shouldSellAllStock() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());

        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        mockMvc.perform(post("/api/stocks/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        assertThat(stockRepository.existsById("AAPL")).isFalse();
    }

    @Test
    void shouldReturnErrorWhenInsufficientStock() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(5).build());

        TransactionRequest request = TransactionRequest.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        mockMvc.perform(post("/api/stocks/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldGetStocksSortedByValue() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(2).build());

        mockMvc.perform(get("/api/stocks/sorted-by-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].symbol").exists());
    }

    @Test
    void shouldGetTotalInvestment() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());

        mockMvc.perform(get("/api/stocks/AAPL/investment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void shouldReturnNotFoundForInvestmentOfNonExistentStock() throws Exception {
        mockMvc.perform(get("/api/stocks/UNKNOWN/investment"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateTransactionRequest() throws Exception {
        TransactionRequest invalidRequest = TransactionRequest.builder()
                .symbol("")
                .quantity(-1)
                .build();

        mockMvc.perform(post("/api/stocks/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}
