package iss.workshop.memorygameapp;

import androidx.appcompat.app.AppCompatActivity;

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

    TextView timerTxt;
    int seconds = 0;
    Button stopBtn;
    Boolean timerRunning;
    private TextView matchesTxt;
    private File dir;
    private boolean isWaitingClose;

    private GameService gs;

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

        stopBtn = findViewById(R.id.stopTimer);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerRunning = false;
            }
        });

        matchesTxt = findViewById(R.id.txtNumOfMatches);

        try {
            gs = new GameService(gameString);
            isWaitingClose = false;
            setGridImages();
        }
        catch(Exception e){
            finish();
        }



        mediaPlayer = MediaPlayer.create(this, R.raw.bgmusic);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void makeMove(AdapterView<?> adapterView, int index) throws InterruptedException {
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
                Toast.makeText(this,"Game over",Toast.LENGTH_LONG).show();
            }
        }

    }

    private void setGridImages(){
        ImageAdaptor adaptor = new ImageAdaptor(this,"cross_image",12);
        GridView gridView = findViewById(R.id.gameGrid);
        if(gridView != null){
            gridView.setAdapter(adaptor);
            gridView.setNumColumns(3);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        makeMove(adapterView, i);
                    }
                    catch(Exception e){

                    }
                }
            });
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

        imgview.setImageBitmap(bitmap);
    }

    private void setImageToDefault(AdapterView<?> adapterView, int index){
        View myview = adapterView.findViewWithTag(String.valueOf(index));
        ImageView imgview = myview.findViewById(R.id.imageView);
        //int id = context.getResources().getIdentifier(imageName,"drawable",context.getPackageName());
        imgview.setImageResource(R.drawable.cross_image);
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