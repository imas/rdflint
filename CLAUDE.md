# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Full build with tests and code quality checks (Checkstyle, PMD, SpotBugs, JaCoCo)
./gradlew build

# Build fat JAR only (faster, skips checks)
./gradlew shadowJar

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.github.imas.rdflint.RdfLintTest"

# Run a single test method
./gradlew test --tests "com.github.imas.rdflint.RdfLintTest.testExample"
```

## Running the Application

```bash
# Basic syntax check
java -jar build/libs/rdflint.jar -targetdir example/dataset

# With configuration file
java -jar build/libs/rdflint.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml

# With degrade validation (compare against original dataset)
java -jar build/libs/rdflint.jar -targetdir example/dataset -origindir example/dataset_origin -config example/dataset/rdflint-config.yml

# Interactive SPARQL mode
java -jar build/libs/rdflint.jar -i -targetdir example/dataset

# Language Server mode (experimental)
java -jar build/libs/rdflint.jar -ls
```

## Architecture

rdflint is an RDF file linter built on Apache Jena. The codebase follows a plugin-based validator architecture.

### Entry Point and Modes
`RdfLint.java` is the main entry point, supporting three modes:
- **CLI mode** (default): Runs validation and outputs problems
- **Interactive mode** (`-i`): SPARQL REPL with commands (`:check`, `:lint`, `:reload`, `:exit`)
- **Language Server mode** (`-ls`): LSP server for IDE integration

### Core Components
- **ValidationRunner**: Orchestrates the validation pipeline - discovers validators, walks files, runs validation phases
- **GenerationRunner**: Executes SPARQL queries and applies Thymeleaf templates to generate RDF files
- **DatasetLoader**: Loads RDF/Turtle files into Jena Graph/Model
- **ConfigurationLoader**: Loads YAML configuration from standard locations (`.rdflint-config.yml`)
- **LintProblemSet/LintProblem**: Collects and formats validation issues (supports console, YAML, GitHub Actions output)

### Validator Plugin System
Validators implement `RdfValidator` interface (extend `AbstractRdfValidator`). They are auto-discovered at runtime via Reflections library from the `validator/impl/` package.

**Validator lifecycle methods:**
1. `setParameters()` - Receive configuration
2. `validateFile()` - File-level checks (before parsing)
3. `prepareValidationResource()` - Build data structures from full triple set
4. `validateTriple()` - Per-triple validation (called during parsing)
5. `validateTripleSet()` - Batch validation after parsing
6. `close()` - Cleanup

**Built-in validators:** `RdfSyntaxValidator`, `FileEncodingValidator`, `UndefinedSubjectValidator`, `DataTypeValidator`, `TrimValidator`, `ShaclValidator`, `CustomQueryValidator`, `DegradeValidator`

### Custom Rules
Custom validation rules are defined in YAML config using SPARQL queries + Groovy scripts. See `CustomQueryValidator` and `CustomRule` config class.

## Code Quality

- **Minimum test coverage**: 70% (enforced by JaCoCo)
- **Static analysis**: Checkstyle, PMD, SpotBugs all run during `./gradlew build`
- **Java version**: 11
