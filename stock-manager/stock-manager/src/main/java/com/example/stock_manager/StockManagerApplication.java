package com.example.stock_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class StockManagerApplication {

	private static volatile ConfigurableApplicationContext lastContext;

	static ConfigurableApplicationContext run(String... args) {
		lastContext = SpringApplication.run(StockManagerApplication.class, args);
		return lastContext;
	}

	static void closeLastContext() {
		ConfigurableApplicationContext ctx = lastContext;
		if (ctx != null) {
			ctx.close();
			lastContext = null;
		}
	}

	public static void main(String[] args) {
		run(args);
	}

}
/**
 * Progetto Stock Manager
 */