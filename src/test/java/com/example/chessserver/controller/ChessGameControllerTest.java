package com.example.chessserver.controller;

import com.example.chessserver.exception.ServiceException;
import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.service.ChessGameService;
import com.example.chessserver.service.ChessmanMovementValidatorService;
import com.example.openapi.chessserver.model.ChessboardResponse;
import com.example.openapi.chessserver.model.ChessmanMovementStatus;
import com.example.openapi.chessserver.model.Color;
import com.example.openapi.chessserver.model.CreateChessGameRequest;
import com.example.openapi.chessserver.model.CreateChessGameResponse;
import com.example.openapi.chessserver.model.ErrorMessage;
import com.example.openapi.chessserver.model.MoveChessmanRequest;
import com.example.openapi.chessserver.model.MoveChessmanResponse;
import com.example.openapi.chessserver.model.PromotePawnRequest;
import com.example.openapi.chessserver.model.PromotePawnResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
class ChessGameControllerTest {

    private static final String CHESSBOARD_DELIMITER = ",";
    private static final String CHESSBOARD_INITIAL_READABLE = StringUtils.join(
            Arrays.asList(
                    "a8rob", "b8knb", "c8bib", "e8qub", "d8kib", "f8bib", "g8knb", "h8rob",
                    "a7pab", "b7pab", "c7pab", "d7pab", "e7pab", "f7pab", "g7pab", "h7pab",

                    "a2paw", "b2paw", "c2paw", "d2paw", "e2paw", "f2paw", "g2paw", "h2paw",
                    "a1row", "b1knw", "c1biw", "d1quw", "e1kiw", "f1biw", "g1knw", "h1row"),
            CHESSBOARD_DELIMITER);

    private static final String ERROR_MESSAGE = "Something goes wrong";
    private static final String UNEXPECTED_ERROR = "Unexpected error";
    private static final ErrorMessage UNEXPECTED_ERROR_MESSAGE = new ErrorMessage().message(UNEXPECTED_ERROR);

    private static final ErrorMessage SOMETHING_GOES_WRONG_ERROR_MESSAGE = new ErrorMessage().message(ERROR_MESSAGE);
    private static final String CREATE_CHESS_URL = "/chess/v1/create";
    private static final String GET_CHESSBOARD_URL = "/chess/v1/chessboard/1ji7a2xo1aqev";
    private static final String MOVE_CHESSMAN_URL = "/chess/v1/move-chessman";
    private static final String PROMOTE_PAWN_URL = "/chess/v1/promote-pawn";
    private static final String GAME_ID = "1ji7a2xo1aqev";

    @Mock
    private ChessGameService chessGameService;

    @Mock
    private ChessmanMovementValidatorService chessmanMovementValidatorService;

    @InjectMocks
    private ChessGameController controller;

    private MockMvc mockMvc;

    private JacksonTester<ErrorMessage> errorMessageTester;
    private JacksonTester<CreateChessGameRequest> createChessGameRequestTester;
    private JacksonTester<CreateChessGameResponse> createChessGameResponseTester;
    private JacksonTester<ChessboardResponse> chessboardResponseTester;
    private JacksonTester<MoveChessmanRequest> moveChessmanRequestTester;
    private JacksonTester<MoveChessmanResponse> moveChessmanResponseTester;
    private JacksonTester<PromotePawnRequest> promotePawnRequestTester;
    private JacksonTester<PromotePawnResponse> promotePawnResponseTester;

