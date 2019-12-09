package com.example.bukukuliah.ui.buku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bukukuliah.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BukuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Buku> bukuList;
    private LayoutInflater inflater;
    private int height;
    private static final int TYPE_NOTE = 0;
    private static final int TYPE_ADD_NEW_NOTE = 1;
    private FirebaseFirestore db;
    private View.OnClickListener onClickNewBuku;
    private OpenBuku openBuku;

    interface OpenBuku {
        void onClickBuku(String key, String judul);
    }

    public BukuAdapter(Context context, List<Buku> bukuList, int height, OpenBuku openBuku, View.OnClickListener onClickNewBuku) {
        this.context = context;
        this.bukuList = bukuList;
        this.inflater = LayoutInflater.from(context);
        this.height = height;
        this.db = FirebaseFirestore.getInstance();
        this.onClickNewBuku = onClickNewBuku;
        this.openBuku = openBuku;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD_NEW_NOTE) {
            View itemView = inflater.inflate(R.layout.viewholder_new_buku, parent, false);
            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height / 2));
            return new NewCatatanViewHolder(itemView, parent, onClickNewBuku);
        }else {
            View itemView = inflater.inflate(R.layout.viewholder_buku, parent, false);
            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height / 2));
            return new CatatanViewHolder(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_ADD_NEW_NOTE;
        } else {
            return TYPE_NOTE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case TYPE_ADD_NEW_NOTE:
                break;
            case TYPE_NOTE:
                CatatanViewHolder catatanViewHolder = (CatatanViewHolder) holder;
                if (position< bukuList.size()) {
                    Buku buku = bukuList.get(position);
                    catatanViewHolder.bind(buku);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bukuList.size() + 1;
    }

    class CatatanViewHolder extends RecyclerView.ViewHolder {

        CardView bukuCardView;
        TextView judul, deskripsi;

        CatatanViewHolder(@NonNull View itemView) {
            super(itemView);
            bukuCardView = itemView.findViewById(R.id.card_buku);
            judul = itemView.findViewById(R.id.tv_judul_buku);
            deskripsi = itemView.findViewById(R.id.tv_deskripsi_buku);
        }

        void bind(final Buku buku){
            judul.setText(buku.judul);
            deskripsi.setText(buku.deskripsi);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openBuku.onClickBuku(buku.key, buku.judul);
                }
            });
        }
    }

    class NewCatatanViewHolder extends RecyclerView.ViewHolder {

        CardView bukuCardView;
        TextView judul, deskripsi;

        NewCatatanViewHolder(@NonNull View itemView, final ViewGroup parent, View.OnClickListener onClickListener) {
            super(itemView);
            bukuCardView = itemView.findViewById(R.id.card_buku);
            judul = itemView.findViewById(R.id.tv_judul_buku);
            deskripsi = itemView.findViewById(R.id.tv_deskripsi_buku);
            itemView.setOnClickListener(onClickListener);
        }
    }
}
