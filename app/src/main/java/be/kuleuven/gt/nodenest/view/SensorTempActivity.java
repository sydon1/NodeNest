package be.kuleuven.gt.nodenest.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import be.kuleuven.gt.nodenest.R;
import be.kuleuven.gt.nodenest.controller.MeasurementDBUpdate;
import be.kuleuven.gt.nodenest.controller.MqttCallbackImpl;
import be.kuleuven.gt.nodenest.model.ChartActivityModel;
import be.kuleuven.gt.nodenest.model.IotDevice;
import be.kuleuven.gt.nodenest.model.MQTTReceiver;
import be.kuleuven.gt.nodenest.model.MQTTconnection;
import be.kuleuven.gt.nodenest.model.Measurement;
import be.kuleuven.gt.nodenest.model.Session;
import be.kuleuven.gt.nodenest.model.userLogin;

// activity for handling details about light sensors
public class SensorTempActivity extends AppCompatActivity implements MQTTReceiver {
    private LineChart lineChart;
    private ChartActivityModel chartActivityModel;
    private IotDevice iotDevice;
    private Button modifyBtn;
    private userLogin user;
    private Session session;
    private LineDataSet lineDataSet;
    private List<Measurement> measurementList = new ArrayList<>();
    private String LIST_URL = "https://studev.groept.be/api/a23PT103/getTemp/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor_temp);

        setSupportActionBar(findViewById(R.id.my_toolbar));
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        chartActivityModel = new ChartActivityModel();
        modifyBtn = findViewById(R.id.addDeviceBtn);
        lineChart = findViewById(R.id.line_chart);

        chartActivityModel.setSensorNameView(findViewById(R.id.sensorName));
        chartActivityModel.setStatusView(findViewById(R.id.statusSensor));
        chartActivityModel.setUnitsView(findViewById(R.id.temperatureUnits));
        chartActivityModel.setValueView(findViewById(R.id.temperatureValue));

        iotDevice = getIntent().getParcelableExtra("iotDevice");
        user = getIntent().getParcelableExtra("user");

        chartActivityModel.setSensorName(iotDevice.getDeviceName());
        chartActivityModel.setStatus(iotDevice.getStatus() == 1 ? "Active" : "Not Active");
        chartActivityModel.setUnits(iotDevice.getUnit());
        chartActivityModel.setValue(Float.parseFloat(iotDevice.getMeasurement()));

        LIST_URL += String.valueOf(iotDevice.getDeviceId());
        fetchMeasurementList();

        session = session.getInstance(new MqttCallbackImpl(SensorTempActivity.this));
        session.mqttCallback.setHandler(SensorTempActivity.this);
        RetryConnection();

        chartUpdater();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SensorTempActivity.this, IoTOverviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user", user);
        intent.putExtra("userId", iotDevice.getUserId());
        startActivity(intent);
        finish();
    }

    // connecting return button in te upper menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(SensorTempActivity.this, IoTOverviewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("user", user);
            intent.putExtra("userId", iotDevice.getUserId());
            startActivity(intent);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // handler for modify device button
    public void ButtonModifyTempPressed(View Caller) {
        Intent intent = new Intent(this, ModifyDeviceActivity.class);
        intent.putExtra("iotDevice", iotDevice);
        startActivity(intent);
        this.finish();
    }

    // getting average measurements for last 7 days from the db
    private void fetchMeasurementList() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest deviceListRequest = new JsonArrayRequest(Request.Method.GET, LIST_URL, null,
                response -> {
                    if (response.length() > 0) {
                        measurementList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject measurementObject = response.getJSONObject(i);
                                int deviceId = measurementObject.getInt("DeviceID");
                                Timestamp measurementHour = Timestamp.valueOf(measurementObject.getString("measurement_hour"));
                                float averageMeasurement = (float) Math.round(measurementObject.getDouble("average_measurement") * 10) / 10.0f;
                                Measurement measurement = new Measurement(deviceId, averageMeasurement, measurementHour);
                                measurementList.add(measurement);

                                lineChart.setData(new LineData(lineDataSet));
                                chartUpdater();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(this::chartUpdater);
                    }

                },
                error -> Toast.makeText(SensorTempActivity.this, "Unable to communicate with the server.", Toast.LENGTH_LONG).show()
        );
        requestQueue.add(deviceListRequest);
    }

    // drawing/updating the line chart
    private void chartUpdater(){

        ArrayList<Entry> entries= new ArrayList<>();

        int i;

        for (i = 0; i < 24 - measurementList.size(); i++) {
            entries.add(new BarEntry(i, 0));
        }

        for (Measurement measurement : measurementList) {
            entries.add(new BarEntry(i, measurement.getMeasurement()));
            i++;
        }

        lineDataSet = new LineDataSet(entries, "Temperature in last 24h");
        lineDataSet.setCircleRadius(1f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setValueTextSize(20F);
        lineDataSet.setFillColor(Color.GREEN);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        lineDataSet.setDrawValues(false);

        lineChart.setData(new LineData(lineDataSet));
        lineChart.animateY(0);
    }

    // handler for receiving mqtt message
    @Override
    public void ReceiveFromMQTT(String topic, String payload) {
        if ((iotDevice.getMqttTopic() + "/").equals(topic)) {
            iotDevice.setMeasurement(payload);
            ContextCompat.getMainExecutor(this).execute(() -> {
                MeasurementDBUpdate.updateDBdevice(SensorTempActivity.this, iotDevice.getDeviceId(), iotDevice.getMeasurement());
                MeasurementDBUpdate.updateDBmeasurement(SensorTempActivity.this, iotDevice.getDeviceId(), iotDevice.getMeasurement());
                fetchMeasurementList();
                chartActivityModel.setValue(Float.parseFloat(payload));
            });
        }
    }

    // handler for losing mqtt connection - trying to reconnect
    @Override
    public void RetryConnection() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        //mqttConnectOptions.setUserName(MQTTconnection.username);
        //mqttConnectOptions.setPassword(MQTTconnection.password);
        while (!session.mqttCallback.connect(MQTTconnection.url, MQTTconnection.port, mqttConnectOptions)) {
            // Retry logic
        }
        String topic = iotDevice.getMqttTopic() + '/';
        session.mqttCallback.my_subscribe(topic);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        // Check if the touch event is within the bounds of the specified views
        if (isTouchInsideView(ev, modifyBtn) || isTouchInsideView(ev, lineChart)) {
            return super.dispatchTouchEvent(ev);
        }

        if (isTouchInsideView(ev, findViewById(R.id.my_toolbar))) {
            return super.dispatchTouchEvent(ev);
        }

        // Ignore the touch event if it's not within the specified views
        return true;
    }

    private boolean isTouchInsideView(MotionEvent ev, View view) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        float x = ev.getRawX();
        float y = ev.getRawY();
        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        return rect.contains((int) x, (int) y);
    }
}
