package com.example.bukukuliah.ui.jadwal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
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
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.bukukuliah.FirebaseHelper.COLLECTION_JADWAL;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_USERS;
import static com.example.bukukuliah.FirebaseHelper.DAY_OF_WEEK;
import static com.example.bukukuliah.FirebaseHelper.DESKRIPSI_KEGIATAN;
import static com.example.bukukuliah.FirebaseHelper.EVENT_ID;
import static com.example.bukukuliah.FirebaseHelper.JUDUL_KEGIATAN;
import static com.example.bukukuliah.FirebaseHelper.LOKASI_KEGIATAN;
import static com.example.bukukuliah.FirebaseHelper.TANGGAL_KEGIATAN;
import static com.example.bukukuliah.FirebaseHelper.WAKTU_KEGIATAN_END;
import static com.example.bukukuliah.FirebaseHelper.WAKTU_KEGIATAN_START;


public class JadwalFragment extends Fragment implements JadwalAdapter.Openjadwal {

    private JadwalViewModel jadwalViewModel;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private List<Jadwal> jadwalList;
    private Context context;
    private JadwalAdapter adapter;
    private DocumentReference reference;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int MY_PERMISSION_READ_CALENDAR = 100;
    private static final int MY_PERMISSION_WRITE_CALENDAR = 101;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        jadwalViewModel =
                ViewModelProviders.of(this).get(JadwalViewModel.class);
        View root = inflater.inflate(R.layout.fragment_jadwal, container, false);
        context = root.getContext();
        jadwalList = new ArrayList<>();

        initFireStore();
        checkPermission();


        recyclerView = (RecyclerView) root.findViewById(R.id.jadwalRecycleView);
        progressBar = root.findViewById(R.id.progressbar);

