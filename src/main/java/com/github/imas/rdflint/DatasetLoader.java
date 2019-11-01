package com.github.imas.rdflint;

import com.github.imas.rdflint.config.RdfLintParameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;

public class DatasetLoader {

  // create model from files (rdf, ttl)
  static Model loadRdfSet(RdfLintParameters params, String targetDir) throws IOException {
    String parentPath = new File(targetDir).getCanonicalPath();
    String baseUri = params.getBaseUri();

    Graph g = Factory.createGraphMem();
    Files.walk(Paths.get(parentPath))
        .filter(e -> e.toString().endsWith(".rdf") || e.toString().endsWith(".ttl"))
        .forEach(e -> {
          Graph gf = Factory.createGraphMem();
          String filename = e.toString().substring(parentPath.length() + 1);
          String subdir = filename.substring(0, filename.lastIndexOf('/') + 1);
          RDFParser.source(e.toString()).base(baseUri + subdir).parse(gf);
          List<Triple> lst = gf.find().toList();
          gf.close();
          lst.forEach(g::add);

          gf.getPrefixMapping().getNsPrefixMap()
              .forEach((k, v) -> g.getPrefixMapping().setNsPrefix(k, v));
        });

    return ModelFactory.createModelForGraph(g);
  }

}
