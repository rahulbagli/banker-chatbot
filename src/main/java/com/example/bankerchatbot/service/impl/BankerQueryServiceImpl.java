package com.example.bankerchatbot.service.impl;

import com.example.bankerchatbot.classification.QueryClassifier;
import com.example.bankerchatbot.configuration.BankerConfig;
import com.example.bankerchatbot.model.ProductAndPlan;
import com.example.bankerchatbot.model.QueryProductAttributes;
import com.example.bankerchatbot.model.QueryResponse;
import com.example.bankerchatbot.parser.DependencyCondition;
import com.example.bankerchatbot.parser.DependencyParsing;
import com.example.bankerchatbot.service.BankerQueryService;
import com.example.bankerchatbot.service.GenerateResponse;
import com.example.bankerchatbot.utility.GenerateQueryToken;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class BankerQueryServiceImpl implements BankerQueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    GenerateQueryToken generateQueryToken;
    @Autowired
    GenerateResponse generateResponse;
    @Autowired
    DependencyParsing dependencyParsing;
    @Autowired
    DependencyCondition dependencyCondition;
    @Autowired
    QueryClassifier queryClassifier;
    @Autowired
    BankerConfig bankerConfig;

    @Override
    public QueryResponse processQuery(String query) {

        ProductAndPlan productAndPlan = new ProductAndPlan();
        QueryProductAttributes queryProductAttributes = new QueryProductAttributes();

        dependencyCondition.setRequiredFields(productAndPlan, queryProductAttributes);
        String queryPostSpecialCharacter = generateQueryToken.removeSpecialCharacter(query);
        String queryPostStopWords = generateQueryToken.stopWords(queryPostSpecialCharacter);
        String queryPostModifiedProductName = generateQueryToken.modifyProductName(queryPostStopWords, queryProductAttributes);
        String classIntent = queryClassifier.fetchQueryIntent(queryPostModifiedProductName);
        LOGGER.info("Class Intent " + classIntent);

        QueryResponse queryResponse = new QueryResponse();
        queryResponse.setQueryIntent(classIntent);
        processQueryWithClassIntent(classIntent, productAndPlan, queryResponse, queryPostModifiedProductName
                , queryProductAttributes);

        return queryResponse;
    }

    private void processQueryWithClassIntent(String classIntent, ProductAndPlan productAndPlan,
                                             QueryResponse queryResponse, String queryPostModifiedProductName,
                                             QueryProductAttributes queryProductAttributes) {
        switch (classIntent) {
            case "compare_products":
            case "product_details":
                extractionLogic(bankerConfig.getPipeline(), queryPostModifiedProductName, queryResponse,
                        productAndPlan, queryProductAttributes);
                break;
            case "display_products":
                displayProducts(productAndPlan, queryResponse);
                break;
            case "display_plans":
                displayPlans(productAndPlan, queryResponse);
                break;
            case "display_plantypes":
                displayPlanTypes(productAndPlan, queryResponse);
                break;
            case "count_products":
                countProducts(productAndPlan, queryResponse);
                break;
            case "count_plantypes":
                countPlanTypes(productAndPlan, queryResponse);
                break;
            case "count_plans":
                countPlans(productAndPlan, queryResponse);
                break;
            case "?":
                noIntentMatched(queryResponse);
                break;
        }
    }

    private void extractionLogic(StanfordCoreNLP pipeline, String queryPostModifiedProductName, QueryResponse queryResponse,
                                 ProductAndPlan productAndPlan, QueryProductAttributes queryProductAttributes) {

        String queryPostCheckingSpelling = generateQueryToken.checkSpelling(queryPostModifiedProductName);
        String queryWithJoinWords = generateQueryToken.convertJoinKeyWordsQuery(queryPostCheckingSpelling);

        Annotation document = new Annotation(queryWithJoinWords);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        SemanticGraph dependencies = sentences.get(0).get(SemanticGraphCoreAnnotations.AlternativeDependenciesAnnotation.class);
        LOGGER.info(dependencies.toString(SemanticGraph.OutputFormat.LIST));

        for (SemanticGraphEdge dependency : dependencies.edgeListSorted()) {
            extractIncomingEdgeAndSetAttribute(dependency);
        }
        generateResponse.setQueryResponse(queryResponse);
        generateResponse.queryProcessResponse(productAndPlan, queryProductAttributes);
    }

    private void extractIncomingEdgeAndSetAttribute(SemanticGraphEdge dependency) {
        String relation = dependency.getRelation().toString();
        switch (relation) {

            case "nn":
                dependencyParsing.parseNNDependency(dependency);
                break;
            case "num":
                dependencyParsing.parseNumDependency(dependency);
                break;
            case "cc":
                dependencyParsing.parseCCDependency(dependency);
                break;
            case "iobj":
                dependencyParsing.parseIObjDependency(dependency);
                break;
            case "dobj":
                dependencyParsing.parseDObjDependency(dependency);
                break;
            case "conj_and":
                dependencyParsing.parseConjAndDependency(dependency);
                break;
            case "conj_versus":
                dependencyParsing.parseConjVersusDependency(dependency);
                break;
            case "prep_of":
                dependencyParsing.parsePrepOfDependency(dependency);
                break;
            case "prep_versus":
                dependencyParsing.parsePrepVersusDependency(dependency);
                break;
            case "nsubj":
                dependencyParsing.parseNSubDependency(dependency);
                break;
            case "prep_with":
                dependencyParsing.parsePrepWithDependency(dependency);
                break;
            case "amod":
                dependencyParsing.parseAmodDependency(dependency);
                break;
            case "det":
                dependencyParsing.parseDetDependency(dependency);
                break;
            case "cop":
                dependencyParsing.parseCOPDependency(dependency);
                break;
            case "number":
                dependencyParsing.parseNumberDependency(dependency);
                break;
            case "quantmod":
                dependencyParsing.parseQuantModDependency(dependency);
                break;
            case "prep_for":
                dependencyParsing.parsePrepForDependency(dependency);
                break;
            case "vmod":
                dependencyParsing.parseVmodDependency(dependency);
                break;
            default:
                dependencyParsing.notFoundDependency();
        }
    }

    private void noIntentMatched(QueryResponse queryResponse) {
        queryResponse.setResponseCode(HttpServletResponse.SC_NO_CONTENT);
        queryResponse.setQueryIntent("No_Intent");
        queryResponse.setResponseText("Apologies! I didn't understood your Query");
    }

    private void displayPlanTypes(ProductAndPlan productAndPlan, QueryResponse queryResponse) {
        queryResponse.setResponseCode(HttpServletResponse.SC_OK);
        queryResponse.setQueryIntent("Display_PlanType");
        queryResponse.setPlanTypes(productAndPlan.getPlanTypes());
        queryResponse.setResponseText("Certainly! Here is the details of Plan Types");
    }

    private void displayPlans(ProductAndPlan productAndPlan, QueryResponse queryResponse) {
        queryResponse.setResponseCode(HttpServletResponse.SC_OK);
        queryResponse.setQueryIntent("Display_Plans");
        queryResponse.setPlanNameWithNumber(productAndPlan.getPlanNameWithNumber());
        queryResponse.setResponseText("Sure! Below are the details of Plans");
    }

    private void displayProducts(ProductAndPlan productAndPlan, QueryResponse queryResponse) {
        queryResponse.setResponseCode(HttpServletResponse.SC_OK);
        queryResponse.setQueryIntent("Display_Products");
        queryResponse.setProductName(productAndPlan.getProductName());
        queryResponse.setResponseText("Okay! Showing products of your query");
    }

    private void countPlans(ProductAndPlan productAndPlan, QueryResponse queryResponse) {
        queryResponse.setResponseCode(HttpServletResponse.SC_OK);
        queryResponse.setQueryIntent("Count_Plans");
        queryResponse.setResponseText("Okay! Found total number of Plans: "+productAndPlan.getPlanName().size());
    }

    private void countPlanTypes(ProductAndPlan productAndPlan, QueryResponse queryResponse) {
        queryResponse.setResponseCode(HttpServletResponse.SC_OK);
        queryResponse.setQueryIntent("Count_PlanTypes");
        queryResponse.setResponseText("Sure! Total count of Plan Types is : " + productAndPlan.getPlanTypes().size());
    }

    private void countProducts(ProductAndPlan productAndPlan, QueryResponse queryResponse) {
        queryResponse.setResponseCode(HttpServletResponse.SC_OK);
        queryResponse.setQueryIntent("Count_Products");
        queryResponse.setResponseText("Certainly! Total Product Number : "+productAndPlan.getProductName().size());
    }
}
