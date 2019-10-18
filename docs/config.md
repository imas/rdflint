# 設定ファイル

[ホーム](index.md) |
[セットアップ](setup.md) |
[使い方](usage.md) |
[設定ファイル](config.md) |
[検証ルール](rules.md) |
[開発者向け](developer.md)

rdflintの設定ファイルの記載方法を説明します。

## 設定ファイルの全体階層

- targetDir
- originDir
- baseUri
- rules
   - リスト
      - name
      - query
      - target
      - valid
- generation
   - リスト
      - query
      - input
      - template
      - output
- validation
   - fileEncoding
      - リスト
         - target
         - charset
         - end_of_line
         - indent_style
         - indent_size
         - insert_final_newline
         - trim_trailing_whitespace

設定ファイルの例

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

## baseUri: 対象リソースURIのベース階層

対象リソースURIのベース階層を指定します。

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

## generation: RDFファイル生成の設定

RDFファイル生成のルールを指定します。

generation配下に、以下のkey-valueを持つマップのリストを指定します。

- query: 実行するSPARQLクエリ
- template: クエリ結果を流し込むthymeleafテンプレートのパス
- output: 出力ファイルのパス

{{site.cookie_consent}}
