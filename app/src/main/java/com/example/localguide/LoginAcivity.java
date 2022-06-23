package com.example.localguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginAcivity extends AppCompatActivity {

    private TextView signup;
    private EditText email, password;
    private Button signin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_acivity);
        getSupportActionBar().hide();
        signup = findViewById(R.id.textViewLRegister);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        signin = findViewById(R.id.btnLLogin);
        progressBar = findViewById(R.id.progress_Login);

        progressBar.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        signin.setOnClickListener(v -> {
            loginUser();
        });

        signup.setOnClickListener(v -> {
            Intent registerIntent = new Intent(LoginAcivity.this, RegisterActivity.class);
            startActivity(registerIntent);
            finish();
        });

    }

    private void loginUser() {
        String myEmail = email.getText().toString().trim();
        String myPassword = password.getText().toString().trim();

        if (myEmail.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(myEmail).matches()) {
            email.setError("Please provide valid email");
            email.requestFocus();
            return;
        }
        if (myPassword.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return;
        }
        if (myPassword.length() < 6) {
            password.setError("Min password length should be 6 characters");
            password.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(myEmail, myPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressBar.setVisibility(View.GONE);
                Intent dashboardIntent = new Intent(LoginAcivity.this, DashboardActivity.class);
                startActivity(dashboardIntent);
                finish();

            }
        })
                .addOnFailureListener(e ->

                        Toast.makeText(LoginAcivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
        progressBar.setVisibility(View.GONE);
    }
}