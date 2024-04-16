package com.example.bankerchatbot.model;

import com.example.bankerchatbot.model.product.ProductList;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryResponse {
    private int responseCode;
    private String responseText;
    private String queryIntent;
    private ProductList productList;
    private List<String> planNumber;
    private List<String> planName;
    private List<String> productNumber;
    private List<String> productName;
    private List<String> planTypes;
    private List<String> planNameWithNumber;
}
