package iss.workshop.memorygameapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class ImageAdaptor extends ArrayAdapter<Object> {
    //Keep reference to the image cell
    private static final int cellLayout = R.layout.imagecell;

    private final Context context;
    protected String imageName;
    protected int resourceSize;


    public ImageAdaptor(@NonNull Context context, String imageName, int resourceSize) {
        super(context, cellLayout);
        this.context = context;
        this.imageName = imageName;
        this.resourceSize = resourceSize;

        addAll(new Object[resourceSize]);
    }

    public View getView(int pos, View view, ViewGroup parent){
        if(view== null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(cellLayout,parent,false);
            view.setTag(String.valueOf(pos));
        }
        //Set the image for imageDisplay
        //Assume no image has been downloaded
        ImageView imageView = view.findViewById(R.id.imageView);
        int id = context.getResources().getIdentifier(imageName,"drawable",context.getPackageName());
        imageView.setImageResource(id);

        return view;
    }


}
