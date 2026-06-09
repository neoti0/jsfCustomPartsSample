package com.example.dads.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/**
 * dads:button の outcome 属性（リテラル遷移先）を MethodExpression として提供する CDI Bean。
 *
 * h:commandButton の action は常に MethodExpression として処理されるため、
 * cc.attrs.outcome（String）を直接 action に渡すと ClassCastException が発生する。
 * このクラスの to(outcome) メソッドを経由することで
 * #{dadsNavigator.to(cc.attrs.outcome)} が正常な MethodExpression になる。
 */
@Named("dadsNavigator")
@RequestScoped
public class DadsNavigator {

    public String to(String outcome) {
        return outcome;
    }
}
