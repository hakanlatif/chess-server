package com.example.chessserver.component;

import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.model.ChessmanWithProperties;
import com.example.chessserver.model.enums.Chessman;
import com.example.chessserver.util.ChessboardMapConversionUtil;
import com.example.openapi.chessserver.model.Color;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
public class Chessboard {

    private static final String CHESSBOARD_DELIMITER = ",";
    private static final Integer CHESSBOARD_SIZE = 8;

    private static final List<String> EMPTY_CHESSBOARD_BLACK_SIDE =
            Arrays.asList(
                    "a8rob", "b8knb", "c8bib", "e8qub", "d8kib", "f8bib", "g8knb", "h8rob",
                    "a7pab", "b7pab", "c7pab", "d7pab", "e7pab", "f7pab", "g7pab", "h7pab");
    private static final List<String> EMPTY_CHESSBOARD_WHITE_SIDE =
            Arrays.asList(
                    "a2paw", "b2paw", "c2paw", "d2paw", "e2paw", "f2paw", "g2paw", "h2paw",
                    "a1row", "b1knw", "c1biw", "d1quw", "e1kiw", "f1biw", "g1knw", "h1row");

    private static final String EMPTY_CHESSBOARD = StringUtils.join(EMPTY_CHESSBOARD_BLACK_SIDE, CHESSBOARD_DELIMITER) +
            CHESSBOARD_DELIMITER + StringUtils.join(EMPTY_CHESSBOARD_WHITE_SIDE, CHESSBOARD_DELIMITER);

    private final Map<String, HashMap<Integer, ChessmanWithProperties>> chessboardMap;

    public Chessboard() {
        this.chessboardMap = ChessboardMapConversionUtil.createChessboardMap(EMPTY_CHESSBOARD);
    }

    public Chessboard(String chessboardReadable) {
        this.chessboardMap = ChessboardMapConversionUtil.createChessboardMap(chessboardReadable);
    }

    // Checks if there is a chessman with same color at given slot
    public boolean isSlotNotAvailableToMoveOn(ChessCoordinate coordinate, @Nonnull Color colorOfPlayer) {
        return !isSlotAvailableToMoveOn(coordinate, colorOfPlayer);
    }

    // Checks if slot is empty or there is an opponent
    public boolean isSlotAvailableToMoveOn(ChessCoordinate coordinate, @Nonnull Color colorOfPlayer) {
        ChessmanWithProperties chessmanWithProperties = getChessmanWithProperties(coordinate);
        return chessmanWithProperties == null || chessmanWithProperties.getColor() != colorOfPlayer;
    }

    // Checks if there is a chessman in given range
    public boolean isPathNotFreeFromChessman(ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo) {
        return !isPathFreeFromChessman(coordinateFrom, coordinateTo);
    }

