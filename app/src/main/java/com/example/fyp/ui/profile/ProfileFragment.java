package com.example.fyp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.fyp.AssignNewBookingService;
import com.example.fyp.ChatService;
import com.example.fyp.NewBookingService;
import com.example.fyp.NewStaffService;
import com.example.fyp.R;
import com.example.fyp.StartActivity;
import com.example.fyp.UserDatabase;
import com.google.android.gms.common.internal.Constants;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        StartActivity.isStart = false;
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button tvEditProfile = view.findViewById(R.id.btn_editProfile);
        Button tvChangePassword = view.findViewById(R.id.btn_changePassword);
        Button tvSignout = view.findViewById(R.id.btn_signout);

        tvEditProfile.setOnClickListener(view13 -> startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        tvChangePassword.setOnClickListener(view12 -> startActivity(new Intent(getActivity(), ChangePasswordActivity.class)));

        tvSignout.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("Sign Out");
            builder.setMessage("Are you sure to sign out?");
            builder.setIcon(R.drawable.warning);
            builder.setPositiveButton("YES", (dialogInterface, i) -> {
                UserDatabase userDatabase = new UserDatabase(requireActivity());
                if (userDatabase.isDeletedDatabase()) {
                    startActivity(new Intent(requireActivity(), StartActivity.class));
                    requireActivity().stopService(new Intent(requireActivity(), NewBookingService.class));
                    requireActivity().stopService(new Intent(requireActivity(), NewStaffService.class));
                    requireActivity().stopService(new Intent(requireActivity(), AssignNewBookingService.class));
                    requireActivity().stopService(new Intent(requireActivity(), ChatService.class));
                } else {
                    Toast.makeText(requireActivity(), "Error, please try again.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
        });

        return view;
    }

}