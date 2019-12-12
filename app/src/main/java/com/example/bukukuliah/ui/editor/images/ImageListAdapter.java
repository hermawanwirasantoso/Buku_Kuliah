package com.example.bukukuliah.ui.editor.images;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bukukuliah.R;

import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageListViewHolder> {

    private List<SavedImage> savedImageList;
    private LayoutInflater layoutInflater;

    public ImageListAdapter(Context context, List<SavedImage> savedImageList){
        this.savedImageList = savedImageList;
        this.layoutInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public ImageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.viewholder_image_list, parent, false);
        return new ImageListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageListViewHolder holder, int position) {
        holder.bind(savedImageList.get(position));
    }

    @Override
    public int getItemCount() {
        return savedImageList.size();
    }

    class ImageListViewHolder extends RecyclerView.ViewHolder{

        private TextView imageItem;

        public ImageListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.image_list_item);
        }

        public void bind(SavedImage savedImage){
            imageItem.setText(savedImage.desc);
        }
    }
}
