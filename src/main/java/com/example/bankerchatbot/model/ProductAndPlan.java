package com.example.bankerchatbot.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@Scope("prototype")
public class ProductAndPlan {
    private List<String> planNumber = new ArrayList<>();
    private List<String> planName = new ArrayList<>();
    private List<String> productNumber = new ArrayList<>();
    private List<String> productName = new ArrayList<>();
    private List<String> planTypes = new ArrayList<>();
    private List<String> planNameWithNumber = new ArrayList<>();
}
