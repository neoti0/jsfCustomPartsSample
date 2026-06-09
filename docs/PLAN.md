# PLAN.md — 要件ダンプ

## やりたいこと

Jakarta EE 10 / JSF 4.0 の学習・検証用サンプルアプリを作りたい。
DADS（デジタル庁デザインシステム）のコンポーネントを JSF の複合コンポーネントとして実装して、
実際の業務アプリに近い画面を作ってみる。

## 使いたい技術

- Jakarta EE 10 / Faces 4.0（JSF）
- WildFly 31（アプリサーバー）
- CDI（依存性注入）
- Jakarta Bean Validation 3.0
- Docker / docker-compose で環境を閉じたい
- デザインは DADS 準拠にしたい

## 作りたい機能
- 郵便番号検索と住所欄と郵便番号検索および結果のプリセットをまとめてカスタムコンポーネントで部品化する。
  - XHTMLとAPIコールをまとめて一つの部品として使い回したい。
  - カスタムコンポーネントの引数はBeanに紐づく郵便番号や都道府県などの値
- 入力確認画面
  - ユーザ登録画面で更新するのではなく、確認画面を介して、入力内容を確認できるようにする。
  - 入力内容を表示するラベルを用意
  - ラベル表示もDADS準拠の複合コンポーネントを用意する
  - 入力画面への戻るボタンも用意する
- 完了画面
  - 入力確認画面で不備がないことを確認し、Submitしたら完了画面へ到達させる。
  - 完了画面には顧客登録を受け付けた旨の文言と感謝を伝えるイラストを用意
  - イラスト表示もDADS準拠の複合コンポーネントを用意する
  - 完了画面からリスト画面への遷移できるようにする。
- ユーザ登録（入力・入力確認・完了）のセッションスコープをFlowにする
  - ユーザ登録の３画面で共有するユーザ情報はFlowスコープに
  - 完了画面まで到達した際にユーザ情報をセッションスコープの登録済みユーザ情報に格納
  - Listはセッションスコープの登録ずみユーザ情報を表示する。
- Backing Beanとデータの責務分割
  - 画面制御やAPI呼び出しなどの操作とデータ保持を別クラスにする。
  - 画面制御はXHTMLファイルと１：１の関係性で作成する。
  - データ保持クラスはFlowスコープのユーザ情報として、変数定義およびBeanValidationの定義を記載する。

### ユーザー登録フォーム
- 名前、メールアドレス、電話番号を入力
- 住所は郵便番号から自動入力できると嬉しい（住所検索 API はとりあえずダミーでいい）
- 都道府県、市区町村、町村番地、ビル・建物名（任意）
- 必須項目は全部チェックしたい
- バリデーションエラーは入力欄の下にメッセージ表示

### ユーザー一覧
- 登録したユーザーを一覧で見られる
- セッション中はデータを保持する（DBは不要）

## DADS 複合コンポーネント

- `dads:inputField` — ラベル（※必須 / 任意バッジつき）＋ 入力欄 ＋ エラーメッセージをひとまとめにしたコンポーネント
- `dads:button` — DADS スタイルのボタン。Ajax 対応（ajaxExecute / ajaxRender 属性）

## 追加要件
### パッケージ構成の変更
- `src/main/java/com/example/jsfsample`配下の見直し
  - 機能による分割ではなく、ドメイン概念を第一階層として、第二階層移行を機能や機能観点の分割にする。
  - 第一階層はuser:顧客と、dads：デジタル庁デザインシステム　に分ける。
  - 第二階層以下は以下の通り。
    - backing：BackingBeanを格納。
    - model：Beanとして保持する値群・Dtoクラスを格納
    - service:API通信などの処理を格納
  - filterなどのシステム共通処理は第一階層にutilフォルダを配置し、その配下に機能別のパッケージを切って、格納する。
  - このパッケージングに関して、ルール化してclaudeで守れるようにする。
- `src/main/webapp`配下の見直し
  - 原則として、webapp配下はフレームワークの管理・依存が大きいと考え、フレームワークの考え・思想を踏襲する。
  - xhtmlは画面数が増えることを想定し、パッケージ構成を検討する。
    - webapps配下にviewsバッケージを作成し、そこに格納する。
    - フロー単位にxhtmlを格納する。
  - 静的資材の管理
    - webapps配下のresourcesパッケージに種別毎のパッケージを切り、格納。
    - なお、現在格納されているdadsはxhtmlなので、webapps配下のviewsバッケージに移動

