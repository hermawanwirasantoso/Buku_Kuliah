package com.example.bukukuliah.ui.jadwal;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bukukuliah.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


public class JadwalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Jadwal> jadwalList;
    private static final int TYPE_NEW_JADWAL = 1;
    private static final int TYPE_JADWAL = 2;

    private View.OnClickListener onClickNewJadwal;
    private Openjadwal openjadwal;

    interface Openjadwal {
        void onClickjadwal(String id);
    }






    public JadwalAdapter(Context context, List<Jadwal> jadwalList, View.OnClickListener onClickNewJadwal, Openjadwal openjadwal) {
        this.context = context;
        this.jadwalList = jadwalList;
        this.onClickNewJadwal = onClickNewJadwal;
        this.openjadwal = openjadwal;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_NEW_JADWAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_jadwal_view, parent, false);
            newJadwalViewHolder newJadwalViewHolder = new newJadwalViewHolder(view, onClickNewJadwal);
            return newJadwalViewHolder;
        } else {
            View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.jadwal_view, parent, false);
            jadwalViewHolder jadwalViewHolder = new jadwalViewHolder(view2);
            return jadwalViewHolder;
        }


    }

    @Override
    public int getItemViewType(int position) {

        if (position == getItemCount() - 1) {
            return TYPE_NEW_JADWAL;
        } else {
            return TYPE_JADWAL;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()){
            case TYPE_NEW_JADWAL:
                break;
            case TYPE_JADWAL:

                jadwalViewHolder jadwalViewHolder = (jadwalViewHolder) holder;
                if (position< jadwalList.size()) {
                    Jadwal jadwal = jadwalList.get(position);
                    jadwalViewHolder.txtViewJadwal.setText(jadwal.Judul_kegiatan);
                    jadwalViewHolder.txtViewDeskripsi.setText(jadwal.Deskripsi_kegiatan);
                    jadwalViewHolder.txtViewHari.setText(jadwal.DayOfWeek+","+jadwal.Hari_Kegiatan);
                    jadwalViewHolder.txtViewWaktuStart.setText(": "+jadwal.Waktu_kegiatan_START);
                    jadwalViewHolder.txtViewWaktuEnd.setText(": "+jadwal.Waktu_kegiatan_END);
                    jadwalViewHolder.txtViewLokasi.setText(jadwal.Lokasi_kegiatan);

                    jadwalViewHolder.bind(jadwal);
                }
                break;
        }


    }

    @Override
    public int getItemCount() {
        return jadwalList.size() +1;

    }

    public class jadwalViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        LinearLayout linearLayout;
        TextView txtViewJadwal, txtViewDeskripsi, txtViewHari, txtViewWaktuStart, txtViewWaktuEnd, txtViewLokasi;

        public jadwalViewHolder(@NonNull View itemView ) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_jadwal);
            linearLayout = itemView.findViewById(R.id.linearlayout);
            txtViewJadwal = itemView.findViewById(R.id.textViewJudulJadwal);
            txtViewDeskripsi = itemView.findViewById(R.id.textViewDeskripsiJadwal);
            txtViewHari = itemView.findViewById(R.id.textViewHariJadwal);
            txtViewWaktuStart = itemView.findViewById(R.id.textViewWaktuJadwalStart);
            txtViewWaktuEnd = itemView.findViewById(R.id.textViewWaktuJadwalEnd);
            txtViewLokasi = itemView.findViewById(R.id.textViewLokasiJadwal);

        }

        void bind(final Jadwal jadwal){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openjadwal.onClickjadwal(jadwal.Jadwal_id);
                }
            });

        }
    }

    public class newJadwalViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayout;
        CardView cardView;
        TextView textViewJadwalBaru;

        public newJadwalViewHolder(@NonNull View itemView, View.OnClickListener onClickListener) {
            super(itemView);

            relativeLayout = itemView.findViewById(R.id.relativeLayout);
            cardView = itemView.findViewById(R.id.card_jadwal);
            textViewJadwalBaru = itemView.findViewById(R.id.tv_new_jadwal);
            itemView.setOnClickListener(onClickListener);
        }
    }


}
