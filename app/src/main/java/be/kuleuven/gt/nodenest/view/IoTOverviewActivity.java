package be.kuleuven.gt.nodenest.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.gt.nodenest.R;
import be.kuleuven.gt.nodenest.controller.MeasurementDBUpdate;
import be.kuleuven.gt.nodenest.controller.MqttCallbackImpl;
import be.kuleuven.gt.nodenest.model.IotDevice;
import be.kuleuven.gt.nodenest.controller.IotDeviceAdapter;
import be.kuleuven.gt.nodenest.model.MQTTReceiver;
import be.kuleuven.gt.nodenest.model.MQTTconnection;
import be.kuleuven.gt.nodenest.model.Session;
import be.kuleuven.gt.nodenest.model.userLogin;

// activity giving a list of all user devices
public class IoTOverviewActivity extends AppCompatActivity implements MQTTReceiver {
    private String LIST_URL = "https://studev.groept.be/api/a23PT103/getUserDevices/";
    private List<IotDevice> deviceList = new ArrayList<>();
    private RecyclerView deviceQueue;
    private Session session;
    userLogin user;
    private int userId;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_io_toverview);
        user = getIntent().getParcelableExtra("user");
        //failsafe incase user is null, can happen when refreshing
        if (user != null && user.getId() != -1) {
            LIST_URL += String.valueOf(user.getId());
        } else {
            userId = getIntent().getIntExtra("userId", -1);
            LIST_URL += String.valueOf(userId);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
           Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
           return insets;
          });
        deviceQueue = findViewById( R.id.deviceQueue );
        IotDeviceAdapter adapter = new IotDeviceAdapter(this, deviceList );
        deviceQueue.setAdapter( adapter );
        deviceQueue.setLayoutManager( new LinearLayoutManager( this ));
        deviceList();

        //MQTT
        session = session.getInstance(new MqttCallbackImpl(IoTOverviewActivity.this));
        session.mqttCallback.setHandler(IoTOverviewActivity.this);
        RetryConnection();

        subscribeToTopics();
        //MQTT end
    }
    // getting list of user devices from the db
    private void deviceList() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest deviceListRequest = new JsonArrayRequest(Request.Method.GET, LIST_URL, null,
                response -> {
                    if (response.length() > 0) {
                        deviceList.clear();
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject deviceObject = null;
                                deviceObject = response.getJSONObject(i);
                                int deviceId = deviceObject.getInt("DeviceID");
                                String mqttTopic = deviceObject.optString("MQTTtopic");
                                String deviceType = deviceObject.getString("DeviceType");
                                int userIdResponse = deviceObject.getInt("UserID");
                                String deviceName = deviceObject.getString("deviceName");
                                int status = deviceObject.getInt("status");
                                String measurement = deviceObject.getString("previousMeasurement");
                                String unit = deviceObject.getString("unit");

                                IotDevice device = new IotDevice(deviceId, mqttTopic, deviceType, userIdResponse, deviceName, status, measurement, unit);
                                deviceList.add(device);

                                subscribeToTopics();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        deviceQueue.getAdapter().notifyDataSetChanged();
                    }

                },
                error -> {
                    Toast.makeText(IoTOverviewActivity.this, "Unable to communicate with the server.", Toast.LENGTH_LONG).show();
                }
        );
        requestQueue.add(deviceListRequest);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(IoTOverviewActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user", user);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    // adding new device button handler
    public void onBtnAddDevice_click(View Caller) {
        Intent intent = new Intent(this, AddingDeviceActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("userId", userId);
        startActivity(intent);
        this.finish();
    }

    // evoking mqtt subscribe on topic for every device
    public void subscribeToTopics(){
        for (IotDevice iotDevice : deviceList) {
            String topic = iotDevice.getMqttTopic() + '/';
            session.mqttCallback.my_subscribe(topic);
        }
    }

    // handler for incoming mqtt messages from subscribed topics
    @Override
    public void ReceiveFromMQTT(String topic, String payload) {
        for (IotDevice iotDevice : deviceList) {
            if ((iotDevice.getMqttTopic() + "/").equals(topic)){
                iotDevice.setMeasurement(payload);
                ContextCompat.getMainExecutor(this).execute(()  -> {
                    MeasurementDBUpdate.updateDBdevice(IoTOverviewActivity.this, iotDevice.getDeviceId(),iotDevice.getMeasurement());
                    MeasurementDBUpdate.updateDBmeasurement(IoTOverviewActivity.this, iotDevice.getDeviceId(),iotDevice.getMeasurement());

                    deviceQueue.getAdapter().notifyDataSetChanged();
                });
                break;
            }
        }
    }

    // retrying connection after losing it
    @Override
    public void RetryConnection() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        //mqttConnectOptions.setUserName(MQTTconnection.username);
        //mqttConnectOptions.setPassword(MQTTconnection.password);
        while (!session.mqttCallback.connect(MQTTconnection.url, MQTTconnection.port, mqttConnectOptions)){
        }
        subscribeToTopics();
    }
}