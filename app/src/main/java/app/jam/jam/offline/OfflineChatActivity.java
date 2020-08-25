package app.jam.jam.offline;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.Message;
import app.jam.jam.methods.Cryptography;

public class OfflineChatActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothChatFragment";

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    private int maxConnectionAttemptTimes = 5;

    private MaterialToolbar mToolbar;
    private RecyclerView mConversationView;
    private EditText mOutEditText;
    private FloatingActionButton mSendButton;

    private String mConnectedDeviceAddress;
    private StringBuffer mOutStringBuffer;

    private List<Message> allMessageList;
    private OfflineChatAdapter mChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_chat);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.message_bluetooth_disabled_exit, Toast.LENGTH_LONG).show();
            finishThisActivity();
        }

        // For initializing views
        initializeViews();

        mConnectedDeviceAddress = getIntent().getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
        String name = getIntent().getStringExtra(Constants.EXTRA_DEVICE_NAME);
        if (name == null) {
            name = mConnectedDeviceAddress;
        }
        mToolbar.setTitle(name);
    }

    // FOr initializing all views from layout
    private void initializeViews() {
        mConversationView = findViewById(R.id.offline_chat_recyclerView);
        mOutEditText = findViewById(R.id.offline_chat_editText);
        mSendButton = findViewById(R.id.offline_chat_send_floatingButton);

        mToolbar = findViewById(R.id.offline_chat_toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBluetoothAdapter == null) {
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (maxConnectionAttemptTimes > 0) {
            if (maxConnectionAttemptTimes % 2 == 0) {
                connectDevice(getIntent(), true);
            } else {
                connectDevice(getIntent(), false);
            }
            maxConnectionAttemptTimes--;
        }

        if (mChatService != null) {
            if (mChatService.getState() == Constants.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                setupChat();
            } else {
                Log.e(TAG, "BT not enabled");
                Toast.makeText(this, R.string.message_bluetooth_disabled_exit, Toast.LENGTH_SHORT).show();
                finishThisActivity();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        allMessageList = new ArrayList<>();
        // Initialize the array adapter for the conversation thread
        mChatAdapter = new OfflineChatAdapter(allMessageList, mConnectedDeviceAddress);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mConversationView.setLayoutManager(linearLayoutManager);
        mConversationView.setAdapter(mChatAdapter);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = mOutEditText.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }


    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        if (mChatService.getState() != Constants.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            message = Cryptography.encrypt(message);
            byte[] send = message.getBytes();
            mChatService.write(send);

            mOutStringBuffer.setLength(0);
            mOutEditText.setText("");
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            setStatus(R.string.status_connected);
                            break;
                        case Constants.STATE_CONNECTING:
                            setStatus(R.string.status_connecting);
                            break;
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
                            setStatus(R.string.subtitle_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    Message message = new Message(Constants.MESSAGE_TYPE_TEXT);
                    message.setBody(writeMessage);
                    message.setTo(mConnectedDeviceAddress);
                    message.setFrom("");

                    allMessageList.add(message);
                    mChatAdapter.notifyDataSetChanged();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    message = new Message(Constants.MESSAGE_TYPE_TEXT);
                    message.setBody(readMessage);
                    message.setFrom(mConnectedDeviceAddress);

                    allMessageList.add(message);
                    mChatAdapter.notifyDataSetChanged();
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(OfflineChatActivity.this, getString(R.string.toast_connected_to, mConnectedDeviceName), Toast.LENGTH_SHORT).show();
                    if (mConnectedDeviceName != null)
                        mToolbar.setTitle(mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    if (maxConnectionAttemptTimes > 0) {
                        if (maxConnectionAttemptTimes % 2 == 0) {
                            connectDevice(getIntent(), true);
                        } else {
                            connectDevice(getIntent(), false);
                        }
                        maxConnectionAttemptTimes--;
                    } else {
                        Toast.makeText(OfflineChatActivity.this, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        mToolbar.setSubtitle(resId);
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link Constants#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        Bundle extras = data.getExtras();
        if (extras == null) {
            return;
        }
        String address = extras.getString(Constants.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device.getName() != null) {
            mToolbar.setTitle(device.getName());
        }

        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    /**
     * Finishes this activity
     */
    private void finishThisActivity() {
        finish();
    }

}