    @BeforeEach
    void setupEach() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        JacksonTester.initFields(this, new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    @Test
    void shouldCreateChessGame() throws Exception {
        CreateChessGameResponse expectedResponse = new CreateChessGameResponse().gameId(GAME_ID);
        when(chessGameService.createChessGame(any()))
                .thenReturn(expectedResponse);

        CreateChessGameRequest request = new CreateChessGameRequest().color(Color.BLACK);

        MockHttpServletResponse response = mockMvc
                .perform(post(CREATE_CHESS_URL)
                        .content(createChessGameRequestTester.write(request).getJson())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(createChessGameResponseTester.write(expectedResponse).getJson());
    }

    @Test
    void shouldGetChessboard() throws Exception {
        ChessboardResponse expectedResponse = new ChessboardResponse().chessboard(CHESSBOARD_INITIAL_READABLE);
        when(chessGameService.getChessGame(any()))
                .thenReturn(expectedResponse);

        MockHttpServletResponse response = mockMvc
                .perform(get(GET_CHESSBOARD_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(chessboardResponseTester.write(expectedResponse).getJson());
    }

    @Test
    void shouldMoveChessboard() throws Exception {
        MoveChessmanResponse expectedResponse = new MoveChessmanResponse()
                .status(ChessmanMovementStatus.SUCCESS);

        when(chessGameService.moveChessman(any(), any(), any()))
                .thenReturn(expectedResponse);

        MoveChessmanRequest request = new MoveChessmanRequest()
                .coordinateFrom("a8").coordinateTo("a7").gameId(GAME_ID);

        MockHttpServletResponse response = mockMvc
                .perform(put(MOVE_CHESSMAN_URL)
                        .content(moveChessmanRequestTester.write(request).getJson())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(moveChessmanResponseTester.write(expectedResponse).getJson());
    }

    @Test
    void shouldNotMoveChessboardIfCoordinateFromIsNotValid() throws Exception {
        when(chessmanMovementValidatorService.isNotInChessBorder(new ChessCoordinate("a9")))
                .thenReturn(true);

        ErrorMessage expectedResponse = new ErrorMessage()
                .message("a9 is not a valid chess coordinate for from");

        MoveChessmanRequest request = new MoveChessmanRequest()
                .coordinateFrom("a9").coordinateTo("a7").gameId(GAME_ID);

        MockHttpServletResponse response = mockMvc
                .perform(put(MOVE_CHESSMAN_URL)
                        .content(moveChessmanRequestTester.write(request).getJson())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(expectedResponse).getJson());
    }

    @Test
    void shouldNotMoveChessboardIfCoordinateToIsNotValid() throws Exception {
        when(chessmanMovementValidatorService.isNotInChessBorder(new ChessCoordinate("a7")))
                .thenReturn(true);

        ErrorMessage expectedResponse = new ErrorMessage()
                .message("a7 is not a valid chess coordinate for to");

        MoveChessmanRequest request = new MoveChessmanRequest()
                .coordinateFrom("a9").coordinateTo("a7").gameId(GAME_ID);

        MockHttpServletResponse response = mockMvc
                .perform(put(MOVE_CHESSMAN_URL)
                        .content(moveChessmanRequestTester.write(request).getJson())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(expectedResponse).getJson());
    }

    @Test
    void shouldPromotePawn() throws Exception {
        when(chessmanMovementValidatorService.isPromotionAllowed(any()))
                .thenReturn(true);

        PromotePawnResponse expectedResponse = new PromotePawnResponse()
                .status(ChessmanMovementStatus.SUCCESS);

        when(chessGameService.promotePawn(any(), any(), any()))
                .thenReturn(expectedResponse);

        PromotePawnRequest request = new PromotePawnRequest()
                .coordinate("a8").chessman("pa").gameId(GAME_ID);

        MockHttpServletResponse response = mockMvc
                .perform(put(PROMOTE_PAWN_URL)
                        .content(promotePawnRequestTester.write(request).getJson())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(promotePawnResponseTester.write(expectedResponse).getJson());
    }

    @Test
    void shouldNotPromotePawnIfCoordinateIsNotValid() throws Exception {
        when(chessmanMovementValidatorService.isNotInChessBorder(any()))
                .thenReturn(true);

        ErrorMessage expectedResponse = new ErrorMessage()
                .message("a9 is not a valid chess coordinate");

        PromotePawnRequest request = new PromotePawnRequest()
                .coordinate("a9").chessman("pa").gameId(GAME_ID);

        MockHttpServletResponse response = mockMvc
                .perform(put(PROMOTE_PAWN_URL)
                        .content(promotePawnRequestTester.write(request).getJson())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(expectedResponse).getJson());
    }

    @Test
    void shouldNotPromotePawnIfChessmanShortNameIsWrong() throws Exception {
        when(chessmanMovementValidatorService.isPromotionAllowed(any()))
                .thenReturn(true);

        ErrorMessage expectedResponse = new ErrorMessage()
                .message("Short name of chessman paa is invalid");

        PromotePawnRequest request = new PromotePawnRequest()
                .coordinate("a8").chessman("paa").gameId(GAME_ID);

        MockHttpServletResponse response = mockMvc
                .perform(put(PROMOTE_PAWN_URL)
                        .content(promotePawnRequestTester.write(request).getJson())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(expectedResponse).getJson());
    }

    @Test
    void shouldNotPromotePawnIfPromotionIsNotAllowed() throws Exception {
        when(chessmanMovementValidatorService.isPromotionAllowed(any()))
                .thenReturn(false);

        ErrorMessage expectedResponse = new ErrorMessage()
                .message("Promotion is not allowed");

        PromotePawnRequest request = new PromotePawnRequest()
                .coordinate("a8").chessman("qu").gameId(GAME_ID);

        MockHttpServletResponse response = mockMvc
                .perform(put(PROMOTE_PAWN_URL)
                        .content(promotePawnRequestTester.write(request).getJson())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(expectedResponse).getJson());
    }

    @Test
    void shouldHandleServiceExceptionWith400() throws Exception {
        when(chessGameService.getChessGame(any()))
                .thenThrow(new ServiceException(ERROR_MESSAGE, HttpStatus.BAD_REQUEST));

        MockHttpServletResponse response = mockMvc
                .perform(get(GET_CHESSBOARD_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(SOMETHING_GOES_WRONG_ERROR_MESSAGE).getJson());
    }

    @Test
    void shouldHandleServiceExceptionWith404() throws Exception {
        when(chessGameService.getChessGame(any()))
                .thenThrow(new ServiceException(ERROR_MESSAGE, HttpStatus.NOT_FOUND));

        MockHttpServletResponse response = mockMvc
                .perform(get(GET_CHESSBOARD_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(SOMETHING_GOES_WRONG_ERROR_MESSAGE).getJson());
    }

    @Test
    void shouldHandleServiceExceptionWith422() throws Exception {
        when(chessGameService.getChessGame(any()))
                .thenThrow(new ServiceException(ERROR_MESSAGE, HttpStatus.UNPROCESSABLE_ENTITY));

        MockHttpServletResponse response = mockMvc
                .perform(get(GET_CHESSBOARD_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(SOMETHING_GOES_WRONG_ERROR_MESSAGE).getJson());
    }

    @Test
    void shouldHandleServiceExceptionWith500() throws Exception {
        when(chessGameService.getChessGame(any()))
                .thenThrow(new ServiceException(ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR));

        MockHttpServletResponse response = mockMvc
                .perform(get(GET_CHESSBOARD_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(SOMETHING_GOES_WRONG_ERROR_MESSAGE).getJson());
    }

    @Test
    void shouldHandleExceptionWith500() throws Exception {
        when(chessGameService.getChessGame(any()))
                .thenThrow(new RuntimeException(ERROR_MESSAGE));

        MockHttpServletResponse response = mockMvc
                .perform(get(GET_CHESSBOARD_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertThat(response.getContentAsString())
                .isEqualTo(errorMessageTester.write(UNEXPECTED_ERROR_MESSAGE).getJson());
    }
}