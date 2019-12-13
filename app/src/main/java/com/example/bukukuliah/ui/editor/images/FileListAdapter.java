package com.example.bukukuliah.ui.editor.images;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bukukuliah.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ImageListViewHolder> {

    private Context context;
    private List<SavedFile> savedFileList;
    private LayoutInflater layoutInflater;
    public static final String INTENT_IMAGE_URL = "intentimageurl";
    private MediaPlayer player;

    public FileListAdapter(Context context, List<SavedFile> savedFileList, MediaPlayer player) {
        this.context = context;
        this.savedFileList = savedFileList;
        this.layoutInflater = LayoutInflater.from(context);
        this.player = player;
    }

    @NonNull
    @Override
    public ImageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.viewholder_image_list, parent, false);
        return new ImageListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageListViewHolder holder, int position) {
        holder.bind(savedFileList.get(position));
    }

    @Override
    public int getItemCount() {
        return savedFileList.size();
    }

    class ImageListViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageItem;
        private TextView imageDesc;

        public ImageListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.image_list_item);
            imageDesc = itemView.findViewById(R.id.image_desc_list_item);
        }

        public void bind(final SavedFile savedFile) {
            String imgDesc = savedFile.desc;
            String date = new SimpleDateFormat("dd/MM/YYYY hh:mm:ss").format(savedFile.timestamp.toDate());
            imgDesc += "\n" + date;
            imageDesc.setText(imgDesc);
            if (player == null) {
                Picasso.get().load(savedFile.fileUrl).into(imageItem);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ImageViewerActivity.class);
                        intent.putExtra(INTENT_IMAGE_URL, savedFile.fileUrl);
                        context.startActivity(intent);
                    }
                });
            } else {
                imageItem.setImageResource(R.drawable.ic_audiotrack_black_24dp);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {
                            player.reset();
                            player.setDataSource(savedFile.fileUrl);
                            player.prepare();
                            player.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }
    }
}
