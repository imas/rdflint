`rdflint` is easy to install with install script. The minimum required version of Java is 11.

## Installation

Paste following script in a Linux shell prompt, and install `rdflint` to your machine.

```sh
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/imas/rdflint/HEAD/install/install.sh)"
```

You can check installed version of `rdflint` with following command.

```sh
rdflint -v
```

## Basic usage

`rdflint` basic usage process to rdf file syntax check.

Prepare target RDF file. rdflint can check files that have extensiont of ``ttl`` or ``rdf``. In this part, use empty file.

file name: target.rdf

```
```

Make rdflint configuration file. In this part, make following file.

file name: rdflint-config.yml

```
baseUri: https://example.com/targetrdf/
```

*Set base directory of target dataset URI to baseUri.*

Execute rdflint. Move to directory of `target.rdf`, `rdflint-config.yml`, And run following command.

```
rdflint -config rdflint-config.yml
```

Validation failed and display following, because rdf file is invalid.

```
target.rdf
  ERROR  [line: 1, col: 1 ] 途中でファイルの末尾に達しました。
```

## Uninstallation

Paste following script in a Linux shell prompt, and remove `rdflint` from your machine.

```sh
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/imas/rdflint/HEAD/install/uninstall.sh)"
```
