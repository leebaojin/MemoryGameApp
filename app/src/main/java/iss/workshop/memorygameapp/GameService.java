package iss.workshop.memorygameapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameService {

    private final int imageInPlay = 6;
    private String[] gameImages;
    private boolean firstOpen;
    private int firstMove;
    private int secondMove;
    private boolean[] solvedImages;
    private int numSolved;


    public GameService(String[] validImages) throws InsufficientGameImagesException {
        if(validImages.length != imageInPlay){
            throw new InsufficientGameImagesException();
        }
        generateGameImages(validImages);
        generateSolvedArray();
        this.firstOpen = true;
        this.firstMove = -1;
        this.secondMove =  -1;
        this.numSolved = 0;
    }

    public int getFirstMove(){
        return firstMove;
    }

    public int getNumSolved(){
        return numSolved;
    }

    public String getImageByIndex(int index){
        if(index >= 0 && index<gameImages.length){
            return gameImages[index];
        }
        return null;
    }

    public boolean isFirstOpen(){
        return firstOpen;
    }

    public String makeMove(int index){
        String output = getImageByIndex(index);
        if(output != null){
            if(firstOpen){
                firstMove = index;
                secondMove = -1;
            }
            else{
                secondMove = index;
            }
            firstOpen = !firstOpen;
        }
        return output;
    }

    public boolean CheckMatch(){
        if(firstMove == -1|| secondMove == -1){
            return false;
        }
        if(firstMove == secondMove){
            return false;
        }
        if(gameImages[firstMove].equals(gameImages[secondMove])){
            solvedImages[firstMove] = true;
            solvedImages[secondMove] = true;
            numSolved += 1;
            //Setting both to -1 after check

            return true;
        }
        //Setting both to -1 after check
        firstMove = -1;
        secondMove = -1;
        return false;
    }

    public void resetMoves(){
        //To reset moves
        firstMove = -1;
        secondMove = -1;
    }

    public boolean isOpen(int index){
        if(index < 0 || index > solvedImages.length){
            return false;
        }
        if(index == firstMove || index == secondMove){
            return false;
        }
        return solvedImages[index];
    }

    public boolean isGameOver(){
        for(boolean b: solvedImages){
            if(!b){
                return false;
            }
        }
        return true;
    }


    private void generateGameImages(String[] validImages){
        List<Integer> intArray = new ArrayList<>();
        for(int i = 0; i < imageInPlay*2; i++){
            intArray.add(i);
        }
        gameImages = new String[imageInPlay*2];
        Random rnd = new Random();
        int randInt;
        for(int i = 0; i < validImages.length; i++){
            //Placement of 1st image
            randInt = rnd.nextInt(intArray.size());
            gameImages[intArray.get(randInt)] = validImages[i];
            intArray.remove(randInt);
            //Placement of 2nd image
            randInt = rnd.nextInt(intArray.size());
            gameImages[intArray.get(randInt)] = validImages[i];
            intArray.remove(randInt);
        }
    }

    private void generateSolvedArray(){
        solvedImages = new boolean[imageInPlay*2];
        for(boolean b:solvedImages){
            b = false;
        }
    }

}
