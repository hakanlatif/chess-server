package com.example.chessserver.component;

import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.model.ChessmanWithProperties;
import com.example.chessserver.model.enums.Chessman;
import com.example.openapi.chessserver.model.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChessboardTest {

    private static final String CHESSBOARD_DELIMITER = ",";

    private static final String CHESSBOARD_INITIAL_READABLE = "a1row,a2paw,a7pab,a8rob,b1knw,b2paw,b7pab,b8knb," +
            "c1biw,c2paw,c7pab,c8bib,d1quw,d2paw,d7pab,d8kib,e1kiw,e2paw,e7pab,e8qub,f1biw,f2paw,f7pab,f8bib," +
            "g1knw,g2paw,g7pab,g8knb,h1row,h2paw,h7pab,h8rob";

    private static final Chessboard CHESSBOARD_INITIAL =
            new Chessboard(StringUtils.join(
                    Arrays.asList(
                            "a8rob", "b8knb", "c8bib", "e8qub", "d8kib", "f8bib", "g8knb", "h8rob",
                            "a7pab", "b7pab", "c7pab", "d7pab", "e7pab", "f7pab", "g7pab", "h7pab",

                            "a2paw", "b2paw", "c2paw", "d2paw", "e2paw", "f2paw", "g2paw", "h2paw",
                            "a1row", "b1knw", "c1biw", "d1quw", "e1kiw", "f1biw", "g1knw", "h1row"),
                    CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_SECOND_ROUND =
            new Chessboard(StringUtils.join(
                    Arrays.asList(
                            "a8rob", "b8knb", "c8bib", "e8qub", "d8kib", "f8bib", "g8knb", "h8rob",
                            "a6pab", "b7pab", "c7pab", "d7pab", "e7pab", "f7pab", "g7pab", "h7pab",

                            "a2paw", "b2paw", "c2paw", "d2paw", "e2paw", "f2paw", "g2paw", "h2paw",
                            "a1row", "b1knw", "c1biw", "d1quw", "e1kiw", "f1biw", "g1knw", "h1row"),
                    CHESSBOARD_DELIMITER));

    private static final Chessboard CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_VERTICAL =
            new Chessboard(StringUtils.join(Arrays.asList(
                            "d7rob", "d5row"),
                    CHESSBOARD_DELIMITER));

    @Test
    void shouldGetChessboard() {
        HashMap<String, HashMap<Integer, ChessmanWithProperties>> expectedChessboardMap = new HashMap<>();

        HashMap<Integer, ChessmanWithProperties> yCoord = new HashMap<>();
        yCoord.put(8, new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("a8"), Color.BLACK));
        expectedChessboardMap.put("a", yCoord);

        Chessboard chessboard = new Chessboard("a8rob");
        Map<String, HashMap<Integer, ChessmanWithProperties>> actualChessboardMap = chessboard.getChessboardMap();

        assertEquals(expectedChessboardMap, actualChessboardMap);
    }

    @Test
    void shouldCheckIfSlotNotAvailableToMoveOnWithSameColor() {
        ChessCoordinate chessCoordinate = new ChessCoordinate("a8");
        assertTrue(CHESSBOARD_INITIAL.isSlotNotAvailableToMoveOn(chessCoordinate, Color.BLACK),
                "Not free a8 showed as free slot");
    }

    @Test
    void shouldCheckIfSlotNotAvailableToMoveOnWithDifferentColor() {
        ChessCoordinate chessCoordinate = new ChessCoordinate("a8");

        assertFalse(CHESSBOARD_INITIAL.isSlotNotAvailableToMoveOn(chessCoordinate, Color.WHITE),
                "Free slot a8 showed as not free slot");
    }

    @Test
    void shouldCheckIfPathIsNotFreeFromChessman() {
        ChessCoordinate chessCoordinateFrom = new ChessCoordinate("d7");
        ChessCoordinate chessCoordinateTo = new ChessCoordinate("d2");
        assertTrue(CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_VERTICAL.isPathNotFreeFromChessman(chessCoordinateFrom, chessCoordinateTo),
                "Not empty path showed as empty");
    }

    @Test
    void shouldCheckIfPathIsFreeFromChessman() {
        ChessCoordinate chessCoordinateFrom = new ChessCoordinate("d7");
        ChessCoordinate chessCoordinateTo = new ChessCoordinate("d6");
        assertTrue(CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_VERTICAL.isPathFreeFromChessman(chessCoordinateFrom, chessCoordinateTo),
                "Empty path showed as not empty");
    }

    @Test
    void shouldCheckIfPathIsFreeFromChessmanWithOpponentAtFrom() {
        ChessCoordinate chessCoordinateFrom = new ChessCoordinate("d7");
        ChessCoordinate chessCoordinateTo = new ChessCoordinate("d5");
        assertTrue(CHESSBOARD_OPPONENT_IS_IN_BETWEEN_OF_COORD_AT_VERTICAL.isPathFreeFromChessman(chessCoordinateFrom, chessCoordinateTo),
                "Empty path showed as not empty");
    }

    @Test
    void shouldCheckIfOpponentLocatedAtSlotWithSameColor() {
        ChessCoordinate chessCoordinate = new ChessCoordinate("a8");
        assertFalse(CHESSBOARD_INITIAL.isOpponentLocatedAtSlot(chessCoordinate, Color.BLACK),
                "Not existing opponent at a8 showed as existing");
    }

    @Test
    void shouldCheckIfOpponentLocatedAtSlotWithDifferentColor() {
        ChessCoordinate chessCoordinate = new ChessCoordinate("a8");
        assertTrue(CHESSBOARD_INITIAL.isOpponentLocatedAtSlot(chessCoordinate, Color.WHITE),
                "Existing opponent at a8 showed as not existing");
    }

    @Test
    void shouldGetAllBlackChessmanWithProperties() {
        List<ChessmanWithProperties> expectedAllChessmanWithProperties = new ArrayList<>() {{
            add(new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("a8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.KNIGHT, new ChessCoordinate("b8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.BISHOP, new ChessCoordinate("c8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.KING, new ChessCoordinate("d8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.QUEEN, new ChessCoordinate("e8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.BISHOP, new ChessCoordinate("f8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.KNIGHT, new ChessCoordinate("g8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("h8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("a7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("b7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("c7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("d7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("e7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("f7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("g7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("h7"), Color.BLACK));
        }};
        expectedAllChessmanWithProperties.sort(Comparator.comparing(c -> c.getCoordinate().getXy()));

        List<ChessmanWithProperties> actualAllChessmanWithProperties = CHESSBOARD_INITIAL.getAllChessmanWithProperties(Color.BLACK);
        assertNotNull(actualAllChessmanWithProperties, "Chessman list is null");

        actualAllChessmanWithProperties.sort(Comparator.comparing(c -> c.getCoordinate().getXy()));

        assertEquals(expectedAllChessmanWithProperties.size(), actualAllChessmanWithProperties.size());
        assertThat(actualAllChessmanWithProperties).isEqualTo(expectedAllChessmanWithProperties);
    }

    @Test
    void shouldGetChessmanWithPropertiesWithCoordinate() {
        ChessCoordinate chessCoordinate = new ChessCoordinate("a8");

        ChessmanWithProperties expectedChessmanWithProperties = new ChessmanWithProperties(
                Chessman.ROOK, chessCoordinate, Color.BLACK
        );

        ChessmanWithProperties chessmanWithProperties = CHESSBOARD_INITIAL.getChessmanWithProperties(chessCoordinate);
        assertEquals(expectedChessmanWithProperties, chessmanWithProperties);
    }

    @Test
    void shouldGetKingForBlack() {
        ChessCoordinate chessCoordinate = new ChessCoordinate("d8");

        ChessmanWithProperties expectedChessmanWithProperties = new ChessmanWithProperties(
                Chessman.KING, chessCoordinate, Color.BLACK
        );

        ChessmanWithProperties chessmanWithProperties = CHESSBOARD_INITIAL.getKing(Color.BLACK);
        assertEquals(expectedChessmanWithProperties, chessmanWithProperties);
    }

    @Test
    void shouldNotGetKingWithEmptyBoard() {
        Chessboard chessboard = new Chessboard("");
        ChessmanWithProperties chessmanWithProperties = chessboard.getKing(Color.BLACK);
        assertNull(chessmanWithProperties, "King should be null");
    }

    @Test
    void shouldNotGetKingWithBoardWithoutKing() {
        Chessboard chessboard = new Chessboard("f8bib");
        ChessmanWithProperties chessmanWithProperties = chessboard.getKing(Color.BLACK);
        assertNull(chessmanWithProperties, "King should be null");
    }

    @Test
    void shouldRemoveChessmanWithProperties() {
        List<ChessmanWithProperties> expectedAllChessmanWithProperties = new ArrayList<>() {{
            add(new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("a8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.KNIGHT, new ChessCoordinate("b8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.BISHOP, new ChessCoordinate("c8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.KING, new ChessCoordinate("d8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.QUEEN, new ChessCoordinate("e8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.BISHOP, new ChessCoordinate("f8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.KNIGHT, new ChessCoordinate("g8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("a7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("b7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("c7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("d7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("e7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("f7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("g7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("h7"), Color.BLACK));
        }};
        expectedAllChessmanWithProperties.sort(Comparator.comparing(c -> c.getCoordinate().getXy()));

        Chessboard chessboard = new Chessboard(CHESSBOARD_INITIAL_READABLE);
        chessboard.removeChessmanWithProperties(new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("h8"), Color.BLACK));
        List<ChessmanWithProperties> actualAllChessmanWithProperties = chessboard.getAllChessmanWithProperties(Color.BLACK);
        assertNotNull(actualAllChessmanWithProperties, "Chessman list is null");

        actualAllChessmanWithProperties.sort(Comparator.comparing(c -> c.getCoordinate().getXy()));

        assertEquals(expectedAllChessmanWithProperties.size(), actualAllChessmanWithProperties.size());
        assertThat(actualAllChessmanWithProperties).isEqualTo(expectedAllChessmanWithProperties);
    }

    @Test
    void shouldPutChessmanWithProperties() {
        List<ChessmanWithProperties> expectedAllChessmanWithProperties = new ArrayList<>() {{
            add(new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("a8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.KNIGHT, new ChessCoordinate("b8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.BISHOP, new ChessCoordinate("c8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.KING, new ChessCoordinate("d8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.QUEEN, new ChessCoordinate("e8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.BISHOP, new ChessCoordinate("f8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.KNIGHT, new ChessCoordinate("g8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("h8"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("a7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("b7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("c7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("d7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("e7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("f7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("g7"), Color.BLACK));
            add(new ChessmanWithProperties(Chessman.PAWN, new ChessCoordinate("h7"), Color.BLACK));
        }};
        expectedAllChessmanWithProperties.sort(Comparator.comparing(c -> c.getCoordinate().getXy()));

        Chessboard chessboard = new Chessboard(CHESSBOARD_INITIAL_READABLE);
        chessboard.removeChessmanWithProperties(new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("h8"), Color.BLACK));
        chessboard.putChessmanWithProperties(new ChessmanWithProperties(Chessman.ROOK, new ChessCoordinate("h8"), Color.BLACK));

        List<ChessmanWithProperties> actualAllChessmanWithProperties = chessboard.getAllChessmanWithProperties(Color.BLACK);
        assertNotNull(actualAllChessmanWithProperties, "Chessman list is null");

        actualAllChessmanWithProperties.sort(Comparator.comparing(c -> c.getCoordinate().getXy()));

        assertEquals(expectedAllChessmanWithProperties.size(), actualAllChessmanWithProperties.size());
        assertThat(actualAllChessmanWithProperties).isEqualTo(expectedAllChessmanWithProperties);
    }

    @Test
    void shouldGetChessboardReadable() {
        List<String> expectedChessboardParts = Arrays.asList(CHESSBOARD_INITIAL_READABLE.split(CHESSBOARD_DELIMITER));
        expectedChessboardParts.sort(String::compareTo);
        List<String> actualChessboardParts = Arrays.asList(CHESSBOARD_INITIAL.getChessboardReadable().split(CHESSBOARD_DELIMITER));
        actualChessboardParts.sort(String::compareTo);

        assertThat(expectedChessboardParts).isEqualTo(actualChessboardParts);
    }

    @Test
    void shouldCheckIfFirstRound() {
        assertTrue(CHESSBOARD_INITIAL.isFirstRound(Color.BLACK), "First round not detected correctly");
    }

    @Test
    void shouldCheckIfNotFirstRound() {
        assertFalse(CHESSBOARD_SECOND_ROUND.isFirstRound(Color.BLACK), "Second round showed as first round");
    }

}