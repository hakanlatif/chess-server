package com.example.chessserver.model;

import com.example.chessserver.model.enums.Chessman;
import com.example.openapi.chessserver.model.Color;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChessmanWithProperties {

    private Chessman chessman;
    private ChessCoordinate coordinate;
    private final Color color;

}
