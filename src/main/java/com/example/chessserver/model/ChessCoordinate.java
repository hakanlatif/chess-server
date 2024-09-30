package com.example.chessserver.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ChessCoordinate {

    private static final Short ASCII_CODE_OF_A_LETTER = 'a';

    private final String x;
    private final int xInt;
    private final int y;
    private final String xy;

    public ChessCoordinate(String xy) {
        if (!StringUtils.isEmpty(xy) && xy.length() == 2) {
            this.x = String.valueOf(xy.charAt(0));
            this.xInt = x.charAt(0) - ASCII_CODE_OF_A_LETTER + 1;
            this.y = Integer.parseInt(xy.substring(1, 2));
            this.xy = xy;
        } else {
            this.x = null;
            this.xInt = -1;
            this.y = -1;
            this.xy = null;
        }
    }

    public ChessCoordinate(String x, int y) {
        this.x = x;
        this.xInt = x.charAt(0) - ASCII_CODE_OF_A_LETTER + 1;
        this.y = y;
        this.xy = x + y;
    }

    public ChessCoordinate(int xInt, int y) {
        this.x = String.valueOf((char) (ASCII_CODE_OF_A_LETTER + (xInt - 1)));
        this.xInt = xInt;
        this.y = y;
        this.xy = x + y;
    }

}
