package com.example.bankerchatbot.parser;

import com.example.bankerchatbot.model.ProductAndPlan;
import com.example.bankerchatbot.model.QueryProductAttributes;
import com.example.bankerchatbot.utility.ExtractProductValue;
import edu.stanford.nlp.ling.IndexedWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DependencyCondition {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static List<String> knownPlanTypes = null;
    private static List<String> knownPlanUse = null;
    private static List<String> keyWordsProductType = null;
    private static List<String> keyWordsPlan = null;
    private static List<String> keyWordsProduct = null;

    QueryProductAttributes queryProductAttributes;
    String conditionType = null;

    @Autowired
    ExtractProductValue extractProductValue;

    public String checkDependency(IndexedWord indexedWord) {

        Pattern productNamePattern = Pattern.compile("UAT.*?\\d+\\.\\d+/\\d+\\.\\d+");
        Matcher productNameTargetMatcher = productNamePattern.matcher(indexedWord.word());

        if (knownPlanTypes.contains(indexedWord.word())) {
            queryProductAttributes.getPlanTypes().add(indexedWord.word());
            conditionType = "comparePlan";
        } else if (knownPlanUse.contains(indexedWord.word())) {
            queryProductAttributes.getPlanName().add(indexedWord.word());
            conditionType = "comparePlan";
        } else if (productNameTargetMatcher.find()) {
            queryProductAttributes.getProductName().add(productNameTargetMatcher.group());
            conditionType = "compareProduct";
        } else if (keyWordsProductType.contains(indexedWord.word())) {
            conditionType = "productType";
        } else if (keyWordsPlan.contains(indexedWord.word())) {
            conditionType = "planUse";
        } else if (keyWordsProduct.contains(indexedWord.word())) {
            conditionType = "product";
        } else if ("CD".equals(indexedWord.tag())) {
            if (conditionType.equals("compareProduct") || conditionType.equals("productType") || conditionType.equals("product")) {
                queryProductAttributes.getProductNumber().add(indexedWord.word());
                conditionType = "compareProduct";
            } else if (conditionType.equals("planUse") || conditionType.equals("comparePlan")) {
                queryProductAttributes.getPlanNumber().add(indexedWord.word());
                conditionType = "comparePlan";
            }
        } else {
            LOGGER.info("No target matched for " + indexedWord.word());
        }
        return conditionType;
    }

    public ProductAndPlan setRequiredFields(ProductAndPlan productAndPlan, QueryProductAttributes queryProductAttributes) {
        this.queryProductAttributes = queryProductAttributes;
        extractProductValue.extractPlanTypes(productAndPlan);
        extractProductValue.extractProductNameAndNumber(productAndPlan);
        extractProductValue.extractPlanNameAndNumber(productAndPlan);

        knownPlanTypes = extractProductValue.joinPlanTypeString(productAndPlan);
        knownPlanUse = extractProductValue.joinPlanUseString(productAndPlan);
        keyWordsProductType = Arrays.asList("product_type", "type");
        keyWordsPlan = Arrays.asList("plan_use", "plan");
        keyWordsProduct = List.of("product");
        return productAndPlan;
    }
}
