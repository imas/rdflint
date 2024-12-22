When create a configuration file of `rdflint`, `rdflint` provide additional checks. Run with ``-config`` parameter like following.

```sh
$ rdflint -targetdir example/dataset -config example/dataset/rdflint-config.yml
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

### Degrade validation

Run with ``-origindir`` parameter like following.

```sh
$ rdflint -targetdir example/dataset -origindir example/dataset_origin -config example/dataset/rdflint-config.yml
```

And check subject and triple, removed from origindir. Its problem report as INFO level.


### SHACL constraint validation

write your SHACL constraint in `.ttl` or `.rdf` file.
and put this file in directory which specified by `-targetdir`.


### RDF generation

Write configuration file like a following.

```
baseUri: https://sparql.crssnky.xyz/imasrdf/
generation:
  - query: |
      PREFIX schema: <http://schema.org/>
      PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      PREFIX imas: <https://sparql.crssnky.xyz/imasrdf/URIs/imas-schema.ttl#>
      SELECT  ?m (group_concat(?s;separator=",,,,,")as ?u)
      WHERE {
        ?s rdf:type imas:Unit;
           schema:member ?m.
      }group by (?m) order by (?m)
    template: .circleci/Unit_memberOf.rdf.template
    output: RDFs/Unit_memberOf.rdf
```

Write template file like a following.

```
<rdf:RDF
xmlns:schema="http://schema.org/"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
xmlns:imas="https://sparql.crssnky.xyz/imasrdf/URIs/imas-schema.ttl#"
>[# th:each="idol : ${rs}"]
  <rdf:Description rdf:about="[(${idol.m.substring(params.baseUri.length()+5)})]">[# th:each="item : ${idol.u.split(',,,,,')}"]
    <schema:memberOf rdf:resource="[(${item})]"/>
[/]  </rdf:Description>
[/]</rdf:RDF>
```

And generate rdf file like a following.

```
<rdf:RDF
xmlns:schema="http://schema.org/"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
xmlns:imas="https://sparql.crssnky.xyz/imasrdf/URIs/imas-schema.ttl#"
>
  <rdf:Description rdf:about="detail/Abe_Nana">
    <schema:memberOf rdf:resource="https://sparql.crssnky.xyz/imasrdf/RDFs/detail/%E8%99%B9%E8%89%B2%E3%83%89%E3%83%AA%E3%83%BC%E3%83%9E%E3%83%BC"/>
    <schema:memberOf rdf:resource="https://sparql.crssnky.xyz/imasrdf/RDFs/detail/%E3%82%AF%E3%83%AC%E3%82%A4%E3%83%B3%EF%BC%86%E3%83%90%E3%83%8B%E3%83%BC"/>
....
```

## Interactive mode

You can try SPARQL query to your data with `rdflint`.

Make target data file. In this part, Make following rdf file.

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

Run `rdflint` with interactive mode. Move to directory of ``target.rdf``, ``rdflint-config.yml``, and ``rdflint-{{site.RDFLINT_VERSION}}.jar``. And run following command.

```sh
$ rdflint -config rdflint-config.yml -i
```

``-i`` is interactive mode option.

Input SPARQL query after ``SPARQL>`` prompt. In this part, input following query. Double return to perform query.

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

If you like terminate interactive mode, input ``:exit``.

## Suppress warnings

In the case of suppress `rdflint` warnings, you can set mute warning to `rdflint`.

Update target data files, and validation result has no warning except you like to suppress.

Run rdflint.

```sh
$ rdflint -config rdflint-config.yml
```

Check result has no warning except you like to suppress.  
In this time, write warnings to `rdflint-problems.yml`.

copy `rdflint-problems.yml` to `rdflint-suppress.yml`.

```sh
$ cp rdflint-problems.yml rdflint-suppress.yml
```

Run rdflint, again. Check result has no warning.  
Note. In the case of warning location cannot identify, suppress target not included. (Custom check etc)

Note. If you like check suppressed warnings again, remove `rdflint-suppress.yml`.

## Command line options

rdflint has the following command line options.

- baseuri: Base directory of target dataset URI.
- targetdir: Target directory path. Default is current directory.
- suppress: Suppress configuration file path.
- origindir: Base directory of degrade validation.
- config: Configuration file path.
- i: Interactive mode. Default is batch mode.
- ls: Language Server mode. (experimental function)

