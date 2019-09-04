package com.ambinusian.adab.ui.userprofile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import com.ambinusian.adab.R;
import com.ambinusian.adab.preferences.UserPreferences;
import com.ambinusian.adab.ui.login.ActivityLogin;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileDialogFragment extends DialogFragment {

    private CircleImageView circleImageView;
    private TextView textUserName;
    private TextView textUserDepartment;
    private MaterialButton buttonAccountDetails;
    private LinearLayout layoutLogout;
    private LinearLayout layoutDark;

    public UserProfileDialogFragment() {
        // Constructor kosong diperlukan untuk DialogFragment.
        // Pastikan tidak memberikan argument/parameter apapun ke
        // constructor ini.
        System.out.println("Ashiap bosq");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile_dialog, container, false);
        // Set transparent background and no title
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserPreferences userPreferences = new UserPreferences(getContext());

        circleImageView = getView().findViewById(R.id.circle_image_profile_picture);
        textUserName = getView().findViewById(R.id.tv_user_name);
        textUserDepartment = getView().findViewById(R.id.tv_user_department);
        buttonAccountDetails = getView().findViewById(R.id.button_account_details);
        layoutLogout = getView().findViewById(R.id.layout_logout);
        layoutDark = getView().findViewById(R.id.layout_profile_dialog_dark);

        textUserName.setText(userPreferences.getUserName());
        textUserDepartment.setText(userPreferences.getUserDepartement());

        layoutLogout.setOnClickListener(v -> {
            userPreferences.clearLoggedInUser();
            if(getActivity()!=null) {
                getActivity().startActivity(new Intent(getContext(), ActivityLogin.class));
                getActivity().finish();
            }
        });

        layoutDark.setOnClickListener(v -> {
            int mode;
            mode = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO)
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;
            userPreferences.setPrefNight(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }
}