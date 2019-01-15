package com.github.takemikami.rdflint;

import java.util.List;

public class RdfLintParameters {

  private String baseUri;
  private List<CustomRule> rules;

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public List<CustomRule> getRules() {
    return rules;
  }

  public void setRules(List<CustomRule> rules) {
    this.rules = rules;
  }
}
