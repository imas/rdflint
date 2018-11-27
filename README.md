rdflint
---

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/47d0f457ec0845c89bcd12d9b7eb8165)](https://www.codacy.com/app/takemikami/rdflint?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=takemikami/rdflint&amp;utm_campaign=Badge_Grade)
[![](https://jitpack.io/v/takemikami/rdflint.svg)](https://jitpack.io/#takemikami/rdflint)

RDF linter

# How to use

Download from jitpack and run.

```
$ wget https://jitpack.io/com/github/takemikami/rdflint/0.0.1/rdflint-0.0.1-all.jar
$ java -jar rdflint-0.0.1-all.jar -baseuri https://sparql.crssnky.xyz/imasrdf/ -targetdir example/dataset
```

# Build and execute

Build rdflint with gradle.

```
$ gradle shadowJar
```

Run rdflint.

```
$ java -jar build/libs/rdflint-all.jar -baseuri https://sparql.crssnky.xyz/imasrdf/ -targetdir example/dataset
```
