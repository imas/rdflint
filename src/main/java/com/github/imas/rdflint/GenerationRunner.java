package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

public class GenerationRunner {

  /**
   * rdflint generation process.
   */
  void execute(RdfLintParameters params, String targetDir)
      throws IOException {
    if (params.getGeneration() == null) {
      return;
    }

    // clear output
    long errSize = params.getGeneration().stream().map(g -> {
      File f = new File(targetDir + "/" + g.getOutput());
      if (!f.exists()) {
        return true;
      }
      return f.delete();
    }).filter(v -> !v).count();
    if (errSize > 0) {
      throw new IOException("rdflint generation, fail to clear existed output.");
    }

    // prepare thymeleaf template engine
    FileTemplateResolver templateResolver = new FileTemplateResolver();
    templateResolver.setTemplateMode("TEXT");
    templateResolver.setPrefix(targetDir + "/");
    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);

    // prepare rdf dataset
    Model m = DatasetLoader.loadRdfSet(params, targetDir);

    params.getGeneration().forEach(g -> {
      String q = g.getQuery();

      try {
        // execute query and build result set
        Query query = QueryFactory.create(q);
        QueryExecution qe = QueryExecutionFactory.create(query, m);
        ResultSet results = qe.execSelect();

        List<Map<String, String>> lst = new LinkedList<>();
        List<String> cols = new LinkedList<>();
        while (results.hasNext()) {
          QuerySolution sol = results.next();
          Iterator<String> it = sol.varNames();
          cols.clear();
          while (it.hasNext()) {
            cols.add(it.next());
          }
          lst.add(cols.stream()
              .collect(Collectors.toMap(
                  c -> c,
                  c -> sol.get(c).toString()
              )));
        }

        // apply template
        Context ctx = new Context();
        ctx.setVariable("params", params);
        ctx.setVariable("rs", lst);
        templateEngine.process(
            g.getTemplate(),
            ctx,
            Files.newBufferedWriter(Paths.get(targetDir + "/" + g.getOutput()))
        );

      } catch (Exception ex) {
        ex.printStackTrace(); // NOPMD
      }
    });
  }

}
