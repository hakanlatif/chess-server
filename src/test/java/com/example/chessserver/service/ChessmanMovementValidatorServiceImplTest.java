package com.example.chessserver.service;

import com.example.chessserver.component.Chessboard;
import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.model.ChessmanWithProperties;
import com.example.chessserver.model.enums.Chessman;
import com.example.openapi.chessserver.model.Color;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class ChessmanMovementValidatorServiceImplTest {

    private static final String CHESSBOARD_DELIMITER = ",";

    private static final Chessboard CHESSBOARD_PROMOTION_IS_ALLOWED_WITH_FREE_SLOT =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d7paw", "b4pab"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_PROMOTION_IS_NOT_ALLOWED_WITH_NOT_FREE_SLOT =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d7pab", "d8pab", "b4paw"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_PROMOTION_IS_ALLOWED_WITH_OPPONENT =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d8paw", "c7pab"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_SLOT_IS_NOT_FREE =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d6pab", "d5pab", "b4paw"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_DIAGONAL =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d4row", "f6rob"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_VERTICAL =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d5row", "d7rob"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_HORIZONTAL =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d7rob", "f7row"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_OPPONENT_IS_NOT_IN_BETWEEN_OF_COORD =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d7rob", "c5row"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_OPPONENT_IS_AT_SLOT =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d6pab", "b4paw"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_CHECKMATE =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d8kib", "a7row", "b6row", "e1quw"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_DRAW_2_KINGS =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d8kib", "c5kiw"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_DRAW_2_KINGS_1_BLACK_BISHOP =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d8kib", "c5kiw", "a3bib"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_DRAW_2_KINGS_1_WHITE_BISHOP =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d8kib", "c5kiw", "a3biw"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_DRAW_2_KINGS_2_BLACK_ROOKS =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d8kib", "c5kiw", "a3rob", "a6rob"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_DRAW_2_KINGS_2_WHITE_ROOKS =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "d8kib", "c5kiw", "a3row", "a6row"),
                            CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_INITIAL =
            new Chessboard(
                    StringUtils.join(Arrays.asList(
                                    "a8rob", "b8knb", "c8bib", "e8qub", "d8kib", "f8bib", "g8knb", "h8rob",
                                    "a7pab", "b7pab", "c7pab", "d7pab", "e7pab", "f7pab", "g7pab", "h7pab",

                                    "a2paw", "b2paw", "c2paw", "d2paw", "e2paw", "f2paw", "g2paw", "h2paw",
                                    "a1row", "b1knw", "c1biw", "d1quw", "e1kiw", "f1biw", "g1knw", "h1row"),
                            CHESSBOARD_DELIMITER));

    @InjectMocks
    private ChessmanMovementValidatorServiceImpl service;

    @Test
    void shouldFindCheckMate() {
        assertTrue(service.isCheckMate(CHESSBOARD_CHECKMATE, Color.WHITE), "Checkmate should be true");
    }

    @Test
    void shouldNotFindCheckMate() {
        assertFalse(service.isCheckMate(CHESSBOARD_INITIAL, Color.WHITE), "Checkmate should be false");
    }

    @Test
    void shouldFindNotDrawGame() {
        assertFalse(service.isDrawGame(CHESSBOARD_INITIAL), "Game should be not draw");
    }

    @Test
    void shouldFindDrawGameWith2Kings() {
        assertTrue(service.isDrawGame(CHESSBOARD_DRAW_2_KINGS), "Game should be draw with 2 kings");
    }

    @Test
    void shouldFindDrawGameWith2KingsAnd1BlackBishop() {
        assertTrue(service.isDrawGame(CHESSBOARD_DRAW_2_KINGS_1_BLACK_BISHOP),
                "Game should be draw with 2 kings and 1 black bishop");
    }

    @Test
    void shouldFindDrawGameWith2KingsAnd1WhiteBishop() {
        assertTrue(service.isDrawGame(CHESSBOARD_DRAW_2_KINGS_1_WHITE_BISHOP),
                "Game should be draw with 2 kings and 1 black bishop");
    }

    @Test
    void shouldFindDrawGameWith2KingsAnd2BlackRooks() {
        assertTrue(service.isDrawGame(CHESSBOARD_DRAW_2_KINGS_2_BLACK_ROOKS),
                "Game should be draw with 2 kings and 2 white rooks");
    }

    @Test
    void shouldFindDrawGameWith2KingsAnd2WhiteRooks() {
        assertTrue(service.isDrawGame(CHESSBOARD_DRAW_2_KINGS_2_WHITE_ROOKS),
                "Game should be draw with 2 kings and 2 white rooks");
    }

    @Test
    void shouldCheckThatChessmanIsAllowedToMoveIfSlotIsEmpty() {
        assertTrue(service.isChessmanAllowedToMove(
                CHESSBOARD_INITIAL,
                new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("b7"), Color.BLACK),
                new ChessCoordinate("b6")), "Chessman couldn't move to empty slot");
    }

    @Test
    void shouldCheckThatRookIsNotAllowedToMoveIfOpponentIsInBetweenOfCoordAtDiagonalLeft() {
        assertFalse(service.isChessmanAllowedToMove(
                CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_DIAGONAL,
                new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("f6"), Color.BLACK),
                new ChessCoordinate("b2")), "Chessman could move to when there is opponent in between of coordinates");
    }

    @Test
    void shouldCheckThatRookIsNotAllowedToMoveIfOpponentIsInBetweenOfCoordAtDiagonalRight() {
        assertFalse(service.isChessmanAllowedToMove(
                CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_DIAGONAL,
                new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("d4"), Color.BLACK),
                new ChessCoordinate("h8")), "Chessman could move to when there is opponent in between of coordinates");
    }

    @Test
    void shouldCheckThatRookIsNotAllowedToMoveIfOpponentIsInBetweenOfCoordAtVerticalUp() {
        assertFalse(service.isChessmanAllowedToMove(
                CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_VERTICAL,
                new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("d5"), Color.BLACK),
                new ChessCoordinate("d8")), "Chessman could move to when there is opponent in between of coordinates");
    }

    @Test
    void shouldCheckThatRookIsNotAllowedToMoveIfOpponentIsInBetweenOfCoordAtVerticalDown() {
        assertFalse(service.isChessmanAllowedToMove(
                CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_VERTICAL,
                new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("d7"), Color.BLACK),
                new ChessCoordinate("d1")), "Chessman could move to when there is opponent in between of coordinates");
    }

    @Test
    void shouldCheckThatRookIsNotAllowedToMoveIfOpponentIsInBetweenOfCoordAtHorizontalAtRight() {
        assertFalse(service.isChessmanAllowedToMove(
                CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_HORIZONTAL,
                new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("d7"), Color.BLACK),
                new ChessCoordinate("h7")), "Chessman could move to when there is opponent in between of coordinates");
    }

    @Test
    void shouldCheckThatRookIsNotAllowedToMoveIfOpponentIsInBetweenOfCoordAtHorizontalAtLeft() {
        assertFalse(service.isChessmanAllowedToMove(
                CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_HORIZONTAL,
                new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("f7"), Color.BLACK),
                new ChessCoordinate("a7")), "Chessman could move to when there is opponent in between of coordinates");
    }

    @Test
    void shouldCheckThatRookIsAllowedToMoveIfOpponentIsInBetweenOfCoord() {
        assertTrue(service.isChessmanAllowedToMove(
                CHESSBOARD_OPPONENT_IS_NOT_IN_BETWEEN_OF_COORD,
                new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("d7"), Color.BLACK),
                new ChessCoordinate("d2")), "Chessman couldn't move to when there is no chessman in between of coordinates");
    }

    @Test
    void shouldCheckThatChessmanIsAllowedToMoveIfOpponentIsAtSlot() {
        assertTrue(service.isChessmanAllowedToMove(
                CHESSBOARD_OPPONENT_IS_AT_SLOT,
                new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("b7"), Color.BLACK),
                new ChessCoordinate("b6")), "Chessman couldn't move to empty slot");
    }

    @Test
    void shouldCheckThatChessmanIsNotAllowedToMoveIfSlotIsNotFree() {
        assertFalse(service.isChessmanAllowedToMove(
                CHESSBOARD_SLOT_IS_NOT_FREE,
                new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("d6"), Color.BLACK),
                new ChessCoordinate("d5")), "Chessman could move to not free slot");
    }

    @Test
    void shouldCheckPromotionAllowedWithPawnWithFreeSlot() {
        assertTrue(service.isPromotion(
                CHESSBOARD_PROMOTION_IS_ALLOWED_WITH_FREE_SLOT,
                new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("d7"), Color.BLACK),
                new ChessCoordinate("d8")), "Chessman could move to not free slot");
    }

    @Test
    void shouldCheckPromotionNotAllowedWithPawnWithNotFreeSlot() {
        assertFalse(service.isPromotion(
                CHESSBOARD_PROMOTION_IS_NOT_ALLOWED_WITH_NOT_FREE_SLOT,
                new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("d7"), Color.BLACK),
                new ChessCoordinate("d8")), "Chessman could move to not free slot");
    }

    @Test
    void shouldCheckPromotionAllowedWithPawnIfOpponentIsAtSlot() {
        assertTrue(service.isPromotion(
                CHESSBOARD_PROMOTION_IS_ALLOWED_WITH_OPPONENT,
                new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("c7"), Color.BLACK),
                new ChessCoordinate("d8")), "Chessman could move to not free slot");
    }

    @Test
    void shouldCheckPromotionNotAllowedToPawn() {
        assertFalse(service.isPromotionAllowed(Chessman.PAWN), "Pawn couldn't promote to pawn");
    }

    @Test
    void shouldCheckPromotionAllowedToBishop() {
        assertTrue(service.isPromotionAllowed(Chessman.BISHOP), "Pawn could promote to bishop");
    }

}