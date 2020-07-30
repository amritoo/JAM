package app.jam.jam.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import app.jam.jam.Manager;
import app.jam.jam.R;
import app.jam.jam.data.User;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "Login Activity";
    private static final String ROOT_CHILD = "users";

    private FirebaseAuth mAuth;
    private DatabaseReference mRootReference;

    private TextInputLayout mUsername, mEmail, mPassword;
    private MaterialButton createAccountButton;
    private ProgressDialog mProgressDialog;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Setting up firebase authentication
        mAuth = FirebaseAuth.getInstance();
        mRootReference = FirebaseDatabase.getInstance().getReference();

        // for initializing views from layout
        InitializeViews();

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsername.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                createNewAccount(username, email, password);
            }
        });
    }

    // for initializing views from layout
    private void InitializeViews() {
        mUsername = findViewById(R.id.username_textInputLayout);
        mEmail = findViewById(R.id.email_textInputLayout);
        mPassword = findViewById(R.id.password_textInputLayout);
        createAccountButton = findViewById(R.id.create_account_button);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.message_create_account));
        mProgressDialog.setMessage(getString(R.string.message_wait));
        mProgressDialog.setCanceledOnTouchOutside(true);

        mToolbar = findViewById(R.id.create_account_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * For creating a new account
     *
     * @param username the name of user
     * @param email    email address
     * @param password password of the account
     */
    private void createNewAccount(final String username, final String email, final String password) {
        if (!Manager.isValidUsername(username)) {
            mUsername.setError(getString(R.string.warning_username));
            mUsername.setErrorEnabled(true);
            Toast.makeText(this, "Incorrect Username", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Manager.isValidEmail(email)) {
            mUsername.setError(getString(R.string.warning_create_email));
            mEmail.setErrorEnabled(true);
            Toast.makeText(this, "Incorrect Email", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Manager.isValidPassword(password)) {
            mUsername.setError(getString(R.string.warning_password));
            mPassword.setErrorEnabled(true);
            Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog.show();
        // Create account with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.i(TAG, "createUserWithEmail:success");

                        User user = new User();
                        user.setUserName(username);

                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            // Update username
                            updateUsername(currentUser, username);

                            //update user database
                            String currentUserID = currentUser.getUid();
                            mRootReference.child(ROOT_CHILD).child(currentUserID)
                                    .setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i(TAG, "Update successful");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Update failed: " + e);
                                        }
                                    });
                        }

                        Toast.makeText(CreateAccountActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        setActivityResult(email);
                        mProgressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "createUserWithEmail:failure", e);
                        Toast.makeText(CreateAccountActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                });
    }

    // Updates firebase user's username
    private void updateUsername(FirebaseUser user, String username) {
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        user.updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "User profile updated.");
                        } else {
                            Log.e(TAG, "User profile update failed!");
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        setActivityResult(null);
        return true;
    }

    // For going to login activity
    private void setActivityResult(String email) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(LoginActivity.EMAIL, email);
        if (email != null) {
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            setResult(Activity.RESULT_CANCELED, resultIntent);
        }
        finish();
    }

}