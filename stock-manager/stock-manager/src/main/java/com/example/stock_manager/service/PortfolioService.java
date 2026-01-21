package com.example.stock_manager.service;

import com.example.stock_manager.dto.PortfolioSummary;
import com.example.stock_manager.model.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final StockPriceService stockPriceService;

    /**
     * Valore totale del portafoglio corrente.
     */
    public double getTotalValue(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return 0.0;
        }
        return stocks.stream()
                .mapToDouble(s -> stockPriceService.getPrice(s.getSymbol()) * s.getQuantity())
                .sum();
    }

    /**
     * Calcola il valore medio per azione (prezzo medio ponderato).
     */
    public double getAveragePricePerShare(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return 0.0;
        }
        
        int totalQuantity = stocks.stream()
                .mapToInt(Stock::getQuantity)
                .sum();

        if (totalQuantity == 0) {
            return 0.0;
        }

        double totalValue = getTotalValue(stocks);
        return totalValue / totalQuantity;
    }

    /**
     * Ottiene un riepilogo completo del portafoglio.
     */
    public PortfolioSummary getPortfolioSummary(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
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

        return PortfolioSummary.builder()
                .totalValue(getTotalValue(stocks))
                .averagePricePerShare(getAveragePricePerShare(stocks))
                .totalStocks(stocks.size())
                .totalQuantity(totalQuantity)
                .stockDetails(details)
                .build();
    }

    /**
     * Trova lo stock con il valore più alto nel portafoglio.
     */
    public Stock findHighestValueStock(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return null;
        }

        return stocks.stream()
                .max((s1, s2) -> {
                    double value1 = stockPriceService.getPrice(s1.getSymbol()) * s1.getQuantity();
                    double value2 = stockPriceService.getPrice(s2.getSymbol()) * s2.getQuantity();
                    return Double.compare(value1, value2);
                })
                .orElse(null);
    }
}