    // Checks if there is no chessman in given range
    public boolean isPathFreeFromChessman(ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo) {
        boolean isDiagonal = coordinateFrom.getXInt() != coordinateTo.getXInt() && coordinateFrom.getY() != coordinateTo.getY();
        ChessCoordinate coordinateOfPlayer = new ChessCoordinate(coordinateFrom.getXy());

        for (char i = 0; i <= CHESSBOARD_SIZE; i++) {
            for (int j = 1; j <= CHESSBOARD_SIZE; j++) {
                ChessCoordinate chessCoordinate = new ChessCoordinate(i, j);
                ChessmanWithProperties chessmanWithProperties = getChessmanWithProperties(chessCoordinate);

                if (chessmanWithProperties == null || chessCoordinate.equals(coordinateOfPlayer)) {
                    continue;
                }

                if (isDiagonal) {
                    // left corner
                    if (coordinateFrom.getY() > coordinateTo.getY() &&
                            chessCoordinate.getY() < coordinateFrom.getY() &&
                            chessCoordinate.getY() > coordinateTo.getY()) {
                        return false;
                        // right corner
                    } else if (coordinateFrom.getY() < coordinateTo.getY() &&
                            chessCoordinate.getY() > coordinateFrom.getY() &&
                            chessCoordinate.getY() < coordinateTo.getY()) {
                        return false;
                    }
                } else {
                    // up
                    if (coordinateFrom.getY() < coordinateTo.getY() &&
                            // same axis
                            chessCoordinate.getXInt() == coordinateFrom.getXInt() &&
                            // in rage
                            chessCoordinate.getY() > coordinateFrom.getY() &&
                            chessCoordinate.getY() < coordinateTo.getY()) {
                        return false;
                        // down
                    } else if (coordinateFrom.getY() > coordinateTo.getY() &&
                            // same axis
                            chessCoordinate.getXInt() == coordinateFrom.getXInt() &&
                            // in rage
                            chessCoordinate.getY() < coordinateFrom.getY() &&
                            chessCoordinate.getY() > coordinateTo.getY()) {
                        return false;
                        // left
                    } else if (coordinateFrom.getXInt() > coordinateTo.getXInt() &&
                            // same axis
                            chessCoordinate.getY() == coordinateFrom.getY() &&
                            // in rage
                            chessCoordinate.getXInt() < coordinateFrom.getXInt() &&
                            chessCoordinate.getXInt() > coordinateTo.getXInt()) {
                        return false;
                        // right
                    } else if (coordinateFrom.getXInt() < coordinateTo.getXInt() &&
                            // same axis
                            chessCoordinate.getY() == coordinateFrom.getY() &&
                            // in rage
                            chessCoordinate.getXInt() > coordinateFrom.getXInt() &&
                            chessCoordinate.getXInt() < coordinateTo.getXInt()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // Checks if there is an opponent at slot
    public boolean isOpponentLocatedAtSlot(ChessCoordinate coordinate, @Nonnull Color colorOfPlayer) {
        ChessmanWithProperties chessmanWithProperties = getChessmanWithProperties(coordinate);
        return chessmanWithProperties != null && chessmanWithProperties.getColor() != colorOfPlayer;
    }

    public List<ChessmanWithProperties> getAllChessmanWithProperties(Color color) {
        List<ChessmanWithProperties> allChessmanWithProperties = new ArrayList<>();

        for (char c = 'a'; c <= 'h'; c++) {
            for (int j = 1; j <= CHESSBOARD_SIZE; j++) {
                ChessCoordinate chessCoordinate = new ChessCoordinate(String.valueOf(c), j);
                ChessmanWithProperties chessmanWithProperties = getChessmanWithProperties(chessCoordinate);
                if (chessmanWithProperties != null && chessmanWithProperties.getColor() == color) {
                    allChessmanWithProperties.add(chessmanWithProperties);
                }
            }
        }

        return allChessmanWithProperties;
    }

    public ChessmanWithProperties getChessmanWithProperties(ChessCoordinate coordinate) {
        HashMap<Integer, ChessmanWithProperties> xCoord = chessboardMap.get(coordinate.getX());
        if (MapUtils.isEmpty(xCoord)) {
            return null;
        }

        return xCoord.get(coordinate.getY());
    }

    public ChessmanWithProperties getKing(Color color) {
        if (MapUtils.isEmpty(chessboardMap)) {
            return null;
        }

        for (Map.Entry<String, HashMap<Integer, ChessmanWithProperties>> entryXCoord : chessboardMap.entrySet()) {
            String xCoord = entryXCoord.getKey();

            HashMap<Integer, ChessmanWithProperties> yCoordMap = chessboardMap.get(xCoord);
            if (MapUtils.isEmpty(yCoordMap)) {
                return null;
            }

            for (Map.Entry<Integer, ChessmanWithProperties> entryYCoord : yCoordMap.entrySet()) {
                ChessmanWithProperties chessmanWithProperties = entryYCoord.getValue();
                if (chessmanWithProperties.getChessman() == Chessman.KING &&
                        chessmanWithProperties.getColor() == color) {
                    return chessmanWithProperties;
                }
            }
        }

        return null;
    }

    public void removeChessmanWithProperties(ChessmanWithProperties chessmanWithProperties) {
        HashMap<Integer, ChessmanWithProperties> xCoordMap = chessboardMap.get(chessmanWithProperties.getCoordinate().getX());
        if (MapUtils.isEmpty(xCoordMap)) {
            return;
        }

        xCoordMap.remove(chessmanWithProperties.getCoordinate().getY(), chessmanWithProperties);
        chessboardMap.put(chessmanWithProperties.getCoordinate().getX(), xCoordMap);
    }

    public void putChessmanWithProperties(ChessmanWithProperties chessmanWithProperties) {
        HashMap<Integer, ChessmanWithProperties> xCoordMap = chessboardMap.get(chessmanWithProperties.getCoordinate().getX());
        if (MapUtils.isEmpty(xCoordMap)) {
            xCoordMap = new HashMap<>();
        }

        xCoordMap.put(chessmanWithProperties.getCoordinate().getY(), chessmanWithProperties);
        chessboardMap.put(chessmanWithProperties.getCoordinate().getX(), xCoordMap);
    }

    public String getChessboardReadable() {
        return ChessboardMapConversionUtil.createChessboardReadable(this.chessboardMap);
    }

    public boolean isFirstRound(Color color) {
        String emptyBoard = Color.BLACK.equals(color) ?
                StringUtils.join(EMPTY_CHESSBOARD_BLACK_SIDE, CHESSBOARD_DELIMITER) :
                StringUtils.join(EMPTY_CHESSBOARD_WHITE_SIDE, CHESSBOARD_DELIMITER);

        Chessboard emptyChessboard = new Chessboard(emptyBoard);
        List<ChessmanWithProperties> emptyChessboardProperties = emptyChessboard.getAllChessmanWithProperties(color);
        List<ChessmanWithProperties> allChessmanWithProperties = getAllChessmanWithProperties(color);

        for (ChessmanWithProperties chessmanWithProperties : allChessmanWithProperties) {
            if (!emptyChessboardProperties.contains(chessmanWithProperties)) {
                return false;
            }
        }

        return true;
    }

}
