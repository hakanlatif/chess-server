package com.example.chessserver.model.jpa;

import com.example.openapi.chessserver.model.Color;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table
public class ChessGame {

    @Id
    @Column(unique = true)
    private String gameId;

    @Enumerated(EnumType.STRING)
    @Column
    private Color colorOfPlayer;

    @Column
    private String chessboard;

    public void switchColorOfPlayer() {
        this.colorOfPlayer = colorOfPlayer == Color.BLACK ? Color.WHITE : Color.BLACK;
    }

}
