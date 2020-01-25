package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.LintProblemLocation;
import com.github.imas.rdflint.utils.DataTypeUtils;
import com.github.imas.rdflint.utils.DataTypeUtils.DataType;
import com.github.imas.rdflint.utils.StatsTestUtils;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

public class DataTypeValidator extends AbstractRdfValidator {

  private static final Logger logger = Logger.getLogger(DataTypeValidator.class.getName());

  private static final double TYPE_GUESS_THRESHOLD = 0.95;

  Map<String, DataType> dataTypeMap;

  ConcurrentHashMap<String, double[]> dataNgValues;

  @Override
  public void prepareValidationResource(Map<String, List<Triple>> fileTripleSet) {
    if (logger.isTraceEnabled()) {
      logger.trace("prepareValidationResource: in");
    }
    // type guess of predicates
    List<Triple> triples = fileTripleSet.values().stream().flatMap(Collection::stream)
        .filter(t -> t.getObject().isLiteral())
        .collect(Collectors.toList());

    Set<String> predicates = triples.stream()
        .map(t -> t.getPredicate().getURI())
        .collect(Collectors.toSet());

    dataTypeMap = new HashMap<>();

    predicates.forEach(p -> {
      List<DataType> datatypes = triples.stream()
          .filter(t -> t.getPredicate().getURI().equals(p))
          .map(t -> DataTypeUtils.guessDataType(t.getObject().getLiteralLexicalForm()))
          .collect(Collectors.toList());

      long cntNatural = 0;
      long cntInteger = 0;
      long cntFloat = 0;
      long cntString = 0;
      long cntTotal = 0;
      for (DataType t : datatypes) {
        cntTotal++;
        cntString++;
        if (t.equals(DataType.STRING)) {
          continue;
        }
        cntFloat++;
        if (t.equals(DataType.FLOAT)) {
          continue;
        }
        cntInteger++;
        if (t.equals(DataType.INTEGER)) {
          continue;
        }
        cntNatural++;
      }

      DataType dataType = DataType.STRING;
      if (((double) cntNatural / cntTotal) >= TYPE_GUESS_THRESHOLD) {
        dataType = DataType.NATURAL;
      } else if (((double) cntInteger / cntTotal) >= TYPE_GUESS_THRESHOLD) {
        dataType = DataType.INTEGER;
      } else if (((double) cntFloat / cntTotal) >= TYPE_GUESS_THRESHOLD) {
        dataType = DataType.FLOAT;
      } else if (((double) cntString / cntTotal) >= TYPE_GUESS_THRESHOLD) {
        dataType = DataType.STRING;
      }

      dataTypeMap.put(p, dataType);
    });

    // compute outlier
    dataNgValues = new ConcurrentHashMap<>();
    predicates.forEach(p -> {
      dataTypeMap.forEach((pred, dataType) -> {
        if (DataTypeUtils.isDataType(dataType, DataType.FLOAT)) {
          List<Double> valueList = triples.stream()
              .filter(t -> t.getPredicate().getURI().equals(pred))
              .filter(t -> DataTypeUtils.isDataType(
                  DataTypeUtils.guessDataType(t.getObject().getLiteralLexicalForm()),
                  DataType.FLOAT))
              .map(t -> Double.parseDouble(t.getObject().getLiteralLexicalForm()))
              .collect(Collectors.toList());
          double[] values = new double[valueList.size()];
          for (int i = 0; i < valueList.size(); i++) {
            values[i] = valueList.get(i);
          }

          double[] range = StatsTestUtils.clusteringOutlierTest(values, 3.0, 10);
          dataNgValues.put(pred, range);
        }
      });
    });
    logger.trace("prepareValidationResource: out");
  }

  @Override
  public List<LintProblem> validateTriple(Node subject, Node predicate, Node object,
      int beginLine, int beginCol, int endLine, int endCol) {
    List<LintProblem> rtn = new LinkedList<>();

    if (object.isLiteral()) {
      String value = object.getLiteralValue().toString();

      // check data type by guessedType
      DataType guessedType = dataTypeMap.get(predicate.getURI());
      DataType dataType = DataTypeUtils.guessDataType(value);
      if (!DataTypeUtils.isDataType(dataType, guessedType)) {
        rtn.add(new LintProblem(ErrorLevel.INFO, this,
            new LintProblemLocation(beginLine, beginCol, endLine, endCol,
                new Triple(subject, predicate, object)),
            "notmatchedGuessedDataType", guessedType, dataType));
      }

      // check data type by language
      String litLang = object.getLiteralLanguage();
      if (!DataTypeUtils.isLang(value, litLang)) {
        rtn.add(new LintProblem(ErrorLevel.INFO, this,
            new LintProblemLocation(beginLine, beginCol, endLine, endCol,
                new Triple(subject, predicate, object)),
            "notmatchedLanguageType", litLang, value));
      }

      // check computed outlier
      double[] ngValues = dataNgValues.get(predicate.getURI());
      if (ngValues != null && ngValues.length > 0) {
        try {
          double val = Double.parseDouble(value);
          boolean match = false;
          for (double v : ngValues) {
            if (v == val) {
              match = true;
            }
          }
          if (match) {
            rtn.add(new LintProblem(ErrorLevel.INFO, this,
                new LintProblemLocation(beginLine, beginCol, endLine, endCol,
                    new Triple(subject, predicate, object)),
                "predictedOutlier", val));
          }
        } catch (NumberFormatException ex) {
          // Invalid Number Format
        }
      }
    }
    return rtn;
  }

}
