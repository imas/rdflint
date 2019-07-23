# 開発者向け

[ホーム](index.md) |
[セットアップ](setup.md) |
[使い方](usage.md) |
[設定ファイル](config.md) |
[検証ルール](rules.md) |
[開発者向け](developer.md)

rdflintの開発に参加、カスタマイズ開発をおこなう方向けの情報です。

## 開発の手順

1. GitHubで、rdflintのリポジトリをforkします。

   rdflint | GitHub  
   [https://github.com/imas/rdflint](https://github.com/imas/rdflint)

2. forkしたリポジトリを、ローカルPCにcloneします。

3. 必要なカスタマイズをしたら、以下のコマンドでビルドします。

   ```
   $ gradle shadowJar
   ```

4. ビルドしたrdflintは、``example/dataset``配下にテスト用データが入っているので、次のように実行を試すことが出来ます。

   ```
   $ java -jar build/libs/rdflint-all.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml
   ```

5. カスタマイズをrdflint本体に取り込みたい場合は、GitHubでPullRequestを作成して下さい。  
   取り込まれたカスタマイズは、[MITライセンス](https://github.com/imas/rdflint/blob/master/LICENSE)での公開になります。
