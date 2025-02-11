package be.kuleuven.gt.nodenest.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import be.kuleuven.gt.nodenest.R;

public class RegisterActivity extends AppCompatActivity {
    private static final String POST_URL = "https://studev.groept.be/api/a23PT103/registerUser/";
    private static final String GET_URL = "https://studev.groept.be/api/a23PT103/checkUser/";
    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        emailEditText = findViewById(R.id.email);
        usernameEditText = findViewById(R.id.registerUsername);
        passwordEditText = findViewById(R.id.registerPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        btnReturn = findViewById(R.id.btnReturn);

        btnRegister.setOnClickListener(view -> createCheckUserJSON());
    }
    //check if user already exists, are there spaces etc.
    public void createCheckUserJSON() {
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = hashPassword(passwordEditText.getText().toString());

        if (checkSpaces(username, password, email)) {
            return; // Stop the process if validation fails
        }

        ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage("Registering, please wait...");
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, GET_URL + username, null,
                response -> {
                    progressDialog.dismiss();
                    if (response.length() > 0) {
                        Toast.makeText(RegisterActivity.this, "User already exists.", Toast.LENGTH_LONG).show();
                    } else {
                        registerUser(username, password, email);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Unable to communicate with the server", Toast.LENGTH_LONG).show();
                }
        );
        requestQueue.add(getRequest);
    }
    //code to register user
    public void registerUser(String username, String hashedPassword, String email) {
        if (checkSpaces(username, hashedPassword, email)) {
            return; // Stop the process if validation fails
        }

        ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage("Registering, please wait...");
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest submitRequest = new StringRequest(
                Request.Method.POST,
                POST_URL,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Registration failed. " + error, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", hashedPassword);
                params.put("email", email);
                return params;
            }
        };

        progressDialog.show();
        requestQueue.add(submitRequest);
    }

    private boolean checkSpaces(String username, String password, String email) {
        if (username.contains(" ") || password.contains(" ") || email.contains(" ")) {
            Toast.makeText(this, "Spaces are not allowed in username, password, or email.", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

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

    public void onBtnReturn_Click(View Caller) {
        this.finish();
    }
}
