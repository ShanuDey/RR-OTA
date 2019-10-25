package com.example.romota;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView tv_version, tv_buildDate;
    private Button btn_updates, btn_changeLogs;
    private AlertDialog alertDialog;

    private String local_build_date;

    public static final String KEY_BUILD_DATE = "buildDate";
    public static final String KEY_DOWNLOAD_URL = "downloadUrl";
    public static final String KEY_VERSION = "version";
    public static final String KEY_CHANGE_LOGS = "changeLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //casting
        tv_version = findViewById(R.id.tv_version);
        tv_buildDate = findViewById(R.id.tv_buildDate);
        btn_updates = findViewById(R.id.btn_updates);
        btn_changeLogs = findViewById(R.id.btn_changelogs);

        final String build_info = new String(Build.VERSION.INCREMENTAL);
        local_build_date = build_info.split("\\.")[2];
        Log.v("build_date", local_build_date);

        tv_buildDate.setText("Build Date: "+local_build_date);
        tv_version.setText("Version: 7.0.2");

        btn_updates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = getString(R.string.ota_url);
                Log.v("shanu","ota url="+url);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try{
                                    String remote_build_date = response.getString(KEY_BUILD_DATE);
                                    if(Integer.parseInt(remote_build_date)>Integer.parseInt(local_build_date)){
                                        String version = response.getString(KEY_VERSION);
                                        String changeLogs = response.getString(KEY_CHANGE_LOGS);
                                        String downloadUrl = response.getString(KEY_DOWNLOAD_URL);

                                        Log.v("shanu","changelogs :"+changeLogs);
                                        alertUpdate(version,remote_build_date, downloadUrl, changeLogs);
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this, "No update available", Toast.LENGTH_SHORT).show();
                                    }
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }
                );

                // add request queue after json object created. Otherwise the textView will not show the fetched data.
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(jsonObjectRequest);
            }
        });

        btn_changeLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(getString(R.string.changeLogsUrl));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

    }

    private void alertUpdate(String version, String buildDate, final String downloadUrl, String changeLogs) {
        String message = "Resurection Remix v"+version+"-"+buildDate+"\n\nChangeLogs:";
        for( String s: changeLogs.substring(1).split(",")){
            message+="\n"+s.substring(1,s.length()-2);
        }
        Log.v("shanu","message\n"+message);

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle("New Update Available")
                .setMessage(message)
                .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri uri = Uri.parse(downloadUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                })
                .setCancelable(true);
        alertDialog = materialAlertDialogBuilder.create();
        alertDialog.show();
    }
}
