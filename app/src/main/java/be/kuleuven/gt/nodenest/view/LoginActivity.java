package be.kuleuven.gt.nodenest.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import be.kuleuven.gt.nodenest.R;
import be.kuleuven.gt.nodenest.controller.MqttCallbackImpl;
import be.kuleuven.gt.nodenest.model.MQTTReceiver;
import be.kuleuven.gt.nodenest.model.MQTTconnection;
import be.kuleuven.gt.nodenest.model.Session;
import be.kuleuven.gt.nodenest.model.userLogin;

public class LoginActivity extends AppCompatActivity implements MQTTReceiver {

    private EditText username;
    private EditText password;
    private Button btnLogin;
    private Button btnRegister;
    private int id;

    //MQTT
    private Session session;
    MqttCallbackImpl mqttCallback;
    //MQQ end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        username = findViewById(R.id.Username);
        password = findViewById(R.id.Password);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    public void onBtnLogin_Click(View Caller) {
        String usernameInput = username.getText().toString();
        String passwordInput = hashPassword(password.getText().toString());

        if (!validateLoginInputs(usernameInput, passwordInput)) {
            return;
        }
        checkCredentials(usernameInput, passwordInput);
    }
    //rules so we dont have spaces
    private boolean validateLoginInputs(String username, String password) {
        if (username.contains(" ") || password.contains(" ")) {
            Toast.makeText(this, "Spaces are not allowed in username or password.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void checkCredentials(String username, String hashedPassword) {
        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Logging in, please wait...");
        progressDialog.show();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "https://studev.groept.be/api/a23PT103/checkUserLogin";

        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() > 0) {
                            JSONObject user = jsonArray.getJSONObject(0);
                            String actualPassword = user.getString("Password");
                            if (actualPassword.equals(hashedPassword)) {

                                //MQTT
                                mqttCallback = new MqttCallbackImpl(LoginActivity.this);


                                MQTTconnection.username = "rw";
                                MQTTconnection.password = "readwrte".toCharArray();
                                MQTTconnection.url = "test.mosquitto.org";
                                MQTTconnection.port = 1883;

                                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                                //mqttConnectOptions.setUserName("rw");
                                //mqttConnectOptions.setPassword("readwrite".toCharArray());
                                while (!mqttCallback.connect("test.mosquitto.org", 1883, mqttConnectOptions)){
                                    String TAG = "TAGmqtt";
                                    Log.d(TAG, "connect failed");
                                }
                                session = session.getInstance(mqttCallback);
                                //MQTT end

                                id = user.getInt("UserID");
                                userLogin login = new userLogin(username, hashedPassword, id);
                                Intent intent = new Intent(this, IoTOverviewActivity.class);
                                intent.putExtra("user", login);
                                startActivity(intent);
                            } else {
                                Toast.makeText(LoginActivity.this, "Invalid username or password.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "No user found.", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this, "Error parsing server response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Unable to communicate with the server: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", hashedPassword);
                return params;
            }
        };
        requestQueue.add(postRequest);
    }

    //hashing password with a salt
    private String hashPassword(String passwordToHash) {
        String hashedPass = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update("fjksqdkjqjdlnjdqnd".getBytes());
            byte[] bytes = md.digest(passwordToHash.getBytes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                hashedPass = Base64.getEncoder().encodeToString(bytes);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedPass;
    }

    public void onBtnRegister_Click(View Caller) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void ReceiveFromMQTT(String topic, String payload) {
    }

    @Override
    public void RetryConnection() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setUserName(MQTTconnection.username);
//        mqttConnectOptions.setPassword(MQTTconnection.password);
        while (!mqttCallback.connect(MQTTconnection.url, MQTTconnection.port, mqttConnectOptions)){
        }
    }
}
