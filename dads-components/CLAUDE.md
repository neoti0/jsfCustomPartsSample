# dads-components — デジタル庁デザインシステム JSF コンポーネントライブラリ

## 概要

DADS（デジタル庁デザインシステム）の UI 部品を Jakarta Faces 4.0 複合コンポーネントとして提供する jar プロジェクト。
複数の JSF プロジェクトから依存として参照することを想定している。

---

## スタック

| 項目 | バージョン |
|------|-----------|
| Jakarta EE | 10.0.0 |
| Jakarta Faces (JSF) | 4.0 |
| JDK | OpenJDK 21 |
| ビルドツール | Maven 3.x (jar パッケージング) |

---

## ビルド手順

単体ビルド（ローカル Maven リポジトリへインストール）:

```bash
mvn clean install
```

親プロジェクトからのビルド（推奨）:

```bash
cd ..
mvn clean package
```

---

## パッケージ配置ルール

### Java（`src/main/java/com/example/dads/`）

業務ドメインを持たないため第一階層はなく、機能分類のみ。

```
com.example.dads/
├── component/   # JSF カスタムコンポーネント（@FacesComponent）
├── model/       # コンポーネントの契約型（POJO・BV アノテーション）
└── service/     # 外部 API 呼び出しなど入出力を伴う処理
```

### リソース（`src/main/resources/META-INF/`）

JSF は jar 内の `META-INF/resources/` を自動スキャンする。
`xmlns:dads="jakarta.faces.composite/dads"` の名前空間解決はここで行われる。

```
META-INF/
├── beans.xml
└── resources/
    ├── dads/    # 複合コンポーネント xhtml（dads:xxx として参照される）
    └── css/     # DADS スタイルシート
```

---

## 管理方針

- **業務の関心事を持ち込まない**: このライブラリが管理するのは UI の仕組みと共通処理のみ。業務ロジックは利用側プロジェクトが担う。
- **画像コンテンツは含めない**: `dads:illustration` 等の表示コンポーネントは仕組みを提供するのみ。表示する画像は利用側プロジェクトが用意する（アイコン等デザインと密結合したものは除く）。
- **利用側プロジェクトとの依存**: 利用側は `pom.xml` に `com.example:dads-components` 依存を追加するだけで `xmlns:dads` が利用可能になる。

---

## コンポーネント一覧

| コンポーネント | 属性 | 説明 |
|---|---|---|
| `dads:inputField` | `label` / `value` / `required` / `size` / `placeholder` | ラベル＋入力欄＋エラーメッセージ |
| `dads:button` | `value` / `action` / `outcome` / `buttonType` / `size` / `ajaxExecute` / `ajaxRender` | DADS スタイルボタン（Ajax 対応） |
| `dads:addressField` | `value`（AddressFormData） | 郵便番号検索＋住所フィールド一体型 |
| `dads:outputField` | `label` / `value` | 確認画面用ラベル＋値の読み取り専用表示 |
| `dads:table` | `headers` / `rows` | DADS スタイルテーブル |
| `dads:illustration` | `src` / `alt` / `size` | イラスト表示（画像パスは利用側が指定） |
| `dads:pageContainer` | `wide`（Boolean, default false） | ページ外枠 div のラッパー。wide=true で `page-container--wide` クラスを使用 |
| `dads:link` | `value` / `outcome` / `styleClass` | DADS スタイルのナビゲーションリンク |
