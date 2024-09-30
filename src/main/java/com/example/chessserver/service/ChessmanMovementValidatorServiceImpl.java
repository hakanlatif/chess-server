package com.example.chessserver.service;

import com.example.chessserver.component.Chessboard;
import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.model.ChessmanWithProperties;
import com.example.chessserver.model.enums.Chessman;
import com.example.openapi.chessserver.model.Color;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ChessmanMovementValidatorServiceImpl implements ChessmanMovementValidatorService {

    private static final Integer CHESSBOARD_START_INDEX = 1;
    private static final Integer CHESSBOARD_END_INDEX = 8;

    @Override
    public boolean isCheckMate(@Nonnull Chessboard chessboard, @Nonnull Color colorOfPlayer) {
        Color colorOfOpponent = colorOfPlayer == Color.BLACK ? Color.WHITE : Color.BLACK;

        ChessmanWithProperties kingOfOpponent = chessboard.getKing(colorOfOpponent);
        ChessmanWithProperties kingOfPlayer = chessboard.getKing(colorOfPlayer);

        return kingOfOpponent == null || kingOfPlayer == null;
    }

    @Override
    public boolean isDrawGame(@Nonnull Chessboard chessboard) {
        ChessmanWithProperties kingOfBlack = chessboard.getKing(Color.BLACK);
        ChessmanWithProperties kingOfWhite = chessboard.getKing(Color.WHITE);

        List<ChessmanWithProperties> allBlackChessmanWithProperties = chessboard.getAllChessmanWithProperties(Color.BLACK);
        List<ChessmanWithProperties> allWhiteChessmanWithProperties = chessboard.getAllChessmanWithProperties(Color.WHITE);

        if (kingOfBlack == null || kingOfWhite == null) {
            return false;
        }

        // There are only 1 kings at black, there are one kings at white
        if (allBlackChessmanWithProperties.size() == 1 && allWhiteChessmanWithProperties.size() == 1) {
            return true;
        }

        // There are 1 king at black, there are 1 king at white and there are one bishop or knight at white
        if (allBlackChessmanWithProperties.size() == 1 &&
                allWhiteChessmanWithProperties.size() == 2 &&
                allWhiteChessmanWithProperties.stream()
                        .filter(chessmanWithProperties -> chessmanWithProperties.getColor() == Color.WHITE &&
                                (chessmanWithProperties.getChessman() == Chessman.BISHOP ||
                                        chessmanWithProperties.getChessman() == Chessman.KNIGHT)).count() == 1) {
            return true;
        }

        // There are 1 king at white, there are 1 king at black and there are one bishop or knight
        if (allBlackChessmanWithProperties.size() == 2 &&
                allWhiteChessmanWithProperties.size() == 1 &&
                allBlackChessmanWithProperties.stream()
                        .filter(chessmanWithProperties -> chessmanWithProperties.getColor() == Color.BLACK &&
                                (chessmanWithProperties.getChessman() == Chessman.BISHOP ||
                                        chessmanWithProperties.getChessman() == Chessman.KNIGHT)).count() == 1) {
            return true;
        }

        // There are 1 king at white, there are 1 king at black and there are 2 rooks at black
        if (allBlackChessmanWithProperties.size() == 3 &&
                allWhiteChessmanWithProperties.size() == 1 &&
                allBlackChessmanWithProperties.stream()
                        .filter(chessmanWithProperties -> chessmanWithProperties.getColor() == Color.BLACK &&
                                chessmanWithProperties.getChessman() == Chessman.ROOK).count() == 2) {
            return true;
        }

        // There are 1 king at white, there are 1 king at black and there are 2 rooks at white
        return allBlackChessmanWithProperties.size() == 1 &&
                allWhiteChessmanWithProperties.size() == 3 &&
                allWhiteChessmanWithProperties.stream()
                        .filter(chessmanWithProperties -> chessmanWithProperties.getColor() == Color.WHITE &&
                                chessmanWithProperties.getChessman() == Chessman.ROOK).count() == 2;
    }

    @Override
    public boolean isChessmanAllowedToMove(@Nonnull Chessboard chessboard, ChessmanWithProperties chessmanWithProperties,
                                           ChessCoordinate coordinateTo) {
        if (chessboard.isSlotNotAvailableToMoveOn(coordinateTo, chessmanWithProperties.getColor())) {
            return false;
        }

        ChessCoordinate coordinateFrom = chessmanWithProperties.getCoordinate();
        Chessman chessman = chessmanWithProperties.getChessman();
        Color color = chessmanWithProperties.getColor();

        if (chessman == Chessman.PAWN) {
            if (chessboard.isFirstRound(color)) {
                if (isMovingToNStepUp(chessboard, coordinateFrom, coordinateTo, 2, color)) {
                    return true;
                }
            }

            if (isMovingToNStepUp(chessboard, coordinateFrom, coordinateTo, 1, color)) {
                return true;
            }

            return chessboard.isOpponentLocatedAtSlot(coordinateTo, color) &&
                    isMovingToOneStepToCorner(coordinateFrom, coordinateTo, color);
            // TODO: Castling
        } else if (chessman == Chessman.ROOK) {
            return isMovingHorizontalOrVertical(chessboard, coordinateFrom, coordinateTo);
        } else if (chessman == Chessman.KNIGHT) {
            return isMovingInLShape(coordinateFrom, coordinateTo);
        } else if (chessman == Chessman.BISHOP) {
            return isMovingDiagonal(chessboard, coordinateFrom, coordinateTo);
        } else if (chessman == Chessman.QUEEN) {
            return isMovingDiagonal(chessboard, coordinateFrom, coordinateTo) ||
                    isMovingHorizontalOrVertical(chessboard, coordinateFrom, coordinateTo);
        } else if (chessman == Chessman.KING) {
            return isMovingToOneStepToCorner(coordinateFrom, coordinateTo, color) ||
                    isMovingToOneStepVerticalOrHorizontal(coordinateFrom, coordinateTo);
        }

        return true;
    }

    @Override
    public boolean isPromotion(@Nonnull Chessboard chessboard, @Nonnull ChessmanWithProperties chessmanWithProperties,
                               ChessCoordinate coordinateTo) {
        if (chessmanWithProperties.getChessman() != Chessman.PAWN) {
            return false;
        }

        int coordOfPromotion = chessmanWithProperties.getColor() == Color.BLACK ? CHESSBOARD_END_INDEX : CHESSBOARD_START_INDEX;
        return chessboard.isSlotAvailableToMoveOn(coordinateTo,
                chessmanWithProperties.getColor()) && coordinateTo.getY() == coordOfPromotion;
    }

    @Override
    public boolean isPromotionAllowed(@Nonnull Chessman chessman) {
        return (chessman == Chessman.QUEEN || chessman == Chessman.ROOK || chessman == Chessman.BISHOP
                || chessman == Chessman.KNIGHT);
    }

    @Override
    public boolean isNotInChessBorder(ChessCoordinate coordinate) {
        return !isInChessboardBorder(coordinate);
    }

    private boolean isInChessboardBorder(ChessCoordinate coordinate) {
        if (coordinate == null || coordinate.getXy() == null) {
            return false;
        }

        return (coordinate.getXInt() >= CHESSBOARD_START_INDEX && coordinate.getXInt() <= CHESSBOARD_END_INDEX) &&
                (coordinate.getY() >= CHESSBOARD_START_INDEX && coordinate.getY() <= CHESSBOARD_END_INDEX);
    }

    private boolean isMovingToOneStepVerticalOrHorizontal(ChessCoordinate coordinateFrom,
                                                          ChessCoordinate coordinateTo) {
        if (coordinateFrom.getXInt() - 1 == coordinateTo.getXInt() && coordinateFrom.getY() == coordinateTo.getY()) {
            return true;
        }

        if (coordinateFrom.getXInt() + 1 == coordinateTo.getXInt() && coordinateFrom.getY() == coordinateTo.getY()) {
            return true;
        }

        if (coordinateFrom.getXInt() == coordinateTo.getXInt() && coordinateFrom.getY() - 1 == coordinateTo.getY()) {
            return true;
        }

        return coordinateFrom.getXInt() == coordinateTo.getXInt() && coordinateFrom.getY() + 1 == coordinateTo.getY();
    }

    private boolean isMovingToNStepUp(Chessboard chessboard, ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo,
                                      int numOfSteps, Color color) {
        if (chessboard.isPathNotFreeFromChessman(coordinateFrom, coordinateTo)) {
            return false;
        }

        if (Color.BLACK == color) {
            return coordinateFrom.getXInt() == coordinateTo.getXInt() && coordinateFrom.getY() - numOfSteps == coordinateTo.getY();
        } else {
            return coordinateFrom.getXInt() == coordinateTo.getXInt() && coordinateFrom.getY() + numOfSteps == coordinateTo.getY();
        }
    }

    private boolean isMovingToOneStepToCorner(ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo, Color color) {
        boolean isMovingToRightCorner = isMovingOneStepToRightCorner(coordinateFrom, coordinateTo, color);
        boolean isMovingToLeftCorner = isMovingOneStepToLeftCorner(coordinateFrom, coordinateTo, color);
        return isMovingToRightCorner || isMovingToLeftCorner;
    }

    private boolean isMovingOneStepToRightCorner(ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo, Color color) {
        if (Color.BLACK == color) {
            return coordinateFrom.getXInt() - 1 == coordinateTo.getXInt() && coordinateFrom.getY() + 1 == coordinateTo.getY();
        } else {
            return coordinateFrom.getXInt() + 1 == coordinateTo.getXInt() && coordinateFrom.getY() + 1 == coordinateTo.getY();
        }
    }

    private boolean isMovingOneStepToLeftCorner(ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo, Color color) {
        if (Color.BLACK == color) {
            return coordinateFrom.getXInt() - 1 == coordinateTo.getXInt() && coordinateFrom.getY() - 1 == coordinateTo.getY();
        } else {
            return coordinateFrom.getXInt() + 1 == coordinateTo.getXInt() && coordinateFrom.getY() - 1 == coordinateTo.getY();
        }
    }

    private boolean isMovingDiagonal(Chessboard chessboard, ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo) {
        if (chessboard.isPathNotFreeFromChessman(coordinateFrom, coordinateTo)) {
            return false;
        }

        int diagonalChangeInX = coordinateTo.getXInt() - coordinateFrom.getXInt();
        int diagonalChangeInY = coordinateTo.getY() - coordinateFrom.getY();
        return diagonalChangeInX == diagonalChangeInY;
    }

    private boolean isMovingHorizontalOrVertical(Chessboard chessboard, ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo) {
        boolean isMovingVertical = isMovingVertical(chessboard, coordinateFrom, coordinateTo);
        boolean isMovingHorizontal = isMovingHorizontal(chessboard, coordinateFrom, coordinateTo);
        return isMovingVertical || isMovingHorizontal;
    }

    private boolean isMovingVertical(Chessboard chessboard, ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo) {
        if (chessboard.isPathNotFreeFromChessman(coordinateFrom, coordinateTo)) {
            return false;
        }

        return coordinateFrom.getXInt() == coordinateTo.getXInt() &&
                (coordinateFrom.getY() <= coordinateTo.getY() || coordinateFrom.getY() >= coordinateTo.getY());
    }

    private boolean isMovingHorizontal(Chessboard chessboard, ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo) {
        if (chessboard.isPathNotFreeFromChessman(coordinateFrom, coordinateTo)) {
            return false;
        }

        return coordinateFrom.getY() == coordinateTo.getY() &&
                (coordinateFrom.getXInt() <= coordinateTo.getXInt() || coordinateFrom.getXInt() >= coordinateTo.getXInt());
    }

    private boolean isMovingInLShape(ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo) {
        if (coordinateFrom.getXInt() + 1 == coordinateTo.getXInt() &&
                coordinateFrom.getY() == coordinateTo.getY() + 2) {
            return true;
        } else if (coordinateFrom.getXInt() + 2 == coordinateTo.getXInt() &&
                coordinateFrom.getY() + 1 == coordinateTo.getY()) {
            return true;
        } else if (coordinateFrom.getXInt() + 2 == coordinateTo.getXInt() &&
                coordinateFrom.getY() == coordinateTo.getY() - 1) {
            return true;
        } else if (coordinateFrom.getXInt() + 1 == coordinateTo.getXInt() &&
                coordinateFrom.getY() == coordinateTo.getY() - 2) {
            return true;
        } else if (coordinateFrom.getXInt() - 1 == coordinateTo.getXInt() &&
                coordinateFrom.getY() == coordinateTo.getY() - 2) {
            return true;
        } else if (coordinateFrom.getXInt() - 2 == coordinateTo.getXInt() &&
                coordinateFrom.getY() == coordinateTo.getY() - 1) {
            return true;
        } else if (coordinateFrom.getXInt() - 2 == coordinateTo.getXInt() &&
                coordinateFrom.getY() == coordinateTo.getY() + 1) {
            return true;
        } else
            return coordinateFrom.getXInt() - 1 == coordinateTo.getXInt() &&
                    coordinateFrom.getY() == coordinateTo.getY() + 2;
    }

}
