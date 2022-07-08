package iss.workshop.memorygameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class GameActivity extends AppCompatActivity {

    GameService gs;

    private String[] gameString = {
            "game1.png", "game2.png", "game3.png", "game4.png", "game5.png", "game6.png"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        try {
            gs = new GameService(gameString);
        }
        catch(Exception e){
            finish();
        }
    }

    private void makeMove(int index){
        if(gs.isOpen(index)){
            //Already opened this index
            return;
        }
        if(gs.isFirstOpen()){
            //First time open
            String imageUrl = gs.makeMove(index);
        }else{
            //Second time open
            String imageUrl = gs.makeMove(index);
            if(gs.CheckMatch()){
                //If check match is a success
            }
            else{
                //If check match fail
            }

            if(gs.isGameOver()){
                //If game is over
            }
        }

    }
}