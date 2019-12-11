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
import android.widget.TextView;
import android.widget.Toast;

import com.example.bukukuliah.FirebaseHelper;
import com.example.bukukuliah.R;
import com.example.bukukuliah.ui.catatan.Catatan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.bukukuliah.FirebaseHelper.COLLECTION_BUKU;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_USERS;
import static com.example.bukukuliah.FirebaseHelper.CONTENT_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.PAGE_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.TANGGAL_CATATAN;
import static com.example.bukukuliah.ui.buku.BukuFragment.INTENT_ID_BUKU;
import static com.example.bukukuliah.ui.buku.BukuFragment.INTENT_JUDUL_BUKU;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialButton prevButton, nextButton, newPageButton, boldButton, italicButton,
            underlineButton, highlightButton, addImageButton, addVoiceButton;
    private TextInputEditText mainEditor;
    private boolean isEditMode;
    private String key;
    private int currentPage;
    private List<Catatan> catatanList;
    private ProgressBar progressBar;
    private TextView pageInfoTextView, dateTextView;
    private DocumentReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        initView();
        initFireStore();
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

    private void initFireStore() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (mAuth.getUid()!=null){
            reference = db.collection(COLLECTION_USERS).document(mAuth.getUid());
        }
    }


    private void updateHalaman() {
        if (catatanList!=null && catatanList.size()>0) {
            mainEditor.setText(catatanList.get(currentPage).content);
            pageInfoTextView.setText(getString(R.string.page_info, String.valueOf((currentPage+1))
                    , String.valueOf(catatanList.size())));
            dateTextView.setText(timeStampToStringDate((Timestamp) catatanList.get(currentPage).timestamp));
        }
    }

    private String timeStampToStringDate(Timestamp timestamp){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY, hh:mm:ss");
        return simpleDateFormat.format(timestamp.toDate());
    }

    private void getBookContent() {
        reference.collection(COLLECTION_BUKU).document(key).collection(key)
                .orderBy(PAGE_CATATAN)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult()!=null) {
                                catatanList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> temp = document.getData();
                                    catatanList.add(new Catatan(document.getId(), temp.get(PAGE_CATATAN).toString(),temp.get(CONTENT_CATATAN).toString(),temp.get(TANGGAL_CATATAN)));
                                }
                                if (task.getResult().size()==0){
                                    Catatan initialNote = new Catatan(null, String.valueOf(currentPage)
                                            , "",
                                            Timestamp.now());
                                    catatanList.add(initialNote);
                                }
                                updateHalaman();
                                progressBar.setVisibility(View.GONE);
                            }
                        } else {
                            Log.w("EditorActivity", "Error getting documents.", task.getException());
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void initView() {
        progressBar = findViewById(R.id.editor_progressbar);
        mainEditor = findViewById(R.id.mainEditorEditText);
        prevButton = findViewById(R.id.menu_previous_page);
        prevButton.setOnClickListener(this);
        nextButton = findViewById(R.id.menu_next_page);
        nextButton.setOnClickListener(this);
        newPageButton = findViewById(R.id.menu_new_page);
        newPageButton.setOnClickListener(this);
        boldButton = findViewById(R.id.menu_bold);
        boldButton.setOnClickListener(this);
        italicButton = findViewById(R.id.menu_italic);
        italicButton.setOnClickListener(this);
        underlineButton = findViewById(R.id.menu_underline);
        underlineButton.setOnClickListener(this);
        highlightButton = findViewById(R.id.menu_highlight);
        highlightButton.setOnClickListener(this);
        addImageButton = findViewById(R.id.menu_add_photo);
        addImageButton.setOnClickListener(this);
        addVoiceButton = findViewById(R.id.menu_add_voice);
        addVoiceButton.setOnClickListener(this);
        pageInfoTextView = findViewById(R.id.editor_page_info);
        dateTextView = findViewById(R.id.editor_last_date);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.menu_new_page:
                addNewPage();
                makeToast("Halaman Baru");
                break;
            case R.id.menu_next_page:
                nextPage();
                break;
            case R.id.menu_previous_page:
                previousPage();
                break;
        }
    }

    private void previousPage() {
        if (currentPage>0) {
            catatanList.get(currentPage).content = mainEditor.getText().toString();
            currentPage--;
            updateHalaman();
            makeToast("Previous Page");
        }
    }

    private void nextPage() {
        if (currentPage<catatanList.size()-1) {
            catatanList.get(currentPage).content = mainEditor.getText().toString();
            currentPage++;
            updateHalaman();
            makeToast("Next Page");
        }
    }

    private void addNewPage() {
        if (catatanList.size()>0) {
            catatanList.get(currentPage).content = mainEditor.getText().toString();
            catatanList.get(currentPage).timestamp = Timestamp.now();
        }
        catatanList.add(new Catatan(null, String.valueOf((currentPage+1)), "", Timestamp.now()));
        currentPage = catatanList.size()-1;
        updateHalaman();
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
                break;
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
            catatanList.get(currentPage).content = mainEditor.getText().toString();
            catatanList.get(currentPage).timestamp = Timestamp.now();
        }else {
            catatanList.add(new Catatan(null, String.valueOf(currentPage),
                    mainEditor.getText().toString(), Timestamp.now()));
        }
        for (Catatan catatan: catatanList){
            Map<String, Object> temp = new HashMap<>();
            temp.put(CONTENT_CATATAN, catatan.content);
            temp.put(PAGE_CATATAN, catatan.page);
            temp.put(TANGGAL_CATATAN, catatan.timestamp);
            if (catatan.uid!=null){
                reference.collection(COLLECTION_BUKU)
                        .document(key)
                        .collection(key)
                        .document(catatan.uid)
                        .set(temp)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                getBookContent();

                            }
                        });
            }else {
                reference.collection(COLLECTION_BUKU)
                        .document(key)
                        .collection(key)
                        .add(temp)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                getBookContent();
                            }
                        });
            }
        }
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
