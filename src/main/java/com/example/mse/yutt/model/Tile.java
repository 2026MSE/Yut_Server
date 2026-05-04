package com.example.yut_server.model;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import  lombok.Setter;

import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Tile {

    private int id;
    private String type;
    private int next; // 다음 칸 번호
    private Integer prev;
    private Integer shortcut; // 지름길

    private List<Piece> pieces = new ArrayList<>();

    public Tile(int id, String type, int next, Integer prev, Integer shortcut)
    {
    
        this.id = id;
        this.type = type;
        this.next = next;
        this.prev = prev;
        this.shortcut = shortcut;
        this.pieces = new ArrayList<>();
    }

    public void addPiece(Piece piece){
        this.pieces.add(piece);
    }

   public void removePiece(Piece p) { 
        this.pieces.remove(p); 
    }
}
