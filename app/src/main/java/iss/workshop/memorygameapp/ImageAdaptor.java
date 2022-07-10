package iss.workshop.memorygameapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAdaptor extends ArrayAdapter<Object> {
    //Keep reference to the image cell
    private static final int cellLayout = R.layout.imagecell;

    private final Context context;
    protected String imageName;
    protected int resourceSize;
    protected List<Bitmap> gridViewImages = new ArrayList<>();

    public ImageAdaptor(@NonNull Context context, String imageName, int resourceSize) {
        super(context, cellLayout);
        this.context = context;
        this.imageName = imageName;
        this.resourceSize = resourceSize;

        addAll(new Object[resourceSize]);
    }

    public ImageAdaptor(@NonNull Context context){
        super(context, cellLayout);
        this.context = context;
        this.resourceSize = 20;

        addAll(new Object[resourceSize]);
    }

    public View getView(int pos, View view, ViewGroup parent){
        File[] files = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();

        if(view== null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(cellLayout,parent,false);
            view.setTag(String.valueOf(pos));
        }
        //Set the image for imageDisplay
        //Assume no image has been downloaded
        ImageView imageView = view.findViewById(R.id.imageView);
        if(files != null && files.length > 0){
            File destFile = files[pos];
            Bitmap bitmap = BitmapFactory.decodeFile(destFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
            imageView.setAlpha(0.5f);
        } else {
            int id = context.getResources().getIdentifier("blankimage","drawable",context.getPackageName());
            imageView.setImageResource(id);
        }

        return view;
    }


}
