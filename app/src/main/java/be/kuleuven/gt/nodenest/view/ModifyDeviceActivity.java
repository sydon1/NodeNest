package be.kuleuven.gt.nodenest.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import be.kuleuven.gt.nodenest.R;
import be.kuleuven.gt.nodenest.model.IotDevice;
import be.kuleuven.gt.nodenest.model.userLogin;

public class ModifyDeviceActivity extends AppCompatActivity {
    private TextView title;
    private EditText nameEditText;
    private Spinner typeSpinner;
    private Spinner unitsSpinner;
    private Spinner statusSpinner;
    private String[] sensorTypes;
    private String[] tempUnits;
    private String[] lightUnits;
    private String[] pressUnits;
    private String[] statusOptions = {"0", "1"};
    private ArrayAdapter<String> typesAdapter;
    private ArrayAdapter<String> tempAdapter;
    private ArrayAdapter<String> lightAdapter;
    private ArrayAdapter<String> pressAdapter;
    private ArrayAdapter<String> statusAdapter;

    private IotDevice iotDevice;
    private EditText name;
    private EditText topic;
    int spinner_type_count;
    int spinner_unit_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modify_device);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.my_toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        title = findViewById(R.id.new_device_name_text);

        sensorTypes = new String[]{"Temperature Sensor", "Pressure Sensor", "Light Sensor"};
        tempUnits = new String[]{"°C", "°K", "°F"};
        lightUnits = new String[]{"Lux"};
        pressUnits = new String[]{"hPa", "Pa", "kPa"};

        typeSpinner = findViewById(R.id.new_device_type_spinner);
        unitsSpinner = findViewById(R.id.new_device_units_spinner);
        statusSpinner = findViewById(R.id.new_device_status_spinner);
        typesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sensorTypes);
        lightAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lightUnits);
        tempAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tempUnits);
        pressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pressUnits);
        statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusOptions);

        typeSpinner.setAdapter(typesAdapter);
        statusSpinner.setAdapter(statusAdapter);

        iotDevice = getIntent().getParcelableExtra("iotDevice");
        name = findViewById(R.id.new_device_name);
        topic = findViewById(R.id.new_device_topic);
        name.setText(iotDevice.getDeviceName());
        topic.setText(iotDevice.getMqttTopic());

        typesAdapter.setDropDownViewResource(R.layout.spinner_list);
        lightAdapter.setDropDownViewResource(R.layout.spinner_list);
        tempAdapter.setDropDownViewResource(R.layout.spinner_list);
        pressAdapter.setDropDownViewResource(R.layout.spinner_list);
        statusAdapter.setDropDownViewResource(R.layout.spinner_list);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0:
                        unitsSpinner.setAdapter(tempAdapter);
                        iotDevice.setDeviceType("Temperature Sensor");
                        break;
                    case 1:
                        unitsSpinner.setAdapter(pressAdapter);
                        iotDevice.setDeviceType("Pressure Sensor");
                        break;
                    case 2:
                        unitsSpinner.setAdapter(lightAdapter);
                        iotDevice.setDeviceType("Light Sensor");
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        spinner_unit_count = 0;

        unitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SpinnerAdapter adapter = unitsSpinner.getAdapter();
                if (adapter.equals(tempAdapter)) {
                    switch (position) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        default:
                            break;
                    }
                } else if (adapter.equals(pressAdapter)) {
                    switch (position) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        default:
                            break;
                    }
                } else if (adapter.equals(lightAdapter)) {
                    switch (position) {
                        case 0:
                            break;
                        default:
                            break;
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        statusSpinner.setSelection(iotDevice.getStatus());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBtnModify_Click(View caller) {
        iotDevice.setDeviceName(String.valueOf(name.getText()));
        iotDevice.setMqttTopic(String.valueOf(topic.getText()));
        iotDevice.setDeviceType(typeSpinner.getSelectedItem().toString());
        iotDevice.setUnit(unitsSpinner.getSelectedItem().toString());
        iotDevice.setStatus(Integer.parseInt(statusSpinner.getSelectedItem().toString()));

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String unit = iotDevice.getUnit();
        int userId = iotDevice.getUserId();
        String mqttTopic = iotDevice.getMqttTopic();
        String deviceType = iotDevice.getDeviceType();
        String deviceName = iotDevice.getDeviceName();
        int status = iotDevice.getStatus();
        String previousmeasurement = iotDevice.getMeasurement();
        int deviceid = iotDevice.getDeviceId();

        String POST_URL = "https://studev.groept.be/api/a23PT103/modifyDevice/";

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding new device, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest submitRequest = new StringRequest(
                Request.Method.POST,
                POST_URL,
                response -> {
                    progressDialog.setMessage("Device added successfully. Redirecting...");

                    Intent intent = new Intent(ModifyDeviceActivity.this, IoTOverviewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("userId", iotDevice.getUserId());
                    startActivity(intent);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(ModifyDeviceActivity.this, "Failed to modify device: " + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mqtttopic", mqttTopic);
                params.put("devicetype", deviceType);
                params.put("userid", String.valueOf(userId));
                params.put("devicename", deviceName);
                params.put("status", String.valueOf(status));
                params.put("previousmeasurement", previousmeasurement);
                params.put("unit", unit);
                params.put("deviceid", String.valueOf(deviceid));
                return params;
            }
        };
        requestQueue.add(submitRequest);
    }
    public void onBtnDelete_Click(View caller) {
        int deviceId = iotDevice.getDeviceId();
        String url = "https://studev.groept.be/api/a23PT103/deleteDevice/" + deviceId;

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting device, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest deleteRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(ModifyDeviceActivity.this, "Device deleted successfully.", Toast.LENGTH_LONG).show();
                    this.finish();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(ModifyDeviceActivity.this, "Failed to delete device: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        );
        requestQueue.add(deleteRequest);
    }
}
