package com.nazran.ffmpegphotostovideo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * The Class GallarySample.
 */
public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = GalleryActivity.class.getSimpleName();
    private int gridSize = 0, imageTouchCount = 0;
    private ArrayList<String> images = new ArrayList<>();
    private ArrayList<String> imagesReverse = new ArrayList<>();
    private Toolbar toolbar;
    private ImageView selectedImageView;
    private String selectedImage;
    private ImageView next, back;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        selectedImageView = (ImageView) findViewById(R.id.selectedPic);
        progressDialog = new ProgressDialog(GalleryActivity.this);
        setSupportActionBar(toolbar);

        makeGridSize();

        images = getAllShownImagesPath(GalleryActivity.this);

        imagesReverse = new ArrayList<String>();
        for (int i = images.size() - 1; i >= 0; i--) {
            imagesReverse.add(images.get(i));
        }

        selectedImage = images.get(images.size() - 1);

        Glide.with(GalleryActivity.this)
                .load(images.get(images.size() - 1))
                .apply(bitmapTransform(new CropTransformation(800, 800, CropTransformation.CropType.CENTER)))
                .into(selectedImageView);

        GridView gallery = (GridView) findViewById(R.id.galleryGridView);
        gallery.setAdapter(new ImageAdapter(this, imagesReverse));
        gallery.setNumColumns(3);

        gallery.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (imageTouchCount < 3) {
                    if (imagesReverse != null && !imagesReverse.isEmpty()) {
                        imageTouchCount++;
                        selectedImage = imagesReverse.get(position);

                        progressDialog.setMessage("Cropping and saving...");
                        progressDialog.show();

                        Glide.with(GalleryActivity.this)
                                .load(imagesReverse.get(position))
                                .into(selectedImageView);

                        Glide.with(GalleryActivity.this)
                                .load(imagesReverse.get(position))
                                .apply(bitmapTransform(new CropTransformation(800, 800, CropTransformation.CropType.CENTER)))
                                .into(new SimpleTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        saveDrawableToMemory(resource, imageTouchCount);
                                    }
                                });
                        if (imageTouchCount == 3)
                            next.setVisibility(View.VISIBLE);
                    }
                } else {
                    Snackbar.make(arg1, "Please press Next button to generate the video", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void makeGridSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.e("width", "" + width);
        Log.e("height", "" + height);

        gridSize = width / 3;
        Log.e("grids", "" + gridSize);
    }

    private void saveDrawableToMemory(Drawable resource, int fileNo) {
        Bitmap bm = drawableToBitmap(resource);

        if (fileNo == 1) {
            Canvas canvas = new Canvas(bm);
            Paint paint1 = new Paint();
            paint1.setColor(Color.WHITE);
            paint1.setTextSize(70);
            canvas.drawText("RISE", 400, 340, paint1);

            Paint paint2 = new Paint();
            paint2.setColor(Color.WHITE);
            paint2.setTextSize(70);
            canvas.drawText("TOGETHER", 400, 420, paint2);

            Paint paint3 = new Paint();
            paint3.setColor(Color.WHITE);
            paint3.setTextSize(70);
            canvas.drawText("WELCOME", 225, 100, paint3);

            Paint paint4 = new Paint();
            paint4.setColor(Color.WHITE);
            paint4.setTextSize(50);
            canvas.drawText("START BEFORE YOU ARE READY", 25, 700, paint4);
        } else if (fileNo == 2) {
            Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/CONA.ttf");
            Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/LondrinaShadow-Regular.ttf");
            Typeface face3 = Typeface.createFromAsset(getAssets(), "fonts/Salmela.ttf");
            Canvas canvas = new Canvas(bm);
            Paint paint1 = new Paint();
            paint1.setColor(Color.WHITE);
            paint1.setTextSize(80);
            paint1.setTypeface(face3);
            canvas.drawText("BEAUTIFUL", 150, 200, paint1);

            Paint paint3 = new Paint();
            paint3.setColor(Color.WHITE);
            paint3.setTextSize(40);
            paint3.setTypeface(face1);
            canvas.drawText("THE WORLD IS", 300, 100, paint3);

            Paint paint4 = new Paint();
            paint4.setColor(Color.WHITE);
            paint4.setTextSize(50);
            paint3.setTypeface(face2);
            canvas.drawText("SAVE THE PLANET", 200, 750, paint4);
        } else if (fileNo == 3) {
            Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/CONA.ttf");
            Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/LondrinaShadow-Regular.ttf");
            Typeface face3 = Typeface.createFromAsset(getAssets(), "fonts/Salmela.ttf");
            Canvas canvas = new Canvas(bm);
            Paint paint1 = new Paint();
            paint1.setColor(Color.WHITE);
            paint1.setTextSize(80);
            paint1.setTypeface(face3);
            canvas.drawText("BEAUTIFUL", 150, 200, paint1);

            Paint paint3 = new Paint();
            paint3.setColor(Color.WHITE);
            paint3.setTextSize(40);
            paint3.setTypeface(face1);
            canvas.drawText("THE WORLD IS", 300, 100, paint3);

            Paint paint4 = new Paint();
            paint4.setColor(Color.WHITE);
            paint4.setTextSize(50);
            paint3.setTypeface(face2);
            canvas.drawText("SAVE THE PLANET", 200, 750, paint4);
        }

        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File file = new File(picDir, "video_photo00" + fileNo + ".PNG");
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            try {
                outStream.flush();
                outStream.close();
                progressDialog.dismiss();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Getting All Images Path.
     *
     * @param activity the activity
     * @return ArrayList with images Path
     */
    private ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            Log.e(TAG, absolutePathOfImage);
            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    /**
     * The Class ImageAdapter.
     */
    private class ImageAdapter extends BaseAdapter {

        /**
         * The context.
         */
        private Activity context;
        private ArrayList<String> imagesList;

        /**
         * Instantiates a new image adapter.
         *
         * @param localContext the local context
         */
        public ImageAdapter(Activity localContext, ArrayList<String> images) {
            context = localContext;
            imagesList = images;
        }

        public int getCount() {
            return imagesList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView.setLayoutParams(new GridView.LayoutParams(gridSize, gridSize));

            } else {
                picturesView = (ImageView) convertView;
            }

            Glide.with(context)
                    .load(imagesList.get(position))
                    .into(picturesView);

            return picturesView;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.gallery_menu, menu);

        MenuItem item = menu.findItem(R.id.galleryMenu);
        MenuItemCompat.setActionView(item, R.layout.gallery_menu_layout);
        RelativeLayout relativeLayout = (RelativeLayout) MenuItemCompat.getActionView(item);

        next = relativeLayout.findViewById(R.id.next);
        back = relativeLayout.findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                back.startAnimation(animFadeIn);

                onBackPressed();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                next.startAnimation(animFadeIn);

                startActivity(new Intent(GalleryActivity.this, VideoViewActivity.class));
                finish();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}