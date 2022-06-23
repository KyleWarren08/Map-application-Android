package com.example.localguide;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localguide.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private TextView login;
    private ProgressBar progressBar;
    private EditText fullname, email, password, phone;
    private Button register, genPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
        login = findViewById(R.id.textviewRlogin);
        progressBar = findViewById(R.id.progressRegister);
        fullname = findViewById(R.id.editTextRFullname);
        email = findViewById(R.id.editTextREmail);
        password = findViewById(R.id.editTextRPassword);
        phone = findViewById(R.id.editTextRNumber);
        register = findViewById(R.id.btnRRegister);
        genPassword=findViewById(R.id.btnGenPassword);

        mAuth = FirebaseAuth.getInstance();
        getSupportActionBar().hide();
        progressBar.setVisibility(View.GONE);
        register.setOnClickListener(v -> registerUser());
        genPassword.setOnClickListener(v->generatePassword());

        login.setOnClickListener(v -> {
            Intent loginIntent = new Intent(RegisterActivity.this, LoginAcivity.class);
            startActivity(loginIntent);
            finish();
        });

    }

    private void generatePassword(){
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(7)+10;
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(93) + 33);
            randomStringBuilder.append(tempChar);
        }
        password.setText(randomStringBuilder.toString());
        Context context = getApplicationContext();
        CharSequence text = "Password set to: "+randomStringBuilder.toString();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void registerUser() {

        String myName = fullname.getText().toString().trim();
        String myEmail = email.getText().toString().trim();
        String myPhone = phone.getText().toString().trim();
        String myPassword = password.getText().toString().trim();

        if (myName.isEmpty()) {
            fullname.setError("Full name is required");
            fullname.requestFocus();
            return;
        }
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

        if (!Patterns.PHONE.matcher(myPhone).matches()) {
            phone.setError("Please provide valid Phone number");
            phone.requestFocus();
            return;
        }
        if (myPhone.isEmpty()) {
            phone.setError("Phone no is required");
            phone.requestFocus();
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
        mAuth.createUserWithEmailAndPassword(myEmail, myPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User(myName, myPhone, "Km", "bank");
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(mAuth.getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    Intent dashboardIntent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                    startActivity(dashboardIntent);
                                    finish();
                                }
                            }
                        });
                    }

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}