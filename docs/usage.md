# 使い方

[ホーム](index.md) |
[セットアップ](setup.md) |
[使い方](usage.md) |
[設定ファイル](config.md) |
[検証ルール](rules.md) |
[開発者向け](developer.md)

rdflintの基本的な使い方を紹介します。

## rdflintの実行手順

rdflintの基本的な実行手順を説明します。ここでは、rdfファイルの文法チェックを実行手順を示します。

事前に[セットアップ](setup.md)の手順を完了させておいて下さい。

1. チェックしたい対象ファイルを用意します。rdflintでは、拡張子がttlとrdfのファイルを対象にチェックを行います。   
   ここでは、空のrdfファイルを作成します。

   target.rdf

   ```
   ```

2. rdflintの設定ファイルを作成します。   
   ここでは、以下のファイルを作成します。

   ファイル名: rdflint-config.yml

   ```
   baseUri: https://example.com/targetrdf/
   ```

   baseUriには、対象リソースURIのベース階層を指定します。

3. rdflintを実行します。   
   ここでは、``target.rdf``と``rdflint-config.yml``、及び``rdflint-0.0.8-all.jar``を置いたディレクトリに移動して、次のコマンドを実行します。

   ```
   $ java -jar rdflint-0.0.8-all.jar -config rdflint-config.yml
   ```

   rdfファイルが正しく無いため、検証に失敗し、次のように表示されます。

   ```
   target.rdf
     ERROR  [line: 1, col: 1 ] 途中でファイルの末尾に達しました。
   ```

## クエリ実行環境の利用

rdflintでSAPRQLクエリのテスト実行を行う手順を説明します。

事前に前項「rdflintの実行手順」を完了させておいて下さい。

1. 対象データを定義したファイルを用意します。   
   ここでは、次のようなrdfファイルを作成します。

   target.rdf

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

2. インタラクティブモードでrdflintを実行します。   
   ここでは、``target.rdf``と``rdflint-config.yml``、及び``rdflint-0.0.8-all.jar``を置いたディレクトリに移動して、次のコマンドを実行します。

   ```
   $ java -jar rdflint-0.0.8-all.jar -config rdflint-config.yml -i
   ```

   ``-i``がインタラクティブモードで実行するオプションです。

3. ``SPARQL>`` プロンプトが表示され、入力待ちになるのでSPARQLクエリを入力して実行します。   
   ここでは、以下のクエリを入力します。クエリは改行を２回連続で入力すると確定します。

   ```
   PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
   PREFIX schema: <http://schema.org/>
   select ?s
   where {?s rdf:type schema:Thing. }
   ```

   クエリを実行すると、以下のように実行結果が表示されます。

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

4. インタラクティブモードを終了する時は、プロンプトで``:exit``と入力します。


## CIからの実行

CircleCIでの設定方法を例に、CIでrdflintを実行する手順を説明します。

1. rdflintの設定ファイルを作成します。   
   ここでは、以下のファイルを作成します。

   ファイル名: .circleci/rdflint-config.yml

   ```
   baseUri: https://example.com/targetrdf/
   ```

   baseUriには、対象リソースURIのベース階層を指定します。

2. CircleCIの設定ファイル``.circleci/config.yml``を作成します。  
   ここでは、次のような設定ファイルを作成します。

   ```
   version: 2
   jobs:
     build:
       docker:
       - image: circleci/openjdk:8
       working_directory: ~/repo
       steps:
       - checkout
       - run:
           name: run rdflint
           command: |
             RDFLINT_VERSION=0.0.8
             wget https://jitpack.io/com/github/imas/rdflint/$RDFLINT_VERSION/rdflint-$RDFLINT_VERSION-all.jar
             java -jar rdflint-$RDFLINT_VERSION-all.jar -config .circleci/config.yml
   ```

3. 上記の２ファイルの追加をgit管理下に追加し、GitHubにpushします。  
   CircleCIを有効化すると、commitやpull request作成の度にrdflintが実行されます。


## コマンドラインオプション

rdflintの実行時には以下のコマンドラインオプションを指定することが出来ます。

- baseuri: 対象リソースURIのベース階層
- targetdir: 対象ディレクトリのパス
  指定しない場合は、カレントディレクトリを対象とする
- origindir: デグレード検証時の比較対象ディレクトリのパス
- config: 設定ファイルのパス
- i: インタラクティブモードでの起動  
  指定しない場合は、バッチモードで起動
