# TODO.md — タスク管理

## 完了済み

- [x] プロジェクト初期構築（Jakarta EE 10 / Faces 4.0 / WildFly 31 / Docker）
- [x] ユーザー登録フォーム（index.xhtml）
- [x] ユーザー一覧（list.xhtml）
- [x] DADS 複合コンポーネント実装（`dads:inputField`、`dads:button`）
- [x] 郵便番号検索（Ajax、ダミー実装）
- [x] 複合コンポーネントの Ajax render 不具合修正（ルート要素に `cc.clientId` を付与）
- [x] バリデーションを Jakarta Bean Validation に一本化（`@NotBlank`、`@Pattern`）
- [x] 空文字列の null 変換設定（`INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL`）
- [x] JSF `required` 属性をすべて撤廃し Bean Validation に統一

---

## フェーズ別履歴

### フェーズ1: クラス設計の整理 ✅

- [x] `model/AddressFormData.java` 作成（郵便番号・都道府県・市区町村・町村番地・ビル名 + BV アノテーション）
- [x] `model/UserFormData.java` 作成（名前・メール・電話番号 + `@Valid AddressFormData` を内包、`@Named @FlowScoped`）
- [x] `backing/RegisterInputBacking.java` 作成（`@Named @RequestScoped`、入力画面と 1:1）
- [x] `backing/RegisterConfirmBacking.java` 作成（`@Named @RequestScoped`、確認画面と 1:1）
- [x] `backing/RegisterCompleteBacking.java` 作成（`@Named @RequestScoped`、完了画面と 1:1）
- [x] `list/UserListBean.java` 作成（`@Named @SessionScoped`、登録済みユーザーのリスト保持）
- [x] 現状の `UserBean.java` を上記クラスへ移行・削除

### フェーズ2: Faces Flow 導入 ✅

- [x] Flow 定義を追加（Flow ID: `register`、`faces-config.xml` に `<flow-definition>` で定義）
- [x] Flow ノード設計（`register-input` → `register-confirm` → `register-complete` → `list`）

### フェーズ3: 画面追加 ✅

- [x] `register-input.xhtml` 作成（`index.xhtml` を Flow 対応に移行）
- [x] `register-confirm.xhtml` 作成（入力確認画面）
  - [x] 「登録する」ボタン → `UserListBean` への格納 → 完了画面遷移
  - [x] 「戻る」ボタン → 入力画面へ戻る（`AddressFormData` / `UserFormData` の値は保持）
- [x] `register-complete.xhtml` 作成（完了画面）
  - [x] 「一覧を見る」リンク → `list.xhtml`（Flow 終了）
- [x] `list.xhtml` を `UserListBacking` 経由の `dads:table` に移行

### フェーズ4: コンポーネント追加 ✅

- [x] `dads:addressField` — `AddressCandidate.java` / `AddressSearchService.java` / `AddressFieldComponent.java` 作成
- [x] 検索結果 0件: エラーメッセージ表示 / 1件: 自動入力 / 2件以上: 候補選択モーダル
- [x] `dads:outputField` — `resources/dads/outputField.xhtml` 作成
- [x] `dads:illustration` — `resources/dads/illustration.xhtml` 作成
- [x] `dads:table` — `resources/dads/table.xhtml` 作成、`UserListBacking` でデータ整形

### フェーズ5: バリデーション・API 強化 ✅

- [x] 郵便番号検索 API を実 API（zipcloud）に差し替え
- [x] `AddressSearchService` の複数件返却ロジックを実装

### フェーズ6: パッケージ構成変更 ✅

- [x] Java パッケージを `user/backing/`・`user/model/`・`util/filter/` に再編
- [x] webapp を `views/` 配下に整理（フロー単位でサブディレクトリ）
- [x] 静的ファイルを `resources/img/` に整理
- [x] `css/dads.css` を DADS jar 移行（フェーズ7）に伴い削除

### フェーズ7: DADS プロジェクト分離 ✅

