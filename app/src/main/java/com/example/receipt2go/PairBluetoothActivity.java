package com.example.receipt2go;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class PairBluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;

    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;

    // MAC Address to be returned as result
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_bluetooth);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize button to perform device discovery
        Button scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                permissionChecking();
            }
        });

        // Sets up list view for both currently paired Bluetooth devices and new devices
        pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.layout_list_bluetooth_device);
        newDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.layout_list_bluetooth_device);
        ListView pairedListView = findViewById(R.id.paired_devices);
        ListView newDevicesListView = findViewById(R.id.new_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        newDevicesListView.setAdapter(newDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(deviceClickListener);

        // Populate list with currently paired Bluetooth devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
    }

    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bluetoothAdapter.cancelDiscovery();

            // Get device MAC address
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private void permissionChecking() {
        newDevicesArrayAdapter.clear();
        doDiscovery();
    }

    private void doDiscovery() {
    }

}