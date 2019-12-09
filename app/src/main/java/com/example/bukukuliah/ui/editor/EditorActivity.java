package com.example.bukukuliah.ui.editor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bukukuliah.R;
import com.example.bukukuliah.ui.catatan.Catatan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.bukukuliah.FirebaseHelper.COLLECTION_BUKU;
import static com.example.bukukuliah.FirebaseHelper.CONTENT_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.PAGE_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.TANGGAL_CATATAN;
import static com.example.bukukuliah.ui.buku.BukuFragment.INTENT_ID_BUKU;
import static com.example.bukukuliah.ui.buku.BukuFragment.INTENT_JUDUL_BUKU;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialButton prevButton, nextButton, newPageButton, boldButton, italicButton,
            underlineButton, highlightButton, addImageButton, addVoiceButton;
    private TextInputEditText mainEditor;
    private boolean isEditMode, isSaved;
    private String key;
    private int currentPage;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Catatan> catatanList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        initView();
        catatanList = new ArrayList<>();
        isEditMode = false;
        currentPage = 0;
        hideEditTools();
        Intent intent = getIntent();
        String judul = intent.getStringExtra(INTENT_JUDUL_BUKU);
        key = intent.getStringExtra(INTENT_ID_BUKU);
        getBookContent();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(judul);
        }
    }

    private void updateHalaman() {
        if (catatanList!=null && catatanList.size()>0) {
            mainEditor.setText(catatanList.get(currentPage).content);
        }
    }

    private void getBookContent() {
        db.collection(COLLECTION_BUKU).document(key).collection(key)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult()!=null) {
                                catatanList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> temp = document.getData();
                                    catatanList.add(new Catatan(temp.get(PAGE_CATATAN).toString(),temp.get(CONTENT_CATATAN).toString(),temp.get(TANGGAL_CATATAN)));
                                }
                                updateHalaman();
                                progressBar.setVisibility(View.GONE);
                            }
                        } else {
                            Log.w("EditorActivity", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void initView() {
        progressBar = findViewById(R.id.editor_progressbar);
        mainEditor = findViewById(R.id.mainEditorEditText);
        prevButton = findViewById(R.id.menu_previous_page);
        nextButton = findViewById(R.id.menu_next_page);
        newPageButton = findViewById(R.id.menu_new_page);
        boldButton = findViewById(R.id.menu_bold);
        italicButton = findViewById(R.id.menu_italic);
        underlineButton = findViewById(R.id.menu_underline);
        highlightButton = findViewById(R.id.menu_highlight);
        addImageButton = findViewById(R.id.menu_add_photo);
        addVoiceButton = findViewById(R.id.menu_add_voice);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.menu_new_page:
                addNewPage();
                break;
        }
    }

    private void addNewPage() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.editor_edit:
                if (isEditMode){
                    isEditMode = false;
                    hideEditTools();
                    makeToast("Read Only Mode");
                }else {
                    isEditMode = true;
                    showEditTools();
                    makeToast("Edit Mode");
                }
                break;
            case R.id.editor_save:
                saveCatatan();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveCatatan();
    }

    private void saveCatatan() {
        if (catatanList.size()>0) {
            catatanList.set(currentPage, new Catatan(String.valueOf(currentPage),
                    mainEditor.getText().toString(), FieldValue.serverTimestamp()));
        }else {
            catatanList.add(new Catatan(String.valueOf(currentPage),
                    mainEditor.getText().toString(), FieldValue.serverTimestamp()));
        }
        db.collection(COLLECTION_BUKU).document(key).collection(key)
                .orderBy(PAGE_CATATAN, Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult()!=null) {
                                if (task.getResult().size()>0) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Catatan temp = catatanList.get(Integer.parseInt(String.valueOf(document.getData().get(PAGE_CATATAN))));
                                        Map<String, Object> catatan = new HashMap<>();
                                        catatan.put(CONTENT_CATATAN, temp.content);
                                        catatan.put(PAGE_CATATAN, temp.page);
                                        catatan.put(TANGGAL_CATATAN, temp.timestamp);
                                        db.collection(COLLECTION_BUKU)
                                                .document(key)
                                                .collection(key)
                                                .document(document.getId())
                                                .set(catatan)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        makeToast("Berhasil Overwrite");
                                                    }
                                                });
                                    }
                                }else {
                                    for (int i=0;i<catatanList.size();i++){
                                        Catatan temp = catatanList.get(i);
                                        Map<String, Object> catatan = new HashMap<>();
                                        catatan.put(CONTENT_CATATAN, temp.content);
                                        catatan.put(PAGE_CATATAN, temp.page);
                                        catatan.put(TANGGAL_CATATAN, temp.timestamp);

                                        db.collection(COLLECTION_BUKU)
                                                .document(key)
                                                .collection(key)
                                                .add(catatan)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        makeToast("Catatan Baru Berhasil Ditambah");
                                                    }
                                                });
                                    }
                                }
                            }
                        } else {
                            Log.w("EditorActivity", "Error getting documents.", task.getException());
                        }
                    }
                });


    }

    private void showEditTools() {
        newPageButton.setVisibility(View.VISIBLE);
        boldButton.setVisibility(View.VISIBLE);
        italicButton.setVisibility(View.VISIBLE);
        underlineButton.setVisibility(View.VISIBLE);
        highlightButton.setVisibility(View.VISIBLE);
        addImageButton.setVisibility(View.VISIBLE);
        addVoiceButton.setVisibility(View.VISIBLE);
        mainEditor.setEnabled(true);
    }
    private void hideEditTools() {
        newPageButton.setVisibility(View.GONE);
        boldButton.setVisibility(View.GONE);
        italicButton.setVisibility(View.GONE);
        underlineButton.setVisibility(View.GONE);
        highlightButton.setVisibility(View.GONE);
        addImageButton.setVisibility(View.GONE);
        addVoiceButton.setVisibility(View.GONE);
        mainEditor.setEnabled(false);
    }

    private void makeToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
