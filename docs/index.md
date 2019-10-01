# ホーム

[ホーム](index.md) |
[セットアップ](setup.md) |
[使い方](usage.md) |
[設定ファイル](config.md) |
[検証ルール](rules.md) |
[開発者向け](developer.md)

*rdflintは、オープンデータを支えるRDFデータをチェックするツールです。データを作成する過程で発生する、文法誤りなどの問題を自動・簡単にチェックする事が出来ます。*

rdflintには、以下のようなチェック機能があります。
- rdf, turtleファイルの文法チェック
- 未定義の主語が、述語・目的語として使用されていないかのチェック
- SPARQLを利用したカスタムクエリによるデータ整合チェック
- SubjectやTripleが誤って削除されていないかのチェック
- 述語に対するデータ型の妥当性・外れ値がないかのチェック
- SHACLによるデータ制約に違反がないかのチェック
- リテラル前後の不要な半角スペースがないかのチェック
- ファイルの文字コード・改行コードが指定された形式かをチェック

チェック機能以外にも、以下のような機能があります。
- SPARQLクエリの実行結果からのRDFファイル生成
- ローカルPCでの試験的にSPARQLクエリ実行

## 紹介資料、解説資料

rdflintの紹介資料や解説資料、記事を紹介します。

- RDFのチェックツール「rdflint」とコミュニティによるオープンデータの作成 | slideshare  
  [https://www.slideshare.net/takemikami/rdfrdflint-153693907](https://www.slideshare.net/takemikami/rdfrdflint-153693907)
- RDFチェックツール「rdflint」のご紹介 | slideshare  
  [https://www.slideshare.net/takemikami/rdfrdflint](https://www.slideshare.net/takemikami/rdfrdflint)
- im@sparqlにContributeしやすくするためにRDFファイルのチェックツールを作った | takemikami.com  
  [https://takemikami.com/2018/12/19/imsparqlContributeRDF.html](https://takemikami.com/2018/12/19/imsparqlContributeRDF.html)

## 利用実績

rdflintを導入しているプロジェクトを紹介します。

- im@sparql  
  [https://sparql.crssnky.xyz/imas/](https://sparql.crssnky.xyz/imas/)
- PrismDB  
  [https://prismdb.takanakahiko.me/](https://prismdb.takanakahiko.me/)
- Cashless_Sparql  
  [https://github.com/crssnky/Cashless_Sparql](https://github.com/crssnky/Cashless_Sparql)
