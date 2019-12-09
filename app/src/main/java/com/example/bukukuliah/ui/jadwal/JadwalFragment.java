package com.example.bukukuliah.ui.jadwal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bukukuliah.R;

public class JadwalFragment extends Fragment {

    private JadwalViewModel jadwalViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        jadwalViewModel =
                ViewModelProviders.of(this).get(JadwalViewModel.class);
        View root = inflater.inflate(R.layout.fragment_jadwal, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        jadwalViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}