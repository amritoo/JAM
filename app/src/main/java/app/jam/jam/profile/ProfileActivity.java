package app.jam.jam.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    /**
     * Tag to use to {@link Log} messages
     */
    private static final String TAG = "Profile";

    private MaterialToolbar mToolbar;
    private CircleImageView mProfileImageView;
    private MaterialTextView mUsernameTextView, mAboutTextView, mWorkTextView, mAddressTextView, mBirthDateTextView;
    private MaterialButton mUpdateProfileButton, mChangePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // For initializing and setting listener for all views
        initializeViews();
        setListeners();

        String currentUserId = getIntent().getStringExtra(Constants.RECEIVER_USER_ID);
        boolean flag = getIntent().getBooleanExtra(Constants.CURRENT_USER, false);
        if (flag) {
            mUpdateProfileButton.setVisibility(View.VISIBLE);
            mChangePasswordButton.setVisibility(View.VISIBLE);
            FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
            assert mCurrentUser != null;
            currentUserId = mCurrentUser.getUid();
        }

        assert currentUserId != null;
        DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.ROOT_USERS).child(currentUserId);
        mUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null)
                    setData(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCreate:addValueEventListener:onCancelled", error.toException());
            }
        });
    }

    /**
     * This method reads data from user object and set it to corresponding views
     *
     * @param user the user object that represents the user information to display
     */
    private void setData(User user) {
        if (user.getUserName() != null)
            mUsernameTextView.setText(user.getUserName());
        if (user.getAbout() != null)
            mAboutTextView.setText(user.getAbout());
        if (user.getWork() != null)
            mWorkTextView.setText(user.getWork());
        if (user.getAddress() != null)
            mAddressTextView.setText(user.getAddress());
        if (user.getBirthDate() != null)
            mBirthDateTextView.setText(user.getBirthDate());
        if (user.getImageUri() != null)
            Picasso.get()
                    .load(user.getImageUri())
                    .placeholder(R.drawable.profile_image)
                    .into(mProfileImageView);
    }

    // For initializing views from layout
    private void initializeViews() {
        mToolbar = findViewById(R.id.profile_toolbar);

        mProfileImageView = findViewById(R.id.profile_circleImage);
        mUsernameTextView = findViewById(R.id.profile_username_textView);
        mAboutTextView = findViewById(R.id.profile_about_textView);
        mWorkTextView = findViewById(R.id.profile_work_textView);
        mAddressTextView = findViewById(R.id.profile_address_textView);
        mBirthDateTextView = findViewById(R.id.profile_birth_date_textView);

        mUpdateProfileButton = findViewById(R.id.profile_update_button);
        mChangePasswordButton = findViewById(R.id.profile_change_password_button);
    }

    // For setting listeners to views
    private void setListeners() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });

        mUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUpdateProfile();
            }
        });

        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChangePassword();
            }
        });
    }

    /**
     * Called to finish this activity and return to the caller activity.
     */
    private void finishThisActivity() {
        finish();
    }

    /**
     * For going from this activity, {@link ProfileActivity}, to {@link UpdateProfileActivity}
     */
    private void goToUpdateProfile() {
        Intent updateIntent = new Intent(this, UpdateProfileActivity.class);
        startActivity(updateIntent);
    }

    /**
     * For going from this activity, {@link ProfileActivity}, to {@link ChangePasswordActivity}
     */
    private void goToChangePassword() {
        Intent changeIntent = new Intent(this, ChangePasswordActivity.class);
        startActivity(changeIntent);
    }

}