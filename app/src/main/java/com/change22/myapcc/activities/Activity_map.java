package com.change22.myapcc.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.change22.myapcc.R;
import com.change22.myapcc.config.GPSTracker1;
import com.change22.myapcc.config.HttpHandler;
import com.change22.myapcc.config.MyApp;
import com.change22.myapcc.database.TABLE_TRUCK;
import com.change22.myapcc.dtoModel.DTOMarkerData;
import com.change22.myapcc.dtoModel.NewTruckDTO;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.maps.model.Marker.*;
import static java.util.stream.Collectors.toList;

public class Activity_map extends FragmentActivity implements OnMapReadyCallback {
    Context context = null;
    double latitude = 0, longitude = 0;
    ArrayList<DTOMarkerData> markerDataArrayList = null;
    ImageView img_legend;
    TextView txt_search_location = null;
    CardView cardView = null;
    int health_count = 1;
    //  private com.change22.myapcc.activities.SlidingPanel popup;
    private Animation animShow, animHide;
    RelativeLayout rlayout_header_menu = null;
    String fontPathBold = "roboto_regular_bold.ttf";
    // Loading Font Face
    Typeface tf_bold;
    ImageButton btn_search = null;
    // Applying font
    // Font path
    String fontPathRegular = "roboto_regular.ttf";
    // Loading Font Face
    Typeface tf_regular;
    private ProgressDialog progressDialog = null;
    Marker userMarker = null, searchMarker;
    ArrayList<Marker> arrMarker = null;
    GoogleMap mGoogleMap;
    public static Dialog dialog1;
    GPSTracker1 new_gps = null;
    Marker marker = null;
    Marker marker1 = null;
    int delay = 0; // delay for 0 sec.
    int period = 10000; // repeat every 10 sec.

    public static Timer timer = new Timer();


    MarkerOptions a;
    static ArrayList<NewTruckDTO> newTruckDTOS = new ArrayList<>();
//    ArrayList<NewTruckDTO>  newTruckDTOS12 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_map);
        context = this;
        initComponents();
        initComponentListener();

        new getTruckData_new().execute();


     //   if(Activity_dashboard.isPaused== true) {
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    new getTruckData_new().execute();

