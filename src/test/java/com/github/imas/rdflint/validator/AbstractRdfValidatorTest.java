package com.github.imas.rdflint.validator;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

public class AbstractRdfValidatorTest {

  private static class ConcreteRdfValidator extends AbstractRdfValidator {

  }

  @Test
  public void validatorName() throws Exception {
    ConcreteRdfValidator validator = new ConcreteRdfValidator();
    assertEquals("concreteRdf", validator.getValidatorName());
  }

  @Test
  public void parameterMap() throws Exception {
    RdfLintParameters conf = new RdfLintParameters();
    conf.setValidation(new HashMap<String, Object>());
    HashMap<String, String> paramMap = new HashMap<>();
    conf.getValidation().put("concreteRdf", paramMap);
    paramMap.put("k", "v");

    ConcreteRdfValidator validator = new ConcreteRdfValidator();
    validator.setParameters(conf);

    assertEquals("v", validator.getValidationParameterMap().get("k"));
  }

  @Test
  public void parameterMapNull() throws Exception {
    RdfLintParameters conf = new RdfLintParameters();

    ConcreteRdfValidator validator = new ConcreteRdfValidator();
    validator.setParameters(conf);

    assertNull(validator.getValidationParameterMap().get("k"));
  }

  @Test
  public void parameterMapList() throws Exception {
    RdfLintParameters conf = new RdfLintParameters();
    conf.setValidation(new HashMap<String, Object>());
    List<HashMap<String, String>> paramMapList = new LinkedList<>();
    HashMap<String, String> paramMap = new HashMap<>();
    conf.getValidation().put("concreteRdf", paramMapList);
    paramMap.put("k", "v");
    paramMapList.add(paramMap);

    ConcreteRdfValidator validator = new ConcreteRdfValidator();
    validator.setParameters(conf);

    assertEquals("v", validator.getValidationParameterMapList().get(0).get("k"));
  }

}