### DADS（デジタル庁デザインシステム）
- `dads:button` をリテラル文字列ナビゲーション（フロー入口など）にも使えるようにしたい
  - 現状は `action` に EL 式（`#{bean.method}` 形式）しか渡せず、`action="register"` のようなリテラル文字列を渡すと ClassCastException が発生する
  - フロー入口ボタンや静的なページ遷移でも `dads:button` で統一できるよう、文字列アウトカムにも対応させたい
- DADS（デジタル庁デザインシステム）コンポーネントへの移行
  - xhtmlファイルでJSFタグを原則利用させず、dadsのコンポーネントを利用させる
  - dadsコンポーネントを利用させることで、デザイン要素を開発者に意識させず、デザイン・レイアウト崩れを抑止したい。
  - 現状JSFタグを利用している要素に関して、dadsコンポーネントへの移行方針を検討し、不足しているコンポーネントがあれば追加開発する。 
- DADS（デジタル庁デザインシステム）の別プロジェクト化
  - DADS（デジタル庁デザインシステム）を別プロジェクトとして、jar展開できるようにしたい。
  - これは実際利用する際に、複数のJSF開発プロジェクトに展開するにあたり、jar化を検討しているから。
  - DADS（デジタル庁デザインシステム）に関する処理をjsf-sampleから切り出し、別プロジェクトにする。
  - 別プロジェクトはjsf-sampleと並列の階層にプロジェクトを作成し、jsf-sampleにjarを渡して、共有する。
  - claude設定やパッケージ構成はjsf-sampleを踏襲する。なお、docs配下もDADS関連の情報を取捨選択して、移行すること。

### 画面開発共通仕様定義（Jakarta Faces / JSF）

#### 1. 画面アーキテクチャ基本原則

本システムでは、画面の肥大化（神クラス化・Fat Controller）を防ぎ、高い保守性とAIによるコード生成の正確性を両立するため、「1画面・1制御Bean・1データコンテナ」の3層構造（ミルフィーユパターン）を徹底する。

* **View（XHTML）**: 画面レイアウトとデータ表示のみを担当する。
* **UI制御・インフラ呼出（Backing Bean）**: 画面と **1:1** で作成し、画面固有のイベント、インフラ層（API/EJB）の呼び出し、処理フローの制御のみを担当する。**業務データ（状態）は一切保持しない。**
* **データコンテナ（Flow Data Bean）**: 一連のウィザード（フロー）内で共通のデータを保持することに特化する。ロジックは持たず、プロパティと Bean Validation 定義のみをカプセル化する。

---

#### 2. 画面と Backing Bean の 1:1 マッピングおよび初期処理仕様

画面の初期化処理（表示時のAPI呼び出しやデータチェック）および遷移処理は、以下のルールに従ってマッピングする。

##### 2.1. 初期表示イベント（`f:viewAction`）の徹底

画面表示時の初期処理は、JSF標準のオンデマンド生成に依存せず、**`f:viewAction` を使用して、明示的に対応する Backing Bean の初期化メソッドを呼び出すこと。**
これにより、画面内の表示要素がデータコンテナを直接参照している場合でも、裏側で確実に制御Beanの初期化（インフラ層呼び出し等）がトリガーされる。

##### 2.2. ボタン（アクション）のマッピング原則

画面に配置するサブミットボタン（`<h:commandButton>` 等）の `action` 属性には、**必ず「今いる画面（現在地）の Backing Bean」のメソッドを指定すること。**
遷移先の画面の Bean（目的地）のメソッドを直接指定する行為は、ライフサイクルの混乱と結合度の悪化を招くため、厳禁（アンチパターン）とする。

---

#### 3. 画面遷移制御規約（フォワードとリダイレクトの使い分け）

ブラウザの「戻る」「更新」ボタンによる不正な二重リクエスト（二重サブミット）を防止し、`@FlowScoped` のメモリ解放を厳密に制御するため、遷移方式は以下のルールを厳守する。

##### 3.1. 同一フロー内の遷移：フォワード（Forward）

* **対象**: `@FlowScoped` 内での画面遷移（例：入力 ➔ 確認、確認 ➔ 入力へ戻る）
* **制御方法**: Backing Bean のメソッドから **遷移先の画面ID（文字列）をそのまま返却** する。
* **理由**: 同一リクエスト内で処理を引き継ぐため高速であり、`@FlowScoped` 境界を維持したまま安全にデータをリレーできるため。

##### 3.2. フローを跨ぐ（外へ出る）遷移：リダイレクト（PRGパターン）

