package com.example.bankerchatbot.parser;

import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DependencyParsing {

    @Autowired
    DependencyCondition dependencyCondition;
    String conditionType = null;

    public void parseNNDependency(SemanticGraphEdge dependency) {
        conditionType = dependencyCondition.checkDependency(dependency.getSource());
        conditionType = dependencyCondition.checkDependency(dependency.getTarget());
    }

    public void parseNumDependency(SemanticGraphEdge dependency) {
        conditionType = dependencyCondition.checkDependency(dependency.getSource());
        conditionType = dependencyCondition.checkDependency(dependency.getTarget());
    }

    public void parseIObjDependency(SemanticGraphEdge dependency) {
        if ("VB".equals(dependency.getSource().tag())) {
            conditionType = dependencyCondition.checkDependency(dependency.getTarget());
        }
    }

    public void parseDObjDependency(SemanticGraphEdge dependency) {
        if ("VB".equals(dependency.getSource().tag())) {
            conditionType = dependencyCondition.checkDependency(dependency.getTarget());
        }
    }

    public void parseConjAndDependency(SemanticGraphEdge dependency) {
        conditionType = dependencyCondition.checkDependency(dependency.getSource());
        conditionType = dependencyCondition.checkDependency(dependency.getTarget());
    }

    public void parseConjVersusDependency(SemanticGraphEdge dependency) {
        conditionType = dependencyCondition.checkDependency(dependency.getSource());
        conditionType = dependencyCondition.checkDependency(dependency.getTarget());
    }

    public void parsePrepOfDependency(SemanticGraphEdge dependency) {
        conditionType = dependencyCondition.checkDependency(dependency.getSource());
        conditionType = dependencyCondition.checkDependency(dependency.getTarget());
    }

    public void parsePrepVersusDependency(SemanticGraphEdge dependency) {
        conditionType = dependencyCondition.checkDependency(dependency.getSource());
        conditionType = dependencyCondition.checkDependency(dependency.getTarget());
    }

    public void parseNSubDependency(SemanticGraphEdge dependency) {
        conditionType = dependencyCondition.checkDependency(dependency.getSource());
        conditionType = dependencyCondition.checkDependency(dependency.getTarget());
    }

    public void parsePrepWithDependency(SemanticGraphEdge dependency) {
    }

    public void parseAmodDependency(SemanticGraphEdge dependency) {
    }

    public void parseDetDependency(SemanticGraphEdge dependency) {
    }

    public void parseCOPDependency(SemanticGraphEdge dependency) {
    }

    public void parsePrepForDependency(SemanticGraphEdge dependency) {
    }

    public void notFoundDependency() {
    }

    public void parseVmodDependency(SemanticGraphEdge dependency) {
        conditionType = dependencyCondition.checkDependency(dependency.getSource());
        conditionType = dependencyCondition.checkDependency(dependency.getTarget());
    }
}
