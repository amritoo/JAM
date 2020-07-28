package app.jam.jam;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    private Button loginButton, recoverPasswordButton, signUpButton, offlineButton;
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberCheckBox;
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
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                login(email, password);
            }
        });

        // for recover password button action
        recoverPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLoginToPassword();
            }
        });

        // for create account button action
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateAccountActivity();
            }
        });

        // for offline button action
        offlineButton.setOnClickListener(new View.OnClickListener() {
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
                emailEditText.setText(email);
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
            Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
            return;
        } else if (!Manager.isValidPassword(password)) {
            Toast.makeText(this, R.string.invalid_password, Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
                    saveLoginInfo(rememberCheckBox.isChecked() ? email : EMAIL, password);
                    // Goto online activity
                    startOnlineActivity();
                } else {
                    String message = String.valueOf(task.getException());
                    Log.i("Login", message);    // log error

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
        editor.putString(REMEMBER_ME, email.equals(EMAIL) ? FALSE : TRUE);
        editor.putString(EMAIL, email);
        editor.putString(PASSWORD, password);
        editor.apply();

        Log.i(TAG, email.equals(EMAIL) ? "Login data cleared" : "Login data saved");
    }

    // For initializing views from layout
    private void InitializeViews() {
        loginButton = findViewById(R.id.login_button);
        emailEditText = findViewById(R.id.email_editText);
        passwordEditText = findViewById(R.id.password_editText);
        recoverPasswordButton = findViewById(R.id.recover_password_button);
        signUpButton = findViewById(R.id.create_account_button);
        offlineButton = findViewById(R.id.offline_button);
        rememberCheckBox = findViewById(R.id.remember_me_checkbox);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.sign_in);
        mProgressDialog.setMessage(getString(R.string.wait_message));
        mProgressDialog.setCanceledOnTouchOutside(true);

        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setTitle(R.string.app_name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    // TODO: From login to recover password
    private void sendLoginToPassword() {
//        Intent step12Intent = new Intent(this, RecoverPasswordActivity.class);
//        startActivity(step12Intent);
    }

    // for log in to bluetooth
    private void startOfflineActivity() {
        Intent offlineActivityIntent = new Intent(this, OfflineActivity.class);
        startActivity(offlineActivityIntent);
    }

}