package kr.co.itid.cms.enums;

import lombok.Getter;

@Getter
public enum Action {
    LOGIN("login"),
    LOGOUT("logout"),
    FORCE("force"),
    RETRIEVE("retrieve"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    VALIDATE("validate");

    private final String value;

    Action(String value) {
        this.value = value;
    }
}