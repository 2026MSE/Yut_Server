package com.example.mse.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.mse.model.GameRoom;
import com.example.mse.model.StickSide;
import com.example.mse.model.YutName;
import com.example.mse.model.YutResult;

@Service
public class PrivateService {

    private Random random = new Random();

    public YutResult getResult(GameRoom room) {

        if (room.isAlreadyThrown()) {
            return room.getCurrentYutResult();
        }

        StickSide[] sticks = generateSticks();

        room.setSticks(sticks);

        room.setPrivateSticks(new StickSide[] {
                sticks[0],
                sticks[1]
        });

        room.setPublicSticks(new StickSide[] {
                sticks[2],
                sticks[3]
        });

        YutResult result = calculateResult(sticks);

        room.setCurrentYutResult(result);
        room.setAlreadyThrown(true);

        return result;
    }

    private StickSide[] generateSticks() {
        StickSide[] sticks = new StickSide[4];

        sticks[0] = random.nextInt(50) == 0
                ? StickSide.BACK // 50% 확률
                : StickSide.HEAD;

        // 나머지 3개
        for (int i = 1; i < 4; i++) {
            sticks[i] = random.nextBoolean()
                    ? StickSide.HEAD
                    : StickSide.TAIL;
        }

        return sticks;
    }

    private YutResult calculateResult(StickSide[] sticks) {
        YutResult result = new YutResult();

        int headCount = 0;
        boolean back = false;

        for (StickSide stick : sticks) {
            if (stick == StickSide.BACK) {
                back = true;
            } else if (stick == StickSide.HEAD) {
                headCount++;
            }
        }

        if (back) {
            result.setResult(YutName.BACK_DO);
            result.setMove(-1);
            result.setExtraTurn(false);
            return result;
        }

        switch (headCount) {
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
        room.setAlreadyThrown(false);
        room.setCurrentYutResult(null);
        room.setSticks(new StickSide[4]);
        room.setPrivateSticks(new StickSide[2]);
        room.setPublicSticks(new StickSide[2]);
    }
}