package com.example.receipt2go;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.mocoo.hang.rtprinter.driver.HsBluetoothPrintDriver;

public class MainActivity extends AppCompatActivity {

    public static HsBluetoothPrintDriver BLUETOOTH_PRINTER = null;
    private final static int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter = null;
    private static BluetoothDevice device;

    private ArrayList<Order> orders = new ArrayList<>();

    private TextView tvNoCurrentOrders = null;
    private static TextView tvBluetoothStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvNoCurrentOrders = findViewById(R.id.tvNoCurrentOrders);
        tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus) ;

        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device does not support Bluetooth.", Toast.LENGTH_SHORT).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (BLUETOOTH_PRINTER == null) {
            } else {
                if (BLUETOOTH_PRINTER.IsNoConnection()) {
                    tvBluetoothStatus.setText(device.getName());
                    tvBluetoothStatus.append(" is offline");
                } else {
                    tvBluetoothStatus.setText(R.string.status_connected);
                    tvBluetoothStatus.append(device.getName());
                }
            }
        }

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