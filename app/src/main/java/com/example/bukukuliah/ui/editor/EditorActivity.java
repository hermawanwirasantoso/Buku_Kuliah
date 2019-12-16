package com.example.bukukuliah.ui.editor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//CREATED BY HERMAWAN WIRA SANTOSO
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bukukuliah.R;
import com.example.bukukuliah.ui.catatan.Catatan;
import com.example.bukukuliah.ui.editor.images.FileListAdapter;
import com.example.bukukuliah.ui.editor.images.SavedFile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_AUDIO;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_BUKU;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_GAMBAR;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_USERS;
import static com.example.bukukuliah.FirebaseHelper.CONTENT_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.DESC_AUDIO;
import static com.example.bukukuliah.FirebaseHelper.DESC_GAMBAR;
import static com.example.bukukuliah.FirebaseHelper.FILENAME_AUDIO;
import static com.example.bukukuliah.FirebaseHelper.FILENAME_GAMBAR;
import static com.example.bukukuliah.FirebaseHelper.HTML_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.PAGE_AUDIO;
import static com.example.bukukuliah.FirebaseHelper.PAGE_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.PAGE_GAMBAR;
import static com.example.bukukuliah.FirebaseHelper.STORAGE_SAVED_USERS;
import static com.example.bukukuliah.FirebaseHelper.TANGGAL_CATATAN;
import static com.example.bukukuliah.FirebaseHelper.TIMESTAMP_AUDIO;
import static com.example.bukukuliah.FirebaseHelper.TIMESTAMP_GAMBAR;
import static com.example.bukukuliah.FirebaseHelper.URL_AUDIO;
import static com.example.bukukuliah.FirebaseHelper.URL_GAMBAR;
import static com.example.bukukuliah.ui.buku.BukuFragment.INTENT_ID_BUKU;
import static com.example.bukukuliah.ui.buku.BukuFragment.INTENT_JUDUL_BUKU;


public class EditorActivity extends AppCompatActivity implements View.OnClickListener, FileListAdapter.FileEditor {

    private MaterialButton prevButton, nextButton, newPageButton, boldButton, italicButton,
            underlineButton, highlightButton, addImageButton, addVoiceButton, showImageButton, showAudioButton,
    clearFormatButton;
    private TextInputEditText mainEditor;
    private boolean isEditMode;
    private String key;
    private int currentPage, pictureCount;
    private List<Catatan> catatanList;
    private ProgressBar progressBar;
    private TextView pageInfoTextView, dateTextView;
    private DocumentReference reference;
    private StorageReference imageStorageRef, audioStorageRef;
    private CharacterStyle normal, bold, italic, underline, highlight;

