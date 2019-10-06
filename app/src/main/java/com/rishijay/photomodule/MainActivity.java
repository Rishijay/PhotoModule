package com.rishijay.photomodule;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    String mFileName = "";
    String mFilePath = "";
    File mName;
    private File photoFile;
    private Uri photoURI;
    private int REQUEST_IMAGE_CAPTURE = 1;
    private int SELECT_PICTURE = 50;
    private String URL="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                selectImage();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("onActivityResult", "entered" + " " + requestCode + " " + resultCode);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.e("REQUEST_IMAGE_CAPTURE", "entered");
            Toast.makeText(this, "" + mFilePath.toString(), Toast.LENGTH_SHORT).show();
            //uploadImage(mFilePath);

        }

        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                Uri uri = data.getData();
                try {

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Uri tempUri = getImageUri(getApplicationContext(), bitmap);


                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    File finalFile = new File(getRealPathFromURI(tempUri));
                    Log.e("Final File ", " " + finalFile.toString());
                    Toast.makeText(this, "" + finalFile.toString(), Toast.LENGTH_SHORT).show();
                    mFilePath = finalFile.toString();
                    // Log.d(TAG, String.valueOf(bitmap));
                    //uploadImage(mFilePath);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Data Null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File folder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "RISHIJAY");
        //File folder = new File("sdcard/PQM");
        if (!folder.exists()) {
            folder.mkdir();
        }

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date());
        mFileName = "RISHIJAY@" + timeStamp + ".jpg";

        mName = new File(folder, mFileName);
        mFilePath = mName.getAbsolutePath();
        return mName;
    }

    private void selectImage() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setMessage("Select your choice");
        alert.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Gallery Intent
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                //intent.setData(Uri.parse("content://media/external/images/media/"));
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
                dialogInterface.dismiss();

            }
        });
        alert.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                try {
                    // Camera Intent
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.e("Exception", "" + ex.toString());
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            //Uri photoURI = Uri.fromFile(photoFile);
                            photoURI = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".my.package.name.provider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Log.e("123", "check");
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            Log.e("123", "check");
                        }
                    }
                } catch (Exception e) {
                    Log.e("Photo Exception", "" + e.toString());
                }


                dialogInterface.dismiss();

            }
        });

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                alert.show();

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                //Toast.makeText(MainActivity.this, "We need these permissions to perform this operation.", Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.with(MainActivity.this)
                .setPermissionListener(permissionlistener)
                .setRationaleTitle("Permission Required")
                .setRationaleMessage("This app needs permissions to access storage and camera to perform this operation.")
                .setDeniedTitle("Permission denied")
                .setDeniedMessage(
                        "If you reject permission(s),you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setGotoSettingButtonText("Permission")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    private void uploadImage(final String path) {
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                //Toast.makeText(getApplicationContext(),s.toString(),Toast.LENGTH_LONG).show();

                String resultResponse = new String(response.data);
                Log.e("response", resultResponse);
                Log.e("config", "called22222");


                try {
                    JSONArray arr = new JSONArray(resultResponse);

                    JSONObject obj = arr.getJSONObject(0);
                    Log.e("Json response", " " + obj.toString());
                    int error = obj.getInt("error_code");
                    Log.e("error code", " " + error);
                    if (error == 100) {
                        String message = obj.getString("message");

                        Log.e("messsage", message);
                    }
                    if (error == 101) {
                        String message = obj.getString("message");

                        Log.e("messsage", message);
                    }
                } catch (JSONException e) {

                    Log.e("Check", "JSONEXCEPTION" + e);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("error response", "Some error occurred!!" + volleyError);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("username", "");
                Log.d("Params", parameters.toString() + "\n" + URL);
                return parameters;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("image", new DataPart(path, AppHelper.getFileDataFromString(path)));
                Log.e("Params Image ", "" + path);
                return params;
            }
        };
        RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
        rQueue.add(multipartRequest);
        multipartRequest.setShouldCache(false);
    }
}
