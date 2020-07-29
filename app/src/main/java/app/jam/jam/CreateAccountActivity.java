package app.jam.jam;

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
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "Login Activity";
    private static final String ROOT_CHILD = "users";

    private FirebaseAuth mAuth;
    private DatabaseReference mRootReference;

    private TextInputLayout mUsername, mEmail, mPassword;
    private MaterialButton createAccountButton;
    private ProgressDialog loadingBar;
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

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Creating new account");
        loadingBar.setMessage("Please wait,while we are creating new account");
        loadingBar.setCanceledOnTouchOutside(true);

        mToolbar = findViewById(R.id.create_account_toolbar);
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setTitle(R.string.app_name);
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
    private void createNewAccount(String username, String email, String password) {
        if (!Manager.isValidUsername(username)) {
            mUsername.setError(getString(R.string.warning_username));
            mUsername.setErrorEnabled(true);
            Toast.makeText(this, "Incorrect Username", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Manager.isValidEmail(email)) {
            mUsername.setError(getString(R.string.warning_email));
            mEmail.setErrorEnabled(true);
            Toast.makeText(this, "Incorrect Email", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Manager.isValidPassword(password)) {
            mUsername.setError(getString(R.string.warning_password));
            mPassword.setErrorEnabled(true);
            Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.show();
        //TODO: make username unique
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                            Log.i(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //TODO: set name
                            String currentUserID = mAuth.getCurrentUser().getUid();
                            mRootReference.child(ROOT_CHILD).child(currentUserID).setValue("");

                            Toast.makeText(CreateAccountActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            goToLoginActivity();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(CreateAccountActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        loadingBar.dismiss();
                    }
                });

    }

    // For going to login activity
    private void goToLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginIntent.putExtra(LoginActivity.EMAIL, mEmail.getEditText().getText().toString());
        startActivity(loginIntent);
    }

}