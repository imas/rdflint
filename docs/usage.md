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

   baseUriには、対象データセットURIのベース階層を指定します。

3. rdflintを実行します。   
   ここでは、``target.rdf``と``rdflint-config.yml``、及び``rdflint-{{site.RDFLINT_VERSION}}.jar``を置いたディレクトリに移動して、次のコマンドを実行します。

   ```
   $ java -jar rdflint-{{site.RDFLINT_VERSION}}.jar -config rdflint-config.yml
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
   ここでは、``target.rdf``と``rdflint-config.yml``、及び``rdflint-{{site.RDFLINT_VERSION}}.jar``を置いたディレクトリに移動して、次のコマンドを実行します。

   ```
   $ java -jar rdflint-{{site.RDFLINT_VERSION}}.jar -config rdflint-config.yml -i
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

GitHub Actionsでの設定方法を例に、CIでrdflintを実行する手順を説明します。

1. rdflintの設定ファイルを作成します。   
   ここでは、以下のファイルを作成します。

   ファイル名: .rdflint/rdflint-config.yml

   ```
   baseUri: https://example.com/targetrdf/
   ```

   baseUriには、対象リソースURIのベース階層を指定します。

2. GitHub Actionsの設定ファイル``.github/workflows/ci.yml``を作成します。  
   ここでは、次のような設定ファイルを作成します。

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

3. 上記の２ファイルの追加をgit管理下に追加し、GitHubにpushします。  
   GitHub Actionsを有効化すると、pull request作成及び修正時にrdflintが実行されます。

## 不要な警告を無視する

rdflintが出力する警告について、場合によっては修正が不要で無視したいケースがあります。  
このような場合に、警告を出力しないように設定する方法を説明します。  
（rdflintには、統計的な値の偏りから正しくない値の疑いがあると判断するロジックも含まれています）

事前に前項「rdflintの実行手順」を完了させておいて下さい。

1. 対象のファイルを修正し、無視したい警告以外が無い状態にします。

2. rdflintを実行します。   

   ```
   $ java -jar rdflint-{{site.RDFLINT_VERSION}}.jar -config rdflint-config.yml
   ```

   無視したい警告のみが表示されることを確認します。  
   この時、出力された警告と同等の内容が、`rdflint-problems.yml`に出力されます。

3. `rdflint-problems.yml`の内容を`rdflint-suppress.yml`にコピーします。

   ```
   $ cp rdflint-problems.yml rdflint-suppress.yml
   ```

4. 再度、rdflintを実行します。  
   再度の実行時には、警告は出力されなくなります。  
   ※カスタムクエリ検証など、データの位置を特定できない一部の警告は無視出来ません。

※ 無視した警告を再度出力させたい場合は、`rdflint-suppress.yml`ファイルを削除します。


## コマンドラインオプション

rdflintの実行時には以下のコマンドラインオプションを指定することが出来ます。

- baseuri: 対象データセットURIのベース階層
- targetdir: 対象ディレクトリのパス
  指定しない場合は、カレントディレクトリを対象とする
- suppress: 無視する警告の定義ファイルのパス
- origindir: デグレード検証時の比較対象ディレクトリのパス
- config: 設定ファイルのパス
- i: インタラクティブモードでの起動  
  指定しない場合は、バッチモードで起動
- ls: Language Server モードでの起動 ※実験的な機能です

{{site.cookie_consent}}
