package iss.workshop.memorygameapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class loadingScreen extends AppCompatActivity {
    Button singlePlayerBtn;
    Button doublePlayerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        ImageView backgroundImage = findViewById(R.id.SplashScreenImage);
        TextView gameTitle = findViewById(R.id.gameTitle);
        Animation slideAnimation = AnimationUtils.loadAnimation(this, R.anim.side_slide);
        backgroundImage.startAnimation(slideAnimation);
        gameTitle.startAnimation(slideAnimation);

        singlePlayerBtn = findViewById(R.id.btn1Player);
        singlePlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextIntent(1);
            }
        });
        doublePlayerBtn = findViewById(R.id.btn2Player);
        doublePlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextIntent(2);
            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Intent intent = new Intent(loadingScreen.this, MainActivity.class);
                //startActivity(intent);
                //finish();
                singlePlayerBtn.setVisibility(View.VISIBLE);
                doublePlayerBtn.setVisibility(View.VISIBLE);
            }
        },3000);
    }

    private void nextIntent(int noOfPlayer){
        if(noOfPlayer != 1 || noOfPlayer != 2){
            noOfPlayer = 1;
        }
        Intent intent = new Intent(loadingScreen.this, MainActivity.class);
        intent.putExtra("playerNo", noOfPlayer);
        startActivity(intent);
        finish();
    }
}