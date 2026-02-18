package com.example.stock_manager;

import org.junit.jupiter.api.Test;

class StockManagerApplicationMainTest {

    @Test
    void main_startsApplicationAndCanBeClosed() {
        StockManagerApplication.main(new String[] {
                "--spring.main.web-application-type=none",
                "--spring.main.banner-mode=off"
        });
        StockManagerApplication.closeLastContext();
    }
}

