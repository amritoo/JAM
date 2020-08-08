package app.jam.jam.offline;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.HashSet;
import java.util.Set;

import app.jam.jam.R;
import app.jam.jam.auth.LoginActivity;
import app.jam.jam.data.Constants;
import app.jam.jam.online.OnlineActivity;
import app.jam.jam.settings.SettingsActivity;

public class OfflineActivity extends AppCompatActivity {

    /**
     * Tag for Log
     */
    private static final String TAG = "DeviceListActivity";

    /**
     * Member fields
     */
    private BluetoothAdapter mBtAdapter;

    /**
     * Newly discovered devices
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private ArrayAdapter<String> pairedDevicesArrayAdapter;

    private Set<String> discoveredSet;

    /**
     * Paired devices
     */
    private Set<BluetoothDevice> mPairedDevices;

    private MaterialButton mScanButton, mDiscoverableButton;
    private ListView mPairedListView, mNewDevicesListView;
    private MaterialToolbar mMaterialToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        initializeViews();

        // initializing set for discovered devices address
        discoveredSet = new HashSet<>();

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not supported in this device.", Toast.LENGTH_LONG).show();
            finish();
        } else if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }

        // Initialize the buttons to perform device discovery and make discoverable
        mScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setEnabled(false);
            }
        });
        mDiscoverableButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ensureDiscoverable();
            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        pairedDevicesArrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item_format);
        mNewDevicesArrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item_format);

        // Find and set up the ListView for paired devices
        mPairedListView.setAdapter(pairedDevicesArrayAdapter);
        mPairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        mNewDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        mNewDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

    }

    @Override
    protected void onResume() {
        // Get a set of currently paired devices
        mPairedDevices = mBtAdapter.getBondedDevices();
        pairedDevicesArrayAdapter.clear();

        // TODO: if too many devices paired then available devices list view is not shown
        // If there are paired devices, add each one to the ArrayAdapter
        if (mPairedDevices.size() > 0) {
//            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : mPairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getString(R.string.no_devices_paired);
            pairedDevicesArrayAdapter.add(noDevices);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                Toast.makeText(getApplicationContext(), "Bluetooth turned on successfully.", Toast.LENGTH_LONG).show();
                break;
            case RESULT_CANCELED:   // end this activity
                Toast.makeText(getApplicationContext(), "Bluetooth needs to be ON for this feature to work.", Toast.LENGTH_LONG).show();
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // For initializing views from layout file
    private void initializeViews() {
        mScanButton = findViewById(R.id.scan_button);
        mDiscoverableButton = findViewById(R.id.make_discoverable_button);
        mPairedListView = findViewById(R.id.paired_devices);
        mNewDevicesListView = findViewById(R.id.new_devices);

        mMaterialToolbar = findViewById(R.id.offline_topAppBar);
        // TODO: set my devices bluetooth name
//        mMaterialToolbar.setTitle();
        mMaterialToolbar.getMenu().add(Menu.NONE, Constants.ONLINE_MENU_ID, 1, R.string.menu_title_online);
        mMaterialToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case Constants.ONLINE_MENU_ID:
                        Toast.makeText(OfflineActivity.this, "Go online selected", Toast.LENGTH_SHORT).show();
                        goToOnlineActivity();
                        break;
                    case R.id.item_settings:
                        Toast.makeText(OfflineActivity.this, "Settings selected", Toast.LENGTH_SHORT).show();
                        goToSettingsActivity();
                        break;
                    case R.id.item_help:
                        Toast.makeText(OfflineActivity.this, "Help selected", Toast.LENGTH_SHORT).show();
                        goToHelpActivity();
                        break;
                    case R.id.item_logout:
                        Toast.makeText(OfflineActivity.this, "Logout selected", Toast.LENGTH_SHORT).show();
                        logoutUser();
                        break;
                    case R.id.item_about:
                        Toast.makeText(OfflineActivity.this, "About selected", Toast.LENGTH_SHORT).show();
                        goToAboutActivity();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // clearing previously found devices
        mNewDevicesArrayAdapter.clear();
        discoveredSet.clear();

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();
            mScanButton.setEnabled(true);    // in case connecting failed and still in this view

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            if (info.equals(getString(R.string.no_devices_paired)) || info.equals(getString(R.string.no_devices_found))) {
                Toast.makeText(getApplicationContext(), "Not a valid device.", Toast.LENGTH_SHORT).show();
                return;
            }
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent chatIntent = new Intent(getApplicationContext(), OfflineChatActivity.class);
            chatIntent.putExtra(Constants.EXTRA_DEVICE_ADDRESS, address);
            // Connect and open chat activity
            startActivity(chatIntent);
            // Set result and finish this Activity
//            setResult(Activity.RESULT_OK, chatIntent);
//            finish();
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED
                        && !discoveredSet.contains(device.getAddress())) {
                    // TODO: getting RSSI value, the smaller the more powerful the connection
                    String rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE));
                    mNewDevicesArrayAdapter.add(device.getName() + "\t" + rssi + "\n" + device.getAddress());
                    discoveredSet.add(device.getAddress());
                }
                // When discovery is finished, enable scan button
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mScanButton.setEnabled(true);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getString(R.string.no_devices_found);
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };


    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        } else {
            Toast.makeText(OfflineActivity.this, "This device is already in discoverable mode.", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        // forget login info
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.REMEMBER_ME, false);
        editor.remove(Constants.PREFERENCE_EMAIL);
        editor.remove(Constants.PREFERENCE_PASSWORD);
        editor.apply();

        Log.i(TAG, "Login data cleared");
        goToLogin();
    }

    // For going back to Sign In
    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToOnlineActivity() {
        // when going online login required again
        goToLogin();
    }

    private void goToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void goToHelpActivity() {
//        Intent intent = new Intent(this, OfflineActivity.class);
//        startActivity(intent);
    }

    private void goToAboutActivity() {
//        Intent intent = new Intent(this, OfflineActivity.class);
//        startActivity(intent);
    }

}
