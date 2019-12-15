package com.example.bukukuliah.ui.jadwal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.bukukuliah.FirebaseHelper;
import com.example.bukukuliah.MainActivity;
import com.example.bukukuliah.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.example.bukukuliah.FirebaseHelper.COLLECTION_JADWAL;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_USERS;
import static com.example.bukukuliah.FirebaseHelper.DESKRIPSI_KEGIATAN;
import static com.example.bukukuliah.FirebaseHelper.JUDUL_KEGIATAN;
import static com.example.bukukuliah.FirebaseHelper.LOKASI_KEGIATAN;
import static com.example.bukukuliah.FirebaseHelper.TANGGAL_KEGIATAN;
import static com.example.bukukuliah.FirebaseHelper.WAKTU_KEGIATAN_END;
import static com.example.bukukuliah.FirebaseHelper.WAKTU_KEGIATAN_START;

public class JadwalView2 extends AppCompatActivity {

    String id;
    Long eventID;
    Intent intent;
    TextInputEditText txtJadwal, txtDeskripsi, txtLokasi;
    ProgressBar progressBar;
    MaterialButton btnEdit, btnDelete,  txtWaktuStart, txtWaktuEnd, txtHari;
    private static final int MY_PERMISSION_READ_CALENDAR = 100;
    private static final int MY_PERMISSION_WRITE_CALENDAR = 101;
    private DocumentReference reference;


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jadwal_view2);

        btnEdit =  findViewById(R.id.edit_jadwal);
        btnDelete =  findViewById(R.id.delete_jadwal);
        txtJadwal =  findViewById(R.id.input_kegiatan);
        txtDeskripsi =  findViewById(R.id.input_deskripsi_kegiatan);
        txtLokasi =  findViewById(R.id.input_lokasi);
        txtWaktuStart =  findViewById(R.id.input_time_start_kegiatan);
        txtWaktuEnd =  findViewById(R.id.input_time_end_kegiatan);
        txtHari =  findViewById(R.id.input_date_kegiatan);
        progressBar = findViewById(R.id.progressbar);

        initFireStore();
        checkPermission();
        getUserInformation();


        txtHari.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String[] dateParts = txtHari.getText().toString().split("-");
                 int day = Integer.parseInt(dateParts[0]);
                 int month = Integer.parseInt(dateParts[1])-1;
                 int year = Integer.parseInt(dateParts[2]);

                DatePickerDialog dialog = new DatePickerDialog(
                        JadwalView2.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month = month+1;
                                String date = dayOfMonth + "-" + month + "-" + year;
                                txtHari.setText(date);
                            }
                        },
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        txtWaktuStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] timeStartParts = txtWaktuStart.getText().toString().split(":");
                int hourStart = Integer.parseInt(timeStartParts[0]);
                final int minuteStart = Integer.parseInt(timeStartParts[1]);

                TimePickerDialog dialog = new TimePickerDialog(
                        JadwalView2.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                if (minute<10 && hourOfDay <10){
                                    txtWaktuStart.setText("0"+hourOfDay+":"+"0"+minute);
                                }else if(minute<10 && hourOfDay>=10){
                                    txtWaktuStart.setText(hourOfDay+":"+"0"+minute);
                                }else if(minute>=10 && hourOfDay<10){
                                    txtWaktuStart.setText("0"+hourOfDay+":"+minute);
                                }else if (minute>=10 && hourOfDay >=10){
                                    txtWaktuStart.setText(hourOfDay+":"+minute);
                                }

                            }
                        },
                        hourStart, minuteStart, true);
                dialog.setTitle("Choose Time : ");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        txtWaktuEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] timeEndParts = txtWaktuEnd.getText().toString().split(":");
                int hourEnd = Integer.parseInt(timeEndParts[0]);
                int minuteEnd = Integer.parseInt(timeEndParts[1]);

                TimePickerDialog dialog = new TimePickerDialog(
                        JadwalView2.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                if (minute<10 && hourOfDay <10){
                                    txtWaktuEnd.setText("0"+hourOfDay+":"+"0"+minute);
                                }else if(minute<10 && hourOfDay>=10){
                                    txtWaktuEnd.setText(hourOfDay+":"+"0"+minute);
                                }else if(minute>=10 && hourOfDay<10){
                                    txtWaktuEnd.setText("0"+hourOfDay+":"+minute);
                                }else if (minute>=10 && hourOfDay >=10){
                                    txtWaktuEnd.setText(hourOfDay+":"+minute);
                                }
                            }
                        },
                        hourEnd, minuteEnd, true);
                dialog.setTitle("Choose Time : ");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                reference = db
                        .collection(COLLECTION_USERS)
                        .document(mAuth.getCurrentUser().getUid())
                        .collection(COLLECTION_JADWAL)
                        .document(id);


                String date = txtHari.getText().toString();
                String[] dateParts = date.split("-");
                Integer day = Integer.parseInt(dateParts[0]);
                Integer month = Integer.parseInt(dateParts[1]);
                month = month-1;
                Integer year = Integer.parseInt(dateParts[2]);

                Calendar dayofWeek = Calendar.getInstance();
                dayofWeek.set(year,month,day);
                Integer intDayofWeek = dayofWeek.get(Calendar.DAY_OF_WEEK);
                String DayOfWeek;
                switch (intDayofWeek){
                    case 1:
                        DayOfWeek="Sun";
                        break;
                    case 2:
                        DayOfWeek="Mon";
                        break;
                    case 3:
                        DayOfWeek="Tue";
                        break;
                    case 4:
                        DayOfWeek="Wed";
                        break;
                    case 5:
                        DayOfWeek="Thu";
                        break;
                    case 6:
                        DayOfWeek="Fri";
                        break;
                    case 7:
                        DayOfWeek="Sat";
                        break;
                    default:
                        DayOfWeek="";
                }

                final Map<String, Object> jadwal = new HashMap<String, Object>();
                jadwal.put(FirebaseHelper.JUDUL_KEGIATAN, txtJadwal.getText().toString());
                jadwal.put(FirebaseHelper.DESKRIPSI_KEGIATAN, txtDeskripsi.getText().toString());
                jadwal.put(FirebaseHelper.LOKASI_KEGIATAN, txtLokasi.getText().toString());
                jadwal.put(FirebaseHelper.TANGGAL_KEGIATAN, txtHari.getText().toString());
                jadwal.put(FirebaseHelper.WAKTU_KEGIATAN_START, txtWaktuStart.getText().toString());
                jadwal.put(FirebaseHelper.WAKTU_KEGIATAN_END, txtWaktuEnd.getText().toString());
                jadwal.put(FirebaseHelper.DAY_OF_WEEK,DayOfWeek);


                reference.update(jadwal)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(JadwalView2.this, "Jadwal Berhasil diubah", Toast.LENGTH_SHORT).show();
                                updateCalendar(jadwal);
                                Intent intent = new Intent(JadwalView2.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(JadwalView2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(JadwalView2.this)
                        .setTitle("Hapus Jadwal?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressBar.setVisibility(View.VISIBLE);
                                reference = db
                                        .collection(COLLECTION_USERS)
                                        .document(mAuth.getCurrentUser().getUid())
                                        .collection(COLLECTION_JADWAL)
                                        .document(id);
                                reference.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressBar.setVisibility(View.GONE);
                                                deleteCalendar(eventID);
                                                Toast.makeText(JadwalView2.this, "Jadwal Telah dihapus", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(JadwalView2.this, MainActivity.class);
                                                finish();
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(JadwalView2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).create().show();


            }
        });

    }

    private void checkPermission(){
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

            ActivityCompat.requestPermissions( JadwalView2.this,
                    new String[]{Manifest.permission.READ_CALENDAR}, MY_PERMISSION_READ_CALENDAR);


        }

        if (checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

            ActivityCompat.requestPermissions( JadwalView2.this,
                    new String[]{Manifest.permission.WRITE_CALENDAR}, MY_PERMISSION_WRITE_CALENDAR);


        }

    }

    private void deleteCalendar(Long eventID) {
        ContentResolver cr = getContentResolver();
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
        int rows = cr.delete(deleteUri, null, null);
    }


    private void updateCalendar(Map<String, Object> jadwal) {

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

        TimeZone utc = TimeZone.getTimeZone("UTC");
        Calendar beginTime = Calendar.getInstance(utc);
        beginTime.clear();
        beginTime.set(year, month, day, hourStart, minuteStart);
        startMillis = beginTime.getTimeInMillis();


        Calendar endTime = Calendar.getInstance(utc);
        endTime.clear();
        endTime.set(year, month, day, hourEnd, minuteEnd);
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = getContentResolver();
        ContentValues new_values = new ContentValues();

        deleteCalendar(eventID);
        // The new title for the event
        new_values.put(CalendarContract.Events.DTSTART, startMillis);
        new_values.put(CalendarContract.Events.DTEND, endMillis);
        new_values.put(CalendarContract.Events.TITLE, jadwal.get(JUDUL_KEGIATAN).toString());
        new_values.put(CalendarContract.Events.DESCRIPTION, jadwal.get(DESKRIPSI_KEGIATAN).toString());
        new_values.put(CalendarContract.Events.EVENT_LOCATION, jadwal.get(LOKASI_KEGIATAN).toString());
        new_values.put(CalendarContract.Events.CALENDAR_ID, calID);
        new_values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Jakarta");
        new_values.put(CalendarContract.Events.HAS_ALARM, "1");
        new_values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY");
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, new_values);

        // get the event ID that is the last element in the Uri
        long new_eventID = Long.parseLong(uri.getLastPathSegment());
        new_values = new ContentValues();
        new_values.put( "event_id", new_eventID);
        new_values.put( "method", 1 );
        new_values.put( "minutes", 15 );
        cr.insert(CalendarContract.Reminders.CONTENT_URI, new_values);

        final Map<String, Object> newEventID = new HashMap<String, Object>();
        newEventID.put(FirebaseHelper.EVENT_ID, new_eventID);

        reference.update(newEventID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(JadwalView2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }


    private void getUserInformation() {
        if (reference != null) {
            progressBar.setVisibility(View.VISIBLE);
            reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    progressBar.setVisibility(View.GONE);
                    txtJadwal.setText(task.getResult().getData().get(FirebaseHelper.JUDUL_KEGIATAN).toString());
                    txtDeskripsi.setText(task.getResult().getData().get(FirebaseHelper.DESKRIPSI_KEGIATAN).toString());
                    txtLokasi.setText(task.getResult().getData().get(FirebaseHelper.LOKASI_KEGIATAN).toString());
                    txtWaktuStart.setText(task.getResult().getData().get(FirebaseHelper.WAKTU_KEGIATAN_START).toString());
                    txtWaktuEnd.setText(task.getResult().getData().get(FirebaseHelper.WAKTU_KEGIATAN_END).toString());
                    txtHari.setText(task.getResult().getData().get(FirebaseHelper.TANGGAL_KEGIATAN).toString());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(JadwalView2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    private void initFireStore() {
        intent = getIntent();
        id = intent.getStringExtra("INTENT_JADWAL_ID");
        if (mAuth.getUid() != null) {
            reference = db
                    .collection(COLLECTION_USERS)
                    .document(mAuth.getCurrentUser().getUid())
                    .collection(COLLECTION_JADWAL)
                    .document(id);

        }
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                eventID = Long.parseLong(task.getResult().get(FirebaseHelper.EVENT_ID).toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(JadwalView2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
