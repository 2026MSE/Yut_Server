package com.example.mse;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class PrivateService {

    private YutResult currentResult;
    private boolean alreadyThrown = false;

    private Random random = new Random();

    public boolean hasThrown() {
        return alreadyThrown;
    }

    public YutResult getResult() {

        if(alreadyThrown){
            return currentResult;
        }

        int n = random.nextInt(6);

        YutResult result = new YutResult();

        switch(n){
            case 0:
                result.setResult("BACK_DO");
                result.setMove(-1);
                break;

            case 1:
                result.setResult("DO");
                result.setMove(1);
                break;

            case 2:
                result.setResult("GAE");
                result.setMove(2);
                break;

            case 3:
                result.setResult("GEOL");
                result.setMove(3);
                break;

            case 4:
                result.setResult("YUT");
                result.setMove(4);
                result.setExtraTurn(true);
                break;

            case 5:
                result.setResult("MO");
                result.setMove(5);
                result.setExtraTurn(true);
                break;
        }

        currentResult = result;
        alreadyThrown=true;

        return result;
    }

    public void resetTurn(){
        alreadyThrown=false;
        currentResult=null;
    }

}