package com.example.bukukuliah.ui.jadwal;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.bukukuliah.R;

import java.util.ArrayList;
import java.util.List;

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


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        jadwalViewModel =
                ViewModelProviders.of(this).get(JadwalViewModel.class);
        View root = inflater.inflate(R.layout.fragment_jadwal, container, false);
        Context context = root.getContext();

        jadwalList = new ArrayList<>();

        recyclerView = (RecyclerView) root.findViewById(R.id.jadwalRecycleView);
        layoutManager = new LinearLayoutManager(context,RecyclerView.VERTICAL,false);

        recyclerView.setLayoutManager(layoutManager);

        final JadwalAdapter adapter = new JadwalAdapter(context, jadwalList);
        recyclerView.setAdapter(adapter);

        return root;
    }
}