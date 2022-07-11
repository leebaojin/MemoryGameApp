package iss.workshop.memorygameapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.File;

public class ImageAdaptor extends ArrayAdapter<Object> {
    //Keep reference to the image cell
    private static final int cellLayout = R.layout.imagecell;

    private final Context context;
    protected String imageName = "cross_image";
    protected int resourceSize;
    protected File[] files;


    public ImageAdaptor(@NonNull Context context, int resourceSize) {
        super(context, cellLayout);
        this.context = context;
        this.resourceSize = resourceSize;

        addAll(new Object[resourceSize]);
    }

    public ImageAdaptor(@NonNull Context context,File[] files){
        super(context, cellLayout);
        this.context = context;
        this.resourceSize = 20;
        this.files = files;
        addAll(new Object[resourceSize]);
    }

    public void UpdateFiles(File[] files){
        this.files = files;
    }

    public View getView(int pos, View view, ViewGroup parent){
        ImageView imageView;
        try {
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

                view = inflater.inflate(cellLayout,parent,false);
                view.setTag(String.valueOf(pos));

            }
            //Set the image for imageDisplay
            //Assume no image has been downloaded
            imageView = view.findViewById(R.id.imageView);
            if (this.files != null && this.files.length > 0) {
                if (this.files[pos] != null) {
                    File destFile = this.files[pos];
                    Bitmap bitmap = BitmapFactory.decodeFile(destFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                    imageView.setAlpha(1f);
                } else {
                    int id = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                    imageView.setImageResource(id);
                }
            } else {
                int id = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                imageView.setImageResource(id);
            }

            return view;
        } catch (Exception ex){
            // handles non-graceful interruption of fetch
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.imagecell, parent, false);
            }
            int id = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
            imageView = view.findViewById(R.id.imageView);
            imageView.setImageResource(id);
            return view;
        }
    }


}
