package com.example.receipt2go;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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

    private BluetoothAdapter bluetoothAdapter = null;
    private final static int REQUEST_ENABLE_BT = 1;
    public static HsBluetoothPrintDriver BLUETOOTH_PRINTER = null;

    private ArrayList<Order> orders = new ArrayList<>();

    private static TextView NoCurrentOrders = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NoCurrentOrders = (TextView) findViewById(R.id.tvNoCurrentOrders);

        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device does not support Bluetooth.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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
                        sb.append(json + "\n");
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
                NoCurrentOrders.setVisibility(View.VISIBLE);
                orders.clear();
                initRecyclerView();
            } else {
                NoCurrentOrders.setVisibility(View.INVISIBLE);
                JSONArray jsonArray = jsonObject.getJSONArray("orders");
                orders.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    orders.add(new Order(obj.getString("Order_Number"), obj.getString("Order_Date"), obj.getString("CustomerName")));
                }
                initRecyclerView();
            }
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
        if (BLUETOOTH_PRINTER.IsNoConnection()) {
            BLUETOOTH_PRINTER.stop();
        }
    }
}