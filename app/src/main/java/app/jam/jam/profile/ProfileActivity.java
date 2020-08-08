package app.jam.jam.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "View Profile";

    private FirebaseUser mCurrentUser;
    private DatabaseReference mUsersReference;

    private MaterialToolbar mToolbar;
    private CircleImageView mProfileImageView;
    private MaterialTextView mUsernameTextView, mAboutTextView, mWorkTextView, mAddressTextView, mBirthDateTextView;
    private MaterialButton mUpdateProfileButton, mChangePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        initializeViews();
        setListeners();

        String currentUserId = mCurrentUser.getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_USER).child(currentUserId);
        mUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null)
                    setData(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadUser:onCancelled", error.toException());
            }
        });
    }

    @Override
    protected void onStart() {
        Intent intent = getIntent();
        boolean flag = intent.getBooleanExtra(Constants.CURRENT_USER, false);
        if (flag) {
            mUpdateProfileButton.setVisibility(View.VISIBLE);
            mChangePasswordButton.setVisibility(View.VISIBLE);
        }
        super.onStart();
    }

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

        mProfileImageView.setImageDrawable(getDrawable(R.drawable.ic_user_108));
    }

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
                changePassword();
            }
        });
    }

    private void finishThisActivity() {
        finish();
    }

    private void goToUpdateProfile() {
        Intent updateIntent = new Intent(this, UpdateProfileActivity.class);
        startActivityForResult(updateIntent, Constants.UPDATE_PROFILE_CODE);
    }

    private void changePassword() {
        // TODO
        Toast.makeText(this, "Change password selected", Toast.LENGTH_SHORT).show();
    }
}