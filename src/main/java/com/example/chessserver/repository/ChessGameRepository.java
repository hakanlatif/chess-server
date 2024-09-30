package com.example.chessserver.repository;

import com.example.chessserver.model.jpa.ChessGame;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChessGameRepository extends JpaRepository<ChessGame, String> {

    ChessGame findByGameId(String gameId);

}
