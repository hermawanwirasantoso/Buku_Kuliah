package com.example.bukukuliah.ui.editor.images;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bukukuliah.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;
import static com.example.bukukuliah.ui.editor.images.ImageListAdapter.INTENT_IMAGE_URL;

public class ImageViewerActivity extends AppCompatActivity implements View.OnClickListener {

    private String url;
    private PhotoView mainImageView;
    private ProgressBar imageViewerProgressBar;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);   //new
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar()!=null) getSupportActionBar().hide();
        setContentView(R.layout.activity_image_viewer);
        mainImageView = findViewById(R.id.main_imageview);
        imageViewerProgressBar = findViewById(R.id.imageviewer_progressbar);
        Intent intent = getIntent();
        url = intent.getStringExtra(INTENT_IMAGE_URL);
        Picasso.get().load(url).into(mainImageView);

        MaterialButton putarButton =  findViewById(R.id.button_putar);
        putarButton.setOnClickListener(this);
        MaterialButton kembaliButton =  findViewById(R.id.button_kembali);
        kembaliButton.setOnClickListener(this);
        imageViewerProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_putar:
                new RotateImage().execute();
                break;
            case R.id.button_kembali:
                onBackPressed();
                break;
        }
    }


    private class RotateImage extends AsyncTask<Void, Void, Bitmap>{

        @Override
        protected void onPreExecute() {
            imageViewerProgressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                if (bitmap==null)
                    bitmap = Picasso.get().load(url).get();

                bitmap = rotateImage(bitmap, 90);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap!=null)
                mainImageView.setImageBitmap(bitmap);
            imageViewerProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
