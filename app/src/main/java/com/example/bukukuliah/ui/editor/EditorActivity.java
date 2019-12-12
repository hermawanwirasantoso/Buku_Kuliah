package com.example.bukukuliah.ui.editor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.bukukuliah.FirebaseHelper.COLLECTION_BUKU;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_GAMBAR;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_USERS;
import static com.example.bukukuliah.FirebaseHelper.CONTENT_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.DESC_GAMBAR;
import static com.example.bukukuliah.FirebaseHelper.PAGE_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.STORAGE_SAVED_IMAGE;
import static com.example.bukukuliah.FirebaseHelper.TANGGAL_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.URL_GAMBAR;
import static com.example.bukukuliah.ui.buku.BukuFragment.INTENT_ID_BUKU;
import static com.example.bukukuliah.ui.buku.BukuFragment.INTENT_JUDUL_BUKU;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialButton prevButton, nextButton, newPageButton, boldButton, italicButton,
            underlineButton, highlightButton, addImageButton, addVoiceButton;
    private TextInputEditText mainEditor;
    private boolean isEditMode;
    private String key;
    private int currentPage, pictureCount;
    private List<Catatan> catatanList;
    private ProgressBar progressBar;
    private TextView pageInfoTextView, dateTextView;
    private DocumentReference reference;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        initView();
        initFireStore();
        catatanList = new ArrayList<>();
        isEditMode = false;
        currentPage = 0;
        pictureCount = 0;
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
        if (mAuth.getUid() != null) {
            reference = db.collection(COLLECTION_USERS).document(mAuth.getUid());
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child(STORAGE_SAVED_IMAGE + mAuth.getUid() + "/");
    }


    private void updateHalaman() {
        if (catatanList != null && catatanList.size() > 0) {
            mainEditor.setText(catatanList.get(currentPage).content);
            pageInfoTextView.setText(getString(R.string.page_info, String.valueOf((currentPage + 1))
                    , String.valueOf(catatanList.size())));
            dateTextView.setText(timeStampToStringDate((Timestamp) catatanList.get(currentPage).timestamp));
        }
    }

    private String timeStampToStringDate(Timestamp timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY, hh:mm:ss");
        return simpleDateFormat.format(timestamp.toDate());
    }

    private void getBookContent() {
        reference.collection(COLLECTION_BUKU).document(key).collection(COLLECTION_CATATAN)
                .orderBy(PAGE_CATATAN)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                catatanList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> temp = document.getData();
                                    catatanList.add(new Catatan(document.getId(), temp.get(PAGE_CATATAN).toString(), temp.get(CONTENT_CATATAN).toString(), temp.get(TANGGAL_CATATAN)));
                                }
                                if (task.getResult().size() == 0) {
                                    Catatan initialNote = new Catatan(null, String.valueOf(currentPage)
                                            , "",
                                            Timestamp.now());
                                    catatanList.add(initialNote);
                                }
                                updateHalaman();
                                progressBar.setVisibility(View.GONE);
                            } else {
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
        switch (view.getId()) {
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
            case R.id.menu_add_photo:
                photoDialog();
                break;
        }
    }

    private void photoDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.photo_source))
                .setItems(getResources().getStringArray(R.array.dialog_add_photo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                openCamera();
                                break;
                            case 1:
                                openGallery();
                                break;
                            case 2:
                                openFirebaseStorage();
                                break;
                        }
                    }
                }).show();
    }

    private void openFirebaseStorage() {

    }

    private void openGallery() {
    }

    static final int REQUEST_IMAGE_CAMERA = 1;
    static final String AUTHORITY = "com.example.bukukuliah.fileprovider";

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                makeToast("Error: " + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        AUTHORITY,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA);
            }
        }

    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_new_image, null);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
