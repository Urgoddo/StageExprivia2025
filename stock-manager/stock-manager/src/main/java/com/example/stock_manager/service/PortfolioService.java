package com.example.stock_manager.service;

import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.model.Stock;
import com.example.stock_manager.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final StockRepository stockRepository; // Nuova dipendenza
    private final StockPriceService stockPriceService;

    public double getTotalValue() {
        List<Stock> stocks = stockRepository.findAll();
        if (stocks.isEmpty()) return 0.0;
        
        return stocks.stream()
                .mapToDouble(s -> stockPriceService.getPrice(s.getSymbol()) * s.getQuantity())
                .sum();
    }

    public double getAveragePricePerShare() {
        List<Stock> stocks = stockRepository.findAll();
        if (stocks.isEmpty()) return 0.0;

        int totalQuantity = stocks.stream()
                .mapToInt(Stock::getQuantity)
                .sum();

        if (totalQuantity == 0) return 0.0;

        double totalValue = getTotalValue(); // Riutilizza il metodo interno
        return totalValue / totalQuantity;
    }

    public PortfolioSummary getPortfolioSummary() {
        List<Stock> stocks = stockRepository.findAll();
        
        if (stocks.isEmpty()) {
            return PortfolioSummary.builder()
                    .totalValue(0.0)
                    .averagePricePerShare(0.0)
                    .totalStocks(0)
                    .totalQuantity(0)
                    .stockDetails(List.of())
                    .build();
        }

        List<PortfolioSummary.StockDetail> details = stocks.stream()
                .map(stock -> {
                    double price = stockPriceService.getPrice(stock.getSymbol());
                    double totalValue = price * stock.getQuantity();
                    return PortfolioSummary.StockDetail.builder()
                            .symbol(stock.getSymbol())
                            .quantity(stock.getQuantity())
                            .currentPrice(price)
                            .totalValue(totalValue)
                            .build();
                })
                .collect(Collectors.toList());

        int totalQuantity = stocks.stream()
                .mapToInt(Stock::getQuantity)
                .sum();

        // Calcoliamo totalValue qui per evitare una seconda query al DB se chiamassimo getTotalValue()
        double totalPortfolioValue = details.stream().mapToDouble(PortfolioSummary.StockDetail::getTotalValue).sum();

        return PortfolioSummary.builder()
                .totalValue(totalPortfolioValue)
                .averagePricePerShare(totalQuantity > 0 ? totalPortfolioValue / totalQuantity : 0)
                .totalStocks(stocks.size())
                .totalQuantity(totalQuantity)
                .stockDetails(details)
                .build();
    }

    public Stock findHighestValueStock() {
        List<Stock> stocks = stockRepository.findAll();
        if (stocks.isEmpty()) return null;

        return stocks.stream()
                .max((s1, s2) -> {
                    double value1 = stockPriceService.getPrice(s1.getSymbol()) * s1.getQuantity();
                    double value2 = stockPriceService.getPrice(s2.getSymbol()) * s2.getQuantity();
                    return Double.compare(value1, value2);
                })
                .orElse(null);
    }
}