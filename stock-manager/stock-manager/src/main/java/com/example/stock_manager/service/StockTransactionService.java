package com.example.stock_manager.service;

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

    /**
     * Acquista stock aggiungendo quantità a uno stock esistente o creando un nuovo stock.
     */
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

    /**
     * Vende stock riducendo la quantità. Se la quantità diventa zero, lo stock viene eliminato.
     */
    @Transactional
    public Stock sellStock(String symbol, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        String upperSymbol = symbol.toUpperCase();
        Stock stock = stockRepository.findById(upperSymbol)
                .orElseThrow(() -> new StockNotFoundException(upperSymbol));

        if (stock.getQuantity() < quantity) {
            throw new InsufficientStockException(upperSymbol, stock.getQuantity(), quantity);
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        
        if (stock.getQuantity() == 0) {
            stockRepository.delete(stock);
            log.info("Sold all {} shares of {} - stock removed", quantity, upperSymbol);
            return null;
        } else {
            Stock saved = stockRepository.save(stock);
            double price = stockPriceService.getPrice(upperSymbol);
            log.info("Sold {} shares of {} at price {} (remaining: {})", 
                    quantity, upperSymbol, price, saved.getQuantity());
            return saved;
        }
    }

    /**
     * Calcola il valore totale di tutte le transazioni per un simbolo.
     */
    public double calculateTotalInvestment(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        Stock stock = stockRepository.findById(upperSymbol)
                .orElseThrow(() -> new StockNotFoundException(upperSymbol));

        double currentPrice = stockPriceService.getPrice(upperSymbol);
        return currentPrice * stock.getQuantity();
    }

    /**
     * Restituisce tutti gli stock ordinati per valore totale decrescente.
     */
    public List<Stock> getStocksByValue() {
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream()
                .sorted((s1, s2) -> {
                    double value1 = stockPriceService.getPrice(s1.getSymbol()) * s1.getQuantity();
                    double value2 = stockPriceService.getPrice(s2.getSymbol()) * s2.getQuantity();
                    return Double.compare(value2, value1); // Decrescente
                })
                .toList();
    }
}
