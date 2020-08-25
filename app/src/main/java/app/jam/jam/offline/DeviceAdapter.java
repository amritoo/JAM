package app.jam.jam.offline;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.Device;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private Context context;
    private List<Device> mDeviceList;
    private BluetoothAdapter mBluetoothAdapter;
    private MaterialButton mScanButton;

    public DeviceAdapter(Context context, List<Device> mDeviceList, BluetoothAdapter mBluetoothAdapter, MaterialButton mScanButton) {
        this.context = context;
        this.mDeviceList = mDeviceList;
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.mScanButton = mScanButton;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_device_format, parent, false);
        DeviceAdapter.DeviceViewHolder viewHolder;
        viewHolder = new DeviceViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceViewHolder holder, int position) {
        final Device device = mDeviceList.get(position);
        String name = device.getName();
        if (device.isPaired()) {
            name = String.format("%s (%s)", name, Constants.TITLE_PAIRED);
        }
        holder.deviceNameTextView.setText(name);
        holder.deviceAddressTextView.setText(device.getAddress());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statChat(device.getName(), device.getAddress());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    /**
     * this method creates the result intent and include the MAC address of receiver.
     * Also it cancels discovery of bluetooth adapter and enables scan button.
     *
     * @param name    receiver device name
     * @param address the MAC address of receiver device
     */
    private void statChat(String name, String address) {
        // Cancel discovery because it's costly and we're about to connect
        mBluetoothAdapter.cancelDiscovery();
        mScanButton.setEnabled(true);

        Intent chatIntent = new Intent(context, OfflineChatActivity.class);
        chatIntent.putExtra(Constants.EXTRA_DEVICE_NAME, name);
        chatIntent.putExtra(Constants.EXTRA_DEVICE_ADDRESS, address);
        context.startActivity(chatIntent);
    }

    /**
     * The view holder class that extends {@link RecyclerView.ViewHolder}
     * for {@link DeviceAdapter}.
     */
    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView deviceNameTextView, deviceAddressTextView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            deviceNameTextView = itemView.findViewById(R.id.device_name);
            deviceAddressTextView = itemView.findViewById(R.id.device_address);
        }
    }

}
