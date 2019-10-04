# セットアップ

[ホーム](index.md) |
[セットアップ](setup.md) |
[使い方](usage.md) |
[設定ファイル](config.md) |
[検証ルール](rules.md) |
[開発者向け](developer.md)

rdflintのセットアップ手順を説明します。

## マニュアルセットアップ

1. rdflintの実行にはJavaの実行環境が必要です。ランタイムがインストールされていない場合は、以下のリンクからJREまたはJDKをダウンロードしてインストールして下さい。

   Java SE - Downloads | Oracle Technology Network | Oracle  
   [https://www.oracle.com/technetwork/java/javase/downloads/index.html](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

2. Javaの実行環境が準備できたら、JitPackからrdflintをダウンロードします。  
  ブラウザのアドレス欄に``https://jitpack.io/com/github/imas/rdflint/0.0.9/rdflint-0.0.9-all.jar``と記載してダウンロードします。  
  wgetコマンドが使える環境であれば、以下のようにダウンロードしても構いません。

   ```
   $ wget https://jitpack.io/com/github/imas/rdflint/0.0.9/rdflint-0.0.9-all.jar
   ```

3. rdflintをダウンロードしたディレクトリに移動し、以下のコマンドでrdflintを実行します。  
   エラーが表示されなければ、準備完了です。

   ```
   $ java -jar rdflint-0.0.9-all.jar
   ```

## Homebrewによるセットアップ (macOSのみ)

1. Homebrewがインストールされていない場合は、以下のリンクを参照してインストールして下さい。

   Homebrew  
   [https://brew.sh/index_ja](https://brew.sh/index_ja)

2. Homebrewが使えるようになったら、以下のコマンドでrdflintをインストールします。

   ```
   $ brew tap takemikami/takemikami
   $ brew install rdflint
   ```

3. 以下のコマンドでrdflintを実行し、エラーが表示されなければ、準備完了です。

   ```
   $ rdflint
   ```

※Homebrewでインストールした場合、「使い方」で``java -jar rdflint-0.0.9-all.jar``と記載されている箇所は``rdflint``に読み替えて下さい。
