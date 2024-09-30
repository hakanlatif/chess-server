package com.example.chessserver.service;

import com.example.chessserver.component.Chessboard;
import com.example.chessserver.exception.ServiceException;
import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.model.ChessmanWithProperties;
import com.example.chessserver.model.enums.Chessman;
import com.example.chessserver.model.jpa.ChessGame;
import com.example.chessserver.repository.ChessGameRepository;
import com.example.openapi.chessserver.model.ChessboardResponse;
import com.example.openapi.chessserver.model.ChessmanMovementStatus;
import com.example.openapi.chessserver.model.Color;
import com.example.openapi.chessserver.model.CreateChessGameRequest;
import com.example.openapi.chessserver.model.CreateChessGameResponse;
import com.example.openapi.chessserver.model.MoveChessmanResponse;
import com.example.openapi.chessserver.model.PromotePawnResponse;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ChessGameServiceImpl implements ChessGameService {

    private static final String NOT_FOUND = "Game ID %s is not found";
    private static final String CHESSMAN_NOT_FOUND = "Chessman is not found at %s%s";
    private static final String CHESSMAN_COLOR_IS_WRONG = "Attempting to move chessman of %s player. It is not turn of %s player";
    private static final String PROMOTION_COORDINATES_ARE_WRONG = "Promotion parameters are wrong";

    private final ChessGameRepository chessGameRepository;
    private final ChessmanMovementValidatorService chessmanMovementValidatorService;

    @Autowired
    public ChessGameServiceImpl(ChessGameRepository chessGameRepository,
                                ChessmanMovementValidatorService chessmanMovementValidatorService) {
        this.chessGameRepository = chessGameRepository;
        this.chessmanMovementValidatorService = chessmanMovementValidatorService;
    }

    @Override
    public CreateChessGameResponse createChessGame(CreateChessGameRequest createChessGameRequest) {
        String gameId = generateGameId();
        storeNewChessGame(gameId, createChessGameRequest.getColor());

        return new CreateChessGameResponse().gameId(gameId);
    }

    @Override
    public ChessboardResponse getChessGame(String gameId) {
        ChessGame chessGame = chessGameRepository.findByGameId(gameId);
        if (chessGame == null) {
            throw new ServiceException(String.format(NOT_FOUND, gameId),
                    HttpStatus.NOT_FOUND);
        }

        return new ChessboardResponse().chessboard(chessGame.getChessboard())
                .colorOfPlayer(chessGame.getColorOfPlayer());
    }

    @Override
    public MoveChessmanResponse moveChessman(ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo, String gameId) {
        // Retrieve chess game
        ChessGame chessGame = chessGameRepository.findByGameId(gameId);
        if (chessGame == null) {
            throw new ServiceException(String.format(NOT_FOUND, gameId),
                    HttpStatus.NOT_FOUND);
        }

        // Build chessboard and get chessman from chessboard
        Chessboard chessboard = new Chessboard(chessGame.getChessboard());

        ChessmanWithProperties chessmanWithProperties = chessboard.getChessmanWithProperties(coordinateFrom);
        validateExistenceOfChessmanWithProperties(chessmanWithProperties, coordinateFrom);
        validateColorOfChessmanWithProperties(chessmanWithProperties, chessGame.getColorOfPlayer());

        // Try to move chessman
        MoveChessmanResponse moveChessmanResponse = new MoveChessmanResponse();

        if (!chessmanMovementValidatorService.isChessmanAllowedToMove(chessboard, chessmanWithProperties,
                coordinateTo)) {
            moveChessmanResponse.setStatus(ChessmanMovementStatus.FAIL);
            return moveChessmanResponse;
        }

        // Update coordinates of chessman and persist it to DB
        chessboard.removeChessmanWithProperties(chessmanWithProperties);

        chessmanWithProperties.setCoordinate(new ChessCoordinate(coordinateTo.getX(), coordinateTo.getY()));
        chessboard.putChessmanWithProperties(chessmanWithProperties);

        if (chessmanMovementValidatorService.isCheckMate(chessboard, chessmanWithProperties.getColor())) {
            moveChessmanResponse.setStatus(ChessmanMovementStatus.CHECKMATE);
            chessGame.switchColorOfPlayer();
        } else if (chessmanMovementValidatorService.isDrawGame(chessboard)) {
            moveChessmanResponse.setStatus(ChessmanMovementStatus.DRAW);
        } else if (chessmanWithProperties.getChessman() == Chessman.PAWN &&
                chessmanMovementValidatorService.isPromotion(chessboard, chessmanWithProperties, coordinateTo)) {
            moveChessmanResponse.setStatus(ChessmanMovementStatus.PROMOTION);
        } else {
            moveChessmanResponse.setStatus(ChessmanMovementStatus.SUCCESS);
            chessGame.switchColorOfPlayer();
        }

        chessGame.setChessboard(chessboard.getChessboardReadable());
        chessGameRepository.save(chessGame);

        return moveChessmanResponse;
    }

    @Override
    public PromotePawnResponse promotePawn(ChessCoordinate coordinate, Chessman chessmanToBePromoted, String gameId) {
        // Retrieve chess game
        ChessGame chessGame = chessGameRepository.findByGameId(gameId);
        if (chessGame == null) {
            throw new ServiceException(String.format(NOT_FOUND, gameId),
                    HttpStatus.NOT_FOUND);
        }

        // Build chessboard and get chessman from chessboard
        Chessboard chessboard = new Chessboard(chessGame.getChessboard());

        ChessmanWithProperties chessmanWithProperties = chessboard.getChessmanWithProperties(coordinate);
        validateExistenceOfChessmanWithProperties(chessmanWithProperties, coordinate);
        validateColorOfChessmanWithProperties(chessmanWithProperties, chessGame.getColorOfPlayer());

        if (!chessmanMovementValidatorService.isPromotion(chessboard, chessmanWithProperties, chessmanWithProperties.getCoordinate())) {
            throw new ServiceException(PROMOTION_COORDINATES_ARE_WRONG, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // Replace chessman with promoted one and persist it to DB
        chessmanWithProperties.setChessman(chessmanToBePromoted);
        chessboard.putChessmanWithProperties(chessmanWithProperties);

        PromotePawnResponse promotePawnResponse = new PromotePawnResponse();

        if (chessmanMovementValidatorService.isCheckMate(chessboard, chessmanWithProperties.getColor())) {
            promotePawnResponse.setStatus(ChessmanMovementStatus.CHECKMATE);
        } else if (chessmanMovementValidatorService.isDrawGame(chessboard)) {
            promotePawnResponse.setStatus(ChessmanMovementStatus.DRAW);
        } else {
            promotePawnResponse.setStatus(ChessmanMovementStatus.SUCCESS);
        }

        chessGame.switchColorOfPlayer();
        chessGame.setChessboard(chessboard.getChessboardReadable());
        chessGameRepository.save(chessGame);

        return promotePawnResponse;
    }

    private void storeNewChessGame(String gameId, Color color) {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(gameId);

        Chessboard chessboard = new Chessboard();
        chessGame.setChessboard(chessboard.getChessboardReadable());
        chessGame.setColorOfPlayer(color);

        chessGameRepository.save(chessGame);
    }

    private void validateExistenceOfChessmanWithProperties(ChessmanWithProperties chessmanWithProperties, ChessCoordinate coordinate) {
        if (chessmanWithProperties == null) {
            throw new ServiceException(String.format(CHESSMAN_NOT_FOUND, coordinate.getX(), coordinate.getY()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void validateColorOfChessmanWithProperties(ChessmanWithProperties chessmanWithProperties, Color colorOfPlayer) {
        if (chessmanWithProperties.getColor() != colorOfPlayer) {
            throw new ServiceException(String.format(CHESSMAN_COLOR_IS_WRONG, colorOfPlayer, colorOfPlayer),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private static String generateGameId() {
        UUID uuid = UUID.randomUUID();
        long uuidLong = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        return Long.toString(uuidLong, Character.MAX_RADIX);
    }

}