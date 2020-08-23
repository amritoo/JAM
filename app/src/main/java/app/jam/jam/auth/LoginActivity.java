package app.jam.jam.auth;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import app.jam.jam.Manager;
import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.offline.OfflineActivity;
import app.jam.jam.online.OnlineActivity;

public class LoginActivity extends AppCompatActivity {

    /**
     * Tag to use to {@link Log} messages
     */
    private static final String TAG = "Login";

    private FirebaseAuth mAuth;

    private TextInputLayout mEmailTextInputLayout, mPasswordTextInputLayout;
    private MaterialCheckBox mRememberCheckBox;
    private MaterialButton mSignInButton, mRecoverPasswordButton, mCreateAccountButton, mOfflineButton;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setting up firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // For initializing views from layout and setting listeners
        InitializeViews();
        setListeners();

        FirebaseUser user = mAuth.getCurrentUser();
        // For checking preference for saved data
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        boolean checkbox = preferences.getBoolean(Constants.REMEMBER_ME, false);
        if (checkbox) {
            if (user != null) {
                startOnlineActivity();
            } else {
                String email = preferences.getString(Constants.PREFERENCE_EMAIL, "");
                String password = preferences.getString(Constants.PREFERENCE_PASSWORD, "");
                loginWithEmailAndPassword(email, password);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.CREATE_ACCOUNT_CODE &&
                resultCode == RESULT_OK && data != null) {
            String email = data.getStringExtra(Constants.EMAIL);
            Objects.requireNonNull(mEmailTextInputLayout.getEditText())
                    .setText(email);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // For initializing views from layout
    private void InitializeViews() {
        mEmailTextInputLayout = findViewById(R.id.login_email_textInputLayout);
        mPasswordTextInputLayout = findViewById(R.id.login_password_textInputLayout);
        mRememberCheckBox = findViewById(R.id.remember_me_checkbox);
        mSignInButton = findViewById(R.id.sign_in_button);

        mRecoverPasswordButton = findViewById(R.id.recover_password_button);
        mCreateAccountButton = findViewById(R.id.login_create_account_button);
        mOfflineButton = findViewById(R.id.offline_button);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.message_wait));
        mProgressDialog.setCanceledOnTouchOutside(true);
    }

