# testTemplate

## What is this

* DB操作のあるプログラムのテストに使われるdbUnit(http://dbunit.sourceforge.net/) を容易に準備できるテンプレート
* Seleniumのテストも支援

## Contains

### util.RuleResource

* application.propertiesに記載した情報を参照するためのクラス。
* @ClassRuleを指定して使用

### util.RuleDataBase

* dbUnit周りの操作を行うクラス
* @ClassRuleを指定して使用＆util.RuleResourceが必須
* テストの開始時に任意のテーブルのバックアップを行い、終了時にリストアを行う

### util.selenium.RuleTestUtil

* SeleniumのjUnitテストを実施する際に使用すると吉なクラス
* @Ruleを指定して使用

### util.selenium.LocalStorageJS

* SeleniumのjUnitテスト実施時にLocalStorageを操作するためのクラス

## Getting Started

### GitHubからclone

1. `git clone https://github.com/rk7fd3s/testTemplate.git`

### テスト実行

1. `cd testTemplate`
1. `./mvnw test`   * if your env is windows then  `mvnw.cmd test`


* 最終的に、Tests run: 2, Failures: 0, Errors: 0, Skipped: 0と、BUILD SUCCESSが表示されればOK。

#### テスト内容

* TestRuleResource
 * 設定ファイル`application.properties`の内容が正しく取得できているかのテスト
* TestRuleDataBase
 * DB操作を行ったあと、テーブル情報が想定される変更であるかのテスト
