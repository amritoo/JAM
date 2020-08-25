package app.jam.jam.offline;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import app.jam.jam.R;
import app.jam.jam.about.AboutActivity;
import app.jam.jam.auth.LoginActivity;
import app.jam.jam.data.Constants;
import app.jam.jam.data.Device;
import app.jam.jam.help.HelpActivity;
import app.jam.jam.settings.SettingsActivity;

public class OfflineActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private BluetoothAdapter mBluetoothAdapter;

    private MaterialToolbar mToolbar;
    private MaterialButton mScanButton, mDiscoverableButton;
    private RecyclerView mRecyclerView;

    private Set<String> mDevicesAddressSet;
    private ArrayList<Device> mDiscoveredDeviceList, mAllDevicesList;
    private DeviceAdapter myDeviceAdapter;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        // For initializing and setting listeners to views
        initializeViews();
        setListeners();

        // Get the local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), R.string.Toast_bluetooth_not_supported, Toast.LENGTH_LONG).show();
            finishThisActivity();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        } else {
            String name = mBluetoothAdapter.getName();
            if (name == null) {
                name = mBluetoothAdapter.getAddress();
            }
            mToolbar.setTitle(name);
        }

        // initializing set for discovered devices address
        mDevicesAddressSet = new HashSet<>();
        mDiscoveredDeviceList = new ArrayList<>();
        mAllDevicesList = new ArrayList<>();

        // Initialize array adapters.
        myDeviceAdapter = new DeviceAdapter(this, mAllDevicesList, mBluetoothAdapter, mScanButton);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(myDeviceAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
    }

    // For initializing views from layout file
    private void initializeViews() {
        mScanButton = findViewById(R.id.scan_button);
        mDiscoverableButton = findViewById(R.id.make_discoverable_button);
        mRecyclerView = findViewById(R.id.paired_devices);

        mToolbar = findViewById(R.id.offline_topAppBar);
    }

    // For setting listeners to views
    private void setListeners() {
        mScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(OfflineActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    doDiscovery();
                    v.setEnabled(false);
                } else {
                    startLocationPermission();
                }
            }
        });

        mDiscoverableButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ensureDiscoverable();
            }
        });

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.offline_go_online:
                        goToLoginActivity();
                        break;
                    case R.id.offline_settings:
                        goToSettingsActivity();
                        break;
                    case R.id.offline_help:
                        goToHelpActivity();
                        break;
                    case R.id.offline_about:
                        goToAboutActivity();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        Set<BluetoothDevice> mPairedDevicesSet = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (mPairedDevicesSet.size() > 0) {
            for (BluetoothDevice device : mPairedDevicesSet) {
                String address = device.getAddress();
                if (!mDevicesAddressSet.contains(address)) {
                    Device device1 = new Device();
                    device1.setName(device.getName());
                    device1.setAddress(address);
                    mAllDevicesList.add(device1);
                    mDevicesAddressSet.add(address);
                }
            }
            myDeviceAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.toast_bluetooth_on_success, Toast.LENGTH_SHORT).show();
                String name = mBluetoothAdapter.getName();
                if (name == null) {
                    name = mBluetoothAdapter.getAddress();
                }
                mToolbar.setTitle(name);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth needs to be ON for this feature to work.", Toast.LENGTH_SHORT).show();
                finishThisActivity();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Clearing previously found devices
        mAllDevicesList.removeAll(mDiscoveredDeviceList);
        for (Device device : mDiscoveredDeviceList) {
            mDevicesAddressSet.remove(device.getAddress());
        }
        mDiscoveredDeviceList.clear();
        myDeviceAdapter.notifyDataSetChanged();

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED
                        && !mDevicesAddressSet.contains(device.getAddress())) {
                    mDevicesAddressSet.add(device.getAddress());

                    Device device1 = new Device();
                    device1.setName(device.getName());
                    device1.setAddress(device.getAddress());
                    mDiscoveredDeviceList.add(device1);
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mScanButton.setEnabled(true);
                if (mDiscoveredDeviceList.size() == 0)
                    Toast.makeText(OfflineActivity.this, R.string.toast_no_new_device_found, Toast.LENGTH_SHORT).show();
                mAllDevicesList.addAll(mDiscoveredDeviceList);
                myDeviceAdapter.notifyDataSetChanged();
            }
        }
    };


    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, Constants.BLUETOOTH_DISCOVERABLE_TIME);
            startActivity(discoverableIntent);
        } else {
            Toast.makeText(this, R.string.toast_already_in_discoverable_mode, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method requests for location permission if it's not given.
     */
    public void startLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.title_text_location_permisssion)
                        .setMessage(R.string.meessage_location_access)
                        .setPositiveButton(R.string.button_text_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(OfflineActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finishThisActivity();
            }
        }
    }

    /**
     * Finishes this activity
     */
    private void finishThisActivity() {
        finish();
    }

    /**
     * For going from this activity, {@link OfflineActivity}, to {@link LoginActivity}
     * and set it as new root.
     */
    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * For going from this activity, {@link OfflineActivity}, to {@link SettingsActivity}
     * and passes offline flag
     */

    private void goToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(Constants.CONNECTION_FLAG, Constants.CONNECTION_OFFLINE);
        startActivity(intent);
    }

    /**
     * For going from this activity, {@link OfflineActivity}, to {@link HelpActivity}
     */

    private void goToHelpActivity() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    /**
     * For going from this activity, {@link OfflineActivity}, to {@link AboutActivity}
     */

    private void goToAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

}