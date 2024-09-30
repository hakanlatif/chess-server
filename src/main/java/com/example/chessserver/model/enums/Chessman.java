package com.example.chessserver.model.enums;

import lombok.Getter;

@Getter
public enum Chessman {

    PAWN("pa"),
    ROOK("ro"),
    KNIGHT("kn"),
    BISHOP("bi"),
    QUEEN("qu"),
    KING("ki");

    private final String shortName;

    Chessman(String shortName) {
        this.shortName = shortName;
    }

    public static Chessman getByShortName(String shortName) {
        if (shortName == null) {
            return null;
        }

        for (Chessman value : Chessman.values()) {
            if (value.getShortName().equals(shortName)) {
                return value;
            }
        }

        return null;
    }

}
