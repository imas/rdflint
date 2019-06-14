# セットアップ

[ホーム](index.md) |
[セットアップ](setup.md) |
[使い方](usage.md) |
[設定ファイル](config.md) |
[CI連携](ci.md) |
[検証ルール](rules.md) |
[開発者向け](developer.md)

rdflintのセットアップ手順を説明します。

1. rdflintの実行にはJavaの実行環境が必要です。ランタイムがインストールされていない場合は、以下のリンクからJREまたはJDKをダウンロードしてインストールして下さい。

   Java SE - Downloads | Oracle Technology Network | Oracle  
   [https://www.oracle.com/technetwork/java/javase/downloads/index.html](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

2. Javaの実行環境が準備できたら、JitPackからrdflintをダウンロードします。  
  ブラウザのアドレス欄に``https://jitpack.io/com/github/imas/rdflint/0.0.6/rdflint-0.0.6-all.jar``と記載してダウンロードします。  
  wgetコマンドが使える環境であれば、以下のようにダウンロードしても構いません。

   ```
   $ wget https://jitpack.io/com/github/imas/rdflint/0.0.6/rdflint-0.0.6-all.jar
   ```

3. rdflintをダウンロードしたディレクトリに移動し、以下のコマンドでrdflintを実行します。  
   エラーが表示されなければ、準備完了です。

   ```
   $ java -jar rdflint-0.0.6-all.jar
   ```
