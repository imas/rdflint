`rdflint` can integrated with following tools.

## Visual Studio Code

You can use rdflint with Visual Studio Code.

Make Visual Studio Code Tasks Configuration file. In this part, Make following file.

file name: .vscode/tasks.json

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "rdflint interactive mode: SPARQL playground",
      "type": "shell",
      "command": "rdflint -i",
      "presentation": {
        "clear": true,
      }
    },
    {
      "label": "rdflint",
      "type": "shell",
      "command": "rdflint",
      "presentation": {
        "reveal": "silent",
        "revealProblems": "onProblem"
      },
      "problemMatcher": {
        "owner": "rdflint",
        "fileLocation": ["relative", "${workspaceFolder}"],
        "pattern": [
          {
            "regexp": "^(.*)$",
            "file": 1
          },
          {
            "regexp": "^\\s+(.*line:\\s+([0-9]+).*)$",
            "line": 2,
            "message": 1,
          }
        ]
      },
    }
  ]
}
```

From Ctrl+Shift+P menu, select `Tasks: Run Task` and `rdflint`, and rdflint checks run.

From Ctrl+Shift+P menu, select `Tasks: Run Task` and `rdflint interactive mode: SPARQL playground`, and rdflint interactive mode run.

## GitHub Actions

You can use rdflint with GitHub Actions.

Make GitHub Actions Configuration file. In this part, Make following file.

file name: .github/workflows/ci.yml

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

Push configuration files to GitHub repository, and execute rdflint when create or update pull request.

See [imas/setup-rdflint](https://github.com/imas/setup-rdflint) for more information.
