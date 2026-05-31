//26.05.07 찬미 말 이동결과 enum으로 변경, 대기석(-1) 처리 추가, 그에 따른 업기랑 잡기 부분 수정
package com.example.mse.service;

import com.example.mse.model.Board;
import com.example.mse.model.BoardNode;
import com.example.mse.model.Piece;
import com.example.mse.model.MoveType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class BoardService {

    // 게임방이 생성될 때 윷판을 초기화하는 메서드
    public Map<Integer, BoardNode> initBoard() {
        Map<Integer, BoardNode> boardMap = new HashMap<>();

        // 1. 0~28번까지 총 29개의 노드 생성 (0번은 시작점이자 도착점)
        for (int i = 0; i <= 28; i++) {
            boardMap.put(i, new BoardNode(i));
        }

        for (int i = 0; i < 19; i++) {
            boardMap.get(i).setNextNodeId(i + 1);
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

    public void initPieces(Board board, List<String> playerIds) {
        Map<String, List<Piece>> piecesMap = new HashMap<>();

        // 찬미 대기석 추가
        board.getNodePiecesMap().putIfAbsent(-1, new ArrayList<>());

        for (String playerId : playerIds) {
            List<Piece> playerPieces = new ArrayList<>();

            // 각 플레이어에게 4개의 말을 만들어 줌.
            for (int i = 1; i <= 4; i++) {
                String pieceId = playerId + "_piece_" + i;
                Piece piece = new Piece(pieceId, playerId);
                // Piece 생성자에게 currentPosition은 -1로 자동 설정됨.

                playerPieces.add(piece);

                // 찬미 대기석(-1)에 말 추가
                board.getNodePiecesMap().get(-1).add(piece);
            }
            // 이 플레이어의 말 4개는 이것들입니다 하고 맵에 저장
            piecesMap.put(playerId, playerPieces);
        }
        // 완성된 말 목록을 보드의 장부에 등록
        board.setPieces(piecesMap);
    }

    public int calculateNextPath(Board board, int currentPos, int moveAmount) {
        Map<Integer, BoardNode> nodeMap = board.getNodeMap();

        // 1. 빽도(-1) 처리
        if (moveAmount == -1) {
            return calculateBackPath(board, currentPos);
        }

        int targetPos = currentPos;
        int prevPos = currentPos;
        boolean isFirstStep = true;

        for (int i = 0; i < moveAmount; i++) {

            if (targetPos == 0 && currentPos != -1 && !isFirstStep) {
                return 99;
            }

            BoardNode currentNode = (targetPos == -1) ? nodeMap.get(0) : nodeMap.get(targetPos);
            int nextPos;

            if (currentNode.getId() == 22 && !isFirstStep) {
                if (prevPos == 21) {
                    nextPos = 27;
                } else {
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

        if (targetPos == 0 && currentPos != -1) {
            return 99;
        }
        return targetPos;
    }

    // 빽도 전용 역추적 메서드
    private int calculateBackPath(Board board, int currentPos) {

        // 대기실(-1)이나 시작점(0번)에서는 빽도를 해도 이동하지 못함 (룰에 따라 다름)
        if (currentPos == -1 || currentPos == 0) {
            return currentPos;
        }

        if (currentPos == 25 || currentPos == 27) {
            return 22;
        }

        if (currentPos == 15) {
            return 14;
        }

        // 전체 윷판을 뒤져서 "누가 나를 가리키고 있는지(나의 이전 칸)"를 찾습니다.
        for (BoardNode node : board.getNodeMap().values()) {
            if (node.getNextNodeId() != null && node.getNextNodeId() == currentPos) {
                return node.getId();
            }
            if (node.getFastNodeId() != null && node.getFastNodeId() == currentPos) {
                return node.getId();
            }
        }
        return currentPos;
    }

    public MoveType predictMoveType(Board board, Piece movingPiece, int targetPos) {

        if (targetPos == 99) {
            return MoveType.FINISH;
        }

        // 대기석(-1) 또는 제자리 이동은 잡기/업기 판정 대상이 아님
        if (targetPos == -1 || movingPiece.getCurrentPosition() == targetPos) {
            return MoveType.NORMAL;
        }

        List<Piece> targetPieces = board.getNodePiecesMap().get(targetPos);

        if (targetPieces == null || targetPieces.isEmpty()) {
            return MoveType.NORMAL;
        }

        Piece targetPiece = targetPieces.get(0);

        if (targetPiece.getOwnerId().equals(movingPiece.getOwnerId())) {
            return MoveType.PIGGYBACK;
        }

        return MoveType.CATCH;
    }

    public MoveType movePieceAndCheckCatch(Board board, Piece movingPiece, int targetPos) {

        if (targetPos == 99) {
            moveSinglePiece(board, movingPiece, 99);
            movingPiece.setCarriedByPieceId(null);

            for (Piece carried : movingPiece.getCarriedPieces()) {
                moveSinglePiece(board, carried, 99);
                carried.setCarriedByPieceId(null);
            }

            movingPiece.getCarriedPieces().clear();

            return MoveType.FINISH;
        }

        // 대기석(-1) 또는 제자리 이동은 실제 이동/잡기/업기 처리하지 않음
        if (targetPos == -1 || movingPiece.getCurrentPosition() == targetPos) {
            return MoveType.NORMAL;
        }

        List<Piece> targetPieces = board.getNodePiecesMap()
                .computeIfAbsent(targetPos, k -> new ArrayList<>());

        if (targetPieces.isEmpty()) {
            movePieceGroup(board, movingPiece, targetPos);
            return MoveType.NORMAL;
        }

        Piece targetPiece = targetPieces.get(0);

        if (targetPiece.getOwnerId().equals(movingPiece.getOwnerId())) {
            List<Piece> carriedList = new ArrayList<>(movingPiece.getCarriedPieces());

            removeFromNode(board, movingPiece);

            movingPiece.setCurrentPosition(targetPos);
            movingPiece.setCarriedByPieceId(targetPiece.getId());

            targetPiece.getCarriedPieces().add(movingPiece);

            for (Piece carried : carriedList) {
                removeFromNode(board, carried);
                carried.setCurrentPosition(targetPos);
                carried.setCarriedByPieceId(targetPiece.getId());
                targetPiece.getCarriedPieces().add(carried);
            }

            movingPiece.getCarriedPieces().clear();

            return MoveType.PIGGYBACK;

        } else {
            board.getNodePiecesMap().putIfAbsent(-1, new ArrayList<>());

            targetPiece.setCurrentPosition(-1);
            targetPiece.setCarriedByPieceId(null);
            board.getNodePiecesMap().get(-1).add(targetPiece);

            for (Piece carried : targetPiece.getCarriedPieces()) {
                carried.setCurrentPosition(-1);
                carried.setCarriedByPieceId(null);
                board.getNodePiecesMap().get(-1).add(carried);
            }

            targetPiece.getCarriedPieces().clear();

            targetPieces.clear();

            movePieceGroup(board, movingPiece, targetPos);

            return MoveType.CATCH;
        }
    }

    // 찬미 말 찾는 메서드 추가
    public Piece findPiece(Board board, String playerId, String pieceId) {
        List<Piece> pieces = board.getPieces().get(playerId);

        if (pieces == null) {
            return null;
        }

        for (Piece piece : pieces) {
            if (piece.getId().equals(pieceId)) {
                return piece;
            }
        }

        return null;
    }

    // 찬미 승리판정 메서드 추가
    public boolean isPlayerFinished(Board board, String playerId) {
        List<Piece> pieces = board.getPieces().get(playerId);

        if (pieces == null || pieces.isEmpty()) {
            return false;
        }

        for (Piece piece : pieces) {
            if (piece.getCurrentPosition() != 99) {
                return false;
            }
        }

        return true;
    }

    // 챌린지 실패 시, 해당 플레이어의 말 중 보드 위에 있는 대표 말 하나를 랜덤으로 대기석(-1)으로 되돌림
    public Piece sendRandomPieceToWaitingArea(Board board, String playerId) {
        List<Piece> pieces = board.getPieces().get(playerId);

        if (pieces == null || pieces.isEmpty()) {
            return null;
        }

        board.getNodePiecesMap().putIfAbsent(-1, new ArrayList<>());

        List<Piece> candidates = new ArrayList<>();

        for (Piece piece : pieces) {
            int currentPos = piece.getCurrentPosition();

            // 대기석, 완주 말 제외
            if (currentPos == -1 || currentPos == 99) {
                continue;
            }

            // 업힌 말은 대표말이 아니므로 제외
            if (piece.getCarriedByPieceId() != null) {
                continue;
            }

            candidates.add(piece);
        }

        if (candidates.isEmpty()) {
            return null;
        }

        Piece selectedPiece = candidates.get(new Random().nextInt(candidates.size()));

        // 대표말을 현재 노드에서 제거
        removeFromNode(board, selectedPiece);

        // 업고 있던 말들도 대기석으로 이동
        for (Piece carried : selectedPiece.getCarriedPieces()) {
            removeFromNode(board, carried);
            carried.setCarriedByPieceId(null);
            addToNode(board, carried, -1);
        }

        selectedPiece.getCarriedPieces().clear();

        // 대표말도 대기석으로 이동
        selectedPiece.setCarriedByPieceId(null);
        addToNode(board, selectedPiece, -1);

        return selectedPiece;
    }

    private void removeFromNode(Board board, Piece piece) {
        int currentPos = piece.getCurrentPosition();

        if (currentPos == 99) {
            return;
        }

        List<Piece> currentNodePieces = board.getNodePiecesMap().get(currentPos);

        if (currentNodePieces != null) {
            currentNodePieces.remove(piece);
        }
    }

    private void addToNode(Board board, Piece piece, int targetPos) {
        board.getNodePiecesMap()
                .computeIfAbsent(targetPos, k -> new ArrayList<>())
                .add(piece);

        piece.setCurrentPosition(targetPos);
    }

    private void moveSinglePiece(Board board, Piece piece, int targetPos) {
        removeFromNode(board, piece);
        addToNode(board, piece, targetPos);
    }

    private void movePieceGroup(Board board, Piece leader, int targetPos) {
        // 대표 말 이동
        moveSinglePiece(board, leader, targetPos);

        // 대표 말이 업고 있는 말들도 같은 위치로 이동
        for (Piece carried : leader.getCarriedPieces()) {
            moveSinglePiece(board, carried, targetPos);
            carried.setCarriedByPieceId(leader.getId());
        }
    }
}
