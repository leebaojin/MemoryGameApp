package iss.workshop.memorygameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GameActivity extends AppCompatActivity {

    private TextView timerTxt;
    private Button stopBtn;
    private Button nextBtn;
    private TextView matchesTxt;

    private File dir;

    private int[] playerScore = {0,0};
    private int currentPlayer;

    private int seconds = 0;
    private Boolean timerRunning;
    private GameService gs;
    private boolean isWaitingClose;

    MediaPlayer mediaPlayer;



    private String[] gameString = {
            "gameimg0.jpg", "gameimg1.jpg", "gameimg2.jpg", "gameimg3.jpg", "gameimg4.jpg", "gameimg5.jpg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        startTimer();

        matchesTxt = findViewById(R.id.txtNumOfMatches);
        stopBtn = findViewById(R.id.stopTimer);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerRunning = false;
            }
        });

        nextBtn = findViewById(R.id.nextPlayer);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupGame();
                view.setVisibility(View.GONE);
                seconds = 0; // Restart timer
                currentPlayer += 1;
                startTimer();
            }
        });
        currentPlayer = 1;
        setupGame();


        mediaPlayer = MediaPlayer.create(this, R.raw.bgmusic);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void setupGame(){
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
        ImageAdaptor adaptor = new ImageAdaptor(this,"cross_image",12);
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
            //Second time open
            String imageName = gs.makeMove(index);

            if(gs.CheckMatch()){
                //If check match is a success
                setImageOnIndex(adapterView, index, imageName);

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
                    }
                },1000);

            }

            if(gs.isGameOver()){
                //If game is over
                timerRunning = false; // Stop timer
                playerScore[currentPlayer-1]=seconds;
                if(currentPlayer<2){
                    nextBtn.setVisibility(View.VISIBLE);
                }

                Toast.makeText(this,"Game over",Toast.LENGTH_LONG).show();
            }
        }

    }

    private void setImageOnIndex(AdapterView<?> adapterView, int index, String imageName){
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
        View myview = adapterView.findViewWithTag(String.valueOf(index));
        ImageView imgview = myview.findViewById(R.id.imageView);
        //int id = context.getResources().getIdentifier(imageName,"drawable",context.getPackageName());
        //imgview.setImageResource(R.drawable.cross_image);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cross_image);
        flipAnimation(imgview,bitmap);
    }

    private void flipAnimation(ImageView imgview, Bitmap nextimg){
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
                objectAnimator2.start();
                imgview.setImageBitmap(nextimg);
            }
        });

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