`rdflint` report along following validation rules.

Test data can be cited for your study.
[https://github.com/imas/rdflint/tree/master/src/test/resources/testValidatorsImpl](https://github.com/imas/rdflint/tree/master/src/test/resources/testValidatorsImpl)

## Syntax validation

Validate syntax of rdfxml or turtle.

## Undefined subject check

Validate undefined subject are not used as predicate or object.

Validation target is following subjects.

- Defined under `baseUri` of configuration.
- Defined in following.
    - http://www.w3.org/1999/02/22-rdf-syntax-ns#
    - http://www.w3.org/2000/01/rdf-schema#
    - http://www.w3.org/ns/shacl#
    - http://schema.org/
    - http://xmlns.com/foaf/0.1/
    - http://purl.org/dc/elements/1.1/
- Defined in dataset of `validation-undefinedSubject` in configuration.

## Custom check

Validate result-set of custom SPARQL Query.

Custom check defined in `rule` in configuration.  
Validation in following process each rdf file.

1. Check that file is target, along ``target`` in configuration. Exit if not target.
2. Execute query of ``query`` in configuration, and set results to variable `rs`.
3. Execute groovy script of ``valid`` in configuration. Script can Output validation error by ``log.warn`` method.

## Degrade validation

Output subjects and triples that removed from before revision.

Directory of before revision defined in `originDir` in configuration.

## Datatype validation

Validate literal object data-type corresponded to predicate.

Validation in following process.

1. Count up literal object each predicate. Exit if under 20 literal objects.
2. Expect object data-type each predicate. Candidate data-type is String, real number, integer number or natural number. Data-type that 95% over objects have is expected type.
3. Validate object data-type is expected type each triple.

## Outlier validation

Validate outlier to numeric object.

Validation in following process.

1. Calculate distance each object.
2. Join the nearest object-pair as cluster.
3. Repeat process 2 until last 2 cluster, validate last cluster have one data only. If the distance of last joined cluster is triple distance of joined before.

## SHACL constraint validation

Validate constraint by SHACL (Shapes Constraint Language).

Shapes Constraint Language (SHACL) | W3C  
[https://www.w3.org/TR/shacl/](https://www.w3.org/TR/shacl/)

## Literal trim validation

Validate literal starts/ends with space.

## File encoding validation

Validate newline character and encodings with `validation-fileEncoding` in configuration.
