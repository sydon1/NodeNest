package be.kuleuven.gt.nodenest.view;

import static be.kuleuven.gt.nodenest.view.IoTOverviewActivity.*;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.ArrayList;

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
public class SensorLightActivity extends AppCompatActivity implements MQTTReceiver {
    private PieChart pieChart;
    ChartActivityModel chartActivityModel;
    private IotDevice iotDevice;
    private String status;
    private Button modifyBtn;
    private userLogin user;

    private float brightness;
    private Session session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor_light);

        setSupportActionBar(findViewById(R.id.my_toolbar));
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        modifyBtn = findViewById(R.id.addDeviceBtn);

        chartActivityModel = new ChartActivityModel();
        pieChart = findViewById(R.id.pie_chart);

        chartActivityModel.setSensorNameView( findViewById(R.id.sensorName) );
        chartActivityModel.setStatusView( findViewById(R.id.statusSensor) );
        chartActivityModel.setUnitsView( findViewById(R.id.lightUnits) );
        chartActivityModel.setValueView( findViewById(R.id.lightValue) );

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


        //MQTT
        session = session.getInstance(new MqttCallbackImpl(SensorLightActivity.this));
        session.mqttCallback.setHandler(SensorLightActivity.this);
        RetryConnection();

        brightness = Float.valueOf(iotDevice.getMeasurement());
        chartUpdater();
    }


    // connecting return button in te upper menu
    @Override
    public boolean onOptionsItemSelected( @NonNull MenuItem item ) {

        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(SensorLightActivity.this, IoTOverviewActivity.class);
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
    public void ButtonModifyLightPressed(View Caller){
        Intent intent = new Intent(this, ModifyDeviceActivity.class);
        intent.putExtra("iotDevice", iotDevice);
        startActivity(intent);
        this.finish();
    }

    // drawing/updating the pie chart
    private void chartUpdater(){

        ArrayList<PieEntry> pieEntries = new ArrayList<>();

//        for (int i = 1;i<5;i++){
//            float value = (float) (i*10.0);
//
//            String label = String.valueOf(i);
//            PieEntry pieEntry = new PieEntry(value, label);
//            pieEntries.add(pieEntry);
//        }

        // calculating brightness level
        brightness = Float.valueOf(iotDevice.getMeasurement());
        float percentage = (brightness/330)*100;
        if(percentage >= 100){
            percentage = 100;
        }
        percentage = Math.round(percentage*10)/10;

        PieEntry pieEntryDark = new PieEntry( 100 - percentage, "% Dark");

        PieEntry pieEntryLit = new PieEntry(percentage, "% Bright");
        pieEntries.add(pieEntryDark);
        pieEntries.add(pieEntryLit);

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Brightness");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(16f);

        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(String.valueOf(percentage)+"%");
        //pieChart.animate();
        pieChart.animateY(0);
        //pieChart.animateX(0);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SensorLightActivity.this, IoTOverviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user", user);
        intent.putExtra("userId", iotDevice.getUserId());
        startActivity(intent);
        finish();
    }

    // handler for receiving mqtt message
    @Override
    public void ReceiveFromMQTT(String topic, String payload) {
        if ((iotDevice.getMqttTopic() + "/").equals(topic)){
            iotDevice.setMeasurement(payload);
            ContextCompat.getMainExecutor(this).execute(()  -> {
                MeasurementDBUpdate.updateDBdevice(SensorLightActivity.this, iotDevice.getDeviceId(),iotDevice.getMeasurement());
                MeasurementDBUpdate.updateDBmeasurement(SensorLightActivity.this, iotDevice.getDeviceId(),iotDevice.getMeasurement());
                chartActivityModel.setValue(payload);
                chartUpdater();
            });
        }
        //database
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
