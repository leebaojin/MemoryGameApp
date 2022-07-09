package iss.workshop.memorygameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity{

    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNext = findViewById(R.id.buttonNext);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GameActivity.class);
                startActivity(intent);
            }
        });

        moveToNextIntent();
        ImageAdaptor adaptor = new ImageAdaptor(this,"blankimage",20);
        GridView gridView = findViewById(R.id.imageGrid);
        if(gridView != null){
            gridView.setAdapter(adaptor);
            gridView.setNumColumns(4);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
        }
    }

    private void moveToNextIntent(){
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

    }


}