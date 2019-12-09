package com.example.bukukuliah.ui.jadwal;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bukukuliah.FirebaseHelper;
import com.example.bukukuliah.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JadwalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Jadwal> jadwalList;
    private static final int TYPE_NEW_JADWAL = 1;
    private static final int TYPE_JADWAL = 2;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private View.OnClickListener onClickNewJadwal;




    public JadwalAdapter(Context context, List<Jadwal> jadwalList, FirebaseAuth mAuth, FirebaseFirestore db, View.OnClickListener onClickNewJadwal) {
        this.context = context;
        this.jadwalList = jadwalList;
        this.mAuth = mAuth;
        this.db = db;
        this.onClickNewJadwal = onClickNewJadwal;
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
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case TYPE_NEW_JADWAL:
                break;
            case TYPE_JADWAL:

                jadwalViewHolder jadwalViewHolder = (jadwalViewHolder) holder;
                if (position< jadwalList.size()) {
                    Jadwal jadwal = jadwalList.get(position);
                    jadwalViewHolder.txtViewJadwal.setText(jadwal.Judul_kegiatan);
                    jadwalViewHolder.txtViewDeskripsi.setText(jadwal.Deskripsi_kegiatan);
                    jadwalViewHolder.txtViewHari.setText(jadwal.Hari_Kegiatan);
                    jadwalViewHolder.txtViewWaktu.setText(jadwal.Waktu_kegiatan);
                    jadwalViewHolder.txtViewLokasi.setText(jadwal.Lokasi_kegiatan);
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
        TextView txtViewJadwal, txtViewDeskripsi, txtViewHari, txtViewWaktu, txtViewLokasi;

        public jadwalViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_jadwal);
            linearLayout = itemView.findViewById(R.id.linearlayout);
            txtViewJadwal = itemView.findViewById(R.id.textViewJudulJadwal);
            txtViewDeskripsi = itemView.findViewById(R.id.textViewDeskripsiJadwal);
            txtViewHari = itemView.findViewById(R.id.textViewHariJadwal);
            txtViewWaktu = itemView.findViewById(R.id.textViewWaktuJadwal);
            txtViewLokasi = itemView.findViewById(R.id.textViewLokasiJadwal);
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
