# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## ビルド・起動コマンド

```bash
# 全モジュールをビルドして WildFly で起動（推奨）
docker compose -f jsf-sample/docker-compose.yml up --build

# 全モジュールをローカル Maven でビルド
mvn clean package

# dads-components 単体をローカル Maven リポジトリへインストール
cd dads-components && mvn clean install

# jsf-sample 単体ビルド（dads-components インストール済みの場合）
cd jsf-sample && mvn clean package
```

テストは存在しないため、ビルド成功＋docker 起動確認が検証手段。

---

## アーキテクチャ概要

### マルチモジュール構成

```
jsfCustomPartsSample/          ← 親 pom.xml（jsf-parent, packaging=pom）
├── dads-components/           ← DADS コンポーネントライブラリ（jar）
└── jsf-sample/                ← ユーザー登録サンプルアプリ（war）
```

**依存方向**: `jsf-sample` → `dads-components`（一方向。dads-components はアプリに依存しない）

### dads-components

JSF が jar 内の `META-INF/resources/` を自動スキャンする仕様を利用して複合コンポーネントを提供。  
`xmlns:dads="jakarta.faces.composite/dads"` の名前空間は `META-INF/resources/dads/` を解決する。  
Java カスタムコンポーネント（`@FacesComponent`）は `com.example.dads.component` パッケージに配置。

### jsf-sample

Faces Flow（`faces-config.xml` で定義、Flow ID: `register`）が3画面遷移を管理。  
フォームデータは `@FlowScoped` の `UserFormData` が Flow 全体で保持し、Backing Bean は `@RequestScoped` で状態を持たない薄い実装にする。  
BV（Bean Validation）アノテーションは `UserFormData` / `AddressFormData` に集約し、JSF の `required` 属性は使わない。

### Docker

`jsf-sample/Dockerfile` がマルチステージビルド（`maven:3.9-eclipse-temurin-21` でビルド → `eclipse-temurin:21-jre` + WildFly 31 に WAR を配置）。  
WildFly は `standalone/deployments/` に `.war` を置くだけで自動デプロイする。

---

## 各モジュールの詳細ガイド

- **jsf-sample/CLAUDE.md** — JSF 開発注意事項（スコープ選択・リダイレクト・xmlns）、パッケージ配置ルール、`docs/` 仕様管理方法、ADR 運用ルール
- **dads-components/CLAUDE.md** — DADS ライブラリのパッケージルール、`META-INF/resources/` 配置、コンポーネント一覧