                    Log.d("", "runMethod evey second : ");

                }
            }, delay, period);


      //  }


        markerDataArrayList = MyApp.db.getMarkersList();
        try {
            MyApp.log("Activity_map", "In try of Map Fragment initialization");
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        } catch (Exception e) {
            latitude = 0.0;
            longitude = 0.0;
        }
        setMarkerData();

    }






    private void setMarkerData() {
    }

    private void initComponents() {
        tf_bold = Typeface.createFromAsset(context.getAssets(), fontPathBold);
        tf_regular = Typeface.createFromAsset(context.getAssets(), fontPathRegular);
        btn_search = (ImageButton) findViewById(R.id.btn_search);
        img_legend = (ImageView) findViewById(R.id.img_legend);
        // popup = (SlidingPanel) findViewById(R.id.popup_window);
        animShow = AnimationUtils.loadAnimation(this, R.anim.popup_show);
        animHide = AnimationUtils.loadAnimation(this, R.anim.popup_hide);
        rlayout_header_menu = (RelativeLayout) findViewById(R.id.rlayout_header_menu);
        txt_search_location = (TextView) findViewById(R.id.txt_search_location);
        txt_search_location.setTypeface(tf_bold);
        cardView = (CardView) findViewById(R.id.cardView);

    }

    private void initComponentListener() {
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call_search_activity();
                //finish();
            }
        });
        img_legend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rlayout_header_menu.getVisibility() == View.VISIBLE) {
                   /* popup.startAnimation(animHide);
                    popup.setVisibility(View.GONE);*/
                    rlayout_header_menu.setVisibility(View.GONE);
                } else {
                    /*popup.setVisibility(View.VISIBLE);
                    popup.startAnimation(animShow);*/
                    rlayout_header_menu.setVisibility(View.VISIBLE);

                }

                /*View v1 = img_legend;
                PopupMenu popup = new PopupMenu(Activity_map.this, v1);
                popup.getMenuInflater().inflate(R.menu.menu_map_legend, popup.getMenu());

                popup.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.red));
                popup.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.blue));
                popup.getMenu().getItem(2).setIcon(getResources().getDrawable(R.drawable.green));

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        return false;
                    }
                });

                popup.show();*/
            }
        });
        txt_search_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call_search_activity();
            }
        });
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call_search_activity();
            }
        });
    }

    private void call_search_activity() {
        //if (MyApp.get_session(MyApp.SESSION_FETCHING_LOCATIONS).equalsIgnoreCase("N")) {
        Intent intent = new Intent(Activity_map.this, Activity_search.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        /*} else {
            final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(getResources().getString(R.string.app_name));
            alertDialog.setMessage(getResources().getString(R.string.fetching_data_msg));
            alertDialog.setIcon(R.mipmap.ic_launcher);

            alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
        }*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
       // loadMarker();
        //   new getTruckData_new().execute();
        if (!MyApp.get_session(MyApp.SESSION_USER_AREA).equalsIgnoreCase("")) {
            MyApp.log("Activity_map", "In set user marker of onMapReady");
            String myAddress = MyApp.get_session(MyApp.SESSION_USER_AREA);
            txt_search_location.setText(myAddress);
            double userLat = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LATITUDE));
            double userLog = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LONGITUDE));
            userMarker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(userLat, userLog))
                    .anchor(0.5f, 0.5f)
                    .title("You Are Here")
                    .snippet(myAddress)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.your_pic)));
            userMarker.showInfoWindow();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    userMarker.hideInfoWindow();
                }
            }, 4000);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLat, userLog), 13.0f));
        }
        if (MyApp.get_session(MyApp.SESSION_FROM_SEARCH).equalsIgnoreCase("Y")) {
            MyApp.log("Activity_map", "In set search marker of onMapReady");
            String searchedAddress = MyApp.get_session(MyApp.SESSION_SEARCH_AREA);
            txt_search_location.setText(searchedAddress);
            double userLat = Double.parseDouble(MyApp.get_session(MyApp.SESSION_SEARCH_LATITUDE));
            double userLog = Double.parseDouble(MyApp.get_session(MyApp.SESSION_SEARCH_LONGITUDE));
            searchMarker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(userLat, userLog))
                    .anchor(0.5f, 0.5f)
                    .title(searchedAddress));
            searchMarker.showInfoWindow();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchMarker.hideInfoWindow();
                }
            }, 4000);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLat, userLog), 13.0f));
            MyApp.set_session(MyApp.SESSION_FROM_SEARCH, "");
        }
    }




/*
    private  void refresh(int milliseconds) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                   new getTruckData_new().execute();



                Log.d("", "run NEwwwww: ");
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }
*/

    public class getTruckData_new extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //   Toast.makeText(MyApp.this, "Json Data is downloading", Toast.LENGTH_LONG).show();
            // refresh(1000);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "http://148.251.194.247/VTS/webservice?token=getLiveData&user=apccaditya&pass=445566&format=json";
            String jsonStr = sh.makeServiceCall(url);

            /*  newTruckDTOS = new ArrayList<>();*/
//            ArrayList<NewTruckDTO>  newTruckDTOS = new ArrayList<>();
            Log.e("", "Response from url12345: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONObject jsonResult = jsonObject.getJSONObject("root");
                    JSONArray jsonArray = jsonResult.getJSONArray("VehicleData");
                    Log.d("", "doInBackgroundArray: " + jsonArray);
                    if (arrMarker == null)
                        arrMarker = new ArrayList<>();
                    else
                        arrMarker.clear();
                    if (jsonArray.length() > 0) {
                        markerDataArrayList = MyApp.db.getMarkersList();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            final String truck_no_new = jsonObject1.getString("Vehicle_No");
                            final Double lati = jsonObject1.getDouble("Latitude");
                            final Double longi = jsonObject1.getDouble("Longitude");
                            NewTruckDTO newTruckDTOmodel = new NewTruckDTO();
                            // String id = jsonObject1.getString("id");
                            newTruckDTOmodel.setVehicle_No(truck_no_new);
                            newTruckDTOmodel.setLatitude(lati);
                            newTruckDTOmodel.setLongitude(longi);
                            newTruckDTOS.add(newTruckDTOmodel);

                            if (markerDataArrayList.size() >= 0 || newTruckDTOS.size() >= 0) {
                                for (int ii = 0; ii < markerDataArrayList.size(); ii++) {
                                    Log.w("truck_no_new",truck_no_new);
                                    Log.w("truck_no_old",markerDataArrayList.get(ii).getTitle());
                                    if (truck_no_new.equalsIgnoreCase(markerDataArrayList.get(ii).getTitle())) {
                                        markerDataArrayList.get(ii).setLatitude(lati);
                                        markerDataArrayList.get(ii).setLongitude(longi);
                                       // markerDataArrayList.get(ii).setTitle(""+ii);
                                    }

                                }

                            } else {
                            }




                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addMarkers(markerDataArrayList);
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.e("", "Json parsing error: " + e.getMessage());

                }

            } else {
                Log.e("", "Couldn't get json from server.");

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

   public void setupUserMarker(){
       //   new getTruckData_new().execute();
       if (!MyApp.get_session(MyApp.SESSION_USER_AREA).equalsIgnoreCase("")) {
           MyApp.log("Activity_map", "In set user marker of onMapReady");
           String myAddress = MyApp.get_session(MyApp.SESSION_USER_AREA);
           txt_search_location.setText(myAddress);
           double userLat = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LATITUDE));
           double userLog = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LONGITUDE));
           userMarker = mGoogleMap.addMarker(new MarkerOptions()
                   .position(new LatLng(userLat, userLog))
                   .anchor(0.5f, 0.5f)
                   .title("You Are Here")
                   .snippet(myAddress)
                   .icon(BitmapDescriptorFactory.fromResource(R.drawable.your_pic)));
       }
       if (MyApp.get_session(MyApp.SESSION_FROM_SEARCH).equalsIgnoreCase("Y")) {
           MyApp.log("Activity_map", "In set search marker of onMapReady");
           String searchedAddress = MyApp.get_session(MyApp.SESSION_SEARCH_AREA);
           txt_search_location.setText(searchedAddress);
           double userLat = Double.parseDouble(MyApp.get_session(MyApp.SESSION_SEARCH_LATITUDE));
           double userLog = Double.parseDouble(MyApp.get_session(MyApp.SESSION_SEARCH_LONGITUDE));
           searchMarker = mGoogleMap.addMarker(new MarkerOptions()
                   .position(new LatLng(userLat, userLog))
                   .anchor(0.5f, 0.5f)
                   .title(searchedAddress));
          MyApp.set_session(MyApp.SESSION_FROM_SEARCH, "");
       }
    }

    public void addMarkers(ArrayList<DTOMarkerData> markerDataArrayList) {
        mGoogleMap.clear();
        if (userMarker!=null) {
            userMarker.remove();
        }
        setupUserMarker();
        for (int i = 0; i < markerDataArrayList.size(); i++) {
            a = new MarkerOptions()
                    .position(new LatLng(markerDataArrayList.get(i).getLatitude(), markerDataArrayList.get(i).getLongitude()))
                  //  .title("" + markerDataArrayList.get(i).getTitle()+""+markerDataArrayList.get(i).getSnippet())
                    .title("" + i)

                    .icon(BitmapDescriptorFactory.fromResource(markerDataArrayList.get(i).getImg_id()));
            Marker my_marker = mGoogleMap.addMarker(a);
            arrMarker.add(my_marker);

            mGoogleMap.setInfoWindowAdapter(new BalloonAdapter(getLayoutInflater()));

        }

    }

    private void loadMarker() {
        if (markerDataArrayList != null) {
            MyApp.log("Activity_map", "array list size is " + markerDataArrayList.size());
            markerDataArrayList.clear();
        }
        markerDataArrayList = MyApp.db.getMarkersList();


      /*  if (arrMarker == null)
            arrMarker = new ArrayList<>();
        else
            arrMarker.clear();*/
        if (markerDataArrayList.size() >= 0 || newTruckDTOS.size() >= 0) {
            for (int i = 0; i < markerDataArrayList.size(); i++) {
                DTOMarkerData dtoMarkerData = markerDataArrayList.get(i);
//                    for (int j = 0; j < newTruckDTOS.size(); j++) {
//                        String newNo= newTruckDTOS.get(j).getVehicle_No();
//                        String oldNo = dtoMarkerData.getTitle();
//                        if(newNo .equalsIgnoreCase(oldNo))
//                        {
                latitude = dtoMarkerData.getLatitude();
                longitude = dtoMarkerData.getLongitude();
                String title = dtoMarkerData.getTitle();
                String snippet = dtoMarkerData.getSnippet();
                int img_id = dtoMarkerData.getImg_id();
                //  mGoogleMap.setInfoWindowAdapter(new BalloonAdapter(getLayoutInflater()));

                          /*  marker = mGoogleMap.addMarker(new MarkerOptions()

                                    .position(new LatLng(latitude, longitude))
                                    .title("" + i)
                                    .icon(BitmapDescriptorFactory.fromResource(img_id)));

                          //  mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13.0f));

                            arrMarker.add(marker);*/
//                        }
//                    }
            }
            // }
            //   }
            mGoogleMap.setInfoWindowAdapter(new BalloonAdapter(getLayoutInflater()));
            /*Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(18.504028, 73.944611))
                    .title("-2")
                    .snippet("1")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.plus_red)));
            arrMarker.add(marker);

            mGoogleMap.setInfoWindowAdapter(new BalloonAdapter(getLayoutInflater()));
            Marker marker1 = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(18.504130, 73.946897))
                    .title("-2")
                    .snippet("2")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.plus_red)));
            arrMarker.add(marker1);*/
        } else {
            Toast.makeText(context, "Emoty", Toast.LENGTH_SHORT).show();
        }

    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyApp.log("ActivityMap->message received");
            // Extract data included in the Intent
            //String service = intent.getStringExtra("service");
            if (intent != null) {
                if (intent.hasExtra("Flag")) {
                    // Toast.makeText(context, "Receive", Toast.LENGTH_SHORT).show();
                    if (intent.getStringExtra("Flag").equalsIgnoreCase("getTruckData")) {
                      /*if (arrMarker != null) {
                          int Size = arrMarker.size();
                          for (int i = 0; i < Size; i++) {
                              arrMarker.get(i).remove();
                          }
                      }
                      ();*/
                    } else if (intent.getStringExtra("Flag").equalsIgnoreCase("truck_location")) {
                        MyApp.log("Flag->truck_location");
                        double Lat = 0.0;
                        double Log = 0.0;
                        if (intent.hasExtra("lattitude")) {
                            Lat = Double.parseDouble(intent.getStringExtra("lattitude"));
                        }
                        if (intent.hasExtra("longitude")) {
                            Log = Double.parseDouble(intent.getStringExtra("longitude"));
                        }
                        String str_truck_no = "";
                        if (intent.hasExtra("truck_no")) {
                            str_truck_no = intent.getStringExtra("truck_no");
                        }
                        MyApp.log("Flag->Bundle");
                        MyApp.log_bundle(intent.getExtras());
                        MyApp.log("Activity_map", "marketDataArrayList" + markerDataArrayList);
                        if (markerDataArrayList != null) {
                            int size = markerDataArrayList.size();
                            MyApp.log("Activity_map", "markerDataArrayListSize->" + size);
                            for (int i = 0; i < size; i++) {
                                DTOMarkerData dtoMarkerData = markerDataArrayList.get(i);
                                if (dtoMarkerData.getTitle().equalsIgnoreCase(str_truck_no)) {
                                    MyApp.log("Activity_map", "markerDataArrayListSize->" + i);
                                    MyApp.log("Activity_map", "markerDataArrayLatLog->" + Lat + ", " + Log);
                                    Marker marker = arrMarker.get(i);
                                    //marker.setPosition(new LatLng(Lat, Log));
                                    animateMarker(marker, new LatLng(Lat, Log), false);
                                    break;
                                }
                            }
                        }

                    }
                }
            }
        }
    };

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mGoogleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1000;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyApp.log("ActivityMap->messege recieved");
            // Extract data included in the Intent
            //String service = intent.getStringExtra("service");
            if (intent != null) {
                /*if(intent.hasExtra("Flag")) {
                Toast.makeText(context,"Receive",Toast.LENGTH_SHORT).show();
                }
                else*/
                {
                    String status = intent.getStringExtra("response_code");
                    String message = intent.getStringExtra("response_message");
                    MyApp.log("CheckSyncUp", "In broadcast receiver of map Activity");
                    MyApp.log("CheckSyncUp", "Status is " + status + " and message is " + message);
                    // Toast.makeText(context,"Status:"+status+"\nMessage:"+message,Toast.LENGTH_SHORT).show();
                    if (status != null) {
                        if (status.equals("0")) {
                            MyApp.log("CheckSyncUp", "In if status = 0 of broadcast receiver of map Activity");
                            if (progressDialog != null)
                                if (progressDialog.isShowing())
                                    progressDialog.hide();
                        }
                        if (status.equals("1") && message.equals("")) {
                            MyApp.log("CheckSyncUp", "In if status = 1 and message = BannerMaster of broadcast receiver of map Activity");
                            if (progressDialog != null)
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();

                        } else if (status.equals("3") && message.equals("location")) {
                            MyApp.log("CheckSyncUp", "In if status = 3 and message = location of broadcast receiver of map Activity");
                            String myAddress = MyApp.get_session(MyApp.SESSION_USER_AREA);
                            double userLat = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LATITUDE));
                            double userLog = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LONGITUDE));
                            if (myAddress != null)
                                txt_search_location.setText(myAddress);
                            if (userMarker != null) {
                                userMarker.setPosition(new LatLng(userLat, userLog));
                                userMarker.setTitle("You Are Here");
                                userMarker.setSnippet(myAddress);
                                userMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.your_pic));
                                context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_map"));
                                context.unregisterReceiver(mMessageReceiver);
                                // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLat, userLog), 11.0f));
                            } else {
                                userMarker = mGoogleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(userLat, userLog))
                                        .anchor(0.5f, 0.5f)
                                        .title("You Are Here")
                                        .snippet(myAddress));
                                userMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.your_pic));
                                userMarker.showInfoWindow();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        userMarker.hideInfoWindow();
                                    }
                                }, 4000);
                                if (searchMarker == null)
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLat, userLog), 13.0f));
                            }

                        } else {
                            MyApp.log("CheckSyncUp", "In else status != 1 of broadcast receiver of map Activity");
                            if (progressDialog != null)
                                if (progressDialog.isShowing())
                                    progressDialog.setMessage(message);
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.log("Activity_map", "onResume");
        if (new_gps == null) {
            new_gps = new GPSTracker1(context);
        }
        try {
            if (new_gps.canGetLocation()) {
                MyApp.log("Activity_map", "onResume of Activity_map canGetLocation");
                MyApp.set_session(MyApp.SESSION_IS_GPS_ENABLED, "Y");
             /*   if (dialog1!=null)
                    if(dialog1.isShowing());
                        dialog1.dismiss();*/
                //new_gps.getLocation();
            } else {
                MyApp.set_session(MyApp.SESSION_IS_GPS_ENABLED, "");
                //showSettingsAlert(context);
            }
        } catch (SecurityException e) {
            MyApp.log("exception onResume Activity_map is " + e.getMessage());
        }
        if (MyApp.get_session(MyApp.SESSION_FROM_SEARCH).equalsIgnoreCase("Y")) {
            if (searchMarker == null) {
                MyApp.log("Activity_map", "In set search marker of onMapReady");
                String searchedAddress = MyApp.get_session(MyApp.SESSION_SEARCH_AREA);
                txt_search_location.setText(searchedAddress);
                double userLat = Double.parseDouble(MyApp.get_session(MyApp.SESSION_SEARCH_LATITUDE));
                double userLog = Double.parseDouble(MyApp.get_session(MyApp.SESSION_SEARCH_LONGITUDE));
                searchMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(userLat, userLog))
                        .anchor(0.5f, 0.5f)
                        .title(searchedAddress));
                searchMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.search_mark));
                searchMarker.showInfoWindow();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchMarker.hideInfoWindow();
                    }
                }, 4000);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLat, userLog), 13.0f));
                MyApp.set_session(MyApp.SESSION_FROM_SEARCH, "");
            } else {
                MyApp.log("Activity_map", "In set search marker of onMapReady");
                String searchedAddress = MyApp.get_session(MyApp.SESSION_SEARCH_AREA);
                txt_search_location.setText(searchedAddress);
                double userLat = Double.parseDouble(MyApp.get_session(MyApp.SESSION_SEARCH_LATITUDE));
                double userLog = Double.parseDouble(MyApp.get_session(MyApp.SESSION_SEARCH_LONGITUDE));
                searchMarker.setPosition(new LatLng(userLat, userLog));
                searchMarker.setTitle(searchedAddress);
                searchMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.search_mark));
                searchMarker.hideInfoWindow();
                searchMarker.showInfoWindow();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchMarker.hideInfoWindow();
                    }
                }, 4000);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLat, userLog), 13.0f));
                MyApp.set_session(MyApp.SESSION_FROM_SEARCH, "");
            }
        }
        //register broadcast receiver
        registerReceiver(mTimeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_map"));
        context.registerReceiver(mMessageReceiver1, new IntentFilter("Activity_map1"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.log("Activity_map", "onPause");
        //register broadcast receiver
        registerReceiver(mTimeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        context.unregisterReceiver(mTimeReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApp.log("Activity_map", "onDestroy");
        context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_map"));
        context.unregisterReceiver(mMessageReceiver);
        //register broadcast receiver
        registerReceiver(mTimeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        context.unregisterReceiver(mTimeReceiver);
        context.registerReceiver(mMessageReceiver1, new IntentFilter("Activity_map1"));
        context.unregisterReceiver(mMessageReceiver1);
    }
    //broadcast class is used as nested class
    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            MyApp.log("Activity_map", "mTimeReceiver");
            //display time on toast

            /*if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {
                //  progressDialog = ProgressDialog.show(Activity_splash.this, "Sync up", "Getting updated data", false, true);
                MyApp.log("start", "service");
                Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, AppService.class);
                intent1.putExtra("Flag", "getTruckData");
                // intent1.putExtra("Flag", "getGarbageData");
                startService(intent1);
            }*/
            if (!MyApp.get_session(MyApp.SESSION_USER_LATITUDE).equalsIgnoreCase("")) {
                MyApp.log("Activity_map", "In set user marker of mTimeReceiver ans latitude != empty");
                String myAddress = MyApp.get_session(MyApp.SESSION_USER_AREA);
                // txt_search_location.setText(myAddress);
                double userLat = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LATITUDE));
                double userLog = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LONGITUDE));
                if (userMarker != null) {
                    MyApp.log("Activity_map", "In set user marker of mTimeReceiver and userMarker != null");
                    userMarker.setPosition(new LatLng(userLat, userLog));
                    userMarker.setTitle("You Are Here");
                    userMarker.setSnippet(myAddress);
                    userMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.your_pic));

               /* context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_map"));
                context.unregisterReceiver(mMessageReceiver);*/
                    /*if (!MyApp.get_session(MyApp.SESSION_FIRST_LOCATION).equalsIgnoreCase("Y")) {
                        MyApp.set_session(MyApp.SESSION_FIRST_LOCATION, "Y");
                        MyApp.log("Activity_map", "In set user marker of mTimeReceiver and userMarker != null and SESSION_FIRST_LOCATION != Y");
                        if (searchMarker == null)
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLat, userLog), 11.0f));
                    }*/
                } else {
                    MyApp.log("Activity_map", "In set user marker of mTimeReceiver and userMarker == null ");
                    userMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(userLat, userLog))
                            .anchor(0.5f, 0.5f)
                            .title("You Are Here")
                            .snippet(myAddress));
                    userMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.your_pic));
                    userMarker.showInfoWindow();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            userMarker.hideInfoWindow();
                        }
                    }, 4000);
                    if (!MyApp.get_session(MyApp.SESSION_FIRST_LOCATION).equalsIgnoreCase("Y")) {
                        MyApp.set_session(MyApp.SESSION_FIRST_LOCATION, "Y");
                        MyApp.log("Activity_map", "In set user marker of mTimeReceiver and userMarker == null and SESSION_FIRST_LOCATION != Y");
                        if (searchMarker == null)
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLat, userLog), 13.0f));
                    }
                }
            } else {
                MyApp.log("Activity_map", "mTimeReceiver");
                int permission_granted = PackageManager.PERMISSION_GRANTED;
                MyApp.log("Activity_map", "PersmissionGrantedCode->" + permission_granted);
                int gps_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
                MyApp.log("Activity_map", "gps_permission->" + gps_permission);
                if (gps_permission == permission_granted) {
                    GPSTracker1 new_gps = new GPSTracker1(context);
                    if (new_gps.canGetLocation())
                        new_gps.getLocation();
                }
            }
        }
    };

    public class BalloonAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater inflater = null;
        private TextView txt_truck_type, txt_truck_no;
        ImageView img_truck;

        public BalloonAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            View v = inflater.inflate(R.layout.marker, null);
            txt_truck_no = (TextView) v.findViewById(R.id.txt_truck_no);
            txt_truck_type = (TextView) v.findViewById(R.id.txt_truck_type);
            img_truck = (ImageView) v.findViewById(R.id.img_truck);
            if (marker != null) {
                String title_pos = marker.getTitle();
                int i = -1;
                try {
                    i = Integer.parseInt(title_pos);
                } catch (NumberFormatException n) {
                }
                if (i == -2) {
                    String title_pos1 = marker.getSnippet();
                    img_truck.setVisibility(View.GONE);
                    txt_truck_type.setVisibility(View.GONE);
                    txt_truck_no.setText("Free HealthCare" + title_pos1);
                    img_truck.setVisibility(View.VISIBLE);
                    Picasso.with(context)
                            .load(R.drawable.freehealthcare)
                            .placeholder(R.drawable.freehealthcare)
                            .error(R.drawable.freehealthcare)
                            .into(img_truck);

                } else if (i == -1) {
                    img_truck.setVisibility(View.GONE);
                    if (title_pos.equalsIgnoreCase("You Are Here")) {
                        txt_truck_type.setText("You Are Here");
                        txt_truck_no.setText(MyApp.get_session(MyApp.SESSION_USER_AREA));

                    } else {
                        txt_truck_type.setVisibility(View.GONE);
                        txt_truck_no.setText(MyApp.get_session(MyApp.SESSION_SEARCH_AREA));
                    }
                } else {
                    DTOMarkerData dtoMarkerData = markerDataArrayList.get(i);
                    int drawable_id = dtoMarkerData.getImg_id();
                    MyApp.log("Activity_map", "image url is " + dtoMarkerData.getTruck_img());
                    if (drawable_id != (R.drawable.green)) {
                        img_truck.setVisibility(View.VISIBLE);
                        txt_truck_no.setText(dtoMarkerData.getTitle());
                        txt_truck_type.setText(dtoMarkerData.getSnippet());
                        if (dtoMarkerData.getSnippet().equalsIgnoreCase("Glutton")) {
                            Picasso.with(context)
                                    .load(R.drawable.glutton)
                                    .placeholder(R.drawable.glutton)
                                    .error(R.drawable.glutton)
                                    .into(img_truck);
                        } else {
                            Picasso.with(context)
                                    .load(R.drawable.tri_lo)
                                    .placeholder(R.drawable.tri_lo)
                                    .error(R.drawable.tri_lo)
                                    .into(img_truck);
                        }

                    } /*else {
                        img_truck.setVisibility(View.VISIBLE);
                        Picasso.with(context)
                                .load(R.drawable.bin)
                                .placeholder(R.drawable.bin)
                                .error(R.drawable.bin)
                                .into(img_truck);

                        txt_truck_no.setText(dtoMarkerData.getTitle());
                        txt_truck_type.setText(dtoMarkerData.getSnippet());
                    }*/
                }

               /* new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        marker.hideInfoWindow();
                    }
                }, 4000);*/
            }
            return (v);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return (null);
        }
    }

    public void showSettingsAlert(final Context context) {
        if (dialog1 != null) {
            if (!dialog1.isShowing())
                dialog1.show();
        } else {
            dialog1 = new Dialog(context);
            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog1.setContentView(R.layout.gps_alert);
            dialog1.show();
            dialog1.setCancelable(false);
            dialog1.setCanceledOnTouchOutside(false);
           /* Button btn_cancel = (Button) dialog1.findViewById(R.id.btn_cancel);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    dialog1.dismiss();
                }
            });*/
            TextView txt_ok = (TextView) dialog1.findViewById(R.id.txt_ok);
            txt_ok.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                    dialog1.dismiss();
                    dialog1 = null;
                }
            });
        }
    }
}
