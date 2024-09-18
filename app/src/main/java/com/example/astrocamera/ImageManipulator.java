package com.example.astrocamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ImageManipulator {

    private Bitmap currentImage;

    private Context context;

    private final List<Bitmap> images = new ArrayList<>();

    public ImageManipulator(Context context) {
        this.context = context;
    }

    public void saveImage(Bitmap bitmap) {
        FileOutputStream fileOutputStream;
        String name = "ASTRO_" + System.currentTimeMillis() + ".jpg";
        try {
            String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            //new File(dir, "AstronomyCameraStuff").mkdirs();
            dir = new File(dir, "AstronomyCameraStuff").getPath();
            File file = new File(dir, name);
            fileOutputStream = new FileOutputStream(file);
            //fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e("Cannot save", "Cannot save image to file");
        }
    }

    public Bitmap imageToBitMap(Image image) {

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    private int median(int a, int b, int c) {
        return Math.max(Math.min(a,b),Math.max(Math.min(b,c),Math.min(a,c)));
    }

    public void addImage(Image image) {
        images.add(imageToBitMap(image));
        if(images.size()==3) {
            int width=images.get(0).getWidth();
            int height = images.get(0).getHeight();
            int size=width*height;
            int[] first = new int[size];
            int[] second = new int[size];
            int[] third = new int[size];
            images.get(0).getPixels(first, 0, width, 0, 0, width, height);
            images.get(1).getPixels(second, 0, width, 0, 0, width, height);
            images.get(2).getPixels(third, 0, width, 0, 0, width, height);
            for(int i=0;i<size;i++) {
                int r1=android.graphics.Color.red(first[i]);
                int g1=android.graphics.Color.green(first[i]);
                int b1=android.graphics.Color.blue(first[i]);
                int r2=android.graphics.Color.red(second[i]);
                int g2=android.graphics.Color.green(second[i]);
                int b2=android.graphics.Color.blue(second[i]);
                int r3=android.graphics.Color.red(third[i]);
                int g3=android.graphics.Color.green(third[i]);
                int b3=android.graphics.Color.blue(third[i]);
                first[i]=android.graphics.Color.rgb(median(r1,r2,r3),median(g1,g2,g3),median(b1,b2,b3));
            }
            Bitmap copy = images.get(0).copy(Bitmap.Config.ARGB_8888, true);
            copy.setPixels(first, 0, width, 0, 0, width, height);
            images.clear();
            augmentImage(copy);
        }
    }
    public void augmentImage(Bitmap add) {
        if(currentImage == null)currentImage = add.copy(Bitmap.Config.ARGB_8888, true);
        else {
            int[] current = new int[currentImage.getWidth()*currentImage.getHeight()];
            int[] toAdd = new int[add.getWidth()*add.getHeight()];
            currentImage.getPixels(current, 0, currentImage.getWidth(), 0, 0, currentImage.getWidth(), currentImage.getHeight());
            add.getPixels(toAdd, 0, add.getWidth(), 0, 0, add.getWidth(), add.getHeight());
            for(int i=0;i<current.length;i++) {
                int r1=android.graphics.Color.red(current[i]);
                int g1=android.graphics.Color.green(current[i]);
                int b1=android.graphics.Color.blue(current[i]);
                int r2=android.graphics.Color.red(toAdd[i]);
                int g2=android.graphics.Color.green(toAdd[i]);
                int b2=android.graphics.Color.blue(toAdd[i]);
                r1=Math.max(r1,r2);
                g1=Math.max(g1,g2);
                b1=Math.max(b1,b2);
                current[i]=android.graphics.Color.rgb(r1,g1,b1);
            }
            currentImage.setPixels(current, 0, currentImage.getWidth(), 0, 0, currentImage.getWidth(), currentImage.getHeight());
        }
        saveImage(currentImage);
    }
}
