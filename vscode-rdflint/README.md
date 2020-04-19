# vscode-rdflint

[![Marketplace Version](https://vsmarketplacebadge.apphb.com/version/takemikami.vscode-rdflint.svg "Current Release")](https://marketplace.visualstudio.com/items?itemName=takemikami.vscode-rdflint)

Provides RDF language support via [rdflint](https://github.com/imas/rdflint).

## Quick Start

- Install JDK or JRE, and setup JAVA_HOME environment variable.
- Start Visual Studio Code.
- Install this extension.
- Shift+Cmd+P and select `rdflint Interactive Mode: SPARQL playground`.
   - You can find the command, with type substring like 'rdflint', 'SPARQL', etc.

### How to enable Rdflint Language Server

- Open Settings, and check 'Extentions > Rdflint Language Server > Enable rdflint language server'
- Shift+Cmd+P and type `Reload Window`

## Features

- rdflint validations.
- SPARQL playground. (rdflint Interactive Mode).

## Requirements

- Install JDK or JRE, and setup JAVA_HOME environment variable.

## Available commands

- `rdflint Interactive Mode: SPARQL playground` - start rdflint Interactive Mode.

## Contributing

Please fork this repository, and create pull request for contributing.

### Build and execute

Install npm libraries.

```
npm install
```

Open this folder on Visual Studio Code, and Hit F5 Key to execute.

In the case of try with Developing rdflint,  
build jar and set environment variable ``RDFLINT_JAR`` like the following.

```
cd ../
gradle shadowJar
export RDFLINT_JAR=`pwd`/build/libs/rdflint.jar
code vscode-rdflint/
```

## License

vscode-rdflint is released under the MIT License.
