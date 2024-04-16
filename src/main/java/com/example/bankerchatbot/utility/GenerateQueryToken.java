package com.example.bankerchatbot.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class GenerateQueryToken {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public String stopWords(String query) {
        List<String> stopWords = dataReadFromFile("stopwords.txt");
        String refinedQuery = Arrays.stream(query.split("\\s+"))
                .filter(word -> !stopWords.contains(word.toLowerCase()))
                .collect(Collectors.joining(" "));
        LOGGER.info("QueryPostStopWords: " + refinedQuery);
        return refinedQuery;
    }

    public String convertJoinKeyWordsQuery(String query) {
        List<String> joinWords = dataReadFromFile("tokenized_keywords.txt");
        String refinedQuery = joinWords.stream()
                .reduce(query, (acc, word) -> acc.replace(word, word.replace(" ", "_")));
        LOGGER.info("QueryWithJoinWords: " + refinedQuery);
        return refinedQuery;
    }


    public List<String> fetchTestQueries() {
        return dataReadFromFile("testQueries.txt");
    }

    public String checkSpelling(String query) {
        List<String> joinWords = dataReadFromFile("allPossibleWords.txt");
        String refinedQuery = Arrays.stream(query.split("\\s+"))
                .map(word -> word.matches("[A-Za-z]+") ? StringCompare.findMostCorrectWord(word, joinWords) : word)
                .collect(Collectors.joining(" "));
        LOGGER.info("QueryPostCorrectWords: " + refinedQuery);
        return refinedQuery;
    }

    public String modifyProductName(String query) {
        String patternString = "UAT.*?\\d+\\.\\d+/\\d+\\.\\d+";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(query);
        Map<String, String> productMap = new HashMap<>();
        while (matcher.find()) {
            LOGGER.info("Product Name Found: " + matcher.group());
            String removeHyphenProductName = matcher.group().replaceAll("\\s*-\\s*", "-");
            String removeSpaceProductName = removeHyphenProductName.replaceAll(" ", "_");
            productMap.put(removeSpaceProductName, matcher.group());
        }

        String modifiedQuery = null;
        for (Map.Entry<String, String> entry : productMap.entrySet()) {
            String modifiedProductName = entry.getKey();
            String actualProductName = entry.getValue();
            modifiedQuery = modifiedQuery == null ? query : modifiedQuery;
            modifiedQuery = modifiedQuery.replace(actualProductName, modifiedProductName);

        }
        LOGGER.info("QueryPostModifiedProductName: " + modifiedQuery);
        return modifiedQuery != null ? modifiedQuery : query;
    }

    private List<String> dataReadFromFile(String fileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        return new BufferedReader(
                new InputStreamReader(inputStream)).lines().toList();
    }

    public String removeSpecialCharacter(String query) {
        String refinedQuery = Arrays.stream(query.split("\\s+"))
                .map(word -> word.replaceAll("[',()\"]", ""))
                .collect(Collectors.joining(" "));
        LOGGER.info("QueryPostRemoveSpecialChar: " + refinedQuery);
        return refinedQuery;
    }
}
