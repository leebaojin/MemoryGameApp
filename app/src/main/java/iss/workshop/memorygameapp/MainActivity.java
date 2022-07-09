package iss.workshop.memorygameapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button btnNext;
    EditText urlInput;
    Button fetch;
    GridView gridView;
    ProgressBar progressBar;
    ObjectAnimator progressAnimator;
    TextView textView;
    int progress = 0;
    AsyncTask task;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(20);

        progressBar.setVisibility(View.GONE);

        textView = findViewById(R.id.textView);

        urlInput = findViewById(R.id.urlInput);
        urlInput.setText("https://stocksnap.io"); // set default value to save typing

        fetch = findViewById(R.id.fetchBtn);
        MainActivity THIS = this;
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                if (task !=null && (task.getStatus() == AsyncTask.Status.PENDING || task.getStatus() == AsyncTask.Status.RUNNING )){
                    task.cancel(true);
                }
                int count = 1;
                progressBar.setVisibility(View.VISIBLE);
                String strURL = urlInput.getText().toString();
                task = new ImagesWebScrape(THIS).execute(strURL);

                // tested on
                //https://www.google.com/search?q=flower&tbm=isch
                //https://stocksnap.io/search/beach
                //https://stocksnap.io
            }
        });

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

    private static class ImagesWebScrape extends AsyncTask<String, Integer, Elements> {
        private final WeakReference<MainActivity> activityReference;

        ImagesWebScrape(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            File[] files = activityReference.get().getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
            if (files != null) {
                for (File f : files) {
                    boolean result = f.delete();
                }
            }
            activityReference.get().progress = 0;
            activityReference.get().textView.setText("Downloading images...");
        }

        @Override
        protected Elements doInBackground(String... strURL) {
            try {
                Document document = Jsoup.connect(strURL[0])
                        .header("Accept-Encoding", "gzip, deflate")
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                        .maxBodySize(0).timeout(0).get();
                Elements elements = document.select("img");
                return elements;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Elements elements) {
            int numOfImages = 20;
            int imgCounter = 1;
            for (Element e : elements) {
                if (imgCounter <= numOfImages) {
                    String imgUrl = e.attr("src");
                    if (imgUrl.contains("https") || imgUrl.contains("http")) {
                        activityReference.get().progressBar.setVisibility(View.VISIBLE);
                        activityReference.get().startDownloadImage(imgUrl, imgCounter);
                        imgCounter++;
                    }
                }
            }
        }
    }


    protected void startDownloadImage(String strImgURL, int counter) {
        String strImgName = strImgURL.substring(strImgURL.lastIndexOf("/") + 1);
        System.out.println("Saving" + strImgName);
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File destFile = new File(dir, counter + ".jpg");

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (downloadImage(strImgURL, destFile)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = BitmapFactory.decodeFile(destFile.getAbsolutePath());
                            GridView gridView = findViewById(R.id.imageGrid);
//                            gridView.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        }).start();
    }

    protected boolean downloadImage(String strImgURL, File destFile) {

        try {
            URL urlImg = new URL(strImgURL);
            InputStream in = urlImg.openStream();
            OutputStream out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            in.close();
            progress++;
            progressBar.setProgress(progress,true);
            if ( progress >= 20){
                textView.setText("Downloaded 20 out of 20 images!");
                Thread.sleep(1000);
                progressBar.setVisibility(View.GONE);
            }else{
                textView.setText("Downloading " + progress + " out of 20 images...");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void closeKeyboard()
    {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {

            // now assign the system
            // service to InputMethodManager
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }
}