package com.example.receipt2go;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import com.mocoo.hang.rtprinter.driver.Contants;
import com.mocoo.hang.rtprinter.driver.HsBluetoothPrintDriver;

public class MainActivity extends AppCompatActivity {

    public static Context CONTEXT;

    public static HsBluetoothPrintDriver BLUETOOTH_PRINTER = null;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_CONNECT_DEVICE = 2;

    private BluetoothAdapter bluetoothAdapter = null;
    private static BluetoothDevice device;

    private ArrayList<Order> orders = new ArrayList<>();

    private TextView tvNoCurrentOrders = null;
    private static Button btnBluetoothStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CONTEXT = getApplicationContext();

        tvNoCurrentOrders = findViewById(R.id.tvNoCurrentOrders);
        btnBluetoothStatus = findViewById(R.id.btnBluetoothStatus);
        btnBluetoothStatus.setOnClickListener(btnBluetoothStatusOnClickListener);

        // Fetch orders periodically
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fetchOrders("http://sushistamford.com/menuDB/OrdersController.php?view=all");
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 5000);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(CONTEXT, "Device does not support Bluetooth.", Toast.LENGTH_SHORT).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (BLUETOOTH_PRINTER == null) {
                initializeBluetoothDevice();
            } else {
                if (BLUETOOTH_PRINTER.IsNoConnection()) {
                    btnBluetoothStatus.setText(R.string.status_offline);
                } else {
                    btnBluetoothStatus.setText("Connected to: " + device.getName());
                }
            }
        }
    }

    private void initializeBluetoothDevice() {
        BLUETOOTH_PRINTER = HsBluetoothPrintDriver.getInstance();
        BLUETOOTH_PRINTER.setHandler(new BluetoothHandler(MainActivity.this));
    }

    static class BluetoothHandler extends Handler {

        private final WeakReference<MainActivity> myWeakReference;

        BluetoothHandler(MainActivity weakReference) {
            myWeakReference = new WeakReference<>(weakReference);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = myWeakReference.get();
            if (mainActivity != null) {
                super.handleMessage(msg);
                Bundle data = msg.getData();

                switch (data.getInt("flag")) {
                    case Contants.FLAG_STATE_CHANGE:
                        int state = data.getInt("state");
                        switch (state) {
                            case HsBluetoothPrintDriver.CONNECTED_BY_BLUETOOTH:
                                btnBluetoothStatus.setText("Connected to: " + device.getName());
                                Toast.makeText(CONTEXT,"Connection successful.", Toast.LENGTH_SHORT).show();
                                break;
                            case HsBluetoothPrintDriver.FLAG_SUCCESS_CONNECT:
                                btnBluetoothStatus.setText(R.string.status_connecting);
                                break;
                            case HsBluetoothPrintDriver.UNCONNECTED:
                                btnBluetoothStatus.setText(R.string.status_connect);
                                break;
                        }
                        break;
                    case Contants.FLAG_SUCCESS_CONNECT:
                        btnBluetoothStatus.setText(R.string.status_connecting);
                        break;
                    case Contants.FLAG_FAIL_CONNECT:
                        Toast.makeText(CONTEXT,"Connection failed.",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    // Bluetooth printer button
    View.OnClickListener btnBluetoothStatusOnClickListener = new View.OnClickListener() {
        Intent intent = null;
        @Override
        public void onClick(View v) {
            // Ask user to enable Bluetooth again if it is disabled
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                intent = new Intent(MainActivity.this, PairBluetoothActivity.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            // PairBluetoothActivity returns with a device to connect
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = Objects.requireNonNull(data.getExtras()).getString(PairBluetoothActivity.EXTRA_DEVICE_ADDRESS);
                    device = bluetoothAdapter.getRemoteDevice(address);
                    BLUETOOTH_PRINTER.start();
                    BLUETOOTH_PRINTER.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    initializeBluetoothDevice();
                } else {
                    Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                }
        }
    }

    // Fetches open orders from URL
    private void fetchOrders(final String urlWebService) {
        class DownloadJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String json) {
                super.onPostExecute(json);
                // JSON data fetched, now parse and initialize orders
                try {
                    initOrders(json);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json).append("\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return e.getMessage();
                }
            }
        }

        DownloadJSON getJSON = new DownloadJSON();
        getJSON.execute();
    }

    // Populate the array with current open orders
    private void initOrders(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("orders")) {
                tvNoCurrentOrders.setVisibility(View.VISIBLE);
                orders.clear();
            } else {
                tvNoCurrentOrders.setVisibility(View.INVISIBLE);
                JSONArray jsonArray = jsonObject.getJSONArray("orders");
                orders.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    orders.add(new Order(obj.getString("Order_Number"), obj.getString("Order_Date"), obj.getString("CustomerName")));
                }
            }
            initRecyclerView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Creates a recycler view of open orders
    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rvOrders);
        RVAdapter adapter = new RVAdapter(this, orders);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}