package iss.workshop.memorygameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    TextView timerTxt;
    int seconds = 0;
    Button stopBtn;
    Boolean timerRunning;

    GameService gs;

    MediaPlayer mediaPlayer;

    private String[] gameString = {
            "game1.png", "game2.png", "game3.png", "game4.png", "game5.png", "game6.png"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        startTimer();

        stopBtn = findViewById(R.id.stopTimer);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerRunning = false;
            }
        });

        try {
            gs = new GameService(gameString);
        }
        catch(Exception e){
            finish();
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.bgmusic);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
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

    public void startTimer() {
        timerRunning = true;
        timerTxt = findViewById(R.id.txtTimer);

        //Create a background thread for timer
        new Thread(new Runnable() {
            @Override
            public void run() {
                //call runOnUiThread() to update a view from a background thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int hours = seconds / 3600;
                        int minutes = (seconds % 3600) / 60;
                        int secs = seconds % 60;

                        // Format the seconds into hours, minutes, and seconds.
                        String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

                        // Set the text view text.
                        timerTxt.setText(time);

                        // If thread is not interrupted, increment the seconds variable.
                        if(timerRunning){
                            seconds++;
                        }

                        // Post the code again with a delay of 1 second.
                        timerTxt.postDelayed(this, 1000);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}