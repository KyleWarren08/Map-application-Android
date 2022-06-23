package com.example.localguide;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.localguide.models.Favourite;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private View mapView;
    private Button btnFind, btnSettings, btnFavourites, btnMoreOptions;
    private double end_latitude, end_longitude;
    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    private String distance;

    private ConstraintLayout constraintLayout;
    private TextView tvDuration, tvRouteDistance;

    private ImageView imageViewFav, imageViewClose;
    private String searchQuery;

    private LatLng favPlaces;
    private String favPlacesName;

    private RippleBackground rippleBackground;


    private final float DEFAULT_ZOOM = 13;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //materialSearchBar = findViewById(R.id.searchBar);
        btnFind = findViewById(R.id.btnFind);
        btnSettings = findViewById(R.id.btnSettings);
        btnFavourites = findViewById(R.id.btnFavourites);
        btnMoreOptions = findViewById(R.id.btnMoreOptions);
        imageViewFav = findViewById(R.id.imageViewFav);
        //imageViewClose = findViewById(R.id.imageViewClose);
        tvDuration = findViewById(R.id.textViewDuration);
        tvRouteDistance = findViewById(R.id.textViewDistance);
        constraintLayout = findViewById(R.id.PlaceInformation);

        imageViewFav.setVisibility(View.INVISIBLE);

        //imageViewClose.setOnClickListener(v -> constraintLayout.setVisibility(View.INVISIBLE));

        polylines = new ArrayList<>();

        String uid = firebaseAuth.getInstance().getUid();


        imageViewFav.setOnClickListener(v -> {

            double lat = favPlaces.latitude;
            double lng = favPlaces.longitude;

            Favourite favouritePlaces = new Favourite(lat, lng, favPlacesName);

            FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Favourite").push()
                    .setValue(favouritePlaces)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Place saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed Saving the places", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

            //button is set to being visible
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("searchQuery") != null) {
                        searchQuery = map.get("searchQuery").toString();
                        //btnFind.setVisibility(View.VISIBLE);
                        btnFind.setText("Find " + searchQuery);
                    } else {
                        btnFind.setVisibility(View.INVISIBLE);
                    }
                    if (map.get("distanceIn") != null) {
                        distance = map.get("distanceIn").toString();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnSettings.setOnClickListener(v -> {
            Intent settingIntent = new Intent(DashboardActivity.this, SettingActivity.class);
            startActivity(settingIntent);

        });

        btnFavourites.setOnClickListener(v -> {
            Intent favouritesIntent = new Intent(DashboardActivity.this, FavouritesActivity.class);
            startActivity(favouritesIntent);

        });

        btnMoreOptions.setOnClickListener(v -> {
            if(btnFind.getVisibility() == View.INVISIBLE && btnFavourites.getVisibility() == View.INVISIBLE && btnSettings.getVisibility() == View.INVISIBLE){
                btnFind.setVisibility(View.VISIBLE);
                btnSettings.setVisibility(View.VISIBLE);
                btnFavourites.setVisibility(View.VISIBLE);
            }
            else if(btnFind.getVisibility() == View.VISIBLE && btnFavourites.getVisibility() == View.VISIBLE && btnSettings.getVisibility() == View.VISIBLE){
                    btnFind.setVisibility(View.INVISIBLE);
                    btnSettings.setVisibility(View.INVISIBLE);
                    btnFavourites.setVisibility(View.INVISIBLE);
            }

        });

        rippleBackground = findViewById(R.id.rippleBg);

        btnFind.setOnClickListener(v -> {
            erasePolylines();
            rippleBackground.startRippleAnimation();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadDefaultPlaces();
                    rippleBackground.stopRippleAnimation();
                }
            }, 3000);

        });

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DashboardActivity.this);
        Places.initialize(DashboardActivity.this, getResources().getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapView = mapFragment.getView();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        if (mapView != null & mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);

        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //check if gps is enabled or not and request user to enable it
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(DashboardActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(DashboardActivity.this, locationSettingsResponse -> {
            getDeviceLocation();
        });

        task.addOnFailureListener(DashboardActivity.this, e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                try {
                    resolvableApiException.startResolutionForResult(DashboardActivity.this, 51);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            }
        });


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            double lat = extras.getDouble("lat");
            double lng = extras.getDouble("lng");
            String title = extras.getString("title");

            LatLng latLng = new LatLng(lat, lng);

            MarkerOptions options = new MarkerOptions();
            //set positions
            options.position(latLng);
            options.title(title);
            mMap.addMarker(options);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            getRouteToMarker(latLng);
        }


        mMap.setOnMarkerClickListener(marker -> {

                imageViewFav.setVisibility(View.VISIBLE);
                favPlaces = marker.getPosition();
                favPlacesName = marker.getTitle();
                getRouteToMarker(marker.getPosition());

                return false;

        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        } else {
            Toast.makeText(this, "Cannot get device location", Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mLastKnownLocation = task.getResult();
                if (mLastKnownLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                } else {
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setInterval(1000);
                    locationRequest.setFastestInterval(5000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            if (locationRequest == null) {
                                return;
                            }
                            mLastKnownLocation = locationResult.getLastLocation();

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                        }
                    };
                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                }
            } else {
                Toast.makeText(this, "Unable to get last location", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println(e.getMessage());
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortesRouteIndex) {
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();

        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //distanceText
            //distanceValue
            //durationText
            //durationValue

            //checking for shortest route
            if (i == shortesRouteIndex) {

                //In case of more than 5 alternative routes
                int colorIndex = i % COLORS.length;

                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(getResources().getColor(COLORS[colorIndex]));
                polyOptions.width(10 + i * 3);
                polyOptions.addAll(route.get(i).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylines.add(polyline);

                constraintLayout.setVisibility(View.VISIBLE);

                String distanceText = route.get(i).getDistanceText();
                System.out.println(distanceText);


                if (distanceText.contains("km")) {
                    System.out.println("distance contains km");
                    double miDistance;
                    String kmDistanceText;
                    miDistance = (route.get(i).getDistanceValue() / 1000) / 1.609;
                    kmDistanceText = route.get(i).getDistanceText();

                    if (distance.equals("Km")) {
                        System.out.println("database distance contains km");
                        tvRouteDistance.setText("Distance:" + kmDistanceText);
                    } else { System.out.println("database distance contains mi");
                        tvRouteDistance.setText("Distance:" + miDistance + " mi");
                    }


                } else {
                    System.out.println("distance contains mile");
                    double kmDistance;
                    String miDistanceText;
                    miDistanceText = route.get(i).getDistanceText();
                    kmDistance = (route.get(i).getDistanceValue() / 1000) * 1.609;
                    if (distance.equals("Km")) {
                        System.out.println("database distance contains km");
                        tvRouteDistance.setText("Distance:" + kmDistance + " km");
                    } else {
                        System.out.println("database distance contains mi");
                        tvRouteDistance.setText("Distance:" + miDistanceText);
                    }

                }

                tvDuration.setText("Driving Time : " + route.get(i).getDurationText());

            }

        }


    }

    @Override
    public void onRoutingCancelled() {

    }

    private class PlaceTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            try {
                data = downloadUrl(strings[0]);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            //Execute parser task
            new ParserTask().execute(s);
        }

    }

    private String downloadUrl(String string) throws IOException {
        //initialize url
        URL url = new URL(string);
        //initialize connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //connect connection
        connection.connect();
        //initialize input stream
        InputStream stream = connection.getInputStream();
        //initialize buffer reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //Initialize string builder
        StringBuilder builder = new StringBuilder();
        //Initialize string variable
        String line = "";
        //use while loop
        while ((line = reader.readLine()) != null) {
            //append line
            builder.append(line);
        }

        //get append data
        String data = builder.toString();
        //close reader
        reader.close();
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            //Create json parser class
            JsonParser jsonParser = new JsonParser();
            List<HashMap<String, String>> mapList = null;

            JSONObject object = null;
            try {
                object = new JSONObject(strings[0]);
                mapList = jsonParser.parseResult(object);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            mMap.clear();

            for (int i = 0; i < hashMaps.size(); i++) {
                HashMap<String, String> hashMap = hashMaps.get(i);
                double lat = Double.parseDouble(hashMap.get("lat"));
                double lng = Double.parseDouble(hashMap.get("lng"));

                String name = hashMap.get("name");
                LatLng latLng = new LatLng(lat, lng);

                MarkerOptions options = new MarkerOptions();
                //set positions
                options.title(name);
                options.position(latLng);
                mMap.addMarker(options);
            }

        }
    }


    private void getRouteToMarker(LatLng latLng) {
        LatLng currentPosition = new LatLng(end_latitude, end_longitude);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(currentPosition, latLng)
                .key(getResources().getString(R.string.google_maps_key))
                .build();
        routing.execute();
    }

    private void loadDefaultPlaces() {
        LatLng currentMarkerLocation = mMap.getCameraPosition().target;
        end_latitude = currentMarkerLocation.latitude;
        end_longitude = currentMarkerLocation.longitude;


        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + //url
                "?location=" + currentMarkerLocation.latitude + "," + currentMarkerLocation.longitude + //lat and long
                "&radius=5000" + //nearby radius
                "&types=" + searchQuery + //place type
                "&sensor=true" + //sensor
                "&key=" + getResources().getString(R.string.google_maps_key); //google map key


        new PlaceTask().execute(url);
    }

    private void erasePolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

}