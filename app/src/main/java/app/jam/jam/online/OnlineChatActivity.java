package app.jam.jam.online;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.jam.jam.R;

public class OnlineChatActivity extends AppCompatActivity {

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private FloatingActionButton mSendButton, mSendImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_chat);

        initializeViews();
        setListeners();
    }

    private void setListeners() {
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO send message
            }
        });
    }

    private void initializeViews() {
        mConversationView = findViewById(R.id.in);
        mOutEditText = findViewById(R.id.edit_text_out);
        mSendButton = findViewById(R.id.send_floatingButton);
        mSendImageButton = findViewById(R.id.image_floatingButton);
    }

}