//            double koefisien = (double)dialogView.getWidth()/(double)bitmap.getWidth();
//            int destHeight = (int)((double)(bitmap.getHeight()*koefisien));
//            bitmap = Bitmap.createScaledBitmap(bitmap,dialogView.getWidth(), destHeight,true);
            ImageView choosenImage = dialogView.findViewById(R.id.choosen_imageview);
            choosenImage.setImageBitmap(bitmap);
            final TextInputEditText descInput = dialogView.findViewById(R.id.input_deskripsi_gambar);
            new AlertDialog.Builder(this).setTitle(getString(R.string.gambar_baru))
                    .setView(dialogView)
                    .setPositiveButton(getString(R.string.simpan), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (descInput.getText()!=null&&descInput.getText().length()>0)
                                uploadPicture(descInput.getText().toString());
                            else
                                uploadPicture("");
                            // TODO: 11-Dec-19 simpan gambar ke firebase dan buat dokumen baru dalam subkoleksi catatan
                        }
                    })
                    .setNegativeButton(getString(R.string.batal), null)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void uploadPicture(final String descInput) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_progress, null);
        final TextView progressTextView = dialogView.findViewById(R.id.progress_textview);
        final ProgressBar uploadProgressBar = dialogView.findViewById(R.id.upload_progressbar);
        final Button dismissButton = dialogView.findViewById(R.id.dismiss_button);
        uploadProgressBar.setVisibility(View.INVISIBLE);
        final Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("Uploading...")
                .setView(dialogView)
                .show();
        Uri file = Uri.fromFile(new File(currentPhotoPath));
        final StorageReference imageRefs = storageRef.child("images/"
                + key
                + "/"
                + file.getLastPathSegment());
        UploadTask uploadTask = imageRefs.putFile(file);
        uploadProgressBar.setVisibility(View.VISIBLE);
        dismissButton.setVisibility(View.GONE);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                dialog.setTitle("Upload Gagal");

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadProgressBar.setVisibility(View.GONE);
                dialog.setTitle("Upload Berhasil");
                dismissButton.setVisibility(View.VISIBLE);
                updateFireStoreGambar(descInput, imageRefs);

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                String progressText = String.format(Locale.US, "Upload Progress: %.2f", progress);
                progressTextView.setText(progressText+"%");
            }
        });
    }

    private void updateFireStoreGambar(final String descInput, StorageReference imageRefs) {
        progressBar.setVisibility(View.VISIBLE);
        imageRefs.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                if (catatanList.get(currentPage).uid==null){
                    Map<String, Object> temp = new HashMap<>();
                    temp.put(CONTENT_CATATAN, mainEditor.getText().toString());
                    temp.put(PAGE_CATATAN, currentPage);
                    temp.put(TANGGAL_CATATAN, Timestamp.now());
                    reference.collection(COLLECTION_BUKU).document(key)
                            .collection(COLLECTION_CATATAN)
                            .add(temp)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    makeToast("Catatan Disimpan");
                                    reference.collection(COLLECTION_BUKU).document(key).collection(COLLECTION_CATATAN)
                                            .orderBy(PAGE_CATATAN)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.getResult() != null) {
                                                            catatanList.clear();
                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                Map<String, Object> temp = document.getData();
                                                                catatanList.add(new Catatan(document.getId(), temp.get(PAGE_CATATAN).toString(), temp.get(CONTENT_CATATAN).toString(), temp.get(TANGGAL_CATATAN)));
                                                            }
                                                            if (task.getResult().size() == 0) {
                                                                Catatan initialNote = new Catatan(null, String.valueOf(currentPage)
                                                                        , "",
                                                                        Timestamp.now());
                                                                catatanList.add(initialNote);
                                                            }
                                                            updateHalaman();
                                                            putImageDetail(descInput, uri);
                                                            progressBar.setVisibility(View.GONE);
                                                        } else {
                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    } else {
                                                        Log.w("EditorActivity", "Error getting documents.", task.getException());
                                                    }
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    makeToast("Gagal Simpan Catatan");
                                }
                            });
                }else {
                    putImageDetail(descInput, uri);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast("Gagal Mengambil Link Download");
            }
        });
    }

    private void putImageDetail(String descInput, Uri uri){
        Map<String, Object> temp = new HashMap<>();
        temp.put(DESC_GAMBAR, descInput);
        temp.put(URL_GAMBAR, uri.toString());
        reference.collection(COLLECTION_BUKU).document(key)
                .collection(COLLECTION_CATATAN)
                .document(catatanList.get(currentPage).uid)
                .collection(COLLECTION_GAMBAR)
                .add(temp)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        makeToast("Berhasil Tambah Gambar");
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast("Gagal Menambah Gambar");
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void previousPage() {
        if (currentPage > 0) {
            catatanList.get(currentPage).content = mainEditor.getText().toString();
            currentPage--;
            updateHalaman();
            makeToast("Previous Page");
        }
    }

    private void nextPage() {
        if (currentPage < catatanList.size() - 1) {
            catatanList.get(currentPage).content = mainEditor.getText().toString();
            currentPage++;
            updateHalaman();
            makeToast("Next Page");
        }
    }

    private void addNewPage() {
        if (catatanList.size() > 0) {
            catatanList.get(currentPage).content = mainEditor.getText().toString();
            catatanList.get(currentPage).timestamp = Timestamp.now();
        }
        catatanList.add(new Catatan(null, String.valueOf((currentPage + 1)), "", Timestamp.now()));
        currentPage = catatanList.size() - 1;
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
                if (isEditMode) {
                    isEditMode = false;
                    hideEditTools();
                    makeToast("Read Only Mode");
                } else {
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
        if (catatanList.size() > 0) {
            catatanList.get(currentPage).content = mainEditor.getText().toString();
            catatanList.get(currentPage).timestamp = Timestamp.now();
        } else {
            catatanList.add(new Catatan(null, String.valueOf(currentPage),
                    mainEditor.getText().toString(), Timestamp.now()));
        }
        for (Catatan catatan : catatanList) {
            Map<String, Object> temp = new HashMap<>();
            temp.put(CONTENT_CATATAN, catatan.content);
            temp.put(PAGE_CATATAN, catatan.page);
            temp.put(TANGGAL_CATATAN, catatan.timestamp);
            if (catatan.uid != null) {
                reference.collection(COLLECTION_BUKU)
                        .document(key)
                        .collection(COLLECTION_CATATAN)
                        .document(catatan.uid)
                        .set(temp)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                getBookContent();

                            }
                        });
            } else {
                reference.collection(COLLECTION_BUKU)
                        .document(key)
                        .collection(COLLECTION_CATATAN)
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

    private void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
