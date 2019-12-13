package com.example.bukukuliah.ui.editor.images;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bukukuliah.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageListViewHolder> {

    private Context context;
    private List<SavedImage> savedImageList;
    private LayoutInflater layoutInflater;

    public static final String INTENT_IMAGE_URL = "intentimageurl";

    public ImageListAdapter(Context context, List<SavedImage> savedImageList){
        this.context = context;
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

        private ImageView imageItem;
        private TextView imageDesc;

        public ImageListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.image_list_item);
            imageDesc = itemView.findViewById(R.id.image_desc_list_item);
        }

        public void bind(final SavedImage savedImage){
            Picasso.get().load(savedImage.imgUrl).into(imageItem);
            String imgDesc = savedImage.desc;
            String date = new SimpleDateFormat("dd/MM/YYYY hh:mm:ss").format(savedImage.timestamp.toDate());
            imgDesc+="\n"+date;
            imageDesc.setText(imgDesc);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageViewerActivity.class);
                    intent.putExtra(INTENT_IMAGE_URL, savedImage.imgUrl);
                    context.startActivity(intent);
                }
            });
        }
    }
}
