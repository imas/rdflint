You can specify options of `rdflint` with configuration file.

## Configuration locations

`rdflint` supports storing its configuration in your project in one of following.

- rdflint-config.yml
- .rdflint-config.yml
- .rdflint/rdflint-config.yml
- config/rdflint/rdflint-config.yml
- .circleci/rdflint-config.yml

## Structure of configuration file

- targetDir
- originDir
- baseUri
- suppressPath
- rules
    - (List)
        - name
        - query
        - target
        - valid
- generation
    - (List)
        - query
        - input
        - template
        - output
- validation
    - undefinedSubject
        - (List)
            - url
            - startswith
            - langtype
    - fileEncoding
        - (List)
            - target
            - charset
            - end_of_line
            - indent_style
            - indent_size
            - insert_final_newline
            - trim_trailing_whitespace


Example

```
baseUri: https://sparql.crssnky.xyz/imasrdf/
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
generation:
- query: |
    PREFIX schema: <http://schema.org/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX imas: <https://sparql.crssnky.xyz/imasrdf/URIs/imas-schema.ttl#>
    SELECT  ?m (group_concat(?s;separator=",,,,,")as ?u)
    WHERE {
      ?s rdf:type imas:Unit;
         schema:member ?m.
    } group by (?m) order by (?m)
  template: .circleci/Unit_memberOf.rdf.template
  output: RDFs/Unit_memberOf.rdf
```

## Core

- targetDir: Path of validation target directory.
- originDir: Path of before revision that used by degrade validation.
- baseUri: URI base path of target dataset.
- suppressPath: Path of suppress configuration file.

## Rules

Rules for custom check.

Put map-list that have following key-value, under rules.

- name: Name of rule.
- target: Path of target file.
- query: SPARQL query.
- valid: Groovy script that validate result set of query.

## Validation

### File encoding validation

Rules for file encoding validation.

Put map-list that have following key-value, under validation-fileEncoding.

- target: Path of target file.
- charset: Character set.
- end_of_line: Newline character.
- indent_style: Indent character.
- indent_size: Indent size.
- insert_final_newline: Requirement of newline at end-of-file.
- trim_trailing_whitespace: Requirement of trim of end-of-line.

### Undefined subject check

Rules for undefined subject check.

Put map-list that have following key-value, under validation-undefinedSubject. You can add dataset that used by Undefined subject check.

- url: Hosted URL of target dataset.
- startswith: URL of target dataset.
- langtype: File type of dataset: turtle or rdfxml.

## Generation

Rule for rdf generation.

Put map-list that have following key-value, under generation.

- query: SPARQL query.
- template: Path of thymeleaf template that will be inserted result set of query.
- output: Path of output.
