package com.example.localguide;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.localguide.models.Favourite;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    private ImageView imageBack;
    private TextView tvSetting;
    private DatabaseReference mDatabase;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();

    private FirebaseAuth firebaseAuth;
    private Spinner distance;
    private Spinner searchQuery;
    private TextView tvDistance, tvPlaces;
    private ListView listFav;

    private Button btnSave;
    private Button btnLogout;

    private String distanceIn = null;
    private String placeBy = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        imageBack = findViewById(R.id.imageBack);
        tvSetting = findViewById(R.id.tvSettings);
        distance = findViewById(R.id.spinnerDistance);
        searchQuery = findViewById(R.id.spinnerSearchQuery);
        tvDistance = findViewById(R.id.tvDistane);
        tvPlaces = findViewById(R.id.tvPlaces);
        listFav = findViewById(R.id.listFav);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        getSupportActionBar().hide();


        String uid = firebaseAuth.getInstance().getUid();


        assert uid != null;
        DatabaseReference root = db.getReference().child("Users").child(uid).child("Favourite");


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.distance, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distance.setAdapter(adapter);


        distance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                distanceIn = parent.getItemAtPosition(position).toString();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(SettingActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }
        });

        //Place picker dropdown menu. //naming of array in strings is important
        ArrayAdapter<CharSequence> sequenceArrayAdapter = ArrayAdapter.createFromResource(this, R.array.searchQuery, android.R.layout.simple_spinner_item);
        sequenceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchQuery.setAdapter(sequenceArrayAdapter);
        searchQuery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                placeBy = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(SettingActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }
        });


        btnSave.setOnClickListener(v -> {
            if (distanceIn != null) {
                mDatabase.child("distanceIn").setValue(distanceIn);
                Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show();

            }
            if (placeBy != null) {
                mDatabase.child("searchQuery").setValue(placeBy);
                Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show();
            }

            Intent mapIntent = new Intent(SettingActivity.this, DashboardActivity.class);
            startActivity(mapIntent);
            finish();
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingActivity.this, LoginAcivity.class);
            startActivity(intent);
            finish();
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("fullname") != null) {
                        tvSetting.setText("Hi, " + map.get("fullname"));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        imageBack.setOnClickListener(v -> {
            Intent mapIntent = new Intent(SettingActivity.this, DashboardActivity.class);
            startActivity(mapIntent);
            finish();
        });
    }

}