package com.example.bukukuliah.ui.buku;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bukukuliah.R;
import com.example.bukukuliah.ui.editor.EditorActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_BUKU;
import static com.example.bukukuliah.FirebaseHelper.COLLECTION_USERS;
import static com.example.bukukuliah.FirebaseHelper.DESKRIPSI_BUKU;
import static com.example.bukukuliah.FirebaseHelper.JUDUL_BUKU;


public class BukuFragment extends Fragment implements BukuAdapter.OpenBuku {

    private List<Buku> bukuList;
    private Context context;
    private View root;
    private LayoutInflater inflater;
    private BukuAdapter adapter;
    private ProgressBar bukuProgressbar;
    private DocumentReference reference;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getUid()!=null){
            reference = db.collection(COLLECTION_USERS).document(mAuth.getUid());
        }
        root = inflater.inflate(R.layout.fragment_buku, container, false);
        this.inflater = inflater;
        context = root.getContext();
        bukuList = new ArrayList<>();
        bukuProgressbar = root.findViewById(R.id.buku_progressbar);
        RecyclerView recyclerView = root.findViewById(R.id.catatanRecycleView);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        int height = 0;
        if (getActivity() != null) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            height = size.y;
        }
        adapter = new BukuAdapter(context, bukuList, height - dpToPx(144),this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewBukuDialog();
            }
        });
        recyclerView.setAdapter(adapter);
        getBookFromFireStore();
        return root;
    }

    private void addNewBukuDialog(){
        View newCatatanDialogView = inflater.inflate(R.layout.dialog_new_buku, null);
        final TextInputEditText judulEditText = newCatatanDialogView.findViewById(R.id.input_judul_buku);
        final TextInputEditText descEditText = newCatatanDialogView.findViewById(R.id.input_deskripsi_buku);
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_title_new_buku))
                .setView(newCatatanDialogView)
                .setPositiveButton(context.getString(R.string.dialog_positive_button_new_buku)
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                bukuProgressbar.setVisibility(View.VISIBLE);
                                if (judulEditText.length()>0&&judulEditText.getText()!=null) {
                                    Map<String, Object> buku = new HashMap<>();
                                    buku.put(JUDUL_BUKU, judulEditText.getText().toString());
                                    if (descEditText.getText()!=null)
                                        buku.put(DESKRIPSI_BUKU, descEditText.getText().toString());
                                    else
                                        buku.put(DESKRIPSI_BUKU, "");

                                    reference.collection(COLLECTION_BUKU)
                                            .add(buku)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Snackbar.make(root, context.getString(R.string.snackbar_success_insert_data),
                                                            Snackbar.LENGTH_SHORT).show();
                                                    getBookFromFireStore();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Snackbar.make(root, context.getString(R.string.snackbar_failed_insert_data),
                                                            Snackbar.LENGTH_SHORT).show();
                                                    Log.w(TAG, "Error adding document", e);
                                                }
                                            });

                                }
                            }
                        })
                .show();
    }

    private void getBookFromFireStore(){
        reference.collection(COLLECTION_BUKU)
                .orderBy(JUDUL_BUKU)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult()!=null) {
                                bukuList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Object> temp = document.getData();
                                    bukuList.add(new Buku(document.getId(),temp.get(JUDUL_BUKU).toString(),temp.get(DESKRIPSI_BUKU).toString()));
                                }
                                adapter.notifyDataSetChanged();
                                bukuProgressbar.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static final String INTENT_JUDUL_BUKU = "intentjudul";
    public static final String INTENT_ID_BUKU = "intentidbuku";

    @Override
    public void onClickBuku(String key, String judul) {
        Intent intent = new Intent(context, EditorActivity.class);
        intent.putExtra(INTENT_JUDUL_BUKU, judul);
        intent.putExtra(INTENT_ID_BUKU, key);
        startActivity(intent);
    }

    @Override
    public void onLongClickBuku(Context context, final String key, final String judul, final String desc) {
        new AlertDialog.Builder(context).setTitle("Opsi Buku")
                .setItems(R.array.dialog_opsi_buku, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                dialogEditBuku(key, judul, desc);
                                break;
                            case 1:
                                dialogHapusBuku(key);
                                break;
                        }
                    }
                }).show();
    }

    private void dialogHapusBuku(final String key) {
        new AlertDialog.Builder(context).setTitle("Hapus Buku")
                .setMessage("Apakah Anda Yakin ingin ")
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reference.collection(COLLECTION_BUKU).document(key).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        makeSnackbar("Berhasil Hapus Buku");
                                        getBookFromFireStore();
                                    }
                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeSnackbar("Gagal Hapus Buku");
                            }
                        });
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void dialogEditBuku(final String key, final String judul, String desc) {
        View newCatatanDialogView = inflater.inflate(R.layout.dialog_new_buku, null);
        final TextInputEditText judulEditText = newCatatanDialogView.findViewById(R.id.input_judul_buku);
        judulEditText.setText(judul);
        final TextInputEditText descEditText = newCatatanDialogView.findViewById(R.id.input_deskripsi_buku);
        descEditText.setText(desc);
        new AlertDialog.Builder(context)
                .setTitle("Edit Buku")
                .setView(newCatatanDialogView)
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (judulEditText.getText()!=null&&judulEditText.getText().toString().length()>0){
                            Map<String, Object> temp = new HashMap<>();
                            temp.put(JUDUL_BUKU, judulEditText.getText().toString());
                            temp.put(DESKRIPSI_BUKU, descEditText.getText().toString());

                            reference.collection(COLLECTION_BUKU).document(key)
                                    .update(temp)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            makeSnackbar("Berhasil Update");
                                            getBookFromFireStore();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            makeSnackbar("Gagal Update");
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void makeSnackbar(String msg){
        Snackbar.make(root, msg,
                Snackbar.LENGTH_SHORT).show();
    }
}