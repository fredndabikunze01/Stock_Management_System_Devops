package com.example.stockmanagement.stock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @GetMapping
    public List<StockItem> listStock() {
        return List.of(
                new StockItem(1L, "Notebook", 120, "Stationery"),
                new StockItem(2L, "Ballpoint pen", 85, "Stationery"),
                new StockItem(3L, "USB flash drive", 24, "Electronics"));
    }
}
