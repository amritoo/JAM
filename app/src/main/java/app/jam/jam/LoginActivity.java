package app.jam.jam;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import app.jam.jam.offline.OfflineActivity;
import app.jam.jam.online.OnlineActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private static final String TAG = "Login Activity";
    private static final int REQUEST_EMAIL = 1;

    private static final String PREFERENCE_FILE_NAME = "login_activity";

    public static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String REMEMBER_ME = "remember";

    private static final String TRUE = Boolean.TRUE.toString();
    private static final String FALSE = Boolean.FALSE.toString();

    private MaterialButton mSignInButton, mRecoverPasswordButton, mCreateAccountButton, mOfflineButton;
    private TextInputLayout mEmailTextInputLayout, mPasswordTextInputLayout;
    private MaterialCheckBox mRememberCheckBox;
    private ProgressDialog mProgressDialog;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.i(TAG, "activity creating");

        // Setting up firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // for initializing views from layout
        InitializeViews();

        //for checking preference for saved data
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
        String checkbox = preferences.getString(REMEMBER_ME, FALSE);
        if (checkbox != null && checkbox.equals(TRUE)) {
            String email = preferences.getString(EMAIL, "");
            String password = preferences.getString(PASSWORD, "");
            login(email, password);
        }

        // For login button action
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailTextInputLayout.getEditText().getText().toString();
                String password = mPasswordTextInputLayout.getEditText().getText().toString();
                login(email, password);
            }
        });

        // for recover password button action
        mRecoverPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecoverPassword();
            }
        });

        // for create account button action
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateAccountActivity();
            }
        });

        // for offline button action
        mOfflineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOfflineActivity();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_EMAIL && data != null) {
            String email = data.getStringExtra(EMAIL);
            if (Manager.isValidEmail(email))
                mEmailTextInputLayout.getEditText().setText(email);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * This function validates 'email' and 'password' passed to it. If valid, it initiates login request to server.
     *
     * @param email    the email to use in login
     * @param password the password to use in login
     */
    private void login(final String email, final String password) {
        // validity checking
        if (!Manager.isValidEmail(email)) {
            mEmailTextInputLayout.setError(getString(R.string.error_login_email));
            mEmailTextInputLayout.setErrorEnabled(true);
            Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
            return;
        } else if (!Manager.isValidPassword(password)) {
            mPasswordTextInputLayout.setError(getString(R.string.error_login_password));
            mPasswordTextInputLayout.setErrorEnabled(true);
            Toast.makeText(this, R.string.invalid_password, Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog.setTitle(R.string.text_sign_in);
        mProgressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "signInWithEmail:success");
                    Toast.makeText(getApplicationContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
                    saveLoginInfo(mRememberCheckBox.isChecked() ? email : EMAIL, password);
                    // Goto online activity
                    startOnlineActivity();
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.getException());

                    Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();
            }
        });
    }

    // For saving email and password if remember me is selected
    private void saveLoginInfo(String email, String password) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (!email.equals(EMAIL)) {
            editor.putString(REMEMBER_ME, TRUE);
            editor.putString(EMAIL, email);
            editor.putString(PASSWORD, password);
        } else {
            editor.putString(REMEMBER_ME, FALSE);
            editor.remove(EMAIL);
            editor.remove(PASSWORD);
        }
        editor.apply();

        Log.i(TAG, email.equals(EMAIL) ? "Login data cleared" : "Login data saved");
    }

    // For initializing views from layout
    private void InitializeViews() {
        mEmailTextInputLayout = findViewById(R.id.login_email_textInputLayout);
        mPasswordTextInputLayout = findViewById(R.id.login_password_textInputLayout);
        mRememberCheckBox = findViewById(R.id.remember_me_checkbox);
        mSignInButton = findViewById(R.id.sign_in_materialButton);
        mRecoverPasswordButton = findViewById(R.id.recover_password_button);
        mCreateAccountButton = findViewById(R.id.login_create_account_button);
        mOfflineButton = findViewById(R.id.offline_button);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.wait_message));
        mProgressDialog.setCanceledOnTouchOutside(true);

        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    // From login to online activity
    private void startOnlineActivity() {
        Intent onlineActivityIntent = new Intent(this, OnlineActivity.class);
        startActivity(onlineActivityIntent);
    }

    // From login to create account activity
    private void startCreateAccountActivity() {
        // auto fill email EditText in onActivityResult after this activity returns
        Intent createAccountIntent = new Intent(this, CreateAccountActivity.class);
        startActivityForResult(createAccountIntent, REQUEST_EMAIL);
    }

    // From login to Offline Activity
    private void startOfflineActivity() {
        Intent offlineActivityIntent = new Intent(this, OfflineActivity.class);
        startActivity(offlineActivityIntent);
    }

    private void startRecoverPassword() {
        // TODO: set margin to edit text
        final EditText emailEditText = new EditText(this);
        emailEditText.setHint(R.string.hint_email);
        emailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(R.dimen.margin_horizontal, 0, R.dimen.margin_horizontal, 0);
        emailEditText.setLayoutParams(params);

        // Creating Dialog class to show recover dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_recover_password);
        builder.setMessage(R.string.message_recover_password);
        builder.setView(emailEditText);
        builder.setPositiveButton(R.string.button_text_recover, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userEmail = emailEditText.getText().toString();
                mProgressDialog.setTitle(R.string.text_recover_password);
                mProgressDialog.show();
                if (!Manager.isValidEmail(userEmail)) {
                    Toast.makeText(LoginActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.sendPasswordResetEmail(userEmail)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(LoginActivity.this, "Please Check Your Email Account\n and Reset Your Password", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                mProgressDialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}