package com.example.bankerchatbot.parser;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FilterProductJson {

    public List<Map<String, Object>> filterProductsByName(Map<String, List<Map<String, Object>>> productsMap, Set<String> list) {
        List<Map<String, Object>> products = productsMap.get("productsList");
        List<Map<String, Object>> filteredProducts = new ArrayList<>();
        for (Map<String, Object> product : products) {
            if (list.contains(product.get("product"))) {
                filteredProducts.add(product);
            }
        }
        return filteredProducts;
    }

    public List<Map<String, Object>> filterPlanTypes(List<Map<String, Object>> products, Set<String> list) {
        List<Map<String, Object>> filteredProducts = new ArrayList<>();
        for (Map<String, Object> product : products) {
            List<Map<String, Object>> planTypeList = (List<Map<String, Object>>) product.get("planTypeList");
            List<Map<String, Object>> filteredPlanTypeList = planTypeList.stream()
                    .filter(planType -> list.contains(planType.get("planType"))).toList();
            if (!filteredPlanTypeList.isEmpty()) {
                product.put("planTypeList", filteredPlanTypeList);
                filteredProducts.add(product);
            }
        }
        return filteredProducts;
    }

    public List<Map<String, Object>> filterPlanUses(List<Map<String, Object>> products, Set<String> list) {
        List<Map<String, Object>> filteredProducts = new ArrayList<>();
        for (Map<String, Object> product : products) {
            List<Map<String, Object>> planTypeList = (List<Map<String, Object>>) product.get("planTypeList");
            for (Map<String, Object> planType : planTypeList) {
                List<Map<String, Object>> planUseList = (List<Map<String, Object>>) planType.get("planUseList");
                List<Map<String, Object>> filteredList = planUseList.stream()
                        .filter(planUse -> list.contains(planUse.get("planUse"))).toList();
                if (!filteredList.isEmpty()) {
                    planType.put("planUseList", filteredList);
                }
            }
            filteredProducts.add(product);
        }
        return filteredProducts;
    }
}
