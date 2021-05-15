# Rules

[Home](index.md) |
[Setup](setup.md) |
[Usage](usage.md) |
[Configuration](config.md) |
[Rules](rules.md) |
[Development](developer.md)

About validation rules of rdflint.

Test data can be cited for your study.
[https://github.com/imas/rdflint/tree/master/src/test/resources/testValidatorsImpl](https://github.com/imas/rdflint/tree/master/src/test/resources/testValidatorsImpl)


## Syntax validation

rdfやturtleファイルの文法が正しいかを検証します。

## Undefined subject check

未定義の主語が、述語・目的語として使用されていないかを検証します。

以下の主語がチェックの対象になります。

- 設定ファイル``baseUri``で指定された階層以下の主語
- 以下で定義されている主語
   - http://www.w3.org/1999/02/22-rdf-syntax-ns#
   - http://www.w3.org/2000/01/rdf-schema#
   - http://www.w3.org/ns/shacl#
   - http://schema.org/
   - http://xmlns.com/foaf/0.1/
   - http://purl.org/dc/elements/1.1/

ｰ 設定ファイルで指定したデータセットの主語

## Custom check

SPARQLを利用したカスタムクエリによるデータ整合を検証します。

検証ルールは、設定ファイル``rule``で指定します。  
検証は以下の流れで実行されます。

1. ``target``の指定から、対象ファイルであることを確認する  
   対象外であれば終了する
2. ``query``で指定されたSPARQLクエリを実行する
3. 2の実行結果を、変数``rs``に設定する  
   ``valid``で指定されたgroovyスクリプトを実行する  
   groovyスクリプトの``log``のwarnメソッドで、検証NG時のエラーを出力できる

## Degrade validation

削除された、SubjectやTripleが無いかを確認して情報を出力します。

比較対象(変更前)の対象ディレクトリを、設定ファイル``originDir``で指定します。

## Datatype validation

述語に対するリテラルのデータ型の妥当性を検証します。

検証は以下の流れで実行されます。

1. 同じ述語のリテラルの値全てが、文字列、有理数、整数、自然数のいずれかを判定する  
   データの95%以上が含まれる型を、その述語に対する妥当なデータ型とする
2. 各トリプルに対して、1で求めたデータ型に含まれるかを判定する  
   含まれない場合は、検証NGとする

同じ述語に対して、20件以上リテラルが存在しない場合は検証対象になりません。

## Outlier validation

数値型データに対して、他のデータと値が離れているデータが無いかを検証します。

検証は以下の流れで実行されます。

1. 各データ間の距離を測る
2. 距離が最も近いデータ対から順にデータを結合してクラスタとして扱う
3. 2を繰り返し、最後に残ったクラスタにデータが1件しか含まれていない場合に、検証NGとする

最後に結合したクラスタ間距離が、その一つ前に結合したクラスタ間距離の3倍以内の場合は検証OKとします。

## SHACL constraint validation

Shapes Constraint Language (SHACL)によって指定された制約に違反していないかを検証します。

Shapes Constraint Language (SHACL) | W3C  
[https://www.w3.org/TR/shacl/](https://www.w3.org/TR/shacl/)

## Literal trim validation

リテラルの先頭・末尾に半角スペースが存在するデータを検証NGとします。

## File encoding validation

ファイルが、設定ファイルで指定した文字コード・改行コードで記載されているかを検証します。指定外の文字コード・改行コードで記載されている場合に検証NGとします。

{{site.cookie_consent}}
