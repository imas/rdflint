# Development

[Home](index.md) |
[Setup](setup.md) |
[Usage](usage.md) |
[Configuration](config.md) |
[Rules](rules.md) |
[Development](developer.md)

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
   $ java -jar build/libs/rdflint.jar -targetdir example/dataset -config example/dataset/rdflint-config.yml
   ```

5. カスタマイズをrdflint本体に取り込みたい場合は、GitHubでPullRequestを作成して下さい。  
   取り込まれたカスタマイズは、[MITライセンス](https://github.com/imas/rdflint/blob/master/LICENSE)での公開になります。

{{site.cookie_consent}}
