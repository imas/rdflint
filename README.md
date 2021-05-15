rdflint
---

[![JitPack](https://jitpack.io/v/imas/rdflint.svg)](https://jitpack.io/#imas/rdflint)
[![Marketplace Version](https://vsmarketplacebadge.apphb.com/version/takemikami.vscode-rdflint.svg "Current Release")](https://marketplace.visualstudio.com/items?itemName=takemikami.vscode-rdflint)
[![Setup rdflint](https://img.shields.io/badge/GitHub_Marketplace-Setup_rdflint-light_green)](https://github.com/marketplace/actions/setup-rdflint)
[![ci](https://github.com/imas/rdflint/actions/workflows/ci.yaml/badge.svg)](https://github.com/imas/rdflint/actions/workflows/ci.yaml)
[![UsersGuide](https://img.shields.io/badge/users_guide-ja-blue)](https://imas.github.io/rdflint/)

[rdflint Users Guide](https://imas.github.io/rdflint/)  
Note. Currencty translate to English from Japanese. Some part is still in Japanese.

## What's rdflint

rdflint is a linter for RDF files. Easy to check syntax and other things. rdflint is powered by [Apache Jena](https://jena.apache.org/).

rdflint has following functions.

- Syntax check of rdf and Turtle(ttl).
- Undefined subject check when use as predicate or object.
- Custom check by SPARQL query.
- Degrade validation.
- Datatype & outlier validation.
- SHACL constraint validation.
- Literal trim validation.
- File encoding validation.
- RDF generation by SPARQL query results.
- SPARQL playground on your PC. (Interactive Mode)

## Getting Started

### Work on Visual Studio Code

Use [vscode-rdflint](vscode-rdflint/README.md)

### Work on your PC

Download from jitpack.

```
$ wget https://jitpack.io/com/github/imas/rdflint/0.1.5/rdflint-0.1.5.jar
```

Run for apply to your RDF files.

```
$ java -jar rdflint-0.1.5.jar -targetdir example/dataset
```

``-targetdir`` parameter is location of target RDF files.

### Work on GitHub Actions

Make ``.github/workflows/ci.yml`` to your repository. Its contents is following.

```
name: CI
on: pull_request
jobs:
  rdflint:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - uses: actions/setup-java@v2
      with:
        distribution: adopt
        java-version: 11
    - uses: imas/setup-rdflint@v1
    - name: Run rdflint
      run: rdflint
```

See [imas/setup-rdflint](https://github.com/imas/setup-rdflint) for more information.

## Configurations

When create a configuration file of rdflint, rdflint provide additional checks. Run with ``-config`` parameter like following.

```
$ java -jar rdflint-0.1.5.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml
```

``-config`` parameter is location of rdflint configuration file.

### Undefined subject checks

Write configuration file like a following.

```
baseUri: https://sparql.crssnky.xyz/imasrdf/
```

And check undefined subject under baseUri, when use as predicate or object.


### SPARQL based custom checks

Write configuration file like a following.

```
rules:
- name: file class
  target: "RDFs/765AS.rdf"
  query: |
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    SELECT ?s ?o
    WHERE {
      ?s rdf:type ?o .
      FILTER NOT EXISTS {
        ?s rdf:type <https://sparql.crssnky.xyz/imasrdf/URIs/imas-schema.ttl#Idol>
      }
    }
  valid: |
    while(rs.hasNext()) {
      log.warn("Idol definition file " + rs.next())
    }
```

And run SPARQL query of ``rules-query`` to target file of ``rules-target``. Groovy script of ``rules-valid`` apply to result set of query.


### Degrade validation

Run with ``-origindir`` parameter like following.

```
$ java -jar rdflint-0.1.5.jar -targetdir example/dataset -origindir example/dataset_origin -config example/dataset/rdflint-config.yml
```

And check subject and triple, removed from origindir. Its problem report as INFO level.


### SHACL constraint validation

write your SHACL constraint in `.ttl` or `.rdf` file.
and put this file in directory which specified by `-targetdir`.


### RDF generation

Write configuration file like a following.

```
baseUri: https://sparql.crssnky.xyz/imasrdf/
generation:
  - query: |
      PREFIX schema: <http://schema.org/>
      PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      PREFIX imas: <https://sparql.crssnky.xyz/imasrdf/URIs/imas-schema.ttl#>
      SELECT  ?m (group_concat(?s;separator=",,,,,")as ?u)
      WHERE {
        ?s rdf:type imas:Unit;
           schema:member ?m.
      }group by (?m) order by (?m)
    template: .circleci/Unit_memberOf.rdf.template
    output: RDFs/Unit_memberOf.rdf
```

Write template file like a following.

```
<rdf:RDF
xmlns:schema="http://schema.org/"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
xmlns:imas="https://sparql.crssnky.xyz/imasrdf/URIs/imas-schema.ttl#"
>[# th:each="idol : ${rs}"]
  <rdf:Description rdf:about="[(${idol.m.substring(params.baseUri.length()+5)})]">[# th:each="item : ${idol.u.split(',,,,,')}"]
    <schema:memberOf rdf:resource="[(${item})]"/>
[/]  </rdf:Description>
[/]</rdf:RDF>
```

And generate rdf file like a following.

```
<rdf:RDF
xmlns:schema="http://schema.org/"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
xmlns:imas="https://sparql.crssnky.xyz/imasrdf/URIs/imas-schema.ttl#"
>
  <rdf:Description rdf:about="detail/Abe_Nana">
    <schema:memberOf rdf:resource="https://sparql.crssnky.xyz/imasrdf/RDFs/detail/%E8%99%B9%E8%89%B2%E3%83%89%E3%83%AA%E3%83%BC%E3%83%9E%E3%83%BC"/>
    <schema:memberOf rdf:resource="https://sparql.crssnky.xyz/imasrdf/RDFs/detail/%E3%82%AF%E3%83%AC%E3%82%A4%E3%83%B3%EF%BC%86%E3%83%90%E3%83%8B%E3%83%BC"/>
....
```


## Interactive Mode

Run interactive mode with ``-i`` parameter.

```
$ java -jar rdflint-0.1.5.jar -i -targetdir example/dataset -config example/dataset/rdflint-config.yml
```

And try to query. Double return to perform query, type ``:exit`` to exit interactive mode.

```
sparql > PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
> SELECT ?s
> WHERE {
>   ?s rdf:type <https://sparql.crssnky.xyz/imasrdf/URIs/imas-schema.ttl#Idol> .
> }
>
---------------------------------------------------------------------
| s                                                                 |
=====================================================================
| <https://sparql.crssnky.xyz/imasrdf/RDFs/detail/Hagiwara_Yukiho>  |
| <https://sparql.crssnky.xyz/imasrdf/RDFs/detail/Miura_Azusa>      |
| <https://sparql.crssnky.xyz/imasrdf/RDFs/detail/Takatsuki_Yayoi>  |
........
```

## Contributing

Please fork this repository, and create pull request for contributing.

### Build and execute

Build rdflint with gradle.

```
$ gradle shadowJar
```

Run rdflint.

```
$ java -jar build/libs/rdflint.jar -targetdir example/dataset
```

### IDE Settings

Use [dotidea](https://github.com/imas/rdflint/tree/dotidea) - Project Settings for IntelliJ IDEA.

## License

rdflint is released under the [MIT License](https://github.com/imas/rdflint/blob/master/LICENSE).
