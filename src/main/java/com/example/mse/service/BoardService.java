package com.example.mse.service;

import com.example.mse.model.Board;
import com.example.mse.model.BoardNode;
import com.example.mse.model.Piece;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoardService {

    // 게임방이 생성될 때 윷판을 초기화하는 메서드
    public Map<Integer, BoardNode> initBoard() {
        Map<Integer, BoardNode> boardMap = new HashMap<>();
    

    // 1. 0~28번까지 총 29개의 노드 생성 (0번은 시작점이자 도착점)
    for(int i = 0; i<=28; i++){
        boardMap.put(i, new BoardNode(i));
    }

    for(int i = 0; i<19; i++){
        boardMap.get(i).setNextNodeId(i+1);
    }
    boardMap.get(19).setNextNodeId(0);

    boardMap.get(5).setFastNodeId(20);
    boardMap.get(20).setFastNodeId(21);
    boardMap.get(21).setFastNodeId(22);

    boardMap.get(10).setFastNodeId(23);
    boardMap.get(23).setFastNodeId(24);
    boardMap.get(24).setFastNodeId(22);

    boardMap.get(22).setFastNodeId(25);
    boardMap.get(22).setNextNodeId(27);

    boardMap.get(25).setFastNodeId(26);
    boardMap.get(26).setFastNodeId(0);

    boardMap.get(27).setFastNodeId(28);
    boardMap.get(28).setFastNodeId(15);

    return boardMap;
    }

    public void initPieces(Board board, List<String> playerIds){
        Map<String, List<Piece>> piecesMap = new HashMap<>();

        for(String playerId : playerIds){
            List<Piece> playerPieces = new ArrayList<>();

            //각 플레이어에게 4개의 말을 만들어 줌.
            for(int i =1; i<=4; i++){
                String pieceId = playerId + "_piece_" + i;
                Piece piece = new Piece(pieceId, playerId);
                //Piece 생성자에게 currentPosition은 -1로 자동 설정됨.

                playerPieces.add(piece);
            }
            //이 플레이어의 말 4개는 이것들입니다 하고 맵에 저장
            piecesMap.put(playerId, playerPieces);
        }
        //완성된 말 목록을 보드의 장부에 등록
        board.setPieces(piecesMap);
    }

    public int calculateNextPath(Board board, int currentPos, int moveAmount){
        Map<Integer, BoardNode> nodeMap = board.getNodeMap();

        // 1. 빽도(-1) 처리
        if(moveAmount == -1){
            return calculateBackPath(board, currentPos);
        }

        int targetPos = currentPos;
        int prevPos = currentPos;
        boolean isFirstStep = true;

        for(int i = 0;i<moveAmount;i++){

            if(targetPos==0 && currentPos != -1 && !isFirstStep){
                return 99;
            }

            BoardNode currentNode = (targetPos == -1) ?nodeMap.get(0) : nodeMap.get(targetPos);
            int nextPos;

            if(currentNode.getId()==22 && !isFirstStep){
                if(prevPos == 21){
                    nextPos = 27;
                }else{
                    nextPos = 25;
                }
            }

            else if (isFirstStep && currentNode.getFastNodeId() != null) {
                nextPos = currentNode.getFastNodeId();
            } 
            // 그 외의 경우는 무조건 기본 길(NextNode)로 직진
            else {
                nextPos = currentNode.getNextNodeId();
            }

            prevPos = targetPos;
            targetPos = nextPos;
            isFirstStep = false;
        }

        if(targetPos == 0 && currentPos != -1){
            return 99;
        }
        return targetPos;
    }

    // 빽도 전용 역추적 메서드
    private int calculateBackPath(Board board, int currentPos){

        // 대기실(-1)이나 시작점(0번)에서는 빽도를 해도 이동하지 못함 (룰에 따라 다름)
        if(currentPos == -1 || currentPos ==0){
            return currentPos;
        }

        if (currentPos == 25 || currentPos == 27) {
            return 22;
        }

        if(currentPos == 15){
            return 14;
        }

        // 전체 윷판을 뒤져서 "누가 나를 가리키고 있는지(나의 이전 칸)"를 찾습니다.
        for(BoardNode node : board.getNodeMap().values()){
            if(node.getNextNodeId() != null && node.getNextNodeId() ==currentPos){
                return node.getId();
            }
            if(node.getFastNodeId() != null && node.getFastNodeId()==currentPos){
                return node.getId();
            }
        }
        return currentPos;
    }

    public String movePieceAndCheckCatch(Board board, Piece movingPiece, int targetPos){
        int oldPos = movingPiece.getCurrentPosition();

        if(oldPos != -1 && oldPos != 99){
            List<Piece> oldPosList = board.getNodePiecesMap().get(oldPos);
            if(oldPosList != null){
                oldPosList.remove(movingPiece);
            }
        }

        if(targetPos == 99){
            movingPiece.setCurrentPosition(99);
            for(Piece carried : movingPiece.getCarriedPieces()){
                carried.setCurrentPosition(99);
            }
            return "FINISH";
        }

        List<Piece> targetPieces = board.getNodePiecesMap()
            .computeIfAbsent(targetPos, k-> new ArrayList<>());

        if(targetPieces.isEmpty()){
            movingPiece.setCurrentPosition(targetPos);
            targetPieces.add(movingPiece);
            return "NORMAL";
        }

        Piece targetPiece = targetPieces.get(0);

        if(targetPiece.getOwnerId().equals(movingPiece.getOwnerId())){
            movingPiece.setCurrentPosition(targetPos);

            targetPiece.getCarriedPieces().add(movingPiece);
            targetPiece.getCarriedPieces().addAll(movingPiece.getCarriedPieces());
            movingPiece.getCarriedPieces().clear();

            return "PIGGYBACK";
        }
    else{
        targetPiece.setCurrentPosition(-1);
        for(Piece carried : targetPiece.getCarriedPieces()){
            carried.setCurrentPosition(-1);
        }
        targetPiece.getCarriedPieces().clear();
        targetPieces.clear();

        movingPiece.setCurrentPosition(targetPos);
        targetPieces.add(movingPiece);

        return "CATCH";
    }
    }
}
