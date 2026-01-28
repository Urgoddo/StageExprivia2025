package com.example.stock_manager.service;

import com.example.stock_manager.dto.StockRequest;
import com.example.stock_manager.exception.DuplicateStockException;
import com.example.stock_manager.exception.InsufficientStockException;
import com.example.stock_manager.exception.StockNotFoundException;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTransactionService {

    private final StockRepository stockRepository;
    private final StockPriceService stockPriceService;

    // --- CRUD OPERATIONS ---

    @Transactional
    public Stock createStock(StockRequest request) {
        String symbol = request.getSymbol().toUpperCase();

        if (stockRepository.existsById(symbol)) {
            throw new DuplicateStockException(symbol);
        }

        Stock stock = Stock.builder()
                .symbol(symbol)
                .quantity(request.getQuantity())
                .build();

        Stock saved = stockRepository.save(stock);
        log.info("Created new stock: {}", symbol);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Stock getStockBySymbol(String symbol) {
        return stockRepository.findById(symbol.toUpperCase())
                .orElseThrow(() -> new StockNotFoundException(symbol));
    }

    @Transactional
    public Stock updateStock(String symbol, StockRequest request) {
        Stock existing = getStockBySymbol(symbol); // Riutilizza la logica di ricerca
        existing.setQuantity(request.getQuantity());
        
        Stock updated = stockRepository.save(existing);
        log.info("Updated quantity for stock: {}", symbol);
        return updated;
    }

    @Transactional
    public void deleteStock(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        if (!stockRepository.existsById(upperSymbol)) {
            throw new StockNotFoundException(symbol);
        }
        stockRepository.deleteById(upperSymbol);
        log.info("Deleted stock: {}", upperSymbol);
    }

    // --- TRANSACTIONS (BUY/SELL/CALCS) ---

    @Transactional
    public Stock buyStock(String symbol, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        String upperSymbol = symbol.toUpperCase();
        double price = stockPriceService.getPrice(upperSymbol);

        Stock stock = stockRepository.findById(upperSymbol)
                .orElse(Stock.builder()
                        .symbol(upperSymbol)
                        .quantity(0)
                        .build());

        stock.setQuantity(stock.getQuantity() + quantity);
        Stock saved = stockRepository.save(stock);

        log.info("Bought {} shares of {} at price {} (total: {})",
                quantity, upperSymbol, price, saved.getQuantity());

        return saved;
    }

    @Transactional
    public Stock sellStock(String symbol, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Stock stock = getStockBySymbol(symbol);

        if (stock.getQuantity() < quantity) {
            throw new InsufficientStockException(stock.getSymbol(), stock.getQuantity(), quantity);
        }

        stock.setQuantity(stock.getQuantity() - quantity);

        if (stock.getQuantity() == 0) {
            stockRepository.delete(stock);
            log.info("Sold all {} shares of {} - stock removed", quantity, stock.getSymbol());
            return null;
        } else {
            Stock saved = stockRepository.save(stock);
            double price = stockPriceService.getPrice(stock.getSymbol());
            log.info("Sold {} shares of {} at price {} (remaining: {})",
                    quantity, stock.getSymbol(), price, saved.getQuantity());
            return saved;
        }
    }

    public double calculateTotalInvestment(String symbol) {
        Stock stock = getStockBySymbol(symbol);
        double currentPrice = stockPriceService.getPrice(stock.getSymbol());
        return currentPrice * stock.getQuantity();
    }

    public List<Stock> getStocksByValue() {
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream()
                .sorted((s1, s2) -> {
                    double value1 = stockPriceService.getPrice(s1.getSymbol()) * s1.getQuantity();
                    double value2 = stockPriceService.getPrice(s2.getSymbol()) * s2.getQuantity();
                    return Double.compare(value2, value1); 
                })
                .toList();
    }
}