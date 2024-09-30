package com.example.chessserver.service;

import com.example.chessserver.component.Chessboard;
import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.model.ChessmanWithProperties;
import com.example.chessserver.model.enums.Chessman;
import com.example.openapi.chessserver.model.Color;
import jakarta.annotation.Nonnull;

public interface ChessmanMovementValidatorService {

    boolean isCheckMate(@Nonnull Chessboard chessboard, @Nonnull Color colorOfPlayer);

    boolean isDrawGame(@Nonnull Chessboard chessboard);

    boolean isChessmanAllowedToMove(@Nonnull Chessboard chessboard, ChessmanWithProperties chessmanWithProperties,
                                    ChessCoordinate coordinateTo);

    boolean isPromotion(@Nonnull Chessboard chessboard, @Nonnull ChessmanWithProperties chessmanWithProperties,
                        ChessCoordinate coordinateTo);

    boolean isPromotionAllowed(@Nonnull Chessman chessman);

    boolean isNotInChessBorder(ChessCoordinate coordinate);

}
