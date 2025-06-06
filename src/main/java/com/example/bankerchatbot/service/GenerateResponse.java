package com.example.bankerchatbot.service;

import com.example.bankerchatbot.constants.Constants;
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
import java.util.Random;

@Component
public class GenerateResponse implements Constants {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ProductValueConverter productValueConverter;
    @Autowired
    FilterProductJson filterProductJson;
    QueryResponse queryResponse = null;

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

        productValueConverter.checkUnMatchedValues(queryProducts, productAndPlan);

        ProductList productListFromJson = productValueConverter.convertJsonToProduct();
        LOGGER.info("ProductList From Json Size: " + productListFromJson.getProductsList().size());
        List<String> allProductList = productValueConverter.generateAllProductList(productListFromJson);
        productValueConverter.checkAndValidateAllQueryFields(queryProducts, productAndPlan, allProductList);

        queryProducts.setConvertPlanType(productValueConverter.convertPlanType(queryProducts));
        LOGGER.info("Convert Plan Type " + queryProducts.getConvertPlanType());

        queryProducts.setConvertProductNameForSearch(productValueConverter.convertProductName(queryProducts));
        LOGGER.info("Convert Product Name " + queryProducts.getConvertProductNameForSearch());

        queryProducts.setConvertProductNumberForSearch(productValueConverter.convertProductNumber(queryProducts, productAndPlan));
        LOGGER.info("Convert Product Number " + queryProducts.getConvertProductNumberForSearch());

        queryProducts.setConvertPlanNameForSearch(productValueConverter.convertPlanName(queryProducts, productAndPlan));
        LOGGER.info("Convert Plan Name " + queryProducts.getConvertPlanNameForSearch());

        queryProducts.setConvertPlanNumberForSearch(productValueConverter.convertPlanNumber(queryProducts, productAndPlan));
        LOGGER.info("Convert Plan Number " + queryProducts.getConvertPlanNumberForSearch());



        StringBuilder unMatchedValues = new StringBuilder();
        if(null != queryProducts.getUnMatchedNumber() && !queryProducts.getUnMatchedNumber().isEmpty()){
            unMatchedValues.append(" Below plan/product number not found : ");
            unMatchedValues.append(queryProducts.getUnMatchedNumber());
        }
        if(null != queryProducts.getUnMatchedProductName() && !queryProducts.getUnMatchedProductName().isEmpty()){
            unMatchedValues.append(" Below Product name not found : ");
            unMatchedValues.append(queryProducts.getUnMatchedProductName());
        }

        ProductList productList = fetchProductDetails(queryProducts);
        if (productList.getProductsList().size() == 0) {
            queryResponse.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
            queryResponse.setResponseText("Sorry! I didn't found any product. Please mention correct product name or number."+" "+unMatchedValues);
        } else {
            int productLength = queryProducts.getConvertProductNumberForSearch().size() + queryProducts.getConvertProductNameForSearch().size();
            queryResponse.setProductList(productList);

            if (productLength == 1 && queryResponse.getQueryIntent().equals("product_details")) {
                queryResponse.setResponseCode(HttpServletResponse.SC_PARTIAL_CONTENT);
                queryResponse.setResponseText(PRODUCT_DETAIL_LENGTH_ONE.get(new Random().nextInt(PRODUCT_DETAIL_LENGTH_ONE.size()))+" "+unMatchedValues);
                queryResponse.setQueryIntent("Product_Detail");
            } else if (productLength == 1) {
                queryResponse.setResponseCode(HttpServletResponse.SC_PARTIAL_CONTENT);
                queryResponse.setResponseText("Okay!Found the product and here is the details"+" "+unMatchedValues);
                queryResponse.setQueryIntent("Compare_Product");
            } else if (productLength == 2) {
                queryResponse.setQueryIntent("Compare_Product");
                queryResponse.setResponseCode(HttpServletResponse.SC_OK);
                queryResponse.setResponseText("Sure! Here is the comparison of Products");
            } else if (productLength >= 3) {
                queryResponse.setQueryIntent("Compare_Product");
                queryResponse.setResponseCode(HttpServletResponse.SC_PARTIAL_CONTENT);
                queryResponse.setResponseText("Apologies! I can't compare more than 2 products. But I can show you the details of all product asked by you");
            } else {
                queryResponse.setResponseCode(HttpServletResponse.SC_NO_CONTENT);
                queryResponse.setResponseText("Apologies! I didn't found any Product. Please mention product name or number"+" "+unMatchedValues);
            }
        }

        String filteredJson = null;
        try {
            filteredJson = objectMapper.writeValueAsString(queryResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Final Response " + filteredJson);
        return queryResponse;
    }

    private ProductList fetchProductDetails(QueryProductAttributes queryProducts) {

        if(queryProducts.getConvertProductNumberForSearch()!= null){
            queryProducts.getConvertProductNameForSearch().addAll(queryProducts.getConvertProductNumberForSearch());
            queryProducts.getConvertProductNumberForSearch().removeAll(queryProducts.getConvertProductNumberForSearch());
        }

        queryProducts.getConvertPlanNameForSearch().addAll(queryProducts.getConvertPlanNumberForSearch());
        Map<String, List<Map<String, Object>>> productsMap = parseProductMap();

        ProductList productList;
        try {
            List<Map<String, Object>> filteredProducts = filterProductJson.filterProductsByName(productsMap, queryProducts.getConvertProductNameForSearch());
            LOGGER.info("Filtered Products Name  " + objectMapper.writeValueAsString(filteredProducts));

            if (!queryProducts.getConvertPlanType().isEmpty()) {
                filteredProducts = filterProductJson.filterPlanTypes(filteredProducts, queryProducts.getConvertPlanType());
                LOGGER.info("Filtered PlanType " + objectMapper.writeValueAsString(filteredProducts));
            }

            if (!queryProducts.getConvertPlanNameForSearch().isEmpty()) {
                filteredProducts = filterProductJson.filterPlanUses(filteredProducts, queryProducts.getConvertPlanNameForSearch());
                LOGGER.info("Filtered PlanUse " + objectMapper.writeValueAsString(filteredProducts));
            }

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

    public void setQueryResponse(QueryResponse queryResponse) {
        this.queryResponse = queryResponse;
    }
}
