package com.example.stock_manager.controller;

import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.dto.StockRequest;
import com.example.stock_manager.dto.TransactionRequest;
import com.example.stock_manager.exception.DuplicateStockException;
import com.example.stock_manager.exception.StockNotFoundException;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.repository.StockRepository;
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

    private final StockRepository stockRepository;
    private final PortfolioService portfolioService;
    private final StockTransactionService transactionService;

    @PostMapping
    public ResponseEntity<Stock> create(@Valid @RequestBody StockRequest request) {
        String symbol = request.getSymbol().toUpperCase();
        
        if (stockRepository.existsById(symbol)) {
            throw new DuplicateStockException(symbol);
        }

        Stock stock = Stock.builder()
                .symbol(symbol)
                .quantity(request.getQuantity())
                .build();

        Stock saved = stockRepository.save(stock);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Stock>> findAll() {
        List<Stock> all = stockRepository.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Stock> findBySymbol(@PathVariable String symbol) {
        Stock stock = stockRepository.findById(symbol.toUpperCase())
                .orElseThrow(() -> new StockNotFoundException(symbol));
        return ResponseEntity.ok(stock);
    }

    @PutMapping("/{symbol}")
    public ResponseEntity<Stock> update(@PathVariable String symbol, 
                                        @Valid @RequestBody StockRequest request) {
        Stock existing = stockRepository.findById(symbol.toUpperCase())
                .orElseThrow(() -> new StockNotFoundException(symbol));

        existing.setQuantity(request.getQuantity());
        Stock updated = stockRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> delete(@PathVariable String symbol) {
        if (!stockRepository.existsById(symbol.toUpperCase())) {
            throw new StockNotFoundException(symbol);
        }
        stockRepository.deleteById(symbol.toUpperCase());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-value")
    public ResponseEntity<Double> getTotalValue() {
        List<Stock> stocks = stockRepository.findAll();
        double total = portfolioService.getTotalValue(stocks);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/average-price")
    public ResponseEntity<Double> getAveragePricePerShare() {
        List<Stock> stocks = stockRepository.findAll();
        double avg = portfolioService.getAveragePricePerShare(stocks);
        return ResponseEntity.ok(avg);
    }

    @GetMapping("/summary")
    public ResponseEntity<PortfolioSummary> getSummary() {
        List<Stock> stocks = stockRepository.findAll();
        PortfolioSummary summary = portfolioService.getPortfolioSummary(stocks);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/highest-value")
    public ResponseEntity<Stock> getHighestValueStock() {
        List<Stock> stocks = stockRepository.findAll();
        Stock highest = portfolioService.findHighestValueStock(stocks);
        if (highest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(highest);
    }

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
        List<Stock> stocks = transactionService.getStocksByValue();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{symbol}/investment")
    public ResponseEntity<Double> getTotalInvestment(@PathVariable String symbol) {
        double investment = transactionService.calculateTotalInvestment(symbol);
        return ResponseEntity.ok(investment);
    }
}