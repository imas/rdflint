rdflint
---

[![](https://jitpack.io/v/imas/rdflint.svg)](https://jitpack.io/#imas/rdflint)

RDF linter

# How to use

Download from jitpack and run.

```
$ wget https://jitpack.io/com/github/imas/rdflint/0.0.3/rdflint-0.0.3-all.jar
$ java -jar rdflint-0.0.3-all.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml
```

# Build and execute

Build rdflint with gradle.

```
$ gradle shadowJar
```

Run rdflint.

```
$ java -jar build/libs/rdflint-all.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml
```
