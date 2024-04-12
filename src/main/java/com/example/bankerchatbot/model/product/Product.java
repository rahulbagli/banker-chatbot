package com.example.bankerchatbot.model.product;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Product {
    private String product;
    private List<PlanType> planTypeList;
}
