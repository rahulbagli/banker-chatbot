package com.example.bankerchatbot.service.impl;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Properties;

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

    @Override
    public QueryResponse processQuery(String query) {
        Properties props = new Properties();
        props.setProperty("ner.useSUTime", "0");
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse, dcoref");
        props.setProperty("tokenize.whitespace", "true");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        ProductAndPlan productAndPlan = new ProductAndPlan();
        QueryProductAttributes queryProductAttributes = new QueryProductAttributes();
        LOGGER.info("Query product: "+queryProductAttributes.getProductName().size());
        dependencyCondition.setRequiredFields(productAndPlan, queryProductAttributes);
        extractionLogic(pipeline, query);
        return generateResponse.queryProcessResponse(productAndPlan, queryProductAttributes);
    }

    private void extractionLogic(StanfordCoreNLP pipeline, String query) {

        String queryPostStopWords = generateQueryToken.stopWords(query);
        String queryPostModifiedProductName = generateQueryToken.modifyProductName(queryPostStopWords);
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
    }

    private void extractIncomingEdgeAndSetAttribute(SemanticGraphEdge dependency) {
        String relation = dependency.getRelation().toString();
        switch (relation){

            case "nn": dependencyParsing.parseNNDependency(dependency); break;
            case "num": dependencyParsing.parseNumDependency(dependency); break;
            case "iobj": dependencyParsing.parseIObjDependency(dependency); break;
            case "dobj": dependencyParsing.parseDObjDependency(dependency); break;
            case "conj_and": dependencyParsing.parseConjAndDependency(dependency); break;
            case "conj_versus": dependencyParsing.parseConjVersusDependency(dependency); break;
            case "prep_of": dependencyParsing.parsePrepOfDependency(dependency); break;
            case "prep_versus": dependencyParsing.parsePrepVersusDependency(dependency); break;
            case "nsubj": dependencyParsing.parseNSubDependency(dependency); break;
            case "prep_with": dependencyParsing.parsePrepWithDependency(dependency); break;
            case "amod": dependencyParsing.parseAmodDependency(dependency); break;
            case "det": dependencyParsing.parseDetDependency(dependency); break;
            case "cop": dependencyParsing.parseCOPDependency(dependency); break;
            case "prep_for": dependencyParsing.parsePrepForDependency(dependency); break;
            case "vmod": dependencyParsing.parseVmodDependency(dependency); break;
            default : dependencyParsing.notFoundDependency();
        }
    }
}
