package com.example.stock_manager.repository;

import com.example.stock_manager.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, String> {
}