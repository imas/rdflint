# Usage

[Home](index.md) |
[Setup](setup.md) |
[Usage](usage.md) |
[Configuration](config.md) |
[Rules](rules.md) |
[Development](developer.md)

rdflint basic usage.

## rdflint basic usage

rdflint basic usage process to rdf file syntax check.

Note. you need to complete [Setup](setup.md) process.

1. Prepare target RDF file. rdflint can check files that have extensiont of ``ttl`` or ``rdf``.   
   In this part, use empty file.

   file name: target.rdf

   ```
   ```

2. Make rdflint configuration file.  
   In this part, make following file.

   file name: rdflint-config.yml

   ```
   baseUri: https://example.com/targetrdf/
   ```

   Set base directory of target dataset URI to baseUri.

3. Execute rdflint.  
   Move to directory of ``target.rdf``, ``rdflint-config.yml``, and ``rdflint-{{site.RDFLINT_VERSION}}.jar``. And run following command.

   ```
   $ java -jar rdflint-{{site.RDFLINT_VERSION}}.jar -config rdflint-config.yml
   ```

   Validation failed and display following, because rdf file is invalid.

   ```
   target.rdf
     ERROR  [line: 1, col: 1 ] 途中でファイルの末尾に達しました。
   ```

## SPARQL playground

You can try SPARQL query to your data with rdflint.

Note. you need to complete 'rdflint basic usage' process.

1. Make target data file.  
   In this part, Make following rdf file.

   file name: target.rdf

   ```
   <?xml version="1.0"?>
   <rdf:RDF
     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     xmlns:schema="http://schema.org/">
     <rdf:Description rdf:about="somenode">
       <schema:name xml:lang="ja">name of something</schema:name>
       <rdf:type rdf:resource="http://schema.org/Thing"/>
     </rdf:Description>
   </rdf:RDF>
   ```

2. Run rdflint with interactive mode.  
   Move to directory of ``target.rdf``, ``rdflint-config.yml``, and ``rdflint-{{site.RDFLINT_VERSION}}.jar``. And run following command.

   ```
   $ java -jar rdflint-{{site.RDFLINT_VERSION}}.jar -config rdflint-config.yml -i
   ```

   ``-i`` is interactive mode option.

3. Input SPARQL query after ``SPARQL>`` prompt.  
   In this part, input following query. Double return to perform query.

   ```
   PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
   PREFIX schema: <http://schema.org/>
   select ?s
   where {?s rdf:type schema:Thing. }
   ```

   After perform query, view following result.

   ```
   SPARQL> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
   > PREFIX schema: <http://schema.org/>
   > select ?s
   > where {?s rdf:type schema:Thing. }
   >
   --------------------------------------------
   | s                                        |
   ============================================
   | <https://example.com/targetrdf/somenode> |
   --------------------------------------------
   ```

4. If you like terminate interactive mode, input ``:exit``.


## Execute on CI

You can use rdflint with GitHub Actions.

1. Make rdflint configuration file.  
   In this part, Make following file.

   file name: .rdflint/rdflint-config.yml

   ```
   baseUri: https://example.com/targetrdf/
   ```

   Set base directory of target dataset URI to baseUri.

2. Make GitHub Actions Configuration file.  
   In this part, Make following file.

   file name: .github/workflows/ci.yml

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

3. Add 2 configuration files to git repository, and push GitHub.  
   Enable GitHub Actions, execute rdflint when create or update pull request.

## Suppress warnings

In the case of suppress rdflint warnings, you can set mute warning to rdflint.

Note. you need to complete 'rdflint basic usage' process.

1. Update target data files, and validation result has no warning except you like to suppress.

2. Run rdflint.

   ```
   $ java -jar rdflint-{{site.RDFLINT_VERSION}}.jar -config rdflint-config.yml
   ```

   Check result has no warning except you like to suppress.  
   In this time, write warnings to `rdflint-problems.yml`.

3. copy `rdflint-problems.yml` to `rdflint-suppress.yml`.

   ```
   $ cp rdflint-problems.yml rdflint-suppress.yml
   ```

4. Run rdflint, again.  
   Check result has no warning.  
   Note. In the case of warning location cannot identify, suppress target not included. (Custom check etc)  

Note. If you like check suppressed warnings again, remove `rdflint-suppress.yml`.

## Command line options

rdflint has following command line options.

- baseuri: Base directory of target dataset URI.
- targetdir: Target directory path.
  Default is current directory.
- suppress: Suppress configuration file path.
- origindir: Base directory of degrade validation.
- config: Configuration file path.
- i: Interactive mode.
  Default is batch mode.
- ls: Language Server mode. Note. experimental function

{{site.cookie_consent}}
