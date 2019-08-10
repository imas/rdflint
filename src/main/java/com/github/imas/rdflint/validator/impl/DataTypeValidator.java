package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.graph.Triple;

public class DataTypeValidator extends AbstractRdfValidator {

  enum DataType {
    STRING,
    FLOAT,
    INTEGER,
    NATURAL
  }

  private static final String REGEX_NATURAL = "\\d+";
  private static final String REGEX_INTEGER = "[+-]?\\d+";
  private static final String REGEX_FLOAT = "[+-]?\\d+(\\.\\d+)?";

  private static final double TYPE_GUESS_THRESHOLD = 0.95;

  Map<String, DataType> dataTypeMap;

  @Override
  public void prepareValidationResource(Map<String, List<Triple>> fileTripleSet) {
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
          .map(t -> guessDataType(t.getObject().getLiteralValue().toString()))
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

  }

  private DataType guessDataType(String s) {
    if (s.matches(REGEX_NATURAL)) {
      return DataType.NATURAL;
    } else if (s.matches(REGEX_INTEGER)) {
      return DataType.INTEGER;
    } else if (s.matches(REGEX_FLOAT)) {
      return DataType.FLOAT;
    }
    return DataType.STRING;
  }

  private boolean checkDataType(DataType dataType, DataType expected) {
    switch (expected) {
      case NATURAL:
        if (dataType == DataType.NATURAL) {
          return true;
        } else {
          return false;
        }
      case INTEGER:
        if (dataType == DataType.NATURAL
            || dataType == DataType.INTEGER) {
          return true;
        } else {
          return false;
        }
      case FLOAT:
        if (dataType == DataType.NATURAL
            || dataType == DataType.INTEGER
            || dataType == DataType.FLOAT) {
          return true;
        } else {
          return false;
        }
      case STRING:
        return true;
      default:
        break;
    }
    return false;
  }

  @Override
  public void validateTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {
    tripeSet.stream().filter(t -> t.getObject().isLiteral()).forEach(t -> {
      DataType guessedType = dataTypeMap.get(t.getPredicate().getURI());
      DataType dataType = guessDataType(t.getObject().getLiteralValue().toString());
      if (!checkDataType(dataType, guessedType)) {
        problems.addProblem(
            file,
            LintProblem.ErrorLevel.INFO,
            "DataType unmatched: expected " + guessedType + ", but " + dataType
                + " (Triple: " + t.getSubject() + " - " + t.getPredicate() + " - "
                + t.getObject() + ")"
        );
      }
    });
  }

}
