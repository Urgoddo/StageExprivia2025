package com.example.stock_manager.controller;

import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.dto.CreateStockRequest;
import com.example.stock_manager.dto.StockResponse;
import com.example.stock_manager.dto.StockValueResponse;
import com.example.stock_manager.dto.TransactionRequest;
import com.example.stock_manager.dto.UpdateStockRequest;
import com.example.stock_manager.mapper.StockMapper;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.service.PortfolioService;
import com.example.stock_manager.service.StockPriceService;
import com.example.stock_manager.service.StockTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final PortfolioService portfolioService;
    private final StockTransactionService transactionService;
    private final StockPriceService stockPriceService;

    // --- CRUD DELEGATED TO SERVICE ---

    @PostMapping
    public ResponseEntity<StockResponse> create(@Valid @RequestBody CreateStockRequest request) {
        Stock created = transactionService.createStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(StockMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<StockResponse>> findAll() {
        List<StockResponse> stocks = transactionService.getAllStocks().stream()
                .map(StockMapper::toResponse)
                .toList();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> findBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(StockMapper.toResponse(transactionService.getStockBySymbol(symbol)));
    }

    @PutMapping("/{symbol}")
    public ResponseEntity<StockResponse> update(@PathVariable String symbol,
                                                @Valid @RequestBody UpdateStockRequest request) {
        Stock updated = transactionService.updateStock(symbol, request);
        return ResponseEntity.ok(StockMapper.toResponse(updated));
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> delete(@PathVariable String symbol) {
        transactionService.deleteStock(symbol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-value")
    public ResponseEntity<Double> getTotalValue() {
        return ResponseEntity.ok(portfolioService.getTotalValue());
    }

    @GetMapping("/average-price")
    public ResponseEntity<Double> getAveragePricePerShare() {
        return ResponseEntity.ok(portfolioService.getAveragePricePerShare());
    }

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummary> getSummary() {
        return ResponseEntity.ok(portfolioService.getPortfolioSummary());
    }

    @GetMapping("/highest-value")
    public ResponseEntity<StockValueResponse> getHighestValueStock() {
        Stock highest = portfolioService.findHighestValueStock();
        if (highest == null) {
            return ResponseEntity.notFound().build();
        }
        double price = stockPriceService.getPrice(highest.getSymbol());
        return ResponseEntity.ok(StockMapper.toValueResponse(highest, price));
    }

    // --- TRANSACTIONS ---

    @PostMapping("/buy")
    public ResponseEntity<StockResponse> buyStock(@Valid @RequestBody TransactionRequest request) {
        Stock stock = transactionService.buyStock(request.getSymbol(), request.getQuantity());
        return ResponseEntity.ok(StockMapper.toResponse(stock));
    }

    @PostMapping("/sell")
    public ResponseEntity<StockResponse> sellStock(@Valid @RequestBody TransactionRequest request) {
        Stock stock = transactionService.sellStock(request.getSymbol(), request.getQuantity());
        if (stock == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(StockMapper.toResponse(stock));
    }

    @GetMapping("/sorted-by-value")
    public ResponseEntity<List<StockValueResponse>> getStocksSortedByValue() {
        List<StockValueResponse> stocks = transactionService.getAllStocks().stream()
                .map(stock -> StockMapper.toValueResponse(stock, stockPriceService.getPrice(stock.getSymbol())))
                .sorted(Comparator.comparingDouble(StockValueResponse::getTotalValue).reversed())
                .toList();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{symbol}/investment")
    public ResponseEntity<Double> getTotalInvestment(@PathVariable String symbol) {
        return ResponseEntity.ok(transactionService.calculateTotalInvestment(symbol));
    }
}