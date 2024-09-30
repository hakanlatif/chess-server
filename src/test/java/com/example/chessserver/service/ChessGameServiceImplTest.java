package com.example.chessserver.service;

import com.example.chessserver.exception.ServiceException;
import com.example.chessserver.model.ChessCoordinate;
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
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ChessGameServiceImplTest {

    private static final String GAME_ID = "1ji7a2xo1aqev";
    private static final String GAME_ID_NOT_EXISTED = "aaaaaaaaaa";

    private static final String CHESSBOARD_DELIMITER = ",";
    private static final String CHESSBOARD_INITIAL_READABLE = StringUtils.join(
            Arrays.asList(
                    "a8rob", "b8knb", "c8bib", "e8qub", "d8kib", "f8bib", "g8knb", "h8rob",
                    "a7pab", "b7pab", "c7pab", "d7pab", "e7pab", "f7pab", "g7pab", "h7pab",

                    "a2paw", "b2paw", "c2paw", "d2paw", "e2paw", "f2paw", "g2paw", "h2paw",
                    "a1row", "b1knw", "c1biw", "d1quw", "e1kiw", "f1biw", "g1knw", "h1row"),
            CHESSBOARD_DELIMITER);

    private static final String CHESSBOARD_PROMOTE_PAWN_READABLE = StringUtils.join(
            Arrays.asList(
                    "c7pab", "b8paw"),
            CHESSBOARD_DELIMITER);

    @Mock
    private ChessmanMovementValidatorServiceImpl chessmanMovementValidatorService;

    @Mock
    private ChessGameRepository chessGameRepository;

    @InjectMocks
    private ChessGameServiceImpl chessGameService;

    @Test
    void shouldCreateChessGame() {
        when(chessGameRepository.save(any())).thenReturn(new ChessGame());
        CreateChessGameResponse createChessGameResponse = chessGameService.createChessGame(new CreateChessGameRequest().color(Color.BLACK));
        assertNotNull(createChessGameResponse, "CreateChessGameResponse is null");
        assertNotNull(createChessGameResponse.getGameId(), "Game ID is null");
    }

    @Test
    void shouldGetChessGame() {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(GAME_ID);
        chessGame.setChessboard(CHESSBOARD_INITIAL_READABLE);
        chessGame.setColorOfPlayer(Color.BLACK);

        when(chessGameRepository.findByGameId(GAME_ID)).thenReturn(chessGame);

        ChessboardResponse expectedResponse = new ChessboardResponse()
                .chessboard(CHESSBOARD_INITIAL_READABLE)
                .colorOfPlayer(Color.BLACK);
        ChessboardResponse actualResponse = chessGameService.getChessGame(GAME_ID);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void shouldFailAtGettingChessGameIfGameNotExists() {
        when(chessGameRepository.findByGameId(GAME_ID_NOT_EXISTED)).thenReturn(null);

        ServiceException thrown =
                assertThrows(ServiceException.class,
                        () -> chessGameService.getChessGame(GAME_ID_NOT_EXISTED));

        assertEquals("Game ID aaaaaaaaaa is not found", thrown.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatus());
    }

    @Test
    void shouldFailAtMovingChessIfGameNotExists() {
        when(chessGameRepository.findByGameId(GAME_ID_NOT_EXISTED)).thenReturn(null);

        ChessCoordinate coordinateStart = new ChessCoordinate("a5");
        ChessCoordinate coordinateEnd = new ChessCoordinate("a4");

        ServiceException thrown =
                assertThrows(ServiceException.class,
                        () -> chessGameService.moveChessman(
                                coordinateStart,
                                coordinateEnd,
                                GAME_ID_NOT_EXISTED));

        assertEquals("Game ID aaaaaaaaaa is not found", thrown.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatus());
    }

    @Test
    void shouldFailAtMovingChessman() {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(GAME_ID);
        chessGame.setChessboard(CHESSBOARD_INITIAL_READABLE);
        chessGame.setColorOfPlayer(Color.BLACK);

        when(chessGameRepository.findByGameId(GAME_ID)).thenReturn(chessGame);
        when(chessmanMovementValidatorService.isChessmanAllowedToMove(any(), any(), any())).thenReturn(false);

        MoveChessmanResponse expected = new MoveChessmanResponse().status(ChessmanMovementStatus.FAIL);
        MoveChessmanResponse actual = chessGameService.moveChessman(
                new ChessCoordinate("a7"),
                new ChessCoordinate("a6"),
                GAME_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldFindCheckMateWhileMovingChessman() {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(GAME_ID);
        chessGame.setChessboard(CHESSBOARD_INITIAL_READABLE);
        chessGame.setColorOfPlayer(Color.BLACK);

        when(chessGameRepository.findByGameId(GAME_ID)).thenReturn(chessGame);
        when(chessmanMovementValidatorService.isChessmanAllowedToMove(any(), any(), any())).thenReturn(true);
        when(chessmanMovementValidatorService.isCheckMate(any(), any())).thenReturn(true);

        MoveChessmanResponse expected = new MoveChessmanResponse().status(ChessmanMovementStatus.CHECKMATE);
        MoveChessmanResponse actual = chessGameService.moveChessman(
                new ChessCoordinate("a7"),
                new ChessCoordinate("a6"),
                GAME_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldFindDrawGameWhileMovingChessman() {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(GAME_ID);
        chessGame.setChessboard(CHESSBOARD_INITIAL_READABLE);
        chessGame.setColorOfPlayer(Color.BLACK);

        when(chessGameRepository.findByGameId(GAME_ID)).thenReturn(chessGame);
        when(chessmanMovementValidatorService.isChessmanAllowedToMove(any(), any(), any())).thenReturn(true);
        when(chessmanMovementValidatorService.isCheckMate(any(), any())).thenReturn(false);
        when(chessmanMovementValidatorService.isDrawGame(any())).thenReturn(true);

        MoveChessmanResponse expected = new MoveChessmanResponse().status(ChessmanMovementStatus.DRAW);
        MoveChessmanResponse actual = chessGameService.moveChessman(
                new ChessCoordinate("a7"),
                new ChessCoordinate("a6"),
                GAME_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldMoveChessman() {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(GAME_ID);
        chessGame.setChessboard(CHESSBOARD_INITIAL_READABLE);
        chessGame.setColorOfPlayer(Color.BLACK);

        when(chessGameRepository.findByGameId(GAME_ID)).thenReturn(chessGame);
        when(chessmanMovementValidatorService.isChessmanAllowedToMove(any(), any(), any())).thenReturn(true);
        when(chessmanMovementValidatorService.isCheckMate(any(), any())).thenReturn(false);

        MoveChessmanResponse expected = new MoveChessmanResponse().status(ChessmanMovementStatus.SUCCESS);
        MoveChessmanResponse actual = chessGameService.moveChessman(
                new ChessCoordinate("a7"),
                new ChessCoordinate("a6"),
                GAME_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldFindPromoteWhileMovingChessman() {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(GAME_ID);
        chessGame.setChessboard(CHESSBOARD_INITIAL_READABLE);
        chessGame.setColorOfPlayer(Color.BLACK);

        when(chessGameRepository.findByGameId(GAME_ID)).thenReturn(chessGame);
        when(chessmanMovementValidatorService.isChessmanAllowedToMove(any(), any(), any())).thenReturn(true);
        when(chessmanMovementValidatorService.isCheckMate(any(), any())).thenReturn(false);
        when(chessmanMovementValidatorService.isPromotion(any(), any(), any())).thenReturn(true);

        MoveChessmanResponse expected = new MoveChessmanResponse().status(ChessmanMovementStatus.PROMOTION);
        MoveChessmanResponse actual = chessGameService.moveChessman(
                new ChessCoordinate("a7"),
                new ChessCoordinate("a6"),
                GAME_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldFailAtPromotingIfGameNotExists() {
        when(chessGameRepository.findByGameId(GAME_ID_NOT_EXISTED)).thenReturn(null);

        ChessCoordinate coordinate = new ChessCoordinate("a8");

        ServiceException thrown =
                assertThrows(ServiceException.class,
                        () -> chessGameService.promotePawn(
                                coordinate,
                                Chessman.QUEEN,
                                GAME_ID_NOT_EXISTED));

        assertEquals("Game ID aaaaaaaaaa is not found", thrown.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatus());
    }

    @Test
    void shouldFindCheckMateWhilePromoting() {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(GAME_ID);
        chessGame.setChessboard(CHESSBOARD_INITIAL_READABLE);
        chessGame.setColorOfPlayer(Color.BLACK);

        when(chessGameRepository.findByGameId(GAME_ID)).thenReturn(chessGame);
        when(chessmanMovementValidatorService.isPromotion(any(), any(), any())).thenReturn(true);
        when(chessmanMovementValidatorService.isCheckMate(any(), any())).thenReturn(true);

        PromotePawnResponse expected = new PromotePawnResponse().status(ChessmanMovementStatus.CHECKMATE);
        PromotePawnResponse actual = chessGameService.promotePawn(
                new ChessCoordinate("a7"),
                Chessman.QUEEN,
                GAME_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldFindDrawGameWhilePromoting() {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(GAME_ID);
        chessGame.setChessboard(CHESSBOARD_INITIAL_READABLE);
        chessGame.setColorOfPlayer(Color.BLACK);

        when(chessGameRepository.findByGameId(GAME_ID)).thenReturn(chessGame);
        when(chessmanMovementValidatorService.isCheckMate(any(), any())).thenReturn(false);
        when(chessmanMovementValidatorService.isPromotion(any(), any(), any())).thenReturn(true);
        when(chessmanMovementValidatorService.isDrawGame(any())).thenReturn(true);

        PromotePawnResponse expected = new PromotePawnResponse().status(ChessmanMovementStatus.DRAW);
        PromotePawnResponse actual = chessGameService.promotePawn(
                new ChessCoordinate("a7"),
                Chessman.QUEEN,
                GAME_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldPromote() {
        ChessGame chessGame = new ChessGame();
        chessGame.setGameId(GAME_ID);
        chessGame.setChessboard(CHESSBOARD_PROMOTE_PAWN_READABLE);
        chessGame.setColorOfPlayer(Color.WHITE);

        when(chessGameRepository.findByGameId(GAME_ID)).thenReturn(chessGame);
        when(chessmanMovementValidatorService.isPromotion(any(), any(), any())).thenReturn(true);

        PromotePawnResponse expected = new PromotePawnResponse().status(ChessmanMovementStatus.SUCCESS);
        PromotePawnResponse actual = chessGameService.promotePawn(
                new ChessCoordinate("b8"),
                Chessman.QUEEN,
                GAME_ID);

        assertEquals(expected, actual);
    }

}