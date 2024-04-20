package com.example.bankerchatbot.converter;

import com.example.bankerchatbot.model.ProductAndPlan;
import com.example.bankerchatbot.model.QueryProductAttributes;
import com.example.bankerchatbot.model.product.PlanType;
import com.example.bankerchatbot.model.product.PlanUse;
import com.example.bankerchatbot.model.product.Product;
import com.example.bankerchatbot.model.product.ProductList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductValueConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private ObjectMapper objectMapper;

    public Set<String> convertPlanNumber(QueryProductAttributes queryProducts, ProductAndPlan productAndPlan) {
        Set<String> matches = new HashSet<>();
        for (String item : queryProducts.getPlanNumber()) {
            for (String otherItem : productAndPlan.getPlanNameWithNumber()) {
                String[] str = otherItem.split("-");
                if (item.equals(str[1])) {
                    matches.add(str[0] + " (" + str[1] + ")");
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
                if (item.replace("_", " ").toUpperCase().equals(str[0])) {
                    matches.add(str[0] + " (" + str[1] + ")");
                }
            }
        }
        return matches;

    }

    public Set<String> convertProductName(QueryProductAttributes queryProducts) {
        return queryProducts.getProductName().stream()
                .map(product -> product.replace(" - ", "-").replace("-", " - ").replace("_", " "))
                .collect(Collectors.toSet());
    }

    public Set<String> convertPlanType(QueryProductAttributes queryProducts) {
        return queryProducts.getPlanTypes().stream()
                .map(plan -> plan.toUpperCase().replace("_", " "))
                .collect(Collectors.toSet());
    }

    public void checkAndValidateAllQueryFields(QueryProductAttributes queryProducts, ProductAndPlan productAndPlan, List<String> productList) {
        if (!queryProducts.getPlanNumber().isEmpty()) {
            boolean containsAll = productAndPlan.getPlanNumber().containsAll(queryProducts.getPlanNumber());
            if (containsAll) {
                checkPlanNumber(queryProducts, productList);
            } else {
                Set<String> notMatchedPlanNumber = queryProducts.getPlanNumber().stream()
                        .filter(plan -> !productAndPlan.getPlanNumber().contains(plan))
                        .collect(Collectors.toSet());
                Set<String> matchedProductNumber = notMatchedPlanNumber.stream()
                        .filter(plan -> productAndPlan.getProductNumber().contains(plan))
                        .collect(Collectors.toSet());
                queryProducts.getPlanNumber().removeAll(notMatchedPlanNumber);
                queryProducts.getProductNumber().addAll(matchedProductNumber);
                checkPlanNumber(queryProducts, productList);
            }
        }

        if (!queryProducts.getProductNumber().isEmpty()) {
            boolean containsAll = productAndPlan.getProductNumber().containsAll(queryProducts.getProductNumber());
            if (!containsAll) {
                Set<String> notMatchedProductNumber = queryProducts.getProductNumber().stream()
                        .filter(product -> !productAndPlan.getProductNumber().contains(product))
                        .collect(Collectors.toSet());
                Set<String> matchedPlanNumber = notMatchedProductNumber.stream()
                        .filter(plan -> productAndPlan.getPlanNumber().contains(plan))
                        .collect(Collectors.toSet());
                queryProducts.getProductNumber().removeAll(notMatchedProductNumber);
                queryProducts.getPlanNumber().addAll(matchedPlanNumber);
                checkPlanNumber(queryProducts, productList);
            }
        }
        if (!queryProducts.getPlanTypes().isEmpty()) {
            boolean containsAll = productAndPlan.getPlanTypes().containsAll(queryProducts.getPlanTypes());
            if (!containsAll) {
                Set<String> notMatchedPlanTypes = queryProducts.getPlanTypes().stream()
                        .filter(planType -> !productAndPlan.getPlanTypes().contains(planType))
                        .collect(Collectors.toSet());
                Set<String> matchedPlanName = notMatchedPlanTypes.stream()
                        .filter(plan -> productAndPlan.getPlanName().contains(plan))
                        .collect(Collectors.toSet());
                Set<String> matchedProductName = notMatchedPlanTypes.stream()
                        .filter(product -> productAndPlan.getProductName().contains(product))
                        .collect(Collectors.toSet());
                queryProducts.getPlanTypes().removeAll(notMatchedPlanTypes);
                queryProducts.getPlanName().addAll(matchedPlanName);
                queryProducts.getProductName().addAll(matchedProductName);
            }
        }

        if (!queryProducts.getPlanName().isEmpty()) {
            boolean containsAll = productAndPlan.getPlanName().containsAll(queryProducts.getPlanName());
            if (!containsAll) {
                Set<String> notMatchedPlanName = queryProducts.getPlanTypes().stream()
                        .filter(planType -> !productAndPlan.getPlanTypes().contains(planType))
                        .collect(Collectors.toSet());
                Set<String> matchedPlanTypes = notMatchedPlanName.stream()
                        .filter(plan -> productAndPlan.getPlanTypes().contains(plan))
                        .collect(Collectors.toSet());
                Set<String> matchedProductName = notMatchedPlanName.stream()
                        .filter(product -> productAndPlan.getProductName().contains(product))
                        .collect(Collectors.toSet());
                queryProducts.getPlanName().removeAll(notMatchedPlanName);
                queryProducts.getPlanTypes().addAll(matchedPlanTypes);
                queryProducts.getProductName().addAll(matchedProductName);
            }
        }

        if (!queryProducts.getProductName().isEmpty()) {
            boolean containsAll = productAndPlan.getProductName().containsAll(queryProducts.getProductName());
            if (!containsAll) {
                Set<String> notMatchedProductName = queryProducts.getPlanTypes().stream()
                        .filter(planType -> !productAndPlan.getPlanTypes().contains(planType))
                        .collect(Collectors.toSet());
                Set<String> matchedPlanName = notMatchedProductName.stream()
                        .filter(plan -> productAndPlan.getPlanName().contains(plan))
                        .collect(Collectors.toSet());
                Set<String> matchedPlanTypes = notMatchedProductName.stream()
                        .filter(plan -> productAndPlan.getPlanTypes().contains(plan))
                        .collect(Collectors.toSet());
                queryProducts.getProductName().removeAll(notMatchedProductName);
                queryProducts.getPlanName().addAll(matchedPlanName);
                queryProducts.getPlanTypes().addAll(matchedPlanTypes);
            }
        }
    }

    private void checkPlanNumber(QueryProductAttributes queryProducts, List<String> productList) {
        for (String product : productList) {
            String[] productArray = product.split("-#");
            String productNumber = productArray[0];
            LOGGER.debug("ProductNumber: " + productNumber);
            String[] planTypeArray = productArray[1].split("#");
            for (int i = 0; i <= planTypeArray.length - 1; i++) {
                String[] planNumber = planTypeArray[i].split("\\|");
                LOGGER.debug("PlanType: " + planNumber[0]);
                for (int j = 1; j <= planNumber.length - 1; j++) {
                    LOGGER.debug("Plan Number: " + planNumber[j]);
                    if (queryProducts.getPlanNumber().contains(planNumber[j])) {
                        queryProducts.getPlanTypes().add(planNumber[0]);
                    }
                }
            }
        }
    }

    public ProductList convertJsonToProduct() {
        try {
            Resource resource = new ClassPathResource("products.json");
            File file = resource.getFile();
            return objectMapper.readValue(file, new TypeReference<ProductList>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> generateAllProductList(ProductList productListFromJson) {
        StringBuilder stringBuilder = null;
        List<String> productList = new ArrayList<>();
        for (Product product : productListFromJson.getProductsList()) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(product.getProduct().split(" ")[4]);
            stringBuilder.append("-");
            for (PlanType planType : product.getPlanTypeList()) {
                stringBuilder.append("#");
                stringBuilder.append(planType.getPlanType());
                for (PlanUse planUse : planType.getPlanUseList()) {
                    stringBuilder.append("|");
                    stringBuilder.append(planUse.getPlanUse().split(" \\(")[1].replace(")", ""));

                }
            }
            productList.add(stringBuilder.toString());
        }
        try {
            LOGGER.info("productList " + objectMapper.writeValueAsString(productList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return productList;
    }

    public void checkUnMatchedValues(QueryProductAttributes queryProducts, ProductAndPlan productAndPlan) {
        Set<String> notMatchedPlanNumber = null;
        Set<String> notMatchedProductNumber = null;
        if (!queryProducts.getPlanNumber().isEmpty()) {
            notMatchedPlanNumber = queryProducts.getPlanNumber().stream()
                    .filter(plan -> !productAndPlan.getPlanNumber().contains(plan))
                    .collect(Collectors.toSet());
        }
        if (!queryProducts.getProductNumber().isEmpty()) {
            notMatchedProductNumber = queryProducts.getProductNumber().stream()
                    .filter(plan -> !productAndPlan.getProductNumber().contains(plan))
                    .collect(Collectors.toSet());
        }
        if (null != notMatchedPlanNumber) {
            Set<String> notMatchedNumber = notMatchedPlanNumber.stream()
                    .filter(product -> !productAndPlan.getProductNumber().contains(product))
                    .collect(Collectors.toSet());
            queryProducts.setUnMatchedNumber(notMatchedNumber);
        }

        if (null != notMatchedProductNumber) {
            Set<String> notMatchedNumber = notMatchedProductNumber.stream()
                    .filter(product -> !productAndPlan.getPlanNumber().contains(product))
                    .collect(Collectors.toSet());
            if (queryProducts.getUnMatchedNumber() != null) {
                queryProducts.getUnMatchedNumber().addAll(notMatchedNumber);
            } else {
                queryProducts.setUnMatchedNumber(notMatchedNumber);
            }
        }

        if (!queryProducts.getProductName().isEmpty()) {
            Set<String> notMatchedProductName = queryProducts.getProductName().stream()
                    .filter(product -> !productAndPlan.getProductName().contains(product.replace("-", " - ").replace("_", " ")))
                    .collect(Collectors.toSet());
            queryProducts.setUnMatchedProductName(notMatchedProductName);
        }
    }
}
