package com.github.imas.rdflint.config;

import java.util.List;
import java.util.Map;

public class RdfLintParameters {

  private String targetDir;
  private String outputDir;
  private String originDir;
  private String baseUri;
  private String suppressPath;
  private List<CustomRule> rules;
  private List<GenerationRule> generation;
  private Map<String, Object> validation;

  public String getTargetDir() {
    return targetDir;
  }

  public void setTargetDir(String targetDir) {
    this.targetDir = targetDir;
  }

  public String getOutputDir() {
    return outputDir;
  }

  public void setOutputDir(String outputDir) {
    this.outputDir = outputDir;
  }

  public String getOriginDir() {
    return originDir;
  }

  public void setOriginDir(String originDir) {
    this.originDir = originDir;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public String getSuppressPath() {
    return suppressPath;
  }

  public void setSuppressPath(String suppressPath) {
    this.suppressPath = suppressPath;
  }

  public List<GenerationRule> getGeneration() {
    return generation;
  }

  public void setGeneration(List<GenerationRule> generation) {
    this.generation = generation;
  }

  public Map<String, Object> getValidation() {
    return validation;
  }

  public void setValidation(Map<String, Object> validation) {
    this.validation = validation;
  }

  public List<CustomRule> getRules() {
    return rules;
  }

  public void setRules(List<CustomRule> rules) {
    this.rules = rules;
  }

  public static void copyProperties(RdfLintParameters src, RdfLintParameters dst) {
    dst.setTargetDir(src.getTargetDir());
    dst.setOutputDir(src.getOutputDir());
    dst.setOriginDir(src.getOriginDir());
    dst.setBaseUri(src.getBaseUri());
    dst.setSuppressPath(src.getSuppressPath());
    dst.setRules(src.getRules());
    dst.setGeneration(src.getGeneration());
    dst.setValidation(src.getValidation());
  }
}
