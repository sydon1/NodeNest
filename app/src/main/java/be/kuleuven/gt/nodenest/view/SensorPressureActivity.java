package be.kuleuven.gt.nodenest.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class SensorPressureActivity extends AppCompatActivity implements MQTTReceiver {
    private BarChart barChart;
    private ChartActivityModel chartActivityModel;
    private IotDevice iotDevice;
    private Button modifyBtn;
    private userLogin user;
    private Session session;


    private String LIST_URL = "https://studev.groept.be/api/a23PT103/getPressure/";
    private List<Measurement> measurementList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor_pressure);

        setSupportActionBar(findViewById(R.id.my_toolbar));
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);


        modifyBtn = findViewById(R.id.addDeviceBtn);

        barChart = findViewById(R.id.bar_chart);

        chartActivityModel = new ChartActivityModel();

        chartActivityModel.setSensorNameView( findViewById(R.id.sensorName) );
        chartActivityModel.setStatusView( findViewById(R.id.statusSensor) );
        chartActivityModel.setUnitsView( findViewById(R.id.pressureUnits) );
        chartActivityModel.setValueView( findViewById(R.id.pressureValue) );

        iotDevice = (IotDevice) getIntent().getParcelableExtra("iotDevice");
        user = (userLogin) getIntent().getParcelableExtra("user");

        chartActivityModel.setSensorName(iotDevice.getDeviceName());
        if(iotDevice.getStatus() == 1){
            chartActivityModel.setStatus("Active");
        }else {

            chartActivityModel.setStatus("Not Active");
        }
        chartActivityModel.setUnits(iotDevice.getUnit());
        chartActivityModel.setValue(iotDevice.getMeasurement());

        LIST_URL += String.valueOf(iotDevice.getDeviceId());
        measurementList();


        //MQTT
        session = session.getInstance(new MqttCallbackImpl(SensorPressureActivity.this));
        session.mqttCallback.setHandler(SensorPressureActivity.this);
        RetryConnection();

        chartUpdater();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SensorPressureActivity.this, IoTOverviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user", user);
        intent.putExtra("userId", iotDevice.getUserId());
        startActivity(intent);
        finish();
    }

    // connecting return button in te upper menu
    @Override
    public boolean onOptionsItemSelected( @NonNull MenuItem item ) {

        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(SensorPressureActivity.this, IoTOverviewActivity.class);
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
    public void ButtonModifyPressPressed(View Caller){
        Intent intent = new Intent(this, ModifyDeviceActivity.class);
        intent.putExtra("iotDevice", iotDevice);
        startActivity(intent);
        this.finish();
    }

    // getting average measurements for last 7 days from the db
    private void measurementList() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest deviceListRequest = new JsonArrayRequest(Request.Method.GET, LIST_URL, null,
                response -> {
                    if (response.length() > 0) {
                        measurementList.clear();
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject measurementObject = null;
                                measurementObject = response.getJSONObject(i);
                                int deviceId = measurementObject.getInt("DeviceID");
                                Timestamp measurement_date = Timestamp.valueOf(measurementObject.getString("measurement_date")+" 00:00:00");
                                float average_measurement = (float) ((float) Math.round(measurementObject.getDouble("average_measurement")*10)/10.0);

                                Measurement measurement = new Measurement(deviceId, average_measurement, measurement_date);
                                measurementList.add(measurement);

                                ArrayList<BarEntry> barEntries= new ArrayList<>();
                                chartUpdater();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                },
                error -> {
                    Toast.makeText(SensorPressureActivity.this, "Unable to communicate with the server.", Toast.LENGTH_LONG).show();
                }
        );
        requestQueue.add(deviceListRequest);
    }

    // drawing/updating the bar chart
    private void chartUpdater(){

        ArrayList<BarEntry> barEntries= new ArrayList<>();

        // filling chart with values
        int i;
        // filling values not found as zeros
        for(i = 0; i < 7 - measurementList.size(); i++){

            BarEntry barEntry = new BarEntry(i,0);
            barEntries.add(barEntry);
        }
        for(Measurement measurement : measurementList){

            BarEntry barEntry = new BarEntry(i,measurement.getMeasurement());
            barEntries.add(barEntry);
            i++;
        }

//        for (int i = 1;i<10;i++){
//            float value = (float) (i*10.0);
//
//            BarEntry barEntry = new BarEntry(i,value);
//            barEntries.add(barEntry);
//        }`

        BarDataSet barDataSet = new BarDataSet(barEntries,"Pressure in last 7d");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barChart.setData(new BarData(barDataSet));
        //barChart.animateY(3000);
        barChart.animateY(0);
        barChart.getDescription().setText("Pressure in last 7d");
        barChart.getDescription().setTextColor(Color.BLUE);
    }

    // handler for receiving mqtt message
    @Override
    public void ReceiveFromMQTT(String topic, String payload) {
        if ((iotDevice.getMqttTopic() + "/").equals(topic)){
            iotDevice.setMeasurement(payload);
            ContextCompat.getMainExecutor(this).execute(()  -> {
                MeasurementDBUpdate.updateDBdevice(SensorPressureActivity.this, iotDevice.getDeviceId(),iotDevice.getMeasurement());
                MeasurementDBUpdate.updateDBmeasurement(SensorPressureActivity.this, iotDevice.getDeviceId(),iotDevice.getMeasurement());
                measurementList();
                chartActivityModel.setValue(payload);
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
        }
        String topic = iotDevice.getMqttTopic() + '/';
        session.mqttCallback.my_subscribe(topic);
    }
}