    //AUDIO
    static final int REQUEST_AUDIO_RECORD = 3;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private String audioFilename = null;
    private MediaRecorder recorder = null;
    private RecordButton recordButton = null;
    private PlayButton playButton = null;
    private MediaPlayer player = null;
    private String htmlMainEditor = "";

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
        normal = new StyleSpan(Typeface.NORMAL);
        bold = new StyleSpan(Typeface.BOLD);
        italic = new StyleSpan(Typeface.ITALIC);
        underline = new UnderlineSpan();
        highlight = new BackgroundColorSpan(Color.YELLOW);
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
        imageStorageRef = storage.getReference().child(STORAGE_SAVED_USERS + mAuth.getUid() + "/");
        audioStorageRef = storage.getReference().child(STORAGE_SAVED_USERS + mAuth.getUid() + "/");
    }


    private void updateHalaman() {
        if (catatanList != null && catatanList.size() > 0) {
            mainEditor.setText(catatanList.get(currentPage).content);
            pageInfoTextView.setText(getString(R.string.page_info, String.valueOf((currentPage + 1))
                    , String.valueOf(catatanList.size())));
            dateTextView.setText(timeStampToStringDate((Timestamp) catatanList.get(currentPage).timestamp));
            if (catatanList.get(currentPage).HTMLtext!=null){
                htmlMainEditor = catatanList.get(currentPage).HTMLtext;
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(htmlMainEditor);
                mainEditor.setText(Html.fromHtml(htmlMainEditor));
            }
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
                                    String tempHtml = null;
                                    if (temp.get(HTML_CATATAN)!=null){
                                        tempHtml = temp.get(HTML_CATATAN).toString();
                                    }
                                    catatanList.add(new Catatan(document.getId(), temp.get(PAGE_CATATAN).toString(), temp.get(CONTENT_CATATAN).toString(), temp.get(TANGGAL_CATATAN), tempHtml));
                                }
                                if (task.getResult().size() == 0) {
                                    Catatan initialNote = new Catatan(null, String.valueOf(currentPage)
                                            , "",
                                            Timestamp.now(), null);
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
        showImageButton = findViewById(R.id.media_picture);
        showImageButton.setOnClickListener(this);
        showAudioButton = findViewById(R.id.media_records);
        showAudioButton.setOnClickListener(this);
        clearFormatButton = findViewById(R.id.menu_normal);
        clearFormatButton.setOnClickListener(this);
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
            case R.id.media_picture:
                pictureListDialog();
                break;
            case R.id.menu_add_voice:
                recordAudioDialog();
                break;
            case R.id.media_records:
                audioListDialog();
                break;
            case R.id.menu_bold:
                selectedTextToBold();
                break;
            case R.id.menu_italic:
                selectedTextToItalic();
                break;
            case R.id.menu_underline:
                selectedTextToUnderline();
                break;
            case R.id.menu_highlight:
                selectedTextToHighlight();
                break;
            case R.id.menu_normal:
                selectedTextToNormal();
                break;
        }
    }

    private void selectedTextToNormal() {
        if (mainEditor.getText() != null && mainEditor.getText().length() > 0) {
            int start = mainEditor.getSelectionStart();
            int end = mainEditor.getSelectionEnd();
            if (htmlMainEditor==null){
                htmlMainEditor = mainEditor.getText().toString();
            }
            if (start!=end){
                Object[] toRemoveSpans = mainEditor.getText().getSpans(start,
                        end,
                        BackgroundColorSpan.class);
                for (Object toRemoveSpan : toRemoveSpans) {
                    mainEditor.getText().removeSpan(toRemoveSpan);
                }
                toRemoveSpans = mainEditor.getText().getSpans(start,
                        end,
                        StyleSpan.class);
                for (Object toRemoveSpan : toRemoveSpans) {
                    mainEditor.getText().removeSpan(toRemoveSpan);
                }
                toRemoveSpans = mainEditor.getText().getSpans(start,
                        end,
                        UnderlineSpan.class);
                for (Object toRemoveSpan : toRemoveSpans) {
                    mainEditor.getText().removeSpan(toRemoveSpan);
                }
                SpannableStringBuilder sb = new SpannableStringBuilder(mainEditor.getText());
                htmlMainEditor = Html.toHtml(sb);
                mainEditor.setText(Html.fromHtml(htmlMainEditor));
                mainEditor.setSelection(end);
            }
        }
    }

    private void selectedTextToBold() {
        if (mainEditor.getText() != null && mainEditor.getText().length() > 0) {
            int start = mainEditor.getSelectionStart();
            int end = mainEditor.getSelectionEnd();
            if (htmlMainEditor==null){
                htmlMainEditor = mainEditor.getText().toString();
            }
            if (start!=end){
                SpannableStringBuilder sb = new SpannableStringBuilder(mainEditor.getText());
                sb.setSpan(bold, start, end, 0);
                htmlMainEditor = Html.toHtml(sb);
                mainEditor.setText(Html.fromHtml(htmlMainEditor));
                mainEditor.setSelection(end);
            }
        }
    }
    private void selectedTextToItalic() {
        if (mainEditor.getText() != null && mainEditor.getText().length() > 0) {
            int start = mainEditor.getSelectionStart();
            int end = mainEditor.getSelectionEnd();
            if (htmlMainEditor==null){
                htmlMainEditor = mainEditor.getText().toString();
            }
            if (start!=end){
                SpannableStringBuilder sb = new SpannableStringBuilder(mainEditor.getText());
                sb.setSpan(italic, start, end, 0);
                htmlMainEditor = Html.toHtml(sb);
                mainEditor.setText(Html.fromHtml(htmlMainEditor));
                mainEditor.setSelection(end);
            }
        }
    }
    private void selectedTextToUnderline() {
        if (mainEditor.getText() != null && mainEditor.getText().length() > 0) {
            int start = mainEditor.getSelectionStart();
            int end = mainEditor.getSelectionEnd();
            if (htmlMainEditor==null){
                htmlMainEditor = mainEditor.getText().toString();
            }
            if (start!=end){
                SpannableStringBuilder sb = new SpannableStringBuilder(mainEditor.getText());
                sb.setSpan(underline, start, end, 0);
                htmlMainEditor = Html.toHtml(sb);
                mainEditor.setText(Html.fromHtml(htmlMainEditor));
                mainEditor.setSelection(end);
            }
        }
    }

    private void selectedTextToHighlight() {
        if (mainEditor.getText() != null && mainEditor.getText().length() > 0) {
            int start = mainEditor.getSelectionStart();
            int end = mainEditor.getSelectionEnd();
            if (htmlMainEditor==null){
                htmlMainEditor = mainEditor.getText().toString();
            }
            if (start!=end){
                SpannableStringBuilder sb = new SpannableStringBuilder(mainEditor.getText());
                sb.setSpan(highlight, start, end, 0);
                htmlMainEditor = Html.toHtml(sb);
                mainEditor.setText(Html.fromHtml(htmlMainEditor));
                mainEditor.setSelection(end);
            }
        }
    }

    @Override
    public void deleteVoice(final String audioFilename, final String uid) {
        new AlertDialog.Builder(this).setTitle("Hapus Audio")
                .setMessage("Apakah Anda Yakin Ingin Menghapus Audio Ini")
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        audioStorageRef.child("audios/"
                                + key
                                + "/"
                                + audioFilename).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                makeToast("Berhasil Menghapus Audio");
                                deleteVoiceFromFireStore(uid);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeToast(e.getMessage());
                            }
                        });
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteVoiceFromFireStore(String uid){
        reference.collection(COLLECTION_BUKU).document(key)
                .collection(COLLECTION_AUDIO)
                .document(uid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast("Berhasil Hapus Dari FireStore");
                        getAudioList(null, isWholeBook);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast(e.getMessage());
                    }
                });
    }

    private void audioListDialog() {
        View picturesView = getLayoutInflater().inflate(R.layout.dialog_list_of_picture, null);
        RecyclerView pictureRecycleView = picturesView.findViewById(R.id.images_recycleview);
        final ProgressBar pictureListProgressbar = picturesView.findViewById(R.id.images_list_progressbar);
        final CheckBox wholeBookCheckBox = picturesView.findViewById(R.id.checkbox_whole_book);
        wholeBookCheckBox.setText("Muat Semua Audio Dari Buku Ini");
        savedFileList = new ArrayList<>();
        final MediaPlayer mediaPlayer = new MediaPlayer();
        fileListAdapter = new FileListAdapter(this, savedFileList, mediaPlayer, this);
        wholeBookCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isWholeBook = wholeBookCheckBox.isChecked();
                getAudioList(pictureListProgressbar, wholeBookCheckBox.isChecked());
            }
        });
        pictureRecycleView.setLayoutManager(new LinearLayoutManager(this));
        pictureRecycleView.setAdapter(fileListAdapter);
        new AlertDialog.Builder(this)
                .setTitle("Daftar Audio")
                .setView(picturesView)
                .setPositiveButton("Ok", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mediaPlayer.release();
                    }
                })
                .show();
        getAudioList(pictureListProgressbar, wholeBookCheckBox.isChecked());
    }

    private void getAudioList(final ProgressBar pictureListProgressbar, final boolean isWholeBook) {
        if (pictureListProgressbar!=null)
            pictureListProgressbar.setVisibility(View.VISIBLE);
        reference.collection(COLLECTION_BUKU).document(key)
                .collection(COLLECTION_AUDIO)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                savedFileList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> temp = document.getData();
                                    if (!isWholeBook) {
                                        if ((long) temp.get(PAGE_AUDIO) == currentPage) {
                                            savedFileList.add(new SavedFile(temp.get(URL_AUDIO).toString(),
                                                    temp.get(DESC_AUDIO).toString(),
                                                    (Timestamp) temp.get(TIMESTAMP_AUDIO),
                                                    (long) temp.get(PAGE_AUDIO),
                                                    temp.get(FILENAME_AUDIO).toString(),
                                                    document.getId()));
                                        }
                                    } else {
                                        savedFileList.add(new SavedFile(temp.get(URL_AUDIO).toString(),
                                                temp.get(DESC_AUDIO).toString(),
                                                (Timestamp) temp.get(TIMESTAMP_AUDIO),
                                                (long) temp.get(PAGE_AUDIO),
                                                temp.get(FILENAME_AUDIO).toString(),
                                                document.getId()));
                                    }
                                }
                                fileListAdapter.notifyDataSetChanged();
                                if (pictureListProgressbar!=null)
                                    pictureListProgressbar.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            makeToast("Gagal Mengambil Data Gambar");
                        }
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(audioFilename);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e("Audio Recorder", "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        recorder.setOutputFile(audioFilename);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("Audio Recorder", "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    class RecordButton extends MaterialButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends MaterialButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    private void recordAudioDialog() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        audioFilename = getExternalCacheDir().getAbsolutePath();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        audioFilename += "/REC_" + timeStamp + ".aac";
        View recordDialog = getLayoutInflater().inflate(R.layout.dialog_record_audio, null);
        LinearLayout linearLayout = recordDialog.findViewById(R.id.record_dialog_container);
        final TextInputEditText audioDescInput = recordDialog.findViewById(R.id.audio_desc_input);
        recordButton = new RecordButton(this);
        linearLayout.addView(recordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        playButton = new PlayButton(this);
        linearLayout.addView(playButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        new AlertDialog.Builder(this)
                .setTitle("Rekam Suara")
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (recorder != null) {
                            recorder.release();
                            recorder = null;
                        }

                        if (player != null) {
                            player.release();
                            player = null;
                        }
                    }
                })
                .setPositiveButton(getString(R.string.simpan), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (new File(audioFilename).exists()) {
                            if (audioDescInput.getText() != null && audioDescInput.length() > 0)
                                uploadFile(audioDescInput.getText().toString(), null, false);
                            else
                                uploadFile("", null, false);
                        } else {
                            makeToast("Rekam Audio Dulu");
                        }
                    }
                })
                .setNegativeButton(getString(R.string.batal), null)
                .setView(recordDialog)
                .show();
    }


    @Override
    public void deleteImage(final String imageFilename, final String uid) {
        new AlertDialog.Builder(this).setTitle("Hapus Gambar")
                .setMessage("Apakah Anda Yakin Ingin Menghapus Gambar Ini?")
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        imageStorageRef.child("images/"
                                + key
                                + "/"
                                + imageFilename).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                makeToast("Berhasil Menghapus Gambar");
                                deleteImageFromFireStore(uid);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeToast(e.getMessage());
                            }
                        });
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteImageFromFireStore(String uid) {
        reference.collection(COLLECTION_BUKU).document(key)
                .collection(COLLECTION_GAMBAR)
                .document(uid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeToast("Berhasil Hapus Dari FireStore");
                        getImagesList(null, isWholeBook);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeToast(e.getMessage());
                    }
                });
    }

    private FileListAdapter fileListAdapter;
    private List<SavedFile> savedFileList;
    private boolean isWholeBook;

    private void pictureListDialog() {
        View picturesView = getLayoutInflater().inflate(R.layout.dialog_list_of_picture, null);
        RecyclerView pictureRecycleView = picturesView.findViewById(R.id.images_recycleview);
        final ProgressBar pictureListProgressbar = picturesView.findViewById(R.id.images_list_progressbar);
        final CheckBox wholeBookCheckBox = picturesView.findViewById(R.id.checkbox_whole_book);
        savedFileList = new ArrayList<>();
        fileListAdapter = new FileListAdapter(this, savedFileList, null, this);
        wholeBookCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isWholeBook = wholeBookCheckBox.isChecked();
                getImagesList(pictureListProgressbar, wholeBookCheckBox.isChecked());
            }
        });
        pictureRecycleView.setLayoutManager(new LinearLayoutManager(this));
        pictureRecycleView.setAdapter(fileListAdapter);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.daftar_gambar))
                .setView(picturesView)
                .setPositiveButton("Ok", null)
                .show();
        getImagesList(pictureListProgressbar, wholeBookCheckBox.isChecked());
    }



    private void getImagesList(final ProgressBar pictureListProgressbar,
                               final boolean isWholeBook) {
        if (pictureListProgressbar!=null)
            pictureListProgressbar.setVisibility(View.VISIBLE);
        reference.collection(COLLECTION_BUKU).document(key)
                .collection(COLLECTION_GAMBAR)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                savedFileList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> temp = document.getData();
                                    if (!isWholeBook) {
                                        if ((long) temp.get(PAGE_GAMBAR) == currentPage) {
                                            savedFileList.add(new SavedFile(temp.get(URL_GAMBAR).toString(),
                                                    temp.get(DESC_GAMBAR).toString(),
                                                    (Timestamp) temp.get(TIMESTAMP_GAMBAR),
                                                    (long) temp.get(PAGE_GAMBAR),
                                                    temp.get(FILENAME_GAMBAR).toString(),
                                                    document.getId()));
                                        }
                                    } else {
                                        savedFileList.add(new SavedFile(temp.get(URL_GAMBAR).toString(),
                                                temp.get(DESC_GAMBAR).toString(),
                                                (Timestamp) temp.get(TIMESTAMP_GAMBAR),
                                                (long) temp.get(PAGE_GAMBAR),
                                                temp.get(FILENAME_GAMBAR).toString(),
                                                document.getId()));
                                    }
                                }
                                fileListAdapter.notifyDataSetChanged();
                                if (pictureListProgressbar!=null)
                                    pictureListProgressbar.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            makeToast("Gagal Mengambil Data Gambar");
                        }
                    }
                });
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
                        }
                    }
                }).show();
    }

    public static final int REQUEST_IMAGE_GALLERY = 2;

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY);
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

    private String currentPhotoPath;

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
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