        adapter = new JadwalAdapter(context, jadwalList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewJadwal();
            }
        }, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        getUserJadwal();
        return root;
    }

    public static final String INTENT_JADWAL_ID = "INTENT_JADWAL_ID";

    @Override
    public void onClickjadwal(String id) {
        Intent intent = new Intent(context, JadwalView2.class);
        intent.putExtra(INTENT_JADWAL_ID, id);


        startActivity(intent);

    }

    private void checkPermission(){
        if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.READ_CALENDAR}, MY_PERMISSION_READ_CALENDAR);


        }

        if (context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_CALENDAR}, MY_PERMISSION_WRITE_CALENDAR);


        }

    }

    private void addNewJadwal() {
        final DatePickerDialog.OnDateSetListener dateDialog;

        LayoutInflater inflater = LayoutInflater.from(context);
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View dialogView = inflater.inflate(R.layout.dialog_new_jadwal, null);
        final TextInputEditText judulKegiatan = (TextInputEditText) dialogView.findViewById(R.id.input_kegiatan);
        final TextInputEditText deskripsiKegiatan = (TextInputEditText) dialogView.findViewById(R.id.input_deskripsi_kegiatan);
        final MaterialButton hariKegiatan = (MaterialButton) dialogView.findViewById(R.id.input_date_kegiatan);
        final MaterialButton waktuKegiatanStart = (MaterialButton) dialogView.findViewById(R.id.input_time_start_kegiatan);
        final MaterialButton waktuKegiatanEnd = (MaterialButton) dialogView.findViewById(R.id.input_time_end_kegiatan);
        final TextInputEditText lokasiKegiatan = (TextInputEditText) dialogView.findViewById(R.id.input_lokasi);

        hariKegiatan.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
        waktuKegiatanStart.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        waktuKegiatanEnd.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));


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

                                String date = dayOfMonth + "-" + month + "-" + year;
                                hariKegiatan.setText(date);
                            }
                        },
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        waktuKegiatanStart.setOnClickListener(new View.OnClickListener() {
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
                                Log.d("TEST", "onTimeSet: "+hourOfDay);

                                if (minute<10 && hourOfDay <10){
                                    waktuKegiatanStart.setText("0"+hourOfDay+":"+"0"+minute);
                                }else if(minute<10 && hourOfDay>=10){
                                    waktuKegiatanStart.setText(hourOfDay+":"+"0"+minute);
                                }else if(minute>=10 && hourOfDay<10){
                                    waktuKegiatanStart.setText("0"+hourOfDay+":"+minute);
                                }else if (minute>=10 && hourOfDay >=10){
                                    waktuKegiatanStart.setText(hourOfDay+":"+minute);
                                }

                            }
                        },
                        hour, minute, true);
                dialog.setTitle("Choose Time : ");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        waktuKegiatanEnd.setOnClickListener(new View.OnClickListener() {
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

                                if (minute<10 && hourOfDay <10){
                                    waktuKegiatanEnd.setText("0"+hourOfDay+":"+"0"+minute);
                                }else if(minute<10 && hourOfDay>=10){
                                    waktuKegiatanEnd.setText(hourOfDay+":"+"0"+minute);
                                }else if(minute>=10 && hourOfDay<10){
                                    waktuKegiatanEnd.setText("0"+hourOfDay+":"+minute);
                                }else if (minute>=10 && hourOfDay >=10){
                                    waktuKegiatanEnd.setText(hourOfDay+":"+minute);
                                }

                            }
                        },
                        hour, minute, true);
                dialog.setTitle("Choose Time : ");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });


        mBuilder.setView(dialogView);
        mBuilder.setTitle("Tambah Jadwal Baru");
        mBuilder.setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setVisibility(View.VISIBLE);
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

                final Map<String, Object> jadwal = new HashMap<String, Object>();
                jadwal.put(FirebaseHelper.JUDUL_KEGIATAN, judulKegiatan.getText().toString());
                jadwal.put(FirebaseHelper.DESKRIPSI_KEGIATAN, deskripsiKegiatan.getText().toString());
                jadwal.put(FirebaseHelper.LOKASI_KEGIATAN, lokasiKegiatan.getText().toString());
                jadwal.put(FirebaseHelper.TANGGAL_KEGIATAN, hariKegiatan.getText().toString());
                jadwal.put(FirebaseHelper.WAKTU_KEGIATAN_START, waktuKegiatanStart.getText().toString());
                jadwal.put(FirebaseHelper.WAKTU_KEGIATAN_END, waktuKegiatanEnd.getText().toString());
                jadwal.put(FirebaseHelper.EVENT_ID, 0);
                jadwal.put(FirebaseHelper.DAY_OF_WEEK, 0);


                reference
                        .collection(COLLECTION_JADWAL)
                        .add(jadwal)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                progressBar.setVisibility(View.GONE);
                                getUserJadwal();
                                addToCalendar(jadwal, documentReference.getId());
                                Toast.makeText(context, "Jadwal Berhasil Ditambahkan", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    private void addToCalendar(Map<String, Object> jadwal, String id) {

        long calID = 3;
        long startMillis = 0;
        long endMillis = 0;

        String date = jadwal.get(TANGGAL_KEGIATAN).toString();
        String timeStart = jadwal.get(WAKTU_KEGIATAN_START).toString();
        String timeEnd = jadwal.get(WAKTU_KEGIATAN_END).toString();

        String[] dateParts = date.split("-");
        Integer day = Integer.parseInt(dateParts[0]);
        Integer month = Integer.parseInt(dateParts[1]);
        month = month - 1;
        Integer year = Integer.parseInt(dateParts[2]);

        String[] timeStartParts = timeStart.split(":");
        Integer hourStart = Integer.parseInt(timeStartParts[0]);
        Integer minuteStart = Integer.parseInt(timeStartParts[1]);

        String[] timeEndParts = timeEnd.split(":");
        Integer hourEnd = Integer.parseInt(timeEndParts[0]);
        Integer minuteEnd = Integer.parseInt(timeEndParts[1]);

        Calendar dayofWeek = Calendar.getInstance();
        dayofWeek.clear();
        dayofWeek.set(year, month, day);
        Integer intDayofWeek = dayofWeek.get(Calendar.DAY_OF_WEEK);
        String DayOfWeek;
        switch (intDayofWeek) {
            case 1:
                DayOfWeek = "Sun";
                break;
            case 2:
                DayOfWeek = "Mon";
                break;
            case 3:
                DayOfWeek = "Tue";
                break;
            case 4:
                DayOfWeek = "Wed";
                break;
            case 5:
                DayOfWeek = "Thu";
                break;
            case 6:
                DayOfWeek = "Fri";
                break;
            case 7:
                DayOfWeek = "Sat";
                break;
            default:
                DayOfWeek = "";
        }
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Calendar beginTime = Calendar.getInstance(utc);
        beginTime.clear();
        beginTime.set(year, month, day, hourStart, minuteStart);
        startMillis = beginTime.getTimeInMillis();


        Calendar endTime = Calendar.getInstance(utc);
        endTime.clear();
        endTime.set(year, month, day, hourEnd, minuteEnd);
        endMillis = endTime.getTimeInMillis();


        ContentResolver cr2 = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, jadwal.get(JUDUL_KEGIATAN).toString());
        values.put(CalendarContract.Events.DESCRIPTION, jadwal.get(DESKRIPSI_KEGIATAN).toString());
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY");
        values.put(CalendarContract.Events.EVENT_LOCATION, jadwal.get(LOKASI_KEGIATAN).toString());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Jakarta");
        values.put(CalendarContract.Events.HAS_ALARM, "1");
        Uri uri2 = cr2.insert(CalendarContract.Events.CONTENT_URI, values);



        // get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uri2.getLastPathSegment());
        values = new ContentValues();
        values.put( "event_id", eventID);
        values.put( "method", 1 );
        values.put( "minutes", 15 );
        cr2.insert(CalendarContract.Reminders.CONTENT_URI, values);



        final Map<String, Object> event = new HashMap<String, Object>();
        event.put(EVENT_ID, eventID);
        event.put(DAY_OF_WEEK, DayOfWeek);
        reference
                .collection(COLLECTION_JADWAL)
                .document(id)
                .update(event)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Calendar : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void getUserJadwal() {
        progressBar.setVisibility(View.VISIBLE);
        reference
                .collection(COLLECTION_JADWAL)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            if (task.getResult() != null) {
                                jadwalList.clear();
                                for (QueryDocumentSnapshot documents : task.getResult()) {
                                    jadwalList.add(
                                            new Jadwal(
                                                    documents.getData().get(FirebaseHelper.JUDUL_KEGIATAN).toString(),
                                                    documents.getData().get(FirebaseHelper.DESKRIPSI_KEGIATAN).toString(),
                                                    documents.getData().get(FirebaseHelper.TANGGAL_KEGIATAN).toString(),
                                                    documents.getData().get(FirebaseHelper.WAKTU_KEGIATAN_START).toString(),
                                                    documents.getData().get(FirebaseHelper.WAKTU_KEGIATAN_END).toString(),
                                                    documents.getData().get(FirebaseHelper.LOKASI_KEGIATAN).toString(),
                                                    documents.getData().get(DAY_OF_WEEK).toString(),
                                                    documents.getId()
                                            )
                                    );

                                    adapter.notifyDataSetChanged();

                                }
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void initFireStore() {

        if (mAuth.getUid() != null) {
            reference = db
                    .collection(COLLECTION_USERS)
                    .document(mAuth.getCurrentUser().getUid());

        }
    }

}