- [x] `dads-components` Maven プロジェクト新規作成（jar パッケージング）
  - [x] `pom.xml` 作成（Jakarta EE 10 依存、jar パッケージング）
  - [x] `META-INF/beans.xml` 作成（CDI 有効化）
  - [x] CLAUDE.md を作成
- [x] DADS クラスを `dads-components` へ移行（`com.example.dads.*`）
  - [x] `AddressFieldComponent.java` → `com.example.dads.component`
  - [x] `AddressFormData.java` → `com.example.dads.model`
  - [x] `AddressCandidate.java` → `com.example.dads.model`
  - [x] `AddressSearchService.java` → `com.example.dads.service`
- [x] DADS リソースを `META-INF/resources/` へ移行
  - [x] `resources/dads/*.xhtml` → `META-INF/resources/dads/`
  - [x] `css/dads.css` → `META-INF/resources/css/dads.css`
- [x] Dockerfile をマルチモジュール対応に更新
- [x] docker-compose.yml のビルドコンテキストを `..` に変更
- [x] XHTML の CSS 参照を `h:outputStylesheet library="css"` に変更（jar からの配信に対応）
- [x] ビルド・動作確認（`docker compose up --build`）

### インフラ・ドキュメント整備 ✅

- [x] GitHub リポジトリ作成（モノリポ）・push（jsf-sample 履歴を保持）
- [x] ルート `README.md` 作成（プロジェクト概要・各モジュール説明・依存関係・ADR テーブル）
- [x] ルート `CLAUDE.md` 作成（ビルドコマンド・マルチモジュールアーキテクチャ）
- [x] `docs/` をルート直下へ移動（`jsf-sample/docs/` → `docs/`）

---

## 次フェーズ

### フェーズ8: DADSコンポーネント追加・修正 ✅

#### dads:button 修正（`action` / `outcome` 属性分離）
- [x] `cc:attribute name="outcome" type="java.lang.String"` を追加
- [x] `h:commandButton` の `action` を `action` / `outcome` の条件分岐に対応（`DadsNavigator.to()` 経由）
- [x] 呼び出し元 XHTML のリテラル指定箇所を `outcome` に変更
- [x] 動作確認（EL式・リテラル両方で遷移できること）

#### dads:link 新規作成（GET ナビゲーション）
- [x] `resources/dads/link.xhtml` 作成（`h:link` の DADS コンポーネント）
  - [x] 属性: `value`（String、必須）、`outcome`（String、必須）、`styleClass`（String、任意）
- [x] 呼び出し元 XHTML の `h:link` を `dads:link` に置き換え

---

### フェーズ9: 画面共通規約の適用 ✅

#### Facelets テンプレート整備（dads-components）
- [x] `dads-components/src/main/resources/META-INF/resources/dads/pageContainer.xhtml` を整備
  - [x] `title` 属性（String）を追加（h1 見出しとして描画）
  - [x] `h:outputStylesheet library="css" name="dads.css"` を追加（共通 CSS を集約）
- [x] 各 XHTML（index / list / register-input / register-confirm / register-complete）を `dads:pageContainer` でテンプレート統合・h1 を title 属性に移行

#### `f:viewAction` 適用
- [x] 各 XHTML のルート直下（`<h:head>` より前）に `<f:metadata>` + `<f:viewAction>` を配置
- [x] 各 Backing Bean に `init()` メソッドを追加（RegisterInputBacking: `guardFlow` → `init` へリネーム、RegisterConfirmBacking / RegisterCompleteBacking: `init()` 新規追加）
- [x] confirm・complete ページが RegisterInputBacking の `guardFlow` を呼んでいた誤りを修正（各自の Backing Bean の `init()` を呼ぶよう変更）

#### 遷移規約確認・修正
- [x] フロー内遷移がフォワード（文字列返却）になっていることを確認
- [x] フロー脱出（完了 → 一覧）が `faces-config.xml` の `flow-return` 経由で `?faces-redirect=true` 付きになっていることを確認

---

## 将来対応（優先度低）

- [ ] データ永続化（JPA + H2 または PostgreSQL）
- [ ] 一覧画面に削除機能を追加
- [ ] 電話番号の形式バリデーション
- [ ] ページネーション
