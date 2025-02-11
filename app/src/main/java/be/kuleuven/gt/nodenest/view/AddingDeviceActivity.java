package be.kuleuven.gt.nodenest.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

//activity for adding a new device to the accounnt
public class AddingDeviceActivity extends AppCompatActivity{
    // form fields
    private TextView title;
    private EditText nameEditText;
    private Spinner typeSpinner;
    private Spinner unitsSpinner;
    private String[] sensorTypes;
    private String[] tempUnits;
    private String[] lightUnits;
    private String[] pressUnits;
    private ArrayAdapter<String> typesAdapter;
    private ArrayAdapter<String> tempAdapter;
    private ArrayAdapter<String> lightAdapter;
    private ArrayAdapter<String> pressAdapter;

    // information storage about the device and user
    private IotDevice iotDevice;
    private EditText name;
    private EditText topic;
    private int userId;
    private userLogin user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_device);

        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID.", Toast.LENGTH_SHORT).show();
            finish(); // Exit the activity if no valid user ID is found
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adding_device);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // action bar
        setSupportActionBar(findViewById(R.id.my_toolbar));
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);


        title = (TextView) findViewById(R.id.new_device_name_text);

        iotDevice = new IotDevice(0,"topic","type",0,"name",0,"0","0");
        name = (EditText) findViewById(R.id.new_device_name);
        topic = (EditText) findViewById(R.id.new_device_topic);

        // lists of possible values for spinners
        sensorTypes = new String[]{"Temperature Sensor", "Pressure Sensor", "Light Sensor"};
        tempUnits = new String[]{"°C", "°K", "°F"};
        lightUnits = new String[]{"Lux"};
        pressUnits = new String[]{"hPa","Pa","kPa"};

        // setting up spinners
        typeSpinner = (Spinner) findViewById(R.id.new_device_type_spinner);
        unitsSpinner = (Spinner) findViewById(R.id.new_device_units_spinner);
        typesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sensorTypes);
        lightAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lightUnits);
        tempAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tempUnits);
        pressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pressUnits);
        typeSpinner.setAdapter(typesAdapter);

        // setting up visuals for spinners
        typesAdapter.setDropDownViewResource(R.layout.spinner_list);
        lightAdapter.setDropDownViewResource(R.layout.spinner_list);
        tempAdapter.setDropDownViewResource(R.layout.spinner_list);
        pressAdapter.setDropDownViewResource(R.layout.spinner_list);

        // device type spinner handler
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                switch(position){
                    case 0:
                        unitsSpinner.setAdapter(tempAdapter); // setting possible units for units spinner
                        iotDevice.setDeviceType("Temperature Sensor");
                        break;
                    case 1:
                        unitsSpinner.setAdapter(pressAdapter); // setting possible units for units spinner
                        iotDevice.setDeviceType("Pressure Sensor");
                        break;
                    case 2:
                        unitsSpinner.setAdapter(lightAdapter); // setting possible units for units spinner
                        iotDevice.setDeviceType("Light Sensor");
                        break;
                }
            }
            public void onNothingSelected(AdapterView<?> parentView)
            {
            }
        });


        // units spinner handler
        unitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                SpinnerAdapter adapter = unitsSpinner.getAdapter();
                if (adapter.equals(tempAdapter)) {
                    switch(position) {
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
                    switch(position) {
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
                    switch(position) {
                        case 0:
                            break;
                        default:
                            break;
                    }

                }
            }
            public void onNothingSelected(AdapterView<?> parentView)
            {
            }
        });

    }

    // connecting return button in te upper menu
    @Override
    public boolean onOptionsItemSelected( @NonNull MenuItem item ) {

        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // saving device in the db
    public void onBtnAdd_Click(View view) {
        // getting values from input fields
        String deviceName = name.getText().toString();
        String mqttTopic = topic.getText().toString();
        String deviceType = typeSpinner.getSelectedItem().toString();
        String unit = unitsSpinner.getSelectedItem().toString();
        userId = getIntent().getIntExtra("userId", -1);
        user = (userLogin) getIntent().getParcelableExtra("user");

        if (deviceName.isEmpty() || mqttTopic.isEmpty()) {
            Toast.makeText(this, "Please fill all fields before adding the device.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding new device, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String POST_URL = "https://studev.groept.be/api/a23PT103/addDevice/";

        StringRequest submitRequest = new StringRequest(
                Request.Method.POST,
                POST_URL,
                response -> {
                    // Assuming the device was  successfully
                    progressDialog.setMessage("Device added successfully. Redirecting...");
                    Intent intent = new Intent(this, IoTOverviewActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddingDeviceActivity.this, "Failed to add device: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mqtttopic", mqttTopic);
                params.put("devicetype", deviceType);
                params.put("userid", String.valueOf(userId));
                params.put("devicename", deviceName);
                params.put("status", "1");
                params.put("previousmeasurement", "0");
                params.put("unit", unit);
                return params;
            }
        };
        requestQueue.add(submitRequest);
    }
}

