package com.example.bankerchatbot.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Component
@Scope("prototype")
public class QueryProductAttributes {

    private Set<String> planNumber = new HashSet<>();
    private Set<String> planName = new HashSet<>();
    private Set<String> productNumber = new HashSet<>();
    private Set<String> productName = new HashSet<>();
    private Set<String> planTypes = new HashSet<>();

    private Set<String> convertPlanNumberForSearch = null;
    private Set<String> convertPlanNameForSearch = null;
    private Set<String> convertProductNumberForSearch = null;
    private Set<String> convertProductNameForSearch = null;
    private Set<String> convertPlanTypesUpperCase = new HashSet<>();

    private ProductAndPlan productAndPlan;
}
