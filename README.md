rdflint
---

[![](https://jitpack.io/v/imas/rdflint.svg)](https://jitpack.io/#imas/rdflint)

## What's rdflint

Rdflint is a linter for RDF files. Easy to check syntax and other things. rdflint is powered by Apache Jena.

Rdflint has following functions.

- Syntax check of rdf and Turtle(ttl).
- Undefined subject check when use as predicate or object.
- Custom check by SPARQL query.
- Degrade validation.
- RDF generation by SPARQL query results.
- SPARQL playground on your PC. (Interractive Mode)

## Getting Started

### Work on your PC

Download from jitpack.

```
$ wget https://jitpack.io/com/github/imas/rdflint/0.0.6/rdflint-0.0.6-all.jar
```

Run for apply to your RDF files.

```
$ java -jar rdflint-0.0.6-all.jar -targetdir example/dataset
```

``-targetdir`` parameter is location of target RDF files.

### Work on CircleCI

Make ``.circleci/config.yml`` to your repository. Its contents is following.

```
version: 2
jobs:
  build:
    docker:
    - image: circleci/openjdk:8
    working_directory: ~/repo
    steps:
    - checkout
    - run:
        name: run rdflint
        command: |
          RDFLINT_VERSION=0.0.6
          wget https://jitpack.io/com/github/imas/rdflint/$RDFLINT_VERSION/rdflint-$RDFLINT_VERSION-all.jar
          java -jar rdflint-$RDFLINT_VERSION-all.jar
```

## Configurations

When create a configuration file of rdflint, rdflint provide additional checks. Run with ``-config`` parameter like following.

```
$ java -jar rdflint-0.0.6-all.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml
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
$ java -jar rdflint-0.0.6-all.jar -targetdir example/dataset -origindir example/dataset_origin -config example/dataset/rdflint-config.yml
```

And check subject and triple, removed from origindir. Its problem report as INFO level.


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
$ java -jar rdflint-0.0.6-all.jar -i -targetdir example/dataset -config example/dataset/rdflint-config.yml
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
$ java -jar build/libs/rdflint.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml
```

## License

rdflint is released under the MIT License.
