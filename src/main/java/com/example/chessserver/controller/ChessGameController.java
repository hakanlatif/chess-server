package com.example.chessserver.controller;

import com.example.chessserver.exception.ServiceException;
import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.model.enums.Chessman;
import com.example.openapi.chessserver.api.ChessApi;
import com.example.openapi.chessserver.model.ChessboardResponse;
import com.example.openapi.chessserver.model.CreateChessGameRequest;
import com.example.openapi.chessserver.model.CreateChessGameResponse;
import com.example.openapi.chessserver.model.MoveChessmanRequest;
import com.example.openapi.chessserver.model.MoveChessmanResponse;
import com.example.openapi.chessserver.model.PromotePawnRequest;
import com.example.openapi.chessserver.model.PromotePawnResponse;
import com.example.chessserver.service.ChessGameService;
import com.example.chessserver.service.ChessmanMovementValidatorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ChessGameController implements ChessApi {

    private static final String PROMOTION_IS_NOT_ALLOWED = "Promotion is not allowed";
    private static final String INVALID_CHESSMAN_NAME = "Short name of chessman %s is invalid";
    private static final String NOT_VALID_COORD_FROM = "%s%s is not a valid chess coordinate for from";
    private static final String NOT_VALID_COORD_TO = "%s%s is not a valid chess coordinate for to";
    private static final String NOT_VALID_COORD = "%s%s is not a valid chess coordinate";

    private final ChessGameService chessGameService;
    private final ChessmanMovementValidatorService chessmanMovementValidatorService;

    @Autowired
    public ChessGameController(ChessGameService chessGameService, ChessmanMovementValidatorService chessmanMovementValidatorService) {
        this.chessGameService = chessGameService;
        this.chessmanMovementValidatorService = chessmanMovementValidatorService;
    }

    @Override
    public ResponseEntity<CreateChessGameResponse> createChessGame(@Valid CreateChessGameRequest body) {
        return new ResponseEntity<>(chessGameService.createChessGame(body), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ChessboardResponse> getChessboard(@PathVariable("gameId") String gameId) {
        return new ResponseEntity<>(chessGameService.getChessGame(gameId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MoveChessmanResponse> moveChessman(@Valid MoveChessmanRequest body) {
        ChessCoordinate coordinateFrom = new ChessCoordinate(body.getCoordinateFrom());

        if (chessmanMovementValidatorService.isNotInChessBorder(coordinateFrom)) {
            throw new ServiceException(String.format(NOT_VALID_COORD_FROM, coordinateFrom.getX(), coordinateFrom.getY()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ChessCoordinate coordinateTo = new ChessCoordinate(body.getCoordinateTo());
        if (chessmanMovementValidatorService.isNotInChessBorder(coordinateTo)) {
            throw new ServiceException(String.format(NOT_VALID_COORD_TO, coordinateTo.getX(), coordinateTo.getY()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(chessGameService.moveChessman(coordinateFrom, coordinateTo, body.getGameId()),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PromotePawnResponse> promotePawn(@Valid PromotePawnRequest body) {
        ChessCoordinate coordinate = new ChessCoordinate(body.getCoordinate());
        if (chessmanMovementValidatorService.isNotInChessBorder(coordinate)) {
            throw new ServiceException(String.format(NOT_VALID_COORD, coordinate.getX(), coordinate.getY()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Chessman chessmanToBePromoted = Chessman.getByShortName(body.getChessman());

        if (chessmanToBePromoted == null) {
            throw new ServiceException(String.format(INVALID_CHESSMAN_NAME, body.getChessman()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (!chessmanMovementValidatorService.isPromotionAllowed(chessmanToBePromoted)) {
            throw new ServiceException(PROMOTION_IS_NOT_ALLOWED,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(chessGameService.promotePawn(coordinate, chessmanToBePromoted, body.getGameId()),
                HttpStatus.OK);
    }

}