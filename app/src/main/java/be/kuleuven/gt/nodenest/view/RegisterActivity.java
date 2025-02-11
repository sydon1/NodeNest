package be.kuleuven.gt.nodenest.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import be.kuleuven.gt.nodenest.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button btnRegister;
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
        username = (EditText) findViewById(R.id.registerUsername);
        password = (EditText) findViewById(R.id.registerPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnReturn = (Button) findViewById(R.id.btnReturn);
    }

    public void onBtnRegister_Click(View Caller) {
    }

    public void onBtnReturn_Click(View Caller) {
    }
}