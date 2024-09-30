package com.example.chessserver.util;

import com.example.chessserver.exception.ServiceException;
import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.model.ChessmanWithProperties;
import com.example.chessserver.model.enums.Chessman;
import com.example.openapi.chessserver.model.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChessboardMapConversionUtil {

    private static final String UNEXPECTED_ERROR = "Unexpected error";
    private static final String CHESSBOARD_DELIMITER = ",";
    private static final int CHESSMAN_MIN_LENGTH = 5;
    private static final String SHORT_NAME_OF_BLACK = "b";

    public static Map<String, HashMap<Integer, ChessmanWithProperties>> createChessboardMap(String chessboardReadable) {
        HashMap<String, HashMap<Integer, ChessmanWithProperties>> chessboardMap = new HashMap<>();

        if (StringUtils.isEmpty(chessboardReadable)) {
            return chessboardMap;
        }

        String[] chessmanParts = chessboardReadable.split(CHESSBOARD_DELIMITER);

        for (String chessmanCoord : chessmanParts) {
            if (chessmanCoord.length() != CHESSMAN_MIN_LENGTH) {
                log.error("Invalid chessman definition '{}'", chessmanCoord);
                throw new ServiceException(UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String xCoord = chessmanCoord.substring(0, 1);
            int yCoord = Integer.parseInt(chessmanCoord.substring(1, 2));
            ChessCoordinate coordinate = new ChessCoordinate(xCoord, yCoord);

            Chessman chessman = Chessman.getByShortName(chessmanCoord.substring(2, 4));
            String colorCode = chessmanCoord.substring(4, 5);
            Color color = colorCode.equals(SHORT_NAME_OF_BLACK) ? Color.BLACK : Color.WHITE;

            placeChessmanToBoard(chessboardMap, coordinate, chessman, color);
        }

        return chessboardMap;
    }

    public static String createChessboardReadable(Map<String, HashMap<Integer, ChessmanWithProperties>> chessboardMap) {
        if (MapUtils.isEmpty(chessboardMap)) {
            return "";
        }

        List<String> chessmen = new ArrayList<>();
        for (Map.Entry<String, HashMap<Integer, ChessmanWithProperties>> entryXCoord : chessboardMap.entrySet()) {
            String xCoord = entryXCoord.getKey();

            HashMap<Integer, ChessmanWithProperties> yCoordMap = chessboardMap.get(xCoord);
            if (MapUtils.isEmpty(yCoordMap)) {
                log.error("X coord {} is missing at chessboard map", xCoord);
                throw new ServiceException(UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            for (Map.Entry<Integer, ChessmanWithProperties> entryYCoord : yCoordMap.entrySet()) {
                ChessmanWithProperties chessmanWithProperties = entryYCoord.getValue();
                if (chessmanWithProperties == null) {
                    continue;
                }

                int yCoord = entryYCoord.getKey();
                Chessman chessman = chessmanWithProperties.getChessman();
                String color = chessmanWithProperties.getColor().name().substring(0, 1).toLowerCase(Locale.ENGLISH);

                chessmen.add(xCoord +
                        yCoord +
                        chessman.getShortName() +
                        color
                );
            }
        }

        return StringUtils.join(chessmen, CHESSBOARD_DELIMITER);
    }

    private static void placeChessmanToBoard(HashMap<String, HashMap<Integer, ChessmanWithProperties>> chessboardMap,
                                             ChessCoordinate coordinate, Chessman chessman, Color color) {
        HashMap<Integer, ChessmanWithProperties> yCoordMap = chessboardMap.get(coordinate.getX());
        if (MapUtils.isEmpty(yCoordMap)) {
            yCoordMap = new HashMap<>();
        }

        ChessmanWithProperties chessmanWithProperties = new ChessmanWithProperties(chessman, coordinate, color);
        yCoordMap.put(coordinate.getY(), chessmanWithProperties);
        chessboardMap.put(coordinate.getX(), yCoordMap);
    }

}
