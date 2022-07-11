package iss.workshop.memorygameapp;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {
    
    private Button nextBtn;
    private TextView matchesTxt;

    private File dir;

    private int[] playerScore = {0,0};
    private int currentPlayer;
    private int seconds = 0;
    private TimerTask timerTask;
    private Timer timer;
    private TextView timerTxt;

    private Boolean timerRunning;
    private GameService gs;
    private boolean isWaitingClose; //To check if the animation is still in progress

    private MediaPlayer mediaPlayer;
    private SoundPlayer sound;

    Dialog dialog;
    private TextView winnerTxt;
    private TextView player1TimeTxt;
    private TextView player2TimeTxt;

    private String[] gameString = {
            "gameimg0.jpg", "gameimg1.jpg", "gameimg2.jpg", "gameimg3.jpg", "gameimg4.jpg", "gameimg5.jpg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Setting up the basics
        //Getting required attributes
        dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        dialog = new Dialog(this);
        sound = new SoundPlayer(this);

        //Getting the required views
        timerTxt = findViewById(R.id.txtTimer);
        matchesTxt = findViewById(R.id.txtNumOfMatches);
        nextBtn = findViewById(R.id.nextPlayer);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Restart the game for the next player
                setupGame();
                view.setVisibility(View.GONE);
                currentPlayer += 1;
                resetTimer();
                startTimer();
            }
        });

        //Setting up the parameters and start timer
        obtainGameImages(); //This will get the intent and the bundle info
        currentPlayer = 1;
        setupGame();
        timer = new Timer();
        startTimer();

        //Start the background music
        mediaPlayer = MediaPlayer.create(this, R.raw.bgmusic);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void obtainGameImages(){
        Intent intent = getIntent();
        //To implement the code for getting the images
        Bundle gameBundle = intent.getBundleExtra("gameBundle");
        if(gameBundle == null){
            finish();
        }
        ArrayList<String> selectedItems = gameBundle.getStringArrayList("gameImages");
        if(selectedItems==null || selectedItems.size() < 6){
            Toast.makeText(this,"Fail to load images",Toast.LENGTH_SHORT).show();
            finish();
        }
        //To implement the code for setting the images to gameString
        gameString = new String[6];
        for(int i = 0; i < gameString.length; i++){
            gameString[i] = selectedItems.get(i);
        }

        //Validate that the image exist
        for(String gameImageName: gameString){
            File imgfile = new File(dir,gameImageName);
            Bitmap bitmap = BitmapFactory.decodeFile(imgfile.getAbsolutePath());
            if(bitmap == null){
                Toast.makeText(this,"Fail to load images",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupGame(){
        //Create a new game to play
        try {
            gs = new GameService(gameString);
            isWaitingClose = false;
            setGridImages();
            matchesTxt.setText(String.valueOf(gs.getNumSolved()) +" of 6 matches");
        }
        catch(Exception e){
            finish();
        }
    }

    private void setGridImages(){
        //Setting up grid Images
        ImageAdaptor adaptor = new ImageAdaptor(this,12);
        GridView gridView = findViewById(R.id.gameGrid);
        if(gridView != null){
            gridView.setAdapter(adaptor);
            gridView.setNumColumns(3);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    makeMove(adapterView, i);
                }
            });
        }
    }

    private void makeMove(AdapterView<?> adapterView, int index)  {
        //This handles the gameplay
        if(isWaitingClose || gs.isOpen(index)){
            //Already opened this index
            return;
        }
        if(gs.isFirstOpen()){
            //First time open
            String imageName = gs.makeMove(index);
            setImageOnIndex(adapterView, index, imageName);
        }else{
            //Get firstmove index
            int firstMove = gs.getFirstMove();
            if(index == firstMove){
                //Multiple clicks
                return;
            }
            //Second time open
            String imageName = gs.makeMove(index);

            if(gs.CheckMatch()){
                //If check match is a success
                setImageOnIndex(adapterView, index, imageName);
                sound.playMatchedSound();
                gs.resetMoves();
                //Set text if match is a success
                matchesTxt.setText(String.valueOf(gs.getNumSolved()) +" of 6 matches");
            }
            else{
                //If check match fail
                setImageOnIndex(adapterView, index, imageName);
                isWaitingClose = true;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setImageToDefault(adapterView, index);
                        setImageToDefault(adapterView, firstMove);
                        isWaitingClose = false;
                        gs.resetMoves();
                    }
                },1000);

                sound.playUnmatchedSound();

            }

            if(gs.isGameOver()){
                //If game is over
                timerTask.cancel(); // Stop timer
                playerScore[currentPlayer-1]=seconds;
                if(currentPlayer<2){
                    nextBtn.setVisibility(View.VISIBLE);
                }
                else if(currentPlayer == 2){
                    if(playerScore[0] != playerScore[1])
                        openWinDialog(playerScore[0],playerScore[1]);
                    else
                        gameDrawDialog(playerScore[0],playerScore[1]);
                }

                Toast.makeText(this,"Game over",Toast.LENGTH_LONG).show();
            }
        }

    }

    private void setImageOnIndex(AdapterView<?> adapterView, int index, String imageName){
        //This sets the image
        View myview = adapterView.findViewWithTag(String.valueOf(index));
        ImageView imgview = myview.findViewById(R.id.imageView);
        File imgfile = new File(dir,imageName);
        Bitmap bitmap = BitmapFactory.decodeFile(imgfile.getAbsolutePath());
        //Resizing image to a square (can consider moving this to the main activity)
        if(bitmap.getWidth() > bitmap.getHeight()){
            bitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth()/2- bitmap.getHeight()/2,0,
                    bitmap.getHeight(), bitmap.getHeight());
        }
        else if(bitmap.getWidth() < bitmap.getHeight()){
            bitmap = Bitmap.createBitmap(bitmap, 0,bitmap.getHeight()/2-bitmap.getWidth()/2,
                    bitmap.getWidth(), bitmap.getWidth());
        }

        //imgview.setImageBitmap(bitmap);
        flipAnimation(imgview,bitmap);
    }

    private void setImageToDefault(AdapterView<?> adapterView, int index){
        //This returns the image to default
        View myview = adapterView.findViewWithTag(String.valueOf(index));
        ImageView imgview = myview.findViewById(R.id.imageView);
        //int id = context.getResources().getIdentifier(imageName,"drawable",context.getPackageName());
        //imgview.setImageResource(R.drawable.cross_image);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cross_image);
        flipAnimation(imgview,bitmap);
    }

    private void flipAnimation(ImageView imgview, Bitmap nextimg){
        //This acts as the animator for the image to flip
        ObjectAnimator objectAnimator1 = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.flip_img);
        ObjectAnimator objectAnimator2 = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.flip_img2);
        objectAnimator1.setTarget(imgview);
        objectAnimator2.setTarget(imgview);
        objectAnimator1.setDuration(100);
        objectAnimator2.setDuration(100);
        objectAnimator1.start();
        objectAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //Animator2 continues from animator1
                //Animator1 flips half way and animator2 continues
                objectAnimator2.start();
                imgview.setImageBitmap(nextimg);
            }
        });
    }

    private void startTimer()
    {
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        seconds++;
                        timerTxt.setText(getTimerText());
                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, 0 ,1000);
    }

    public void resetTimer()
    {
        if(timerTask != null)
        {
            timerTask.cancel();
            seconds = 0;
            timerTxt.setText("");
        }
    }

    private String getTimerText()
    {
        int secs = ((seconds % 86400) % 3600) % 60;
        int minutes = ((seconds % 86400) % 3600) / 60;
        int hours = ((seconds % 86400) / 3600);

        String timeStr = String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",secs);

        return timeStr;
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

    public void openWinDialog(int player1Seconds, int player2Seconds){

        String winnerMsg = "";
        if(player1Seconds < player2Seconds){
            winnerMsg = "Winner - Player 1";
        }else{
            winnerMsg = "Winner - Player 2";
        }

        String player1TimeFormatted = formatTime(player1Seconds);
        String player2TimeFormatted = formatTime(player2Seconds);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vi = inflater.inflate(R.layout.winner_layout_dialog, null);

        winnerTxt = (TextView)vi.findViewById(R.id.dialog_txtView1);
        winnerTxt.setText(winnerMsg);
        player1TimeTxt = (TextView)vi.findViewById(R.id.dialog_txtView2);
        player1TimeTxt.setText("Player 1 : " + player1TimeFormatted);
        player2TimeTxt = (TextView)vi.findViewById(R.id.dialog_txtView3);
        player2TimeTxt.setText("Player 2 : " + player2TimeFormatted);

        dialog.setContentView(vi);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnOk = dialog.findViewById(R.id.dialog_btnOK);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }

    public void gameDrawDialog(int player1Seconds, int player2Seconds) {

        String player1TimeFormatted = formatTime(player1Seconds);
        String player2TimeFormatted = formatTime(player2Seconds);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vi = inflater.inflate(R.layout.game_draw_layout_dialog, null);

        player1TimeTxt = (TextView)vi.findViewById(R.id.dialog_txtView2);
        player1TimeTxt.setText("Player 1 : " + player1TimeFormatted);
        player2TimeTxt = (TextView)vi.findViewById(R.id.dialog_txtView3);
        player2TimeTxt.setText("Player 2 : " + player2TimeFormatted);

        dialog.setContentView(vi);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnOk = dialog.findViewById(R.id.dialog_btnOK);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }

    public String formatTime(int sec){
        int hours = sec / 3600;
        int minutes = (sec % 3600) / 60;
        int secs = sec % 60;

        // Format the seconds into hours, minutes, and seconds.
        String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
        return time;
    }
}