`rdflint` is open source software that is released under MIT License. You can contribute `rdflint`.

## Reporting

Please make issue on GitHub.

## Development

Please fork this repository, and create pull request about new features and bugfixes.

### Build and execute

Build `rdflint` with gradle.

```sh
./gradlew shadowJar
```

Run rdflint.

```sh
java -jar build/libs/rdflint.jar -targetdir example/dataset
```

### IDE Settings

Use [dotidea](https://github.com/imas/rdflint/tree/dotidea) - Project Settings for IntelliJ IDEA.

### Docs

Install mkdocs, and serve.

```sh
pip install mkdocs mkdocs-material
mkdocs serve
```
