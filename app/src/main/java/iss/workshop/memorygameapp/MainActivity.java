package iss.workshop.memorygameapp;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    EditText urlInput;
    Button fetch;
    GridView gridView;
    ProgressBar progressBar;
    ObjectAnimator progressAnimator;
    TextView textView;
    int progress = 0;
    AsyncTask task;
    Thread downloadThread;
    boolean isDownloading;
    ImageAdaptor adaptor;


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
        urlInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stop download if download thread is running
                if (downloadThread != null && isDownloading == true) {
                    downloadThread.interrupt();
                    downloadThread = null;
                    isDownloading = false;
                    progressBar.setVisibility(View.GONE);
                    textView.setText("STOPPED");
                }
            }
        });

        fetch = findViewById(R.id.fetchBtn);
        MainActivity THIS = this;
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                if (task !=null && (task.getStatus() == AsyncTask.Status.PENDING || task.getStatus() == AsyncTask.Status.RUNNING )){
                    task.cancel(true);
                }

                showProgress(true);

                String strURL = urlInput.getText().toString();
                task = new ImagesWebScrape(THIS).execute(strURL);

                // tested on
                //https://www.google.com/search?q=flower&tbm=isch
                //https://stocksnap.io/search/beach
                //https://stocksnap.io
            }
        });

        deleteAllDownloads();

        adaptor = new ImageAdaptor(this);
        gridView = findViewById(R.id.imageGrid);
        if (gridView != null) {
            gridView.setAdapter(adaptor);
            gridView.setNumColumns(4);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
        }

    }

    private void showProgress(boolean show){
        if (show){
            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        }

    }

    private void deleteAllDownloads(){
        File[] files = getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();
        if (files != null) {
            for (File f : files) {
                boolean result = f.delete();
            }
        }
    }

    //for use in imagecell.xml
    public void setImageViewAlpha(View v){
        ImageView imageView = v.findViewById(R.id.imageView);
        if(imageView.getAlpha() != 1f){
            imageView.setAlpha(1f);
        } else {
            imageView.setAlpha(0.5f);
        }
    }

    private void closeKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private static class ImagesWebScrape extends AsyncTask<String, Integer, Elements> {
        private final WeakReference<MainActivity> activityReference;

        ImagesWebScrape(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activityReference.get().deleteAllDownloads();
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
            String[] imgUrls = new String[20];
            if (elements == null){
                Toast.makeText(activityReference.get(), R.string.invalidUrlMsg, Toast.LENGTH_SHORT).show();
                activityReference.get().showProgress(false);
                return;
            }
            for (Element e : elements) {
                if (imgCounter <= numOfImages) {
                    String imgUrl = e.attr("src");
                    if (imgUrl.contains("https") || imgUrl.contains("http")) {
                        imgUrls[imgCounter - 1] = imgUrl;
                        imgCounter++;
                    }
                }
            }
            activityReference.get().startDownloadMultipleImages(imgUrls);
        }
    }

    protected void startDownloadMultipleImages(String... imgUrls) {
        downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isDownloading = true;
                if (downloadMultipleImages(imgUrls)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Thread.interrupted()) {
                                return;
                            }
                            adaptor.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        downloadThread.start();
    }

    protected boolean downloadMultipleImages(String... imgUrls) {

        try {
            for(int i = 0; i < imgUrls.length; i++){
                String imgUrl = imgUrls[i];
                URL urlImg = new URL(imgUrl);
                InputStream in = urlImg.openStream();
                File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File destFile = new File(dir, i+1 + ".jpg");
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
                }else{
                    textView.setText("Downloading " + progress + " out of 20 images...");
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}