//            double koefisien = (double)dialogView.getWidth()/(double)bitmap.getWidth();
//            int destHeight = (int)((double)(bitmap.getHeight()*koefisien));
//            bitmap = Bitmap.createScaledBitmap(bitmap,dialogView.getWidth(), destHeight,true);
            addImageDialog(adjustOrientation(bitmap), null);
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                try {
                    final InputStream imageStream = getContentResolver().openInputStream(uri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    currentPhotoPath = uri.getPath();
                    addImageDialog(adjustOrientation(selectedImage), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_AUDIO_RECORD && resultCode == Activity.RESULT_OK) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap adjustOrientation(Bitmap bitmap) {
        try {
            ExifInterface ei = new ExifInterface(currentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void addImageDialog(Bitmap bitmap, final Uri uri) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_new_image, null);
        ImageView choosenImage = dialogView.findViewById(R.id.choosen_imageview);
        choosenImage.setImageBitmap(bitmap);
        final TextInputEditText descInput = dialogView.findViewById(R.id.input_deskripsi_gambar);
        new AlertDialog.Builder(this).setTitle(getString(R.string.gambar_baru))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.simpan), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (descInput.getText() != null && descInput.getText().length() > 0)
                            uploadFile(descInput.getText().toString(), uri, true);
                        else
                            uploadFile("", uri, true);
                        // TODO: 11-Dec-19 simpan gambar ke firebase dan buat dokumen baru dalam subkoleksi catatan
                    }
                })
                .setNegativeButton(getString(R.string.batal), null)
                .show();
    }


    private void uploadFile(final String descInput, Uri uri, final boolean isPhoto) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_progress, null);
        final TextView progressTextView = dialogView.findViewById(R.id.progress_textview);
        final ProgressBar uploadProgressBar = dialogView.findViewById(R.id.upload_progressbar);
        final Button dismissButton = dialogView.findViewById(R.id.dismiss_button);
        uploadProgressBar.setVisibility(View.INVISIBLE);
        final Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("Uploading...")
                .setView(dialogView)
                .show();
        final Uri file;
        if (isPhoto) {
            if (uri == null) {
                file = Uri.fromFile(new File(currentPhotoPath));
            } else {
                file = uri;
            }
        } else {
            file = Uri.fromFile(new File(audioFilename));
        }
        final StorageReference imageRefs = imageStorageRef.child("images/"
                + key
                + "/"
                + file.getLastPathSegment());
        final StorageReference audioRefs = audioStorageRef.child("audios/"
                + key
                + "/"
                + file.getLastPathSegment());
        UploadTask uploadTask;
        if (isPhoto)
            uploadTask = imageRefs.putFile(file);
        else
            uploadTask = audioRefs.putFile(file);
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
                if (isPhoto) {
                    updateFireStoreGambar(descInput, imageRefs, file.getLastPathSegment());
                } else {
                    updateFireStoreAudio(descInput, audioRefs, file.getLastPathSegment());
                }

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                String progressText = String.format(Locale.US, "Upload Progress: %.2f", progress);
                progressTextView.setText(progressText + "%");
            }
        });
    }

    private void updateFireStoreAudio(final String descInput, StorageReference audioRefs, final String filename) {
        progressBar.setVisibility(View.VISIBLE);
        audioRefs.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                Map<String, Object> temp = new HashMap<>();
                temp.put(DESC_AUDIO, descInput);
                temp.put(URL_AUDIO, uri.toString());
                temp.put(TIMESTAMP_AUDIO, Timestamp.now());
                temp.put(PAGE_AUDIO, currentPage);
                temp.put(FILENAME_AUDIO, filename);
                reference.collection(COLLECTION_BUKU).document(key)
                        .collection(COLLECTION_AUDIO)
                        .add(temp)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                makeToast("Berhasil Tambah Audio");
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeToast("Gagal Menambah Audio");
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast("Gagal Mengambil Link Download");
            }
        });
    }

    private void updateFireStoreGambar(final String descInput, StorageReference imageRefs, final String lastPathSegment) {
        progressBar.setVisibility(View.VISIBLE);
        imageRefs.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                putImageDetail(descInput, uri, lastPathSegment);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast("Gagal Mengambil Link Download");
            }
        });
    }

    private void putImageDetail(String descInput, Uri uri, String lastPathSegment) {
        Map<String, Object> temp = new HashMap<>();
        temp.put(DESC_GAMBAR, descInput);
        temp.put(URL_GAMBAR, uri.toString());
        temp.put(TIMESTAMP_GAMBAR, Timestamp.now());
        temp.put(PAGE_GAMBAR, currentPage);
        temp.put(FILENAME_GAMBAR, lastPathSegment);
        reference.collection(COLLECTION_BUKU).document(key)
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
            SpannableStringBuilder span = new SpannableStringBuilder(mainEditor.getText());
            catatanList.get(currentPage).HTMLtext = Html.toHtml(span);
        }
        catatanList.add(new Catatan(null, String.valueOf((currentPage + 1)), "", Timestamp.now(), null));
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
            case R.id.editor_delete:
                hapusCatatanDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hapusCatatanDialog() {
        new AlertDialog.Builder(this).setTitle("Hapus Catatan")
                .setMessage("Apakah Anda Yakin Ingin Menghapus Catatan Ini?")
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hapusCatatanFronFireStore();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();

    }

    private void hapusCatatanFronFireStore() {
        if (catatanList.get(currentPage).uid!=null) {
            progressBar.setVisibility(View.VISIBLE);
            reference.collection(COLLECTION_BUKU).document(key)
                    .collection(COLLECTION_CATATAN)
                    .document(catatanList.get(currentPage).uid)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.GONE);
                            makeToast("Berhasil Hapus Catatan");
                            updateNomorHalaman();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            makeToast(e.getMessage());
                        }
                    });
        }else {
            catatanList.remove(currentPage);
            currentPage--;
            for (int i=0;i<catatanList.size();i++){
                catatanList.get(i).page = String.valueOf(i);
            }
            updateHalaman();
        }
    }

    private void updateNomorHalaman() {
        catatanList.remove(currentPage);
        currentPage--;
        for (int i=0;i<catatanList.size();i++){
            catatanList.get(i).page = String.valueOf(i);
        }
        updateHalaman();
        getBookContent();
        saveCatatan();
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
            SpannableStringBuilder span = new SpannableStringBuilder(mainEditor.getText());
            catatanList.get(currentPage).HTMLtext = Html.toHtml(span);
        } else {
            catatanList.add(new Catatan(null, String.valueOf(currentPage),
                    mainEditor.getText().toString(), Timestamp.now(), htmlMainEditor));
        }
        for (Catatan catatan : catatanList) {
            Map<String, Object> temp = new HashMap<>();
            temp.put(CONTENT_CATATAN, catatan.content);
            temp.put(PAGE_CATATAN, catatan.page);
            temp.put(TANGGAL_CATATAN, catatan.timestamp);
            temp.put(HTML_CATATAN, catatan.HTMLtext);
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
