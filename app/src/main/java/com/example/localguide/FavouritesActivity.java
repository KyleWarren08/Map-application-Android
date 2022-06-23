package com.example.localguide;

import android.content.Intent;
import android.os.Bundle;

import com.example.localguide.models.Favourite;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth firebaseAuth;
    private ListView listFav;


    List<Favourite> favourites = new ArrayList<>();

    List<Double> latList = new ArrayList<>();
    List<Double> lngList = new ArrayList<>();

    List<String> listPlace = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        listFav = findViewById(R.id.listFav);


        listFav.setOnItemClickListener((parent, view, position, id) -> {

            Intent intent = new Intent(FavouritesActivity.this, DashboardActivity.class);

            intent.putExtra("lat", latList.get(position));
            intent.putExtra("lng", lngList.get(position));
            intent.putExtra("title", listPlace.get(position));
            startActivity(intent);
            finish();
        });

        String uid = firebaseAuth.getInstance().getUid();


        assert uid != null;
        DatabaseReference root = db.getReference().child("Users").child(uid).child("Favourite");


        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Favourite favourite = dataSnapshot.getValue(Favourite.class);
                    favourites.add(favourite);
                }


                for (int i = 0; i < favourites.size(); i++) {

                    listPlace.add(favourites.get(i).getTitle());
                    latList.add(favourites.get(i).getLat());
                    lngList.add(favourites.get(i).getLng());

                    System.out.println(favourites.get(i).getTitle());
                }
                ArrayAdapter<String> adapterFav = new ArrayAdapter<String>(FavouritesActivity.this, android.R.layout.simple_dropdown_item_1line, listPlace);
                listFav.setAdapter(adapterFav);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavouritesActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}