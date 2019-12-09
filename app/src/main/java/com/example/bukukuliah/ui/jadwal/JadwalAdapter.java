package com.example.bukukuliah.ui.jadwal;

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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.bukukuliah.FirebaseHelper;
import com.example.bukukuliah.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class JadwalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Jadwal> jadwalList;
    private static final int TYPE_JADWAL = 0;
    private static final int TYPE_ADD_NEW_JADWAL = 1;
    private String userUUID;

    public JadwalAdapter(Context context, List<Jadwal> jadwalist) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.db = FirebaseFirestore.getInstance();
        this.jadwalList = jadwalist;
        this.mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userUUID = mAuth.getCurrentUser().getUid();
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.new_jadwal_view, parent, false);
        loadUserInformation();
        return new newJadwalViewHolder(view, parent);
    }

    private void loadUserInformation() {


        db.collection("jadwal")
                .whereEqualTo("User_UUID", userUUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot documents : task.getResult()){
                            Log.d("Database" , documents.getId() + "=>" + documents.getData());
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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        newJadwalViewHolder newJadwalViewHolder = (newJadwalViewHolder) holder;
    }

    @Override
    public int getItemCount() {
        return 1;
    }


    class jadwalViewHolder extends RecyclerView.ViewHolder {

        public jadwalViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class newJadwalViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText judul, deskripsi, lokasi;
        Button InputDate, InputTime;
        DatePickerDialog.OnDateSetListener dateDialog;
        TimePickerDialog.OnTimeSetListener timeDialog;


        public newJadwalViewHolder(@NonNull View itemView, final ViewGroup parent) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View newJadwalDialogView = inflater.inflate(R.layout.dialog_new_jadwal, parent, false);
                    judul = newJadwalDialogView.findViewById(R.id.input_kegiatan);
                    deskripsi = newJadwalDialogView.findViewById(R.id.input_deskripsi_kegiatan);
                    lokasi = newJadwalDialogView.findViewById(R.id.input_lokasi);
                    InputDate = newJadwalDialogView.findViewById(R.id.input_date_kegiatan);
                    InputTime = newJadwalDialogView.findViewById(R.id.input_time_kegiatan);


                    InputDate.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
                    InputTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));

                    InputDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar cal = Calendar.getInstance();
                            int year = cal.get(Calendar.YEAR);
                            int month = cal.get(Calendar.MONTH);
                            int day = cal.get(Calendar.DAY_OF_MONTH);

                            DatePickerDialog dialog = new DatePickerDialog(
                                    context,
                                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                    dateDialog,
                                    year, month, day);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();
                        }
                    });

                    dateDialog = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            month = month + 1;
                            Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                            String date = month + "/" + day + "/" + year;
                            InputDate.setText(date);
                        }
                    };

                    InputTime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar cal = Calendar.getInstance();
                            int hour = cal.get(Calendar.HOUR_OF_DAY);
                            final int minute = cal.get(Calendar.MINUTE);

                            TimePickerDialog dialog = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, timeDialog, hour, minute, true);
                            dialog.setTitle("Choose Time : ");
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();
                        }
                    });


                    timeDialog = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int Hour, int Minute) {
                            InputTime.setText(Hour + ":" + Minute);
                        }
                    };


                    new AlertDialog.Builder(context)
                            .setTitle("Tambah Kegiatan Baru")
                            .setView(newJadwalDialogView)
                            .setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (judul.getText().toString() == null) {
                                        judul.setError("Please Fill The Title");
                                        judul.requestFocus();
                                    }
                                    if (deskripsi.getText().toString() == null) {
                                        deskripsi.setError("Please Fill The Description");
                                        deskripsi.requestFocus();
                                    }
                                    if (lokasi.getText().toString() == null) {
                                        lokasi.setError("Please Fill The Location");
                                        lokasi.requestFocus();
                                    }

                                    Map<String, Object> jadwal = new HashMap<String, Object>();

                                    jadwal.put(FirebaseHelper.JUDUL_KEGIATAN, judul.getText().toString());
                                    jadwal.put(FirebaseHelper.DESKRIPSI_KEGIATAN, deskripsi.getText().toString());
                                    jadwal.put(FirebaseHelper.LOKASI_KEGIATAN, lokasi.getText().toString());
                                    jadwal.put(FirebaseHelper.TANGGAL_KEGIATAN, InputDate.getText().toString());
                                    jadwal.put(FirebaseHelper.WAKTU_KEGIATAN, InputTime.getText().toString());
                                    jadwal.put(FirebaseHelper.USER_UUID, userUUID);

                                    db.collection("jadwal")
                                            .add(jadwal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
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
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            })
                            .create()
                            .show();
                }
            });

        }
    }


}
