package com.example.bankerchatbot.utility;

import com.example.bankerchatbot.model.ProductAndPlan;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class ExtractProductValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    @Autowired
    private ObjectMapper objectMapper;

    public List<String> joinPlanUseString(ProductAndPlan productAndPlan) {
        List<String> joinWords = productAndPlan.getPlanName();
        return joinWords.stream()
                .map(word -> word.toLowerCase().replace(" ", "_"))
                .toList();
    }

    public List<String> extractPlanUse() {
        Map<String, List<Map<String, Object>>> productsMap = parseProductMap();
        List<String> planUseList = productsMap.values().stream()
                .flatMap(Collection::stream)
                .flatMap(productMap -> ((List<Object>) productMap.get("planTypeList")).stream())
                .map(planType -> (Map<String, Object>) planType)
                .flatMap(planTypeMap -> ((List<Object>) planTypeMap.get("planUseList")).stream())
                .map(planUse -> ((Map<String, Object>) planUse).get("planUse").toString())
                .distinct().toList();
        LOGGER.info("Extracted Plans Use:  "+planUseList);
        return planUseList;
    }

    public void extractPlanNameAndNumber(ProductAndPlan productAndPlan) {
        List<String> planUseList = extractPlanUse();
        List<String> extractedPlanName = planUseList.stream()
                .map(plan -> plan.split(" \\(")[0]).toList();
        LOGGER.info("Extracted Plan Name: " + extractedPlanName);

        List<String> extractedPlanNumber = planUseList.stream()
                .map(plan -> plan.replaceAll("[^0-9]", "")).toList();

        List <String> extractedPlanNameWithNumber = planUseList.stream()
                        .map(plan -> plan.split(" \\(")[0]+"-"+ plan.replaceAll("[^0-9]", "")
                        ).toList();
        LOGGER.info("Extracted Plan Numbers: " + extractedPlanNumber);
        productAndPlan.setPlanNameWithNumber(extractedPlanNameWithNumber);
        productAndPlan.setPlanName(extractedPlanName);
        productAndPlan.setPlanNumber(extractedPlanNumber);
    }

    public List<String> joinPlanTypeString(ProductAndPlan productAndPlan) {
        List<String> joinWords = productAndPlan.getPlanTypes();
        return joinWords.stream()
                .map(word -> word.toLowerCase().replace(" ", "_"))
                .toList();
    }

    public List<String> extractPlanTypes(ProductAndPlan productAndPlan) {
        Map<String, List<Map<String, Object>>> productsMap = parseProductMap();
        List<String> planTypes = productsMap.values().stream()
                .flatMap(Collection::stream)
                .flatMap(productMap -> ((List<Object>) productMap.get("planTypeList")).stream()) // Nested Stream for planTypeList
                .map(planType -> (Map<String, Object>) planType)
                .map(planTypeMap -> planTypeMap.get("planType").toString()) // Extract productType
                .distinct().toList();
        productAndPlan.setPlanTypes(planTypes);
        LOGGER.info("Extracted Plan Types:  "+planTypes);
        return planTypes;
    }

    public void extractProductNameAndNumber(ProductAndPlan productAndPlan) {
        Map<String, List<Map<String, Object>>> productsMap = parseProductMap();
        List<String> products = productsMap.values().stream()
                .flatMap(Collection::stream)
                .map(planTypeMap -> planTypeMap.get("product").toString())
                .distinct().toList();

        List<String> extractedProductNumber = products.stream()
                .map(product -> product.split(" ")[4]).toList();

        LOGGER.info("Extracted Product Number: " + extractedProductNumber);
        LOGGER.info("Extract Products : "+products);
        productAndPlan.setProductName(products);
        productAndPlan.setProductNumber(extractedProductNumber);

    }

    private Map<String, List<Map<String, Object>>> parseProductMap() {
        try {
            Resource resource = new ClassPathResource("products.json");
            File file = resource.getFile();
            return objectMapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
