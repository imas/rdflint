# Configuration

[Home](index.md) |
[Setup](setup.md) |
[Usage](usage.md) |
[Configuration](config.md) |
[Rules](rules.md) |
[Development](developer.md)

About rdflint configuration file.

## Structure of rdflint configuration file

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

## targetDir: 対象ディレクトリの指定

検証対象のディレクトリのパスを指定します。

## originDir: デグレード検証時の比較対象ディレクトリの指定

デグレード検証時の比較対象のディレクトリのパスを指定します。

## baseUri: 対象データセットURIのベース階層

対象データセットURIのベース階層を指定します。

## suppressPath: 無視する警告の定義ファイルのパス

無視する警告を定義したファイルのパスを指定します。

## rules: カスタムクエリ検証のルール指定

カスタムクエリ検証のルールを指定します。

rule配下に、以下のkey-valueを持つマップのリストを指定します。

- name: ルール名
- target: 対象とするファイルのパス
- query: 実行するSPARQLクエリ
- valid: queryの結果を処理するgroovyスクリプト

## validation - fileEncoding: 文字改行コード検証のルール指定

文字改行コード検証のルールを指定します。

validation-fileEncoding配下に、以下のkey-valueを持つマップのリストを指定します。

- target: 対象ファイル名
- charset: 文字コード
- end_of_line: 改行コード
- indent_style: インデント文字
- indent_size: インデントサイズ
- insert_final_newline: ファイル末尾改行の要否
- trim_trailing_whitespace: 行末空白削除の要否

## validation - undefinedSubject: 未定義主語の使用検証のルール指定

未定義主語の使用検証のルール指定を指定します。

validation-undefinedSubject配下に、以下のkey-valueを持つマップのリストを指定します。  
この指定によって、未定義主語の使用検証に使用するデータセットを追加することが出来ます。

- url: データセットを定義するファイルの格納先URL
- startswith: 対象データセットのURL,  
   ここで指定した文字列で始まる(前方一致する)URLはurlで指定したファイルに定義されている必要があります
- langtype: urlで指定したファイルの形式, turtle または rdfxml

## generation: RDFファイル生成の設定

RDFファイル生成のルールを指定します。

generation配下に、以下のkey-valueを持つマップのリストを指定します。

- query: 実行するSPARQLクエリ
- template: クエリ結果を流し込むthymeleafテンプレートのパス
- output: 出力ファイルのパス

{{site.cookie_consent}}
