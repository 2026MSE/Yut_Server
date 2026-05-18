package com.example.mse.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.mse.dto.ThrowResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.model.StickSide;
import com.example.mse.model.YutName;
import com.example.mse.model.YutResult;

@Service
public class YutService {

    private Random random = new Random();

    public YutResult getResult(GameRoom room) {

        if (room.getCurrentYutResult() != null) {
            return room.getCurrentYutResult();
        }

        StickSide[] sticks = generateSticks();

        room.setSticks(sticks);

        YutResult result = calculateResult(sticks);

        room.setCurrentYutResult(result);

        return result;
    }

    private StickSide[] generateSticks() {
        StickSide[] sticks = new StickSide[4];

        // 찬미 첫 번째는 백도 여부 결정
        sticks[0] = random.nextBoolean()
                ? StickSide.BACK
                : StickSide.HEAD;

        // 나머지 3개
        for (int i = 1; i < 4; i++) {
            sticks[i] = random.nextBoolean()
                    ? StickSide.HEAD
                    : StickSide.TAIL;
        }

        return sticks;
    }

    // 찬미 백도 로직 이상해서 backside기준으로 수정
    private YutResult calculateResult(StickSide[] sticks) {
        YutResult result = new YutResult();

        boolean hasBack = sticks[0] == StickSide.BACK;

        int backSideCount = 0;

        for (StickSide stick : sticks) {
            if (stick == StickSide.TAIL || stick == StickSide.BACK) {
                backSideCount++;
            }
        }

        // BACK + HEAD + HEAD + HEAD
        if (hasBack && backSideCount == 1) {
            result.setResult(YutName.BACK_DO);
            result.setMove(-1);
            result.setExtraTurn(false);
            return result;
        }

        switch (backSideCount) {
            case 1:
                result.setResult(YutName.DO);
                result.setMove(1);
                break;

            case 2:
                result.setResult(YutName.GAE);
                result.setMove(2);
                break;

            case 3:
                result.setResult(YutName.GEOL);
                result.setMove(3);
                break;

            case 4:
                result.setResult(YutName.YUT);
                result.setMove(4);
                result.setExtraTurn(true);
                break;

            case 0:
                result.setResult(YutName.MO);
                result.setMove(5);
                result.setExtraTurn(true);
                break;
        }

        return result;
    }

    public YutResult getCurrentResult(GameRoom room) {
        return room.getCurrentYutResult();
    }

    public void resetTurn(GameRoom room) {

        room.setCurrentYutResult(null);
        room.setSticks(new StickSide[4]);
    }

    // 찬미 ThrowResponse 메서드 추가
    public ThrowResponse getThrowResponse(GameRoom room) {
        YutResult yutResult = getResult(room);

        ThrowResponse response = new ThrowResponse();
        response.setSticks(room.getSticks());
        response.setPrivateSticks(room.getPrivateSticks());
        response.setPublicSticks(room.getPublicSticks());
        response.setYutResult(yutResult);

        return response;
    }
}