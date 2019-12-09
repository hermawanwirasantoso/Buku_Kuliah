package com.example.bukukuliah.ui.profile;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.bukukuliah.R;
import com.example.bukukuliah.ui.login.LoginPage;
import com.example.bukukuliah.ui.profile.UserProfile.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.auth.User;

public class ProfileFragment extends Fragment {

    private ProfileViewModel mViewModel;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_fragment,container,false);

        String[] menuItems = {"Edit Profile", "Logout"};
        ListView listView = (ListView) view.findViewById(R.id.profileListView);

        ArrayAdapter<String> listviewAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_1,
                menuItems
        );

        listView.setAdapter(listviewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent intent = new Intent(getActivity(), UserProfile.class);
                        startActivity(intent);
                        break;
                    case 1:
                        FirebaseAuth.getInstance().signOut();
                        intent = new Intent (getActivity(), LoginPage.class);
                        startActivity(intent);

                        break;
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel
    }

}
