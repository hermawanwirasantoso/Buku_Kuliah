package com.example.bukukuliah.ui.jadwal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.bukukuliah.FirebaseHelper;
import com.example.bukukuliah.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class JadwalFragment extends Fragment {

    private JadwalViewModel jadwalViewModel;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private List<Jadwal> jadwalList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Context context;
    private JadwalAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        jadwalViewModel =
                ViewModelProviders.of(this).get(JadwalViewModel.class);
        View root = inflater.inflate(R.layout.fragment_jadwal, container, false);
        context = root.getContext();
        jadwalList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();




        recyclerView = (RecyclerView) root.findViewById(R.id.jadwalRecycleView);

        adapter = new JadwalAdapter(context, jadwalList, mAuth, db, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewJadwal();
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Log.d("ERROR", "jadwallist FINAL : " + jadwalList.size());
        getUserJadwal();
        return root;
    }

    private void addNewJadwal() {
        final DatePickerDialog.OnDateSetListener dateDialog;

        LayoutInflater inflater = LayoutInflater.from(context);
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View dialogView = inflater.inflate(R.layout.dialog_new_jadwal, null);
        final TextInputEditText judulKegiatan = (TextInputEditText) dialogView.findViewById(R.id.input_kegiatan);
        final TextInputEditText deskripsiKegiatan = (TextInputEditText) dialogView.findViewById(R.id.input_deskripsi_kegiatan);
        final MaterialButton hariKegiatan = (MaterialButton) dialogView.findViewById(R.id.input_date_kegiatan);
        final MaterialButton waktuKegiatan = (MaterialButton) dialogView.findViewById(R.id.input_time_kegiatan);
        final TextInputEditText lokasiKegiatan = (TextInputEditText) dialogView.findViewById(R.id.input_lokasi);

        hariKegiatan.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
        waktuKegiatan.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));


        hariKegiatan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog dialog = new DatePickerDialog(
                        context,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month = month + 1;

                                String date = month + "/" + dayOfMonth + "/" + year;
                                hariKegiatan.setText(date);
                            }
                        },
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        waktuKegiatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                final int minute = cal.get(Calendar.MINUTE);

                TimePickerDialog dialog = new TimePickerDialog(
                        context,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                waktuKegiatan.setText(hourOfDay + ":" + minute);
                            }
                        },
                        hour, minute, true);
                dialog.setTitle("Choose Time : ");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });


        mBuilder.setView(dialogView);
        mBuilder.setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (judulKegiatan.getText().toString() == null) {
                    judulKegiatan.setError("Please Fill The Title");
                    judulKegiatan.requestFocus();
                }
                if (deskripsiKegiatan.getText().toString() == null) {
                    deskripsiKegiatan.setError("Please Fill The Description");
                    deskripsiKegiatan.requestFocus();
                }
                if (lokasiKegiatan.getText().toString() == null) {
                    lokasiKegiatan.setError("Please Fill The Location");
                    lokasiKegiatan.requestFocus();
                }

                Map<String, Object> jadwal = new HashMap<String, Object>();
                jadwal.put(FirebaseHelper.JUDUL_KEGIATAN, judulKegiatan.getText().toString());
                jadwal.put(FirebaseHelper.DESKRIPSI_KEGIATAN, deskripsiKegiatan.getText().toString());
                jadwal.put(FirebaseHelper.LOKASI_KEGIATAN, lokasiKegiatan.getText().toString());
                jadwal.put(FirebaseHelper.TANGGAL_KEGIATAN, hariKegiatan.getText().toString());
                jadwal.put(FirebaseHelper.WAKTU_KEGIATAN, waktuKegiatan.getText().toString());
                jadwal.put(FirebaseHelper.USER_UUID, mAuth.getCurrentUser().getUid());

                db.collection(FirebaseHelper.COLLECTION_JADWAL)
                        .add(jadwal)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                getUserJadwal();
                                Toast.makeText(context, "Jadwal Berhasil Ditambahkan", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog dialog = mBuilder.create();
        dialog.show();


    }

    private void getUserJadwal() {
        db.collection("jadwal")
                .whereEqualTo("User_UUID", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if(task.getResult()!=null){
                                jadwalList.clear();
                                for (QueryDocumentSnapshot documents : task.getResult()) {

                                    Log.d("ERROR", "onComplete: " + documents.getData());

                                    jadwalList.add(new Jadwal(documents.getData().get(FirebaseHelper.JUDUL_KEGIATAN).toString(),
                                            documents.getData().get(FirebaseHelper.DESKRIPSI_KEGIATAN).toString(),
                                            documents.getData().get(FirebaseHelper.TANGGAL_KEGIATAN).toString(),
                                            documents.getData().get(FirebaseHelper.WAKTU_KEGIATAN).toString(),
                                            documents.getData().get(FirebaseHelper.LOKASI_KEGIATAN).toString()));
                                    adapter.notifyDataSetChanged();
                                    Log.d("ERROR", "jadwallist size : " + jadwalList.size());

                                }
                            }
                        }else{
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}