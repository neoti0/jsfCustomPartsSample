# jsfCustomPartsSample

Jakarta EE 10 / JSF 4.0 のサンプルプロジェクトです。  
DADS（デジタル庁デザインシステム）を JSF コンポーネントライブラリとして分離し、サンプルアプリから利用するマルチモジュール構成になっています。

---

## 技術スタック

| 項目 | バージョン |
|------|-----------|
| Jakarta EE | 10.0.0 |
| Jakarta Faces (JSF) | 4.0 |
| アプリケーションサーバー | WildFly 31.0.0.Final |
| JDK | OpenJDK 21 |
| ビルドツール | Maven 3.x |
| コンテナ | Docker (Compose Plugin) |

---

## リポジトリ構成

2モジュール構成の Maven マルチモジュールプロジェクトです。

| モジュール | パッケージング | 役割 |
|---|---|---|
| `dads-components` | jar | DADS コンポーネントライブラリ（デザインシステム部品） |
| `jsf-sample` | war | ユーザー登録サンプルアプリ（dads-components を利用） |

---

## モジュール概要

### dads-components

DADS を Jakarta Faces 4.0 の複合コンポーネントおよびカスタムコンポーネントとして実装したライブラリ。  
`xmlns:dads="jakarta.faces.composite/dads"` で任意の JSF プロジェクトから参照できます。

| コンポーネント | 説明 |
|---|---|
| `dads:inputField` | ラベル＋入力欄＋エラーメッセージ |
| `dads:button` | DADS スタイルボタン（Ajax 対応、`action` / `outcome` 属性） |
| `dads:addressField` | 郵便番号検索＋住所フィールド一体型（zipcloud API 連携） |
| `dads:outputField` | 確認画面用ラベル＋値の読み取り専用表示 |
| `dads:table` | DADS スタイルテーブル（ヘッダー＋明細行） |
| `dads:illustration` | イラスト表示（画像パスは利用側が指定） |

→ 詳細: [dads-components/CLAUDE.md](dads-components/CLAUDE.md)

---

### jsf-sample

入力 → 確認 → 完了の3画面 Faces Flow でユーザー登録を行うサンプルアプリ。  
`dads:*` コンポーネントを使用して DADS デザインを適用しています。

```
index.xhtml（トップ）
    ↓ Faces Flow 開始
register-input.xhtml（登録入力）
    ↓ 「確認する」
register-confirm.xhtml（登録確認）
    ↓ 「登録する」       ↓ 「戻る」
register-complete.xhtml          register-input.xhtml
（登録完了）
    ↓ 「一覧を見る」（Flow 終了）
list.xhtml（登録済みユーザー一覧）
```

→ 詳細: [jsf-sample/README.md](jsf-sample/README.md)

---

## モジュール間の依存関係

```
jsf-sample (WAR)
    └── depends on ──▶ dads-components (JAR)
```

`jsf-sample/pom.xml` に `com.example:dads-components` 依存を追加することで `dads:*` タグが利用可能になります。  
`dads-components` は `jsf-sample` に依存せず、独立して再利用できます。

---

## ビルド・起動

```bash
docker compose -f jsf-sample/docker-compose.yml up --build
```

Dockerfile 内でマルチステージビルドを行うため、Maven のローカルインストールは不要です。

### アクセス URL

| 用途 | URL |
|------|-----|
| トップ（フロー入口） | http://localhost:8080/jsf-sample/views/index.xhtml |
| 登録済みユーザー一覧 | http://localhost:8080/jsf-sample/views/list.xhtml |
| WildFly 管理コンソール | http://localhost:9990 |

管理コンソールの認証情報: `admin` / `admin123`

---

## ADR（アーキテクチャディシジョンレコード）

設計上の意思決定は `docs/adr/` で管理しています。

| ADR | タイトル | ステータス |
|---|---|---|
| [ADR-001](docs/adr/ADR-001-bv-annotations-on-flowscoped-model.md) | Bean Validation アノテーションを FlowScoped モデルクラスに集約する | Accepted |
| [ADR-002](docs/adr/ADR-002-backing-bean-requestscoped-no-snapshot.md) | Backing Bean を @RequestScoped とし、ViewScoped スナップショット方式を採用しない | Accepted |
| [ADR-003](docs/adr/ADR-003-addressfield-custom-component.md) | dads:addressField をカスタムコンポーネントで実装する | Accepted |
| [ADR-004](docs/adr/ADR-004-extract-address-form-data.md) | 住所情報を AddressFormData として UserFormData から切り出す | Accepted |
