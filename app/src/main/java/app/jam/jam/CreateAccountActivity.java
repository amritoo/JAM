package app.jam.jam;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "Login Activity";
    private static final String ROOT_CHILD = "users";

    private FirebaseAuth mAuth;
    private DatabaseReference mRootReference;

    private EditText mEmail, mPassword, mUsername;
    private Button createAccountButton;
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
                String username = mUsername.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                createNewAccount(username, email, password);
            }
        });
    }

    // for initializing views from layout
    private void InitializeViews() {
        createAccountButton = findViewById(R.id.create_account_button);
        mEmail = findViewById(R.id.email_editText);
        mPassword = findViewById(R.id.password_editText);
        mUsername = findViewById(R.id.username_editText);

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Creating new account");
        loadingBar.setMessage("Please wait,while we are creating new account");
        loadingBar.setCanceledOnTouchOutside(true);

        mToolbar = findViewById(R.id.create_account_toolbar);
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setTitle(R.string.app_name);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            Toast.makeText(this, "Please Enter Username", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Manager.isValidEmail(email)) {
            Toast.makeText(this, "Please Enter Email", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Manager.isValidPassword(password)) {
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.show();
        //TODO: make username unique
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String currentUserID;
                if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                    currentUserID = mAuth.getCurrentUser().getUid();
                    mRootReference.child(ROOT_CHILD).child(currentUserID).setValue("");
                    Toast.makeText(CreateAccountActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                    goToLoginActivity();
                } else {
                    currentUserID = String.valueOf(task.getException());
                    Log.e(TAG, currentUserID);
                    Toast.makeText(CreateAccountActivity.this, "New account create unsuccessful!", Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            }
        });

    }

    // For going to login activity
    private void goToLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginIntent.putExtra(LoginActivity.EMAIL, mEmail.getText());
        startActivity(loginIntent);
    }

}