    // For setting listeners to views
    private void setListeners() {
        Objects.requireNonNull(mEmailTextInputLayout.getEditText())
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mEmailTextInputLayout.setErrorEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Do nothing
                    }
                });

        Objects.requireNonNull(mPasswordTextInputLayout.getEditText())
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mPasswordTextInputLayout.setErrorEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Do nothing
                    }
                });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        mRecoverPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecoverPassword();
            }
        });

        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateAccountActivity();
            }
        });

        mOfflineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOfflineActivity();
            }
        });
    }

    /**
     * This method reads data from user and tests for validity.
     * If valid, it calls {@link LoginActivity#loginWithEmailAndPassword(String, String)}
     * with email, password.
     */
    private void login() {
        String email = Objects
                .requireNonNull(mEmailTextInputLayout.getEditText())
                .getText().toString();
        String password = Objects
                .requireNonNull(mPasswordTextInputLayout.getEditText())
                .getText().toString();

        // validity checking
        if (!Manager.isValidEmail(email)) {
            mEmailTextInputLayout.setError(getString(R.string.error_email));
            mEmailTextInputLayout.setErrorEnabled(true);
            return;
        } else if (!Manager.isValidPassword(this, password)) {
            mPasswordTextInputLayout.setError(getString(R.string.error_password));
            mPasswordTextInputLayout.setErrorEnabled(true);
            return;
        }

        loginWithEmailAndPassword(email, password);
    }

    /**
     * This method initiates login request to server.
     *
     * @param email    the email to use in login
     * @param password the password to use in login
     */
    private void loginWithEmailAndPassword(final String email, final String password) {
        mProgressDialog.setTitle(R.string.title_text_sign_in);
        mProgressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.i(TAG, "loginWithEmailAndPassword:success");
                        // Goto online activity
                        saveLoginInfo(mRememberCheckBox.isChecked() ? email : null, password);
                        startOnlineActivity();
                        mProgressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "loginWithEmailAndPassword:failure", e);
                        Snackbar.make(mEmailTextInputLayout, R.string.snackbar_login_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        login();
                                    }
                                })
                                .show();
                        mProgressDialog.dismiss();
                    }
                });
    }

    /**
     * For saving email and password to Preference if remember me is selected.
     *
     * @param email    the email address to save, if email is null then it removes preferences
     * @param password the password to save
     */
    private void saveLoginInfo(String email, String password) {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (email == null) {
            editor.putBoolean(Constants.REMEMBER_ME, false);
            editor.remove(Constants.PREFERENCE_EMAIL);
            editor.remove(Constants.PREFERENCE_PASSWORD);
            Log.i(TAG, "saveLoginInfo:cleared");
        } else {
            editor.putBoolean(Constants.REMEMBER_ME, true);
            editor.putString(Constants.PREFERENCE_EMAIL, email);
            editor.putString(Constants.PREFERENCE_PASSWORD, password);
            Log.i(TAG, "saveLoginInfo:saved");
        }
        editor.apply();
    }

    /**
     * This method shows password recover dialog with a TextInputLayout for further process.
     */
    private void startRecoverPassword() {
        // Creating Dialog class to show recover dialog
        final View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_recover_password, null);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_text_forgot_password)
                .setMessage(R.string.message_recover_password)
                .setView(view)
                .setPositiveButton(R.string.button_text_recover, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputLayout textInputLayout = view.findViewById(R.id.recover_email_textInputLayout);
                        final String userEmail = Objects
                                .requireNonNull(textInputLayout.getEditText())
                                .getText().toString();
                        if (Manager.isValidEmail(userEmail)) {
                            passwordResetEmail(userEmail);
                        } else {
                            textInputLayout.setError(getString(R.string.help_email));
                            textInputLayout.setErrorEnabled(true);
                            Toast.makeText(LoginActivity.this, R.string.help_email, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNeutralButton(R.string.button_text_cancel, null)
                .show();
    }

    /**
     * Sends password reset request to server using {@link FirebaseAuth#sendPasswordResetEmail}.
     *
     * @param userEmail valid email used to create the account
     */
    private void passwordResetEmail(final String userEmail) {
        mProgressDialog.setTitle(R.string.title_text_reset_password);
        mProgressDialog.show();
        mAuth.sendPasswordResetEmail(userEmail)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "passwordResetEmail:success");
                        mProgressDialog.dismiss();
                        new MaterialAlertDialogBuilder(LoginActivity.this)
                                .setTitle(R.string.title_text_reset_password)
                                .setMessage(getString(R.string.message_reset_password_email_check, userEmail))
                                .setPositiveButton(R.string.button_text_ok, null)
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "passwordResetEmail:failure", e);
                        Snackbar.make(mEmailTextInputLayout, R.string.snackbar_reset_password_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startRecoverPassword();
                                    }
                                })
                                .show();
                        mProgressDialog.dismiss();
                    }
                });
    }

    /**
     * For going from this activity, {@link LoginActivity}, to {@link OnlineActivity}
     * and set it as new root.
     */
    private void startOnlineActivity() {
        Intent onlineActivityIntent = new Intent(this, OnlineActivity.class);
        onlineActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        onlineActivityIntent.putExtra(Constants.LOGIN_TO_ONLINE, Constants.LOGIN_TO_ONLINE);
        startActivity(onlineActivityIntent);
        finish();
    }

    /**
     * For going from this activity, {@link LoginActivity}, to {@link CreateAccountActivity}
     * After this activity returns, auto fills email EditText if new user is successfully registered.
     */
    private void startCreateAccountActivity() {
        Intent createAccountIntent = new Intent(this, CreateAccountActivity.class);
        startActivityForResult(createAccountIntent, Constants.CREATE_ACCOUNT_CODE);
    }

    /**
     * For going from this activity, {@link LoginActivity}, to {@link OfflineActivity}
     * and set it as new root.
     */
    private void startOfflineActivity() {
        Intent offlineActivityIntent = new Intent(this, OfflineActivity.class);
        offlineActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(offlineActivityIntent);
        finish();
    }

}