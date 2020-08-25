package app.jam.jam.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.help.HelpActivity;
import app.jam.jam.profile.ProfileActivity;

public class SettingsActivity extends AppCompatActivity {

    /**
     * Tag to use to {@link Log} messages
     */
    private static final String TAG = "Settings";

    private MaterialToolbar mToolbar;
    private MaterialButton mProfileButton, mHelpButton, mContactButton;
    private SwitchMaterial mThemeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setListeners();

        String flag = getIntent().getStringExtra(Constants.CONNECTION_FLAG);
        assert flag != null;
        if (flag.equals(Constants.CONNECTION_OFFLINE)) {
            mProfileButton.setVisibility(View.GONE);
        }
        // Setting theme switch
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        boolean isChecked = preferences.getBoolean(Constants.CURRENT_THEME, false);
        mThemeSwitch.setChecked(isChecked);
    }

    // For initializing views from layout
    private void initializeViews() {
        mToolbar = findViewById(R.id.settings_toolbar);

        mProfileButton = findViewById(R.id.settings_profile_button);
        mThemeSwitch = findViewById(R.id.settings_theme_switch);
        mHelpButton = findViewById(R.id.settings_help_button);
        mContactButton = findViewById(R.id.settings_contact_button);
    }

    // All listeners for views
    private void setListeners() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });

        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHelp();
            }
        });

        mContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactUs();
            }
        });

        mThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mThemeSwitch.setText(R.string.switch_text_dark);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    mThemeSwitch.setText(R.string.switch_text_light);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(Constants.CURRENT_THEME, isChecked);
                editor.apply();
                Log.i(TAG, "saveTheme:saved");
            }
        });
    }

    /**
     * For finishing this activity.
     */
    private void finishThisActivity() {
        finish();
    }

    /**
     * For going from this activity, {@link SettingsActivity}, to {@link ProfileActivity}.
     * This method puts current user extra as true.
     */
    private void goToProfile() {
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra(Constants.CURRENT_USER, true);
        startActivity(profileIntent);
    }

    /**
     * For going from this activity, {@link SettingsActivity}, to {@link HelpActivity}.
     */
    private void goToHelp() {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    /**
     * Shows a dialog containing message.
     */
    private void showContactUs() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_text_contact_us)
                .setMessage(R.string.message_contact_us)
                .setPositiveButton(R.string.button_text_ok, null)
                .show();
    }

}