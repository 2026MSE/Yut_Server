package com.example.yut_server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.yut_server.model.MoveResult;
import com.example.yut_server.model.Piece;
import com.example.yut_server.model.Tile;

@Service
public class YutService {

    public MoveResult calculateMove(Map<Integer, Tile> board, Piece piece, int rollResult){
        String playerId = piece.getPlayerId();
        int currentPos = piece.getPosition();
        List<Integer> path = new ArrayList<>();

        Tile currentTile = board.get(currentPos);
        List<Integer> movingPieceIds = new ArrayList<>();

        if(currentPos !=0){
            for(Piece p : currentTile.getPieces()){
                if(p.getPlayerId().equals(playerId)){
                    movingPieceIds.add(p.getPieceId());
                }
            }
        } else{
            movingPieceIds.add(piece.getPieceId());
        }

        int nextPos = currentPos;
        int lastPos = -1;

        if(rollResult == -1){
            nextPos = board.get(currentPos).getPrev() != null ? board.get(currentPos).getPrev() : 0;
            path.add(nextPos);
        }   else{
            for(int i = 0; i<rollResult; i++){
                lastPos = nextPos;
                nextPos = getNextStep(board.get(nextPos), i == 0, lastPos);
                path.add(nextPos);
                if(nextPos==0) break;
            }
        }

        boolean isFinished = (nextPos==0 && currentPos!=0);
        boolean isCatch = false;

        if(!isFinished){
            Tile targetTile = board.get(nextPos);
            if(!targetTile.getPieces().isEmpty()){
                String opponentId = targetTile.getPieces().get(0).getPlayerId();
                if(!opponentId.equals(playerId)){
                    isCatch = true;
                }
            }
        }
        return new MoveResult(playerId, path, path, isCatch, isFinished);
    }

    private int getNextStep(Tile currentTile, boolean isStart,int lastPos){
        if(isStart && currentTile.getShortcut()!=null){
            return currentTile.getShortcut();
        }

        if(currentTile.getId() == 23){
            if(lastPos == 22) return 24;
            if(lastPos == 27) return 29;
        }
        return currentTile.getNext();
    }
}
