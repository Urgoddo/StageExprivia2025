package com.example.stock_manager.controller;

import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.dto.StockRequest;
import com.example.stock_manager.dto.TransactionRequest;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.service.PortfolioService;
import com.example.stock_manager.service.StockTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final PortfolioService portfolioService;
    private final StockTransactionService transactionService;

    // --- CRUD DELEGATED TO SERVICE ---

    @PostMapping
    public ResponseEntity<Stock> create(@Valid @RequestBody StockRequest request) {
        Stock created = transactionService.createStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Stock>> findAll() {
        return ResponseEntity.ok(transactionService.getAllStocks());
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Stock> findBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(transactionService.getStockBySymbol(symbol));
    }

    @PutMapping("/{symbol}")
    public ResponseEntity<Stock> update(@PathVariable String symbol, 
                                        @Valid @RequestBody StockRequest request) {
        Stock updated = transactionService.updateStock(symbol, request);
        return ResponseEntity.ok(updated);
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
    public ResponseEntity<Stock> getHighestValueStock() {
        Stock highest = portfolioService.findHighestValueStock();
        if (highest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(highest);
    }

    // --- TRANSACTIONS ---

    @PostMapping("/buy")
    public ResponseEntity<Stock> buyStock(@Valid @RequestBody TransactionRequest request) {
        Stock stock = transactionService.buyStock(request.getSymbol(), request.getQuantity());
        return ResponseEntity.ok(stock);
    }

    @PostMapping("/sell")
    public ResponseEntity<Stock> sellStock(@Valid @RequestBody TransactionRequest request) {
        Stock stock = transactionService.sellStock(request.getSymbol(), request.getQuantity());
        if (stock == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/sorted-by-value")
    public ResponseEntity<List<Stock>> getStocksSortedByValue() {
        return ResponseEntity.ok(transactionService.getStocksByValue());
    }

    @GetMapping("/{symbol}/investment")
    public ResponseEntity<Double> getTotalInvestment(@PathVariable String symbol) {
        return ResponseEntity.ok(transactionService.calculateTotalInvestment(symbol));
    }
}