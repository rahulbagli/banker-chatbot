package com.example.bankerchatbot.service;

import com.example.bankerchatbot.converter.ProductValueConverter;
import com.example.bankerchatbot.model.ProductAndPlan;
import com.example.bankerchatbot.model.QueryProductAttributes;
import com.example.bankerchatbot.model.QueryResponse;
import com.example.bankerchatbot.model.product.ProductList;
import com.example.bankerchatbot.parser.FilterProductJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

@Component
public class GenerateResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ProductValueConverter productValueConverter;
    @Autowired
    FilterProductJson filterProductJson;
    @Autowired
    QueryResponse queryResponse;

    public QueryResponse queryProcessResponse(ProductAndPlan productAndPlan, QueryProductAttributes queryProducts) {

        LOGGER.info("Plan Number " + queryProducts.getPlanNumber());
        LOGGER.info("Plan Name " + queryProducts.getPlanName());
        LOGGER.info("Product Name " + queryProducts.getProductName());
        LOGGER.info("Product Number " + queryProducts.getProductNumber());
        LOGGER.info("Plan Type " + queryProducts.getPlanTypes());

        LOGGER.info("Total Plan Number " + productAndPlan.getPlanNumber());
        LOGGER.info("Total Plan Name " + productAndPlan.getPlanName());
        LOGGER.info("Total Product Name " + productAndPlan.getProductName());
        LOGGER.info("Total Product Number " + productAndPlan.getProductNumber());
        LOGGER.info("Total Plan Type " + productAndPlan.getPlanTypes());
        LOGGER.info("Total Plan Number with Number " + productAndPlan.getPlanNameWithNumber());

        queryProducts.setConvertPlanTypesUpperCase(productValueConverter.convertPlanTypeToUpperCase(queryProducts));

        queryProducts.setConvertProductNameForSearch(productValueConverter.convertProductName(queryProducts));
        LOGGER.info("Convert Product Name " + queryProducts.getConvertProductNameForSearch());

        queryProducts.setConvertProductNumberForSearch(productValueConverter.convertProductNumber(queryProducts, productAndPlan));
        LOGGER.info("Convert Product Number " + queryProducts.getConvertProductNumberForSearch());

        queryProducts.setConvertPlanNameForSearch(productValueConverter.convertPlanName(queryProducts, productAndPlan));
        LOGGER.info("Convert Plan Name " + queryProducts.getConvertPlanNameForSearch());

        queryProducts.setConvertPlanNumberForSearch(productValueConverter.convertPlanNumber(queryProducts, productAndPlan));
        LOGGER.info("Convert Plan Number " + queryProducts.getConvertPlanNumberForSearch());

        int productLength = queryProducts.getProductNumber().size() + queryProducts.getProductName().size();

        if (productLength == 1) {
            queryResponse.setResponseCode(HttpServletResponse.SC_PARTIAL_CONTENT);
            queryResponse.setResponseText("Sorry! I can't compare product with only one products. But I can show you the detail of One");
            ProductList productList = fetchProductDetails(queryProducts);
            queryResponse.setProductList(productList);
        } else if (productLength == 2) {
            queryResponse.setResponseCode(HttpServletResponse.SC_OK);
            queryResponse.setResponseText("Sure! Here is the comparison of Products");
            ProductList productList = fetchProductDetails(queryProducts);
            queryResponse.setProductList(productList);
        } else if (productLength >= 3) {
            queryResponse.setResponseCode(HttpServletResponse.SC_PARTIAL_CONTENT);
            queryResponse.setResponseText("Apologies! I can't compare more than 2 products. But I can show you the details of all product asked by you");
            ProductList productList = fetchProductDetails(queryProducts);
            queryResponse.setProductList(productList);
        } else {
            queryResponse.setResponseCode(HttpServletResponse.SC_NO_CONTENT);
            queryResponse.setResponseText("Sorry! I didn't found any Product. Please mention product name or number");
        }
        String filteredJson = null;
        try {
            filteredJson = objectMapper.writeValueAsString(queryResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Finaal Response " + filteredJson);
        return queryResponse;
    }


    private ProductList fetchProductDetails(QueryProductAttributes queryProducts) {

        queryProducts.getConvertProductNameForSearch().addAll(queryProducts.getConvertProductNumberForSearch());
        queryProducts.getConvertPlanNameForSearch().addAll(queryProducts.getConvertPlanNumberForSearch());
        Map<String, List<Map<String, Object>>> productsMap = parseProductMap();

        ProductList productList;
        try {
            List<Map<String, Object>> filteredProducts = filterProductJson.filterProductsByName(productsMap, queryProducts.getConvertProductNameForSearch());
            LOGGER.info("Filtered Products Name  " + objectMapper.writeValueAsString(filteredProducts));

            filteredProducts = filterProductJson.filterPlanTypes(filteredProducts, queryProducts.getConvertPlanTypesUpperCase());
            LOGGER.info("Filtered PlanType " + objectMapper.writeValueAsString(filteredProducts));

            filteredProducts = filterProductJson.filterPlanUses(filteredProducts, queryProducts.getConvertPlanNameForSearch());
            LOGGER.info("Filtered PlanUse " + objectMapper.writeValueAsString(filteredProducts));

            Map<String, List<Map<String, Object>>> filteredProductsMap = Map.of("productsList", filteredProducts);

            String filteredJson = objectMapper.writeValueAsString(filteredProductsMap);
            productList = objectMapper.convertValue(filteredProductsMap, ProductList.class);
            LOGGER.info(filteredJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return productList;
    }

    private Map<String, List<Map<String, Object>>> parseProductMap() {
        try {
            Resource resource = new ClassPathResource("products.json");
            File file = resource.getFile();
            return objectMapper.readValue(file, new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
