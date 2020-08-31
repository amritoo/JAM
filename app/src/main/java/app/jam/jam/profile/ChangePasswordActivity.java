package app.jam.jam.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
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

import java.util.Objects;

import app.jam.jam.R;
import app.jam.jam.methods.Checker;

public class ChangePasswordActivity extends AppCompatActivity {

    /**
     * Tag to use to {@link Log} messages
     */
    private static final String TAG = "Change Password";

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private MaterialToolbar mToolbar;
    private TextInputLayout mOldPasswordLayout, mNewPasswordLayout;
    private MaterialButton mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initializeViews();
        setListeners();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            finishThisActivity();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void initializeViews() {
        mToolbar = findViewById(R.id.change_password_toolbar);

        mOldPasswordLayout = findViewById(R.id.change_password_old_textInputLayout);
        mNewPasswordLayout = findViewById(R.id.change_password_new_textInputLayout);
        mButton = findViewById(R.id.change_password_button);
    }

    private void setListeners() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });

        Objects.requireNonNull(mOldPasswordLayout.getEditText())
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mOldPasswordLayout.setErrorEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Do nothing
                    }
                });

        Objects.requireNonNull(mNewPasswordLayout.getEditText())
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mNewPasswordLayout.setErrorEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Do nothing
                    }
                });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChangePassword();
            }
        });
    }

    /**
     * For change password option. This method creates and shows the dialog to enter new password.
     */
    private void startChangePassword() {
        final String oldPassword = Objects
                .requireNonNull(mOldPasswordLayout.getEditText())
                .getText().toString();
        final String newPassword = Objects
                .requireNonNull(mNewPasswordLayout.getEditText())
                .getText().toString();

        if (!Checker.isValidPassword(this, oldPassword)) {
            mOldPasswordLayout.setError(getString(R.string.error_password));
            mOldPasswordLayout.setErrorEnabled(true);
            return;
        }
        if (!Checker.isValidPassword(this, newPassword)) {
            mNewPasswordLayout.setError(getString(R.string.error_create_password));
            mNewPasswordLayout.setErrorEnabled(true);
            return;
        }

        String email = mCurrentUser.getEmail();
        assert email != null;
        mAuth.signInWithEmailAndPassword(email, oldPassword)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.i(TAG, "loginWithEmailAndPassword:success");
                        changePassword(newPassword);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "loginWithEmailAndPassword:failure", e);
                        Snackbar.make(mOldPasswordLayout, R.string.toast_authentication_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startChangePassword();
                                    }
                                })
                                .show();
                    }
                });
    }

    /**
     * Sends password update request to server using {@link FirebaseUser#updatePassword}
     *
     * @param newPassword the new password
     */
    private void changePassword(String newPassword) {
        mCurrentUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "changePassword:success");
                        Toast.makeText(ChangePasswordActivity.this, R.string.toast_change_password_success, Toast.LENGTH_SHORT).show();
                        finishThisActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "changePassword:failure", e);
                        Snackbar.make(mOldPasswordLayout, R.string.snackbar_change_password_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startChangePassword();
                                    }
                                })
                                .show();
                    }
                });
    }

    /**
     * Finishes this activity
     */
    private void finishThisActivity() {
        finish();
    }

}