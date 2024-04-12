package com.example.bankerchatbot.model;

import com.example.bankerchatbot.model.product.ProductList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryResponse {
    private int responseCode;
    private String responseText;
    private ProductList productList;
}
