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
  ブラウザのアドレス欄に``https://jitpack.io/com/github/imas/rdflint/{{site.RDFLINT_VERSION}}/rdflint-{{site.RDFLINT_VERSION}}.jar``と記載してダウンロードします。  
  wgetコマンドが使える環境であれば、以下のようにダウンロードしても構いません。

   ```
   $ wget https://jitpack.io/com/github/imas/rdflint/{{site.RDFLINT_VERSION}}/rdflint-{{site.RDFLINT_VERSION}}.jar
   ```

3. rdflintをダウンロードしたディレクトリに移動し、以下のコマンドでrdflintを実行します。  
   エラーが表示されなければ、準備完了です。

   ```
   $ java -jar rdflint-{{site.RDFLINT_VERSION}}.jar
   ```

## Visual Studio Codeへのセットアップ

1. rdflintの実行にはJavaの実行環境が必要です。ランタイムがインストールされていない場合は、以下のリンクからJREまたはJDKをダウンロードしてインストールして下さい。

   Java SE - Downloads | Oracle Technology Network | Oracle  
   [https://www.oracle.com/technetwork/java/javase/downloads/index.html](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

2. Javaのインストールが完了したら、環境変数「JAVA_HOME」にJavaのインストール先を設定します。  

   「エクスプローラー」を開き、「PC」を選択する、  
   右クリックメニューから「プロパティ」を選ぶ、  
   「システム」が開くので、の左側メニューで「システムの詳細設定」を選ぶ、  
   「システムのプロパティ」が開くので、「詳細設定」タブで「環境変数」を選ぶ、  
   「新規」ボタンを押す、  
   「変数名」に「JAVA_HOME」を指定、  
   「ディレクトリの参照」からJavaのインストール先を指定、  
   「OK」を押す。  
   ※Javaのインストール先は、変更していなければ「C:\Program Files\Java\jdk-(番号)」などです。

3. Visual Studio Codeを起動します。（環境変数を有効にするため、既に起動していた場合は一度終了してください）

4. Extensionsメニュー(File->Preferences->Extensions)から、  
   「rdflint」などのキーワードで検索し「RDF lanauage support via rdflint」を選びます。  
   「Install」を押して、拡張機能をインストールします。

5. Ctrl+Shift+Pメニューで、「rdflint interactive mode: SPARQL playground」を選ぶと、  
   インタラクティブモードでrdflintが起動します。

## Homebrewによるセットアップ (macOSのみ)

1. Homebrewがインストールされていない場合は、以下のリンクを参照してインストールして下さい。

   Homebrew  
   [https://brew.sh/index_ja](https://brew.sh/index_ja)

2. Homebrewが使えるようになったら、以下のコマンドでrdflintをインストールします。

   ```
   $ brew tap imas/rdflint
   $ brew install rdflint
   ```

   ※0.1.1以前に ``takemikami/takemikami`` からインストールしていた方は、以下の手順でHomebrewの参照先を変更してください。

   ```
   $ brew untap takemikami/takemikami
   $ brew tap imas/rdflint
   ```

3. 以下のコマンドでrdflintを実行し、エラーが表示されなければ、準備完了です。

   ```
   $ rdflint
   ```

※Homebrewでインストールした場合、「使い方」で``java -jar rdflint-{{site.RDFLINT_VERSION}}.jar``と記載されている箇所は``rdflint``に読み替えて下さい。

{{site.cookie_consent}}
