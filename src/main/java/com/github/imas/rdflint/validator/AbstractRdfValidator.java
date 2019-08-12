package com.github.imas.rdflint.validator;

import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.config.RdfLintParameters;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.jena.graph.Triple;

public class AbstractRdfValidator implements RdfValidator {

  private RdfLintParameters params;
  private Object validationParams;
  private Map<String, String> validationParamMap = new ConcurrentHashMap<>();
  private List<Map<String, String>> validationParamMapList = new LinkedList<>();
  private String validatorName;

  /**
   * constructor.
   */
  public AbstractRdfValidator() {
    String clzName = this.getClass().getSimpleName();
    if (clzName.length() > "Validator".length()) {
      clzName = clzName.substring(0, clzName.length() - "Validator".length());
    }
    validatorName = clzName.substring(0, 1).toLowerCase() + clzName.substring(1);
  }

  @Override
  public void setParameters(RdfLintParameters params) {
    this.params = params;
    validationParams = null;
    if (this.params != null && this.params.getValidation() != null) {
      validationParams = this.params.getValidation().get(this.getValidatorName());
    }

    Object paramObj = this.getValidationParameter();
    validationParamMap.clear();
    validationParamMapList.clear();
    if (paramObj instanceof Map) {
      validationParamMap = makeStringMap((Map) paramObj);
    } else if (paramObj instanceof List) {
      for (Object obj : (List) paramObj) {
        if (obj instanceof Map) {
          validationParamMapList.add(makeStringMap((Map) obj));
        }
      }
    }
  }

  private Map<String, String> makeStringMap(Map paramObj) {
    Map<String, String> map = new ConcurrentHashMap<>();
    for (Object obj : ((Map) paramObj).entrySet()) {
      if (obj instanceof Map.Entry) {
        Map.Entry e = (Map.Entry) obj;
        map.put(e.getKey().toString(), e.getValue().toString());
      }
    }
    return map;
  }

  public RdfLintParameters getParameters() {
    return this.params;
  }

  public String getValidatorName() {
    return this.validatorName;
  }

  public Object getValidationParameter() {
    return validationParams;
  }

  public Map<String, String> getValidationParameterMap() {
    return validationParamMap;
  }

  public List<Map<String, String>> getValidationParameterMapList() {
    return validationParamMapList;
  }

  @Override
  public void validateFile(LintProblemSet problems, String path, String parentPath) {
  }

  @Override
  public void prepareValidationResource(Map<String, List<Triple>> fileTripleSet) {
  }

  @Override
  public void validateTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {
  }

  @Override
  public void validateOriginTripleSet(LintProblemSet problems, String file, List<Triple> tripeSet) {
  }

  @Override
  public void close() {
  }
}
