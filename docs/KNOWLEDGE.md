# KNOWLEDGE.md — 学びの蓄積

## JSF 複合コンポーネントと Ajax render

**問題**: `dads:inputField` を `ajaxRender` の対象に指定しても画面が更新されなかった。

**原因**: JSF の Ajax partial render はレスポンスの `<update id="...">` に対応する DOM 要素を id で探す。
複合コンポーネントの `cc:implementation` のルート要素に id がないと、対象要素が見つからず更新が静かに失敗する。

**解決策**: `cc:implementation` のルート要素に `id="#{cc.clientId}"` を付与する。

```xml
<div id="#{cc.clientId}" class="dads-form-control-label" ...>
```

`cc.clientId` は JSF が割り当てるクライアント ID（例: `registerForm:prefecture`）を返す。

---

## h:message の Ajax 再描画

**問題**: Ajax リクエスト後に `h:message` のバリデーションエラーが表示されなかった。

**原因**: `h:message` が `ajaxRender` の対象に含まれていなかった。
`<span>` タグは JSF コンポーネントではないため id が付かず、render 対象に指定できない。

**解決策**: `h:message` を `<h:panelGroup id="postalCodeInput">` でラップし、その id を `ajaxRender` に追加する。

```xml
<h:panelGroup id="postalCodeInput" styleClass="dads-input-text">
    <h:inputText id="postalCode" ... />
    <h:message for="postalCode" ... />
</h:panelGroup>
```

---

## Bean Validation で @NotBlank と @Pattern が二重発火する

**問題**: 空欄で送信すると「郵便番号は必須です」と「形式が正しくありません」が両方表示された。

**原因**: Bean Validation はデフォルトで全制約を同時評価する。
空文字列 `""` は `@NotBlank`（空は NG）にも `@Pattern`（空は正規表現不一致）にも引っかかる。

**解決策**: `web.xml` に以下を追加して空文字列を `null` に変換する。
`null` は `@Pattern` の仕様上「有効」として扱われるため、`@NotBlank` だけが発火する。

```xml
<context-param>
    <param-name>jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL</param-name>
    <param-value>true</param-value>
</context-param>
```

---

## JSF required と Bean Validation の二重管理問題

**問題**: `h:inputText` の `required="true"` と `@NotBlank` が両方設定されており、
どちらのメッセージが出るか不定で管理が煩雑だった。

**方針**: `required` 属性をすべて廃止し、Bean Validation に一本化する。
- バリデーションロジック → Bean のフィールドアノテーションで管理
- ラベルの「※必須 / 任意」表示 → 複合コンポーネントの `required` 属性（表示制御のみ）で管理

---

## dads:button にリテラルナビゲーション結果を渡すと ClassCastException

**問題**: `index.xhtml` や `list.xhtml` で `<dads:button action="register" .../>` と書いたら
起動時に `ClassCastException: String cannot be cast to ValueExpression` が発生した。

**原因**: `dads:button` の `action` 属性は composite component インターフェースで
`method-signature="java.lang.String action()"` と宣言されている。
この宣言があると JSF は呼び出し元で渡された値を `MethodExpression` に変換しようとする。
しかしリテラル文字列 `"register"` は `ValueExpression` でも `MethodExpression` でもないため、
`retargetMethodExpressions` フェーズで `String → ValueExpression` キャストに失敗する。

`h:commandButton action="register"` が問題なく動くのは、JSF コアコンポーネントが
リテラル値を特別扱い（固定ナビゲーション結果として処理）するためであり、
composite component 経由では同じ挙動にならない。

**解決策**: フロー入口など「リテラル文字列でナビゲーションしたいだけ」のボタンは
`dads:button` を使わず `h:commandButton` を直接書く。

```xml
<!-- NG: composite component はリテラルを MethodExpression に変換しようとして失敗 -->
<dads:button action="register" ... />

<!-- OK: h:commandButton はリテラルをそのままナビゲーション結果として扱う -->
<h:commandButton action="register"
                 styleClass="dads-button"
                 pt:data-type="solid-fill"
                 pt:data-size="lg" />
```

**適用ルール（フェーズ7まで）**: `dads:button` の `action` には必ず EL 式（`#{bean.method}` 形式）を渡すこと。
フェーズ8で `outcome` 属性を追加し、リテラル遷移先は `outcome="register"` で指定可能になった。

---

## dads-components jar のコンポーネントが jsf-sample に反映されない

**問題**: `dads-components` に新しいコンポーネント（例: `link.xhtml`）を追加しても、
jsf-sample で `<dads:link>` を使うと「no tag was defined for name: link」エラーになる。

**原因**: jsf-sample の `pom.xml` に `dads-components` 依存が未追加のため、
JSF は `dads-components` jar ではなく `jsf-sample/src/main/webapp/resources/dads/` をコンポーネントの解決先として使っている。
ローカルの `resources/dads/` に該当ファイルがなければ「未定義のタグ」になる。

**現状の対処（フェーズ7完了前）**: `dads-components` の XHTML ファイルを
`jsf-sample/src/main/webapp/resources/dads/` にも同期コピーして両方に持つ。

**恒久対応**: フェーズ7の残課題（jsf-sample pom.xml に依存追加 / ローカル resources/dads/ 削除）を完了させれば
dads-components jar 単体からコンポーネントが解決されるようになる。

---

## JSF ライフサイクルと searchAddress() の呼び出し

**前提**: JSF の Ajax で `ajaxExecute` に `postalCode` を指定した場合、
`postalCode` フィールドのバリデーションが失敗すると `searchAddress()` は呼ばれない（Invoke Application フェーズに到達しない）。

**影響**: `searchAddress()` の中でバリデーションを行っても、未入力の場合は到達しない。
→ 未入力チェックは Bean Validation に任せ、`searchAddress()` には正常系のみ書く。
