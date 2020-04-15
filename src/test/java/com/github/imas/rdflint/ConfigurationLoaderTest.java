package com.github.imas.rdflint;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.github.imas.rdflint.config.RdfLintParameters;
import org.junit.Test;

public class ConfigurationLoaderTest {

  public String getParentPath(String testSet) {
    return this.getClass().getClassLoader().getResource("testRDFs/" + testSet).getPath();
  }

  @Test
  public void loadConfig() throws Exception {
    RdfLintParameters params = ConfigurationLoader
        .loadConfig(getParentPath("config/rdflint-config-ok.yml"));

    assertEquals("https://sparql.crssnky.xyz/imasrdf/", params.getBaseUri());
    assertEquals("valid.rdf", params.getRules().get(0).getTarget());
  }

  @Test
  public void loadConfigValid() throws Exception {
    RdfLintParameters params = ConfigurationLoader
        .loadConfig(getParentPath("config/rdflint-config-validation.yml"));

    assertEquals("https://sparql.crssnky.xyz/imasrdf/", params.getBaseUri());
    assertEquals("value", params.getValidation().get("hoge"));
  }

  @Test
  public void loadConfigEmpty() throws Exception {
    RdfLintParameters params = ConfigurationLoader
        .loadConfig(getParentPath("config/rdflint-config-empty.yml"));

    assertNotNull(params);
    assertNull(params.getBaseUri());
  }

}
