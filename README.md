rdflint
---

[![](https://jitpack.io/v/imas/rdflint.svg)](https://jitpack.io/#imas/rdflint)

## What's rdflint

Rdflint is a linter for RDF files. Easy to check syntax and other things. rdflint is powered by Apache Jena.

Rdflint has following functions.

- Syntax check of rdf and Turtle(ttl).
- Undefined subject check when use as predicate or object.
- Custom check by SPARQL query.

## Getting Started

### Work on your PC

Download from jitpack.

```
$ wget https://jitpack.io/com/github/imas/rdflint/0.0.3/rdflint-0.0.3-all.jar
```

Run for apply to your RDF files.

```
$ java -jar rdflint-0.0.3-all.jar -targetdir example/dataset
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
          RDFLINT_VERSION=0.0.3
          wget https://jitpack.io/com/github/imas/rdflint/$RDFLINT_VERSION/rdflint-$RDFLINT_VERSION-all.jar
          java -jar rdflint-$RDFLINT_VERSION-all.jar
```

## Configurations

When create a configuration file of rdflint, rdflint provide additional checks. Run with ``-config`` parameter like following.

```
$ java -jar rdflint-0.0.3-all.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml
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


## Interactive Mode

Run interactive mode with ``-i`` parameter.

```
$ java -jar rdflint-0.0.3-all.jar -i -targetdir example/dataset -config example/dataset/rdflint-config.yml
```

And try to query. Double return to perform query, type ``exit;`` to exit interactive mode.

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
$ java -jar build/libs/rdflint-all.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml
```

## License

rdflint is released under the MIT License.
