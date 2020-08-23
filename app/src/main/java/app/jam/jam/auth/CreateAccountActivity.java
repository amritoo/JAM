package app.jam.jam.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import app.jam.jam.Manager;
import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.User;

public class CreateAccountActivity extends AppCompatActivity {

    /**
     * Tag to use to {@link Log} messages
     */
    private static final String TAG = "Create Account";

    private FirebaseAuth mAuth;
    private DatabaseReference mRootReference;

    private TextInputLayout mUsername, mEmail, mPassword;
    private MaterialButton createAccountButton;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Setting up firebase authentication
        mAuth = FirebaseAuth.getInstance();
        mRootReference = FirebaseDatabase.getInstance().getReference();

        // for initializing views from layout
        InitializeViews();
        setListeners();
    }

    // for initializing views from layout
    private void InitializeViews() {
        mUsername = findViewById(R.id.create_account_username_textInputLayout);
        mEmail = findViewById(R.id.create_account_email_textInputLayout);
        mPassword = findViewById(R.id.create_account_password_textInputLayout);
        createAccountButton = findViewById(R.id.create_account_button);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.message_create_account));
        mProgressDialog.setMessage(getString(R.string.message_wait));
        mProgressDialog.setCanceledOnTouchOutside(true);

        MaterialToolbar toolbar = findViewById(R.id.create_account_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActivityResult(null);
            }
        });
    }

    // all listeners for corresponding views
    private void setListeners() {
        Objects.requireNonNull(mUsername.getEditText())
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mUsername.setErrorEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Do nothing
                    }
                });

        Objects.requireNonNull(mEmail.getEditText())
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mEmail.setErrorEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Do nothing
                    }
                });

        Objects.requireNonNull(mPassword.getEditText())
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mPassword.setErrorEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Do nothing
                    }
                });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    /**
     * This method reads input data and validates them. If valid, then it
     * initiates {@link CreateAccountActivity#createNewAccount(String, String, String)}
     * using these data
     */
    private void createAccount() {
        String username = Objects
                .requireNonNull(mUsername.getEditText())
                .getText().toString();
        String email = Objects
                .requireNonNull(mEmail.getEditText())
                .getText().toString();
        String password = Objects
                .requireNonNull(mPassword.getEditText())
                .getText().toString();

        // validating parameter data
        if (!Manager.isValidUsername(this, username)) {
            mUsername.setError(getString(R.string.error_username));
            mUsername.setErrorEnabled(true);
            return;
        } else if (!Manager.isValidEmail(email)) {
            mEmail.setError(getString(R.string.error_create_email));
            mEmail.setErrorEnabled(true);
            return;
        } else if (!Manager.isValidPassword(this, password)) {
            mPassword.setError(getString(R.string.error_create_password));
            mPassword.setErrorEnabled(true);
            return;
        }

        createNewAccount(username, email, password);
    }

    /**
     * For creating a new account using {@link FirebaseAuth}
     *
     * @param username the name of the user
     * @param email    email address of the user
     * @param password password of the account
     */
    private void createNewAccount(final String username, final String email, final String password) {
        mProgressDialog.show();
        // Create account with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.i(TAG, "createUserWithEmail:success");

                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            updateUsername(currentUser, username);

                            User user = new User();
                            user.setUserName(username);
                            // add user to database and add admin as contact
                            addUserToDatabase(currentUser, user);
                        }
                        Toast.makeText(CreateAccountActivity.this, R.string.toast_create_account_success, Toast.LENGTH_SHORT).show();
                        setActivityResult(email);
                        mProgressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "createUserWithEmail:failure", e);
                        Snackbar.make(mUsername, R.string.snackbar_create_account_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        createAccount();
                                    }
                                })
                                .show();
                        mProgressDialog.dismiss();
                    }
                });
    }

    /**
     * Updates firebase user's username
     *
     * @param user     the user instance of {@link FirebaseUser}
     * @param username the name to set to user
     */
    private void updateUsername(FirebaseUser user, String username) {
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileChangeRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "updateUsername:success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "updateUsername:failure", e);
                    }
                });
    }

    private void addUserToDatabase(FirebaseUser currentUser, User user) {
        //update user database
        String currentUserID = currentUser.getUid();
        mRootReference.child(Constants.ROOT_USERS).child(currentUserID)
                .setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "addUserToDatabase:success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "addUserToDatabase:failure" + e);
                    }
                });
        addAdminContact(currentUserID);
    }

    /**
     * This method adds the admin account to registered user's contact list.
     */
    private void addAdminContact(final String currentUserId) {
        mRootReference.child(Constants.ROOT_CONTACTS)
                .child(currentUserId).child(Constants.CHILD_ADMIN_USER_ID)
                .child(Constants.CHILD_CONTACT).setValue(Constants.CHILD_CONTACT_SAVED)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "addAdminContact:successStage1");
                        mRootReference.child(Constants.ROOT_CONTACTS)
                                .child(Constants.CHILD_ADMIN_USER_ID).child(currentUserId)
                                .child(Constants.CHILD_CONTACT).setValue(Constants.CHILD_CONTACT_SAVED)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i(TAG, "addAdminContact:successStage2");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "addAdminContact:failureStage2", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "addAdminContact:failureStage1", e);
                    }
                });
    }

    /**
     * For going from this activity, {@link CreateAccountActivity}, to {@link LoginActivity}
     *
     * @param email the value to pass to login activity,
     *              if null then result will be set to {@link Activity#RESULT_CANCELED};
     *              otherwise it'll be {@link Activity#RESULT_OK}
     */
    private void setActivityResult(String email) {
        Intent resultIntent = new Intent();
        if (email != null) {
            resultIntent.putExtra(Constants.EMAIL, email);
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            setResult(Activity.RESULT_CANCELED, resultIntent);
        }
        finish();
    }

}