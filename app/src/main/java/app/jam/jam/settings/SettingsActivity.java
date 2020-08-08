package app.jam.jam.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.profile.ProfileActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private MaterialToolbar mToolbar;
    private MaterialButton mProfileButton;
    private CircleImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setListeners();

        mImageView.setImageDrawable(getDrawable(R.drawable.ic_user_108));
    }

    private void initializeViews() {
        mToolbar = findViewById(R.id.settings_toolbar);

        mImageView = findViewById(R.id.profile_image);
        mProfileButton = findViewById(R.id.profile_button);
    }

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
    }

    private void finishThisActivity() {
        finish();
    }

    private void goToProfile() {
        // TODO put extra
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra(Constants.CURRENT_USER, true);
        startActivity(profileIntent);
    }
}