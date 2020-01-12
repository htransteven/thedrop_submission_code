package com.example.thedrop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.app.ActivityOptions;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import com.google.firebase.firestore.FirebaseFirestore;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int DEVICE_LOCATION_PERMISSION = 0;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView scan_result;
    TextView venmo_username;

    View mapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        scan_result = (TextView)findViewById(R.id.confirmation_code);
        venmo_username = (TextView)findViewById(R.id.current_username);
        venmo_username.setText("@" + getIntent().getExtras().getString("username"));

        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("notifications_channel", "Drop Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("drop_updates")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed to drop notifications";
                        if (!task.isSuccessful()) {
                            msg = "Subscription failed";
                        }
                        Log.d("sub-update", msg);
                        //Toast.makeText(MapsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void scanCode(View v) {
        Intent intent = new Intent(this,BarcodeDetect.class);
        startActivityForResult(intent, 0);
    }

    public void changeVenmo(View v) {
        Intent intent = new Intent(this,LoginScreen.class);
        intent.putExtra("changing_venmo", "true");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==0) {
            if(resultCode== CommonStatusCodes.SUCCESS){
                if(data!=null){
                     Barcode barcode = data.getParcelableExtra("barcode");
                    EditText confirmation_code = (EditText)findViewById(R.id.confirmation_code);
                    confirmation_code.setText(barcode.displayValue);

                    Timestamp current_time = Timestamp.now();
                    Log.v("found", "barcode: " + barcode.displayValue);
                    db.collection("drops")
                            .whereEqualTo("code", barcode.displayValue)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    Log.v("found", "Drop found");
                                    if (task.isSuccessful()) {
                                        Log.v("found", "Task Successful");
                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                            Log.v("found", "doc-id=" + doc.getId());
                                            if (doc.getData().get("status").toString().equals("active") && doc.getData().get("winner").toString().isEmpty()) {
                                                Log.v("found", "Enter if winner is empty");
                                                DocumentReference drop = db.collection("drops").document(doc.getId());
                                                Log.v("found", "winner: " + venmo_username.getText());
                                                drop.update("winner", venmo_username.getText().subSequence(1,venmo_username.getText().length()))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("drop-win", "Drop winner has been updated!");
                                                                Context context = getBaseContext();
                                                                CharSequence text = "Congratulations, you won!";
                                                                int duration = Toast.LENGTH_SHORT;

                                                                Toast toast = Toast.makeText(context, text, duration);
                                                                toast.show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w("drop-fail", "Error updating document", e);
                                                            }
                                                        });
                                                drop.update("status", "completed")
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                            }
                                                        });
                                            } else {
                                                Context context = getBaseContext();
                                                CharSequence text = "Sorry, someone got here before you.";
                                                int duration = Toast.LENGTH_SHORT;

                                                Toast toast = Toast.makeText(context, text, duration);
                                                toast.show();
                                            }
                                        }
                                    } else {
                                        Log.d("and-fs-e", "Error getting documents: ", task.getException());
                                    }
                                }
                            });

                } else {
                    //scan_result.setText("No barcode found");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this,R.raw.gmap_style);
        mMap.setMapStyle(mapStyleOptions);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);
            if (mapView != null &&
                    mapView.findViewById(Integer.parseInt("1")) != null) {
                // Get the button view
                View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                // and next place it, on bottom right (as Google Maps app)
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();
                // position on right bottom
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                locationButton.setTranslationX(-90);
                layoutParams.setMargins(0, 0, 0, 325);
            }
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    DEVICE_LOCATION_PERMISSION);
        }

        db.collection("drops")
                .whereEqualTo("status", "active")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("and-fs-f", "Listen failed.", e);
                            return;
                        }
                        mMap.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            if(doc.getData().get("status") != null) {
                                if (doc.getData().get("location") != null) {
                                    GeoPoint gp = (GeoPoint) doc.getData().get("location");
                                    LatLng dropLoc = new LatLng(gp.getLatitude(), gp.getLongitude());
                                    String dropName = "generic_drop";
                                    double dropPrize = 0.0;
                                    Marker marker_temp = mMap.addMarker(new MarkerOptions().title(dropName).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker)).position(dropLoc).snippet("Prize: $" + dropPrize));

                                    if (doc.getData().get("name") != null) {
                                        marker_temp.setTitle(doc.getData().get("name").toString());
                                    }
                                    if (doc.getData().get("prize")!= null) {
                                        marker_temp.setSnippet("Prize: $" + doc.getData().get("prize"));
                                    }
                                    Log.v("mkr_add", "Added Marker");
                                } else {
                                    Log.v("loc", "NULL LOCATION");
                                }
                            }
                        }
                    }
                });

    }
}
