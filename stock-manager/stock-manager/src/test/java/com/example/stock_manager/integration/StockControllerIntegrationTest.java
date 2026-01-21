package com.example.stock_manager.integration;

import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.dto.StockRequest;
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
class StockControllerIntegrationTest {

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
    void shouldCreateStock() throws Exception {
        StockRequest request = StockRequest.builder()
                .symbol("AAPL")
                .quantity(10)
                .build();

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(10));

        assertThat(stockRepository.count()).isEqualTo(1);
        assertThat(stockRepository.findById("AAPL")).isPresent();
    }

    @Test
    void shouldNotCreateDuplicateStock() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());

        StockRequest request = StockRequest.builder()
                .symbol("AAPL")
                .quantity(20)
                .build();

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Stock with symbol 'AAPL' already exists"));
    }

    @Test
    void shouldValidateStockRequest() throws Exception {
        StockRequest invalidRequest = StockRequest.builder()
                .symbol("")
                .quantity(-1)
                .build();

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void shouldGetAllStocks() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(5).build());

        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].symbol").exists())
                .andExpect(jsonPath("$[1].symbol").exists());
    }

    @Test
    void shouldGetStockBySymbol() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());

        mockMvc.perform(get("/api/stocks/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void shouldReturnNotFoundForNonExistentStock() throws Exception {
        mockMvc.perform(get("/api/stocks/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Stock with symbol 'UNKNOWN' not found"));
    }

    @Test
    void shouldUpdateStock() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());

        StockRequest updateRequest = StockRequest.builder()
                .symbol("AAPL")
                .quantity(20)
                .build();

        mockMvc.perform(put("/api/stocks/AAPL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.quantity").value(20));

        Stock updated = stockRepository.findById("AAPL").orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(20);
    }

    @Test
    void shouldDeleteStock() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());

        mockMvc.perform(delete("/api/stocks/AAPL"))
                .andExpect(status().isNoContent());

        assertThat(stockRepository.findById("AAPL")).isEmpty();
    }

    @Test
    void shouldCalculateTotalValue() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(2).build());

        mockMvc.perform(get("/api/stocks/total-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void shouldCalculateAveragePrice() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(5).build());

        mockMvc.perform(get("/api/stocks/average-price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void shouldGetPortfolioSummary() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(5).build());

        mockMvc.perform(get("/api/stocks/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalValue").exists())
                .andExpect(jsonPath("$.averagePricePerShare").exists())
                .andExpect(jsonPath("$.totalStocks").value(2))
                .andExpect(jsonPath("$.totalQuantity").value(15))
                .andExpect(jsonPath("$.stockDetails.length()").value(2));
    }

    @Test
    void shouldGetHighestValueStock() throws Exception {
        stockRepository.save(Stock.builder().symbol("AAPL").quantity(10).build());
        stockRepository.save(Stock.builder().symbol("GOOGL").quantity(2).build());

        mockMvc.perform(get("/api/stocks/highest-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").exists())
                .andExpect(jsonPath("$.quantity").exists());
    }
}
