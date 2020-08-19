package app.jam.jam.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import app.jam.jam.Manager;
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

        // FOr initializing and setting listener for all views
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

        Picasso.get().load(user.getImageUri()).placeholder(R.drawable.profile_image).into(mProfileImageView);
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
                startChangePassword();
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
        startActivityForResult(updateIntent, Constants.UPDATE_PROFILE_CODE);
    }

    /**
     * For change password option. This method creates and shows the dialog to enter new password.
     */
    private void startChangePassword() {
        Toast.makeText(this, "Change password selected", Toast.LENGTH_SHORT).show();
        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_text_change_password)
                .setMessage(R.string.message_change_password)
                .setView(view)
                .setPositiveButton(R.string.button_text_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputLayout textInputLayout = view.findViewById(R.id.change_password_textInputLayout);
                        final String newPassword = textInputLayout.getEditText().getText().toString();
                        if (Manager.isValidPassword(ProfileActivity.this, newPassword)) {
                            changePassword(newPassword);
                        } else {
                            textInputLayout.setError(getString(R.string.warning_create_password));
                            textInputLayout.setErrorEnabled(true);
                        }
                    }
                })
                .setNeutralButton(R.string.button_text_cancel, null)
                .show();
    }

    /**
     * Sends password update request to server using {@link FirebaseUser#updatePassword}
     *
     * @param newPassword the new password
     */
    private void changePassword(String newPassword) {
        mCurrentUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this, R.string.toast_change_password_success, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "updatePassword:failure", e);
                Toast.makeText(ProfileActivity.this, R.string.toast_change_password_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}