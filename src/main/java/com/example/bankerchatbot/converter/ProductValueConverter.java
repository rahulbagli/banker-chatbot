package com.example.bankerchatbot.converter;

import com.example.bankerchatbot.model.ProductAndPlan;
import com.example.bankerchatbot.model.QueryProductAttributes;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductValueConverter {

    public Set<String> convertPlanNumber(QueryProductAttributes queryProducts, ProductAndPlan productAndPlan) {
        Set<String> matches = new HashSet<>();
        for (String item : queryProducts.getPlanNumber()) {
            for (String otherItem : productAndPlan.getPlanNameWithNumber()) {
                String[] str = otherItem.split("-");
                if (item.equals(str[1])) {
                    matches.add(str[0]+" ("+str[1]+")");
                }
            }
        }
        return matches;
    }

    public Set<String> convertProductNumber(QueryProductAttributes queryProducts, ProductAndPlan productAndPlan) {
        Set<String> matches = new HashSet<>();
        for (String item : queryProducts.getProductNumber()) {
            for (String otherItem : productAndPlan.getProductName()) {
                String[] str = otherItem.split(" ");
                if (item.equals(str[4])) {
                    matches.add(otherItem);
                }
            }
        }
        return matches;
    }
    public Set<String> convertPlanName(QueryProductAttributes queryProducts, ProductAndPlan productAndPlan) {
        Set<String> matches = new HashSet<>();
        for (String item : queryProducts.getPlanName()) {
            for (String otherItem : productAndPlan.getPlanNameWithNumber()) {
                String[] str = otherItem.split("-");
                if (item.replace("_"," ").toUpperCase().equals(str[0])) {
                    matches.add(str[0]+" ("+str[1]+")");
                }
            }
        }
        return matches;

    }

    public Set<String> convertProductName(QueryProductAttributes queryProducts) {
        return queryProducts.getProductName().stream()
                .map(product -> product.replace("-", " - ").replace("_", " "))
                .collect(Collectors.toSet());
    }

    public Set<String> convertPlanType(QueryProductAttributes queryProducts) {
        return queryProducts.getPlanTypes().stream()
                .map(plan -> plan.toUpperCase().replace("_", " "))
                .collect(Collectors.toSet());
    }
}
