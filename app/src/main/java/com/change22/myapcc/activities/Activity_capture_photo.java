package com.change22.myapcc.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.change22.myapcc.R;
import com.change22.myapcc.config.FileUtils;
import com.change22.myapcc.config.GPSTracker1;
import com.change22.myapcc.config.MyApp;
import com.change22.myapcc.database.TABLE_GARBAGE;
import com.change22.myapcc.database.TABLE_LOCATIONS;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Rasika on 04/08/2016.
 */
public class Activity_capture_photo extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = "Activity_capture_photo";
    Button btn_click_camera = null, btn_select_from_gallery = null;
    Context context = null;
    private int permission_count = 3;
    File fileImage = null;
    String str_path = "";
    int TAKE_IMAGE = 100, PICK_IMAGE = 200;

    public static String new_str_path = "";
    String str_pic_path = "";

    GPSTracker1 new_gps = null;

    TextView txt_header = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_photo);
        context = this;
        initComponents();
        initComponentListener();

        new_gps = new GPSTracker1(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getPermissionCount() > 0)
                check_app_persmission();
            /*else {
                if (new_gps.canGetLocation())
                    new_gps.getLocation();
                else
                    new_gps.showSettingsAlert(context);
            }*/
        }
         /*else {
            if (new_gps.canGetLocation())
                new_gps.getLocation();
            else
                new_gps.showSettingsAlert(context);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_map"));
        if (new_gps.canGetLocation())
            new_gps.getLocation();
       /* else
            new_gps.showSettingsAlert(context);*/
    }

    public void initComponents() {

        btn_click_camera = (Button) findViewById(R.id.btn_click_camera);
        btn_select_from_gallery = (Button) findViewById(R.id.btn_select_from_gallery);

        txt_header= (TextView) findViewById(R.id.txt_title);
        txt_header.setText("Welcome "+MyApp.get_session(MyApp.SESSION_USER_NAME)+", report your garbage!");
        txt_header.setGravity(Gravity.CENTER);
    }

    public void initComponentListener() {

        btn_click_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyApp.log("Image Clicked");
                String current_date = ((MyApp) getApplication()).get_current_date();
                MyApp.log(LOG_TAG, "current date is: " + current_date);
                try {
                    MyApp.log("In try");
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File imageStorageDir = new File(Environment.getExternalStorageDirectory(), "MYAPPCC");
                    if (!imageStorageDir.exists()) {
                        imageStorageDir.mkdirs();
                    }
                    str_path = imageStorageDir + File.separator + "IMG-" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                    fileImage = new File(str_path);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImage));


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getPermissionCount() > 0)
                            check_app_persmission();
                        else {
                            startActivityForResult(intent, TAKE_IMAGE);
                        }

                    } else {
                        startActivityForResult(intent, TAKE_IMAGE);
                    }
                } catch (Exception e) {
                    MyApp.log("In catch");
                    Snackbar.make(btn_click_camera, "Unable to get Camera, Please try again later!", Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        btn_select_from_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });

    }


    private void check_app_persmission() {
        permission_count = 3;
        int permission_granted = PackageManager.PERMISSION_GRANTED;
        MyApp.log(LOG_TAG, "PersmissionGrantedCode->" + permission_granted);

        int storage_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        MyApp.log(LOG_TAG, "StoragePermission->" + storage_permission);
        if (storage_permission == permission_granted)
            permission_count -= 1;

        int camera_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        MyApp.log(LOG_TAG, "CameraPermission->" + camera_permission);
        if (camera_permission == permission_granted)
            permission_count -= 1;

        int location_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        MyApp.log(LOG_TAG, "location_permission->" + location_permission);
        if (location_permission == permission_granted)
            permission_count -= 1;

       /* int location2_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        MyApp.log(LOG_TAG, "location_permission->" + location_permission);
        if(location2_permission == permission_granted)
            permission_count -= 1;*/


        MyApp.log(LOG_TAG, "check_app_permission PermissionCount->" + permission_count);

        if (permission_count > 0) {
            String permissionArray[] = new String[permission_count];

            for (int i = 0; i < permission_count; i++) {
                MyApp.log(LOG_TAG, "i->" + i);

                if (storage_permission != permission_granted) {
                    if (!Arrays.asList(permissionArray).contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        permissionArray[i] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        MyApp.log(LOG_TAG, "i->WRITE_EXTERNAL_STORAGE");
                        // break;
                    }
                }

                if (camera_permission != permission_granted) {
                    if (!Arrays.asList(permissionArray).contains(Manifest.permission.CAMERA)) {
                        permissionArray[i] = Manifest.permission.CAMERA;
                        MyApp.log(LOG_TAG, "i->CAMERA");
                        //break;
                    }
                }
                if (location_permission != permission_granted) {
                    if (!Arrays.asList(permissionArray).contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        permissionArray[i] = Manifest.permission.ACCESS_FINE_LOCATION;
                        MyApp.log(LOG_TAG, "i->ACCESS_FINE_LOCATION");
                        //break;
                    }
                }
                /*if (location2_permission != permission_granted) {
                    if(!Arrays.asList(permissionArray).contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        permissionArray[i] = Manifest.permission.ACCESS_COARSE_LOCATION;
                        MyApp.log(LOG_TAG, "i->ACCESS_COARSE_LOCATION");
                        //break;
                    }
                }*/


            }
            MyApp.log(LOG_TAG, "PermissionArray->" + Arrays.deepToString(permissionArray));

            ActivityCompat.requestPermissions(Activity_capture_photo.this, permissionArray, permission_count);//requestPermissions(permissionArray, permission_count);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission_count = permissions.length;
        MyApp.log(LOG_TAG, "In onRequestPermissionsResult");
        MyApp.log(LOG_TAG, "requestCode->" + requestCode);
        MyApp.log(LOG_TAG, "permissions->" + Arrays.deepToString(permissions));
        int len = grantResults.length;
        MyApp.log(LOG_TAG, "permissionsLength->" + len);

        int permission_granted = PackageManager.PERMISSION_GRANTED;
        MyApp.log(LOG_TAG, "PermissionGrantedCode->" + permission_granted);
        String str = "";
        for (int i = 0; i < len; i++) {

            if (permissions[i].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)) {
                int location_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
                MyApp.log(LOG_TAG, "AccessCore->" + location_permission);
                if (location_permission == permission_granted) {
                    permission_count -= 1;
                } else {
                    str += "Location, ";
                }
            }


            if (permissions[i].equalsIgnoreCase(Manifest.permission.CAMERA)) {
                int camera_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
                MyApp.log(LOG_TAG, "AccessCore->" + camera_permission);
                if (camera_permission == permission_granted) {
                    permission_count -= 1;
                } else {
                    str += "Camera, ";
                }
            }

            if (permissions[i].equalsIgnoreCase(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                int storage_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                MyApp.log(LOG_TAG, "AccessCore->" + storage_permission);
                if (storage_permission == permission_granted) {
                    permission_count -= 1;
                } else {
                    str += "Storage, ";
                }
            }

            MyApp.log(LOG_TAG, "onRequestPermissionsResult PermissionCount->" + permission_count);
        }

        if (permission_count > 0) {
            Snackbar.make(btn_click_camera, "My APCC needs permissions : " + str,
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    String SCHEME = "package";
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts(SCHEME, "com.change22.myapcc", null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }).show();
        } else {
            if (new_gps.canGetLocation())
                new_gps.getLocation();
           /* else
                new_gps.showSettingsAlert(context);*/
        }
    }

    public int getPermissionCount() {
        int count = 3;
        int permission_granted = PackageManager.PERMISSION_GRANTED;
        int camera_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (camera_permission == permission_granted)
            count -= 1;
        int storage_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (storage_permission == permission_granted)
            count -= 1;
        int access_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (access_permission == permission_granted)
            count -= 1;
        return count;
    }

    // your Final lat Long Values
    Float Latitude, Longitude;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApp.log(LOG_TAG, "onActivityResult");
        MyApp.log(LOG_TAG, "RequestCode->" + requestCode);//profile_pic_1 = 501,profile_pic_2 = 502
        MyApp.log(LOG_TAG, "ResultCode->" + resultCode);
        if (data != null) {
            Bundle extras = data.getExtras();
            if (extras != null)
                MyApp.log_bundle(extras);
        }
        if (resultCode != 0) {

            if (requestCode == TAKE_IMAGE) {

                File f = new File(Environment.getExternalStorageDirectory().toString());
                MyApp.log(LOG_TAG, "IsFileExists->" + fileImage.exists());
                if (fileImage.exists()) {
                    f = fileImage;

                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = compressImage(str_path);
                    //Bitmap bmp = BitmapFactory.decodeFile(str_path);
                    //img_photo_1.setImageBitmap(bitmap);
                    MyApp.log(LOG_TAG, "Image url is " + f.getAbsolutePath() + ", " + f.getAbsoluteFile());

                    if(((MyApp) getApplication()).isNetworkConnectionAvailable()){

                        AsyncReportGarbage report_garbage = new AsyncReportGarbage();
                        report_garbage.execute(new_str_path,"from_camera","", "");
                        //report_garbage.execute(str_path);
                    }
                    else{

                        showInternetDialog("");
                    }

                   /* String path = Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = fileImage;//new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (requestCode == PICK_IMAGE) {
                if (data != null) {
                    // Get the URI of the selected file
                    final Uri uri = data.getData();
                    final String path = FileUtils.getPath(context, uri);
                    str_pic_path = path;

                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(str_pic_path);

                        String LATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String LATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                        String LONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                        String LONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);



                        if((LATITUDE !=null)
                                && (LATITUDE_REF !=null)
                                && (LONGITUDE != null)
                                && (LONGITUDE_REF !=null))
                        {

                            if(LATITUDE_REF.equals("N")){
                                Latitude = convertToDegree(LATITUDE);
                            }
                            else{
                                Latitude = 0 - convertToDegree(LATITUDE);
                            }

                            if(LONGITUDE_REF.equals("E")){
                                Longitude = convertToDegree(LONGITUDE);
                            }
                            else{
                                Longitude = 0 - convertToDegree(LONGITUDE);
                            }

                        }

                        MyApp.log(LOG_TAG,"captured lat and log " + Latitude + ", " + Longitude);

                        if (Latitude != null && Longitude != null ) {
                            MyApp.log(LOG_TAG, "Image url from gallery is " + str_pic_path);
                            Bitmap bitmap;
                            bitmap = compressImage(str_pic_path);
                            //Bitmap bmp = BitmapFactory.decodeFile(path);
                            //img_photo_1.setImageBitmap(bmp);

                            if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {

                                AsyncReportGarbage report_garbage = new AsyncReportGarbage();
                                report_garbage.execute(new_str_path,"from_gallary","" + Latitude , "" + Longitude);
                            } else {

                                showInternetDialog("");
                            }
                        }else{

                            final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            alertDialog.setTitle("My APCC");
                            alertDialog.setMessage("Your image has no GPS Coordinates.");
                            alertDialog.setIcon(R.mipmap.ic_launcher);

                            alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();

                                }
                            });

                            alertDialog.show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private Float convertToDegree(String stringDMS){
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

        return result;


    };



    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return (String.valueOf(Latitude)
                + ", "
                + String.valueOf(Longitude));
    }

    public int getLatitudeE6(){
        return (int)(Latitude*1000000);
    }

    public int getLongitudeE6(){
        return (int)(Longitude*1000000);
    }

    public static Bitmap compressImage(String filePath) {

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        Log.i(LOG_TAG, "BactualWidth->" + actualWidth);
        Log.i(LOG_TAG, "BactualHeight->" + actualHeight);
//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 768.0f;
        float maxWidth = 1024.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }
        Log.i(LOG_TAG, "AactualWidth->" + actualWidth);
        Log.i(LOG_TAG, "AactualHeight->" + actualHeight);
//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.i(LOG_TAG, "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.i(LOG_TAG, "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.i(LOG_TAG, "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.i(LOG_TAG, "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        new_str_path = getFilename();
        try {
            out = new FileOutputStream(new_str_path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//          write the compressed bitmap at the destination specified by filename.
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        return scaledBitmap;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static String getFilename() {
        File imageStorageDir = new File(Environment.getExternalStorageDirectory(), "MyAPCC");
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        String new_str_path = imageStorageDir + File.separator + "IMG-" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        return new_str_path;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_map"));
        context.unregisterReceiver(mMessageReceiver);
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyApp.log("messege recieved");
            // Extract data included in the Intent
            //String service = intent.getStringExtra("service");
            if (intent != null) {
                String status = intent.getStringExtra("response_code");
                String message = intent.getStringExtra("response_message");
                MyApp.log("CheckSyncUp", "In broadcast receiver of map Activity");
                MyApp.log("CheckSyncUp", "Status is " + status + " and message is " + message);

                // Toast.makeText(context,"Status:"+status+"\nMessage:"+message,Toast.LENGTH_SHORT).show();
                if (status != null) {
                    if (status.equals("0")) {
                        MyApp.log("CheckSyncUp", "In if status = 0 of broadcast receiver of map Activity");
                       /* if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.hide();*/
                    }
                    if (status.equals("1") && message.equals("")) {
                        MyApp.log("CheckSyncUp", "In if status = 1 and message = BannerMaster of broadcast receiver of map Activity");
                        /*if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();*/

                    } else if (status.equals("3") && message.equals("location")) {
                        MyApp.log("CheckSyncUp", "In if status = 3 and message = location of broadcast receiver of map Activity");
                        context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_map"));
                        context.unregisterReceiver(mMessageReceiver);

                    } else {
                        MyApp.log("CheckSyncUp", "In else status != 1 of broadcast receiver of map Activity");
                       /* if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.setMessage(message);*/
                    }
                }
            }
        }
    };


    public class AsyncReportGarbage extends AsyncTask<String, Void, String> {
        String response = "-0";

        String name = "", email_id = "", device_id = "", gcm_id = "", image_file = "", latitude = "", longitude = "",
                date = "";

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String title = getResources().getString(R.string.app_name);
            SpannableString ss1=  new SpannableString(title);
            ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss1.length(), 0);
            progressDialog = ProgressDialog.show(context, ss1, "Please wait...registering complaint", false, false);
        }

        @Override
        protected String doInBackground(String... params) {

            name = MyApp.get_session(MyApp.SESSION_USER_NAME);
            email_id = MyApp.get_session(MyApp.SESSION_USER_EMAIL);
            device_id = MyApp.get_device_id();
            gcm_id = MyApp.regid;//MyApp.get_session(MyApp.PREFS_PROPERTY_REG_ID);
            image_file = params[0];

            String status_flag = params[1];
            if (status_flag.equalsIgnoreCase("from_camera")) {
                latitude = MyApp.get_session(MyApp.SESSION_USER_LATITUDE);
                longitude = MyApp.get_session(MyApp.SESSION_USER_LONGITUDE);
            } else{
                latitude = params[2];
                longitude = params[3];
            }

            /*latitude = MyApp.get_session(MyApp.SESSION_USER_LATITUDE);
            longitude = MyApp.get_session(MyApp.SESSION_USER_LONGITUDE);*/
            date = MyApp.get_current_date();

            MyApp.log(LOG_TAG,"name: " + name);
            MyApp.log(LOG_TAG,"email_id: " + email_id);
            MyApp.log(LOG_TAG,"device_id: " + device_id);
            MyApp.log(LOG_TAG,"gcm_id: " + gcm_id);
            MyApp.log(LOG_TAG,"image_file: " + image_file);
            MyApp.log(LOG_TAG,"latitude: " + latitude);
            MyApp.log(LOG_TAG,"longitude: " + longitude);
            MyApp.log(LOG_TAG,"date: " + date);

            if(latitude.equals("null")|| latitude.equals(""))
            {
                latitude = "0.0";
            }

            if(longitude.equals("null")|| longitude.equals(""))
            {
                longitude = "0.0";
            }

            boolean flag = TABLE_LOCATIONS.compare_lat_long(Double.parseDouble(latitude), Double.parseDouble(longitude));
            MyApp.log(LOG_TAG,"flag after comparing: " + flag);
            if (flag) {
                MyApp.log(LOG_TAG,"in if flag after comparing: " + flag);
                response = MyApp.add_report_garbage(name, email_id, device_id, gcm_id, image_file, latitude, longitude, date, MyApp.get_session(MyApp.SESSION_USER_ID));
                return response;
            } else {
                //given co-ordinates are not in 40 feets with existing co-ordinates
                MyApp.log(LOG_TAG,"in else flag after comparing: " + flag);
                response = "-1";
                return response;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MyApp.log(LOG_TAG, "Add report garbage response is " + response);
            if (progressDialog != null)
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            if (!response.equalsIgnoreCase("-1")) {
                if (!response.equalsIgnoreCase("-0")) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("status")) {
                            String status = jsonObject.getString("status");
                            if (status.equalsIgnoreCase("true")) {
                                String response_message = jsonObject.getString("message");

                                showDialog(response_message);

                                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                String garbage_id = jsonObject1.getString("garbage_id");
                                String image_url = jsonObject1.getString("image_url");

                                JSONObject jsonObject2 = jsonObject1.getJSONObject("locality");
                                String area = jsonObject2.getString("area");
                                String locality = jsonObject2.getString("locality");
                                String city = jsonObject2.getString("city");
                                String state = jsonObject2.getString("state");
                                String pincode = jsonObject2.getString("pincode");

                                MyApp.log(LOG_TAG, "response_message: " + response_message);
                                MyApp.log(LOG_TAG, "garbage_id: " + garbage_id);
                                MyApp.log(LOG_TAG, "image_url: " + image_url);
                                MyApp.log(LOG_TAG, "area: " + area);
                                MyApp.log(LOG_TAG, "locality: " + locality);
                                MyApp.log(LOG_TAG, "city: " + city);
                                MyApp.log(LOG_TAG, "state: " + state);
                                MyApp.log(LOG_TAG, "pincode: " + pincode);

                                String user_id = MyApp.get_session(MyApp.SESSION_USER_ID);
                                TABLE_GARBAGE.insert_garbage_list(garbage_id, user_id, "", "Garbage", name, image_url, area, locality, city, state, pincode, latitude, longitude, date,"REPORTED");


                            } else if (status.equalsIgnoreCase("false")) {
                                /*String response_message = jsonObject.getString("message");
                                // Snackbar.make(btn_click_camera, response_message, Snackbar.LENGTH_SHORT).show();
                                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                alertDialog.setTitle(getResources().getString(R.string.app_name));
                                alertDialog.setMessage(response_message);
                                alertDialog.setIcon(R.mipmap.ic_launcher);

                                alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.dismiss();

                                    }
                                });

                                alertDialog.show();*/

                                    String response_message = jsonObject.getString("message");
                                    if (response_message.equalsIgnoreCase("Your email id is not validated yet.")) {

                                        MyApp.set_session(MyApp.SESSION_USER_EMAIL, "");
                                        MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "");
                                        showSuccessDialog(response_message);

                                    }else if (response_message.equalsIgnoreCase("Failed to save image")) {

                                        showDialog(response_message);

                                    }else if(response_message.equalsIgnoreCase("Photo is required.")){

                                        showDialog(response_message);

                                    }else if(response_message.equalsIgnoreCase("Failed to save complaint.")){

                                        showDialog(response_message);

                                    }
                                }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else
                showMessageDialog();
        }
    }

    public void showDialog(String title) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(title);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();

                MyApp.set_session(MyApp.SESSION_PHOTO_CAPTURE,"Y");
                finish();
            }
        });

        alertDialog.show();
    }

    public void showSuccessDialog(String title) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(title);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                MyApp.set_session(MyApp.SESSION_USER_EMAIL, "");
                MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "");
                finish();
            }
        });

        alertDialog.show();
    }

    public void showInternetDialog(String title){
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(getResources().getString(R.string.no_internet_message));
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void showMessageDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage("APCC does not cover this area yet. Coming Soon!");
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

}
