package com.example.bankerchatbot.classification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.*;
import weka.core.converters.ConverterUtils;

import java.lang.invoke.MethodHandles;

public class QueryClassifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String args[]) throws Exception {
        String testQuery = "Compare the features of Product UAT-234+9.11/345 versus Product UAT-6755+11.11/345";
        QueryClassifier queryClassifier = new QueryClassifier();
        PredictedQueryClassify predictedQueryClassify = new PredictedQueryClassify();
        Instances trainDataSetInstance = queryClassifier.loadModal();
        Classifier classifier = queryClassifier.getClassifier();
        Instances instance = queryClassifier.evaluateTrainingInstance(trainDataSetInstance, classifier, predictedQueryClassify);
        String predictedQueryClass = predictedQueryClassify.predictedQueryClassify(testQuery, instance, trainDataSetInstance, classifier);
    }

    private Instances loadModal() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("training.arff");
        Instances trainDataSetInstance = source.getDataSet();
        trainDataSetInstance.setClassIndex(trainDataSetInstance.numAttributes() - 1);
        return trainDataSetInstance;
    }

    private Classifier getClassifier() throws Exception {
        String classString = "weka.classifiers.bayes.NaiveBayes";
        return AbstractClassifier.forName(classString, null);
    }

    private Instances evaluateTrainingInstance(Instances trainDataSetInstance, Classifier classifier, PredictedQueryClassify predictedQueryClassify) throws Exception {
        Instances instance = predictedQueryClassify.filterText(trainDataSetInstance);
        classifier.buildClassifier(instance);
        Evaluation evaluation = new Evaluation(trainDataSetInstance);
        try {
            evaluation.evaluateModel(classifier, trainDataSetInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info(printClassifierAndEvaluation(classifier, evaluation) + "\n");
        return instance;
    }




    // information about classifier and evaluation
    private StringBuffer printClassifierAndEvaluation(Classifier thisClassifier, Evaluation thisEvaluation) {
        StringBuffer result = new StringBuffer();
        try {
            LOGGER.info("INFORMATION ABOUT THE CLASSIFIER AND EVALUATION:\n");
            LOGGER.info("\nclassifier.toString():\n" + thisClassifier.toString() + "\n");
            LOGGER.info("\nevaluation.toSummaryString(title, false):\n" + thisEvaluation.toSummaryString("Summary", false) + "\n");
            LOGGER.info("\nevaluation.toMatrixString():\n" + thisEvaluation.toMatrixString() + "\n");
            LOGGER.info("\nevaluation.toClassDetailsString():\n" + thisEvaluation.toClassDetailsString("Details") + "\n");
            LOGGER.info("\nevaluation.toCumulativeMarginDistribution:\n" + thisEvaluation.toCumulativeMarginDistributionString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("\nException (sorry!):\n" + e.toString());
        }
        return result;
    }
}