* **対象**: フローの最終完了時（完了 ➔ トップ画面へ）、またはフローの一時中断による外部画面への遷移。
* **制御方法**: 戻り値の文字列の末尾に必ず **`?faces-redirect=true`** を付与する（PRG: Post-Redirect-Get パターンの適用）。
* **理由**:
1. ブラウザのURLを完全に書き換えることで、完了画面での「ページ更新（F5キー）」による処理の二重実行（再サブミット）を物理的に防止する。
2. JSFコンテナに対して「フローの終了」を明確に検知させ、データコンテナ（`@FlowScoped`）を確実にメモリから破棄（解放）させるため。



---

#### 4. 画面共通テンプレート（Facelets）活用仕様

画面の共通デザイン、フォント読み込み、共通CSS（`dads.css`）などの外枠はテンプレートファイル（`layout.xhtml`）として共通化し、個別画面（子画面）はそれを拡張（はめ込み）する形式をとる。

##### 4.1. テンプレート側の定義（`layout.xhtml`）

子画面から「タイトル」および「コンテンツ」を動的に受け取れるよう、`<ui:insert>` を配置する。

```xml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:ui="jakarta.faces.facelets"
      lang="ja">
<h:head>
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <title><ui:insert name="title">デフォルトタイトル</ui:insert></title>
    
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin="anonymous" />
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@100..900&amp;display=swap" rel="stylesheet" />
    <h:outputStylesheet library="css" name="dads.css" />
    <ui:insert name="headScripts" />
</h:head>
<h:body>
    <div class="page-container">
        <h1 class="dads-heading" data-size="28" data-rule="4">
            <ui:insert name="title">デフォルトタイトル</ui:insert>
        </h1>
        <ui:insert name="content" />
    </div>
    <ui:insert name="bodyScripts" />
</h:body>
</html>

```

##### 4.2. 個別画面（子画面）における `f:viewAction` の配置制約

テンプレートを適用した個別画面において、`f:viewAction` を含む `<f:metadata>` タグは、**必ず `<ui:composition>` タグの外側（ファイルのルート直下）に記述しなければならない。**

---

#### 5. 標準コードパターン（確認画面 ➔ 完了画面の例）

##### 5.1. 個別画面（`confirm.xhtml`）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<f:metadata>
    <f:viewAction action="#{confirmBean.init()}" />
</f:metadata>

<ui:composition template="/WEB-INF/templates/layout.xhtml"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:h="jakarta.faces.html"
                xmlns:f="jakarta.faces.core"
                xmlns:ui="jakarta.faces.facelets">

    <ui:define name="title">登録内容確認</ui:define>

    <ui:define name="content">
        <h:form id="confirmForm">
            <div class="form-group">
                <h:outputText value="お名前：" />
                <h:outputText value="#{registrationData.user.name}" />
            </div>

            <div class="button-group">
                <h:commandButton value="入力画面に戻る" action="#{confirmBean.back()}" />
                <h:commandButton value="確定して完了へ" action="#{confirmBean.submit()}" />
            </div>
        </h:form>
    </ui:define>
</ui:composition>

```

##### 5.2. UI制御・遷移管理 Bean（`ConfirmBean.java`）

```java
package com.example.view;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import com.example.flow.RegistrationData;
import com.example.infrastructure.ExternalApiService;

@Named
@ViewScoped
public class ConfirmBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private RegistrationData registrationData;

    @Inject
    private ExternalApiService apiService;

    public void init() {
        // 初期表示時のチェックやAPI呼び出し
        registrationData.setApiResult(apiService.callCheck(registrationData.getUser()));
    }

    /**
     * 同一フロー内（入力画面）へ戻る：フォワード遷移（文字列のみを返却）
     */
    public String back() {
        return "registration"; 
    }

    /**
     * 確定処理：DB保存後に完了画面へフォワード遷移
     */
    public String submit() {
        // ビジネスロジック層への委譲など
        // 同一フローの「完了画面(complete.xhtml)」へはフォワードで繋ぐ
        return "complete"; 
    }
}

```

##### 5.3. フロー脱出 Bean（`CompleteBean.java`）

```java
package com.example.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped // 完了画面は状態を持たないためRequestScopedで十分
public class CompleteBean {

    /**
     * フローを完全に跨いでトップ画面へ戻る：リダイレクト遷移
     * これによりURLが正常化し、@FlowScoped のメモリが解放される
     */
    public String exitToTop() {
        return "/home?faces-redirect=true"; 
    }
}

```



## 気にしていること

- JSF の Faces 4.0 は `jakarta.*` 名前空間になっていること
- 複合コンポーネントと Ajax の相性（render が効かないケースがある？）
- Bean Validation と JSF required の二重管理は避けたい
