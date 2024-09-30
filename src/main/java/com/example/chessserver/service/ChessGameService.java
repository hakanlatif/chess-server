package com.example.chessserver.service;

import com.example.chessserver.model.ChessCoordinate;
import com.example.chessserver.model.enums.Chessman;
import com.example.openapi.chessserver.model.ChessboardResponse;
import com.example.openapi.chessserver.model.CreateChessGameRequest;
import com.example.openapi.chessserver.model.CreateChessGameResponse;
import com.example.openapi.chessserver.model.MoveChessmanResponse;
import com.example.openapi.chessserver.model.PromotePawnResponse;

public interface ChessGameService {


    CreateChessGameResponse createChessGame(CreateChessGameRequest createChessGameRequest);

    ChessboardResponse getChessGame(String gameId);

    MoveChessmanResponse moveChessman(ChessCoordinate coordinateFrom, ChessCoordinate coordinateTo,
                                      String gameId);

    PromotePawnResponse promotePawn(ChessCoordinate coordinate, Chessman chessmanToBePromoted,
                                    String gameId);

}
