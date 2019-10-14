package com.github.imas.rdflint;

import static org.hamcrest.MatcherAssert.assertThat;

import com.github.imas.rdflint.LintProblem.ErrorLevel;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.io.ByteArrayOutputStream;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class LintProblemFormatterTest {

  private static class ConcreteRdfValidator extends AbstractRdfValidator {

  }

  @Test
  public void outputProblemSet() throws Exception {
    LintProblemSet set = new LintProblemSet();

    ConcreteRdfValidator validator = new ConcreteRdfValidator();
    set.addProblem("file1",
        new LintProblem(ErrorLevel.WARN, validator, "key_global", "global arg"));
    set.addProblem("file1",
        new LintProblem(ErrorLevel.WARN, 1, validator, "key_line", "line arg"));
    set.addProblem("file1",
        new LintProblem(ErrorLevel.WARN, 2, 4, validator, "key_linecol", "linecol arg"));

    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    LintProblemFormatter.out(byteOut, set);
    String outStd = byteOut.toString("UTF-8");
    assertThat("stdout: global", outStd, CoreMatchers.containsString("global arg"));
    assertThat("stdout: line", outStd, CoreMatchers.containsString("line arg"));
    assertThat("stdout: line", outStd, CoreMatchers.containsString("line: 1"));
    assertThat("stdout: linecol", outStd, CoreMatchers.containsString("linecol arg"));
    assertThat("stdout: linecol", outStd, CoreMatchers.containsString("line: 2"));
    assertThat("stdout: linecol", outStd, CoreMatchers.containsString("col: 4"));

    byteOut.reset();
    LintProblemFormatter.yaml(byteOut, set);
    String outYaml = byteOut.toString("UTF-8");
    assertThat("yamlout: global", outYaml, CoreMatchers.containsString("key_global"));
    assertThat("yamlout: line", outYaml, CoreMatchers.containsString("key_line"));
    assertThat("yamlout: line", outYaml, CoreMatchers.containsString("line: '1'"));
    assertThat("yamlout: linecol", outYaml, CoreMatchers.containsString("key_linecol"));
    assertThat("yamlout: linecol", outYaml, CoreMatchers.containsString("line: '2'"));
    assertThat("yamlout: linecol", outYaml, CoreMatchers.containsString("column: '4'"));
  }

}
