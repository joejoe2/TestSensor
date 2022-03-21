package com.edutalk.app.edutalk;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.edutalk.app.R;
import com.edutalk.app.sa.EduTalkSmartphoneSA;
import com.edutalk.app.sensor.BaseSensorType;
import com.edutalk.app.sensor.streamsensor.StreamSensorType;
import com.edutalk.app.sensor.triggersensor.TriggerSensorType;
import com.edutalk.app.utils.DialogQueue;
import com.edutalk.app.utils.LinkedMapForIntent;
import com.edutalk.app.utils.PermissionManager;
import com.edutalk.app.utils.Utils;
import com.edutalk.app.utils.VersionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class EduTalkBridgeAppLink extends AppCompatActivity {
    EduTalkRCConfig eduTalkRCConfig;
    final Integer[] SAMPLE_RATES = new Integer[]{1, 10, 25, 50, 100, 200, 300, 400};

    Button startSAButton;
    Spinner sampleRateSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_link);
        PermissionManager.requestPermissions(this, PermissionManager.getLackingPermissions(this));
        //check update
        checkUpdate();
        //use chrome based intent to open app, and receive query param
        onReceivedAppLink(getIntent().getData());
    }

    private void onReceivedAppLink(Uri appLink){
        //link format: (?data should be encoded !)
        // https://joejoe2.github.io/applink/edutalk/Smartphone
        // ?data=http%3A%2F%2Fphyedu.iottalk.tw%2Flecture%2F6%2Frc%2F%3Ftoken%3D99d42d77825346e4ad24a7653c06ffdb
        // the query param "data" is in format (rc index):
        // http://phyedu.iottalk.tw/lecture/6/rc/?token=99d42d77825346e4ad24a7653c06ffdb !!!
        System.out.println(appLink);
        new Thread(()->{
            try {
                //receive param
                String rc_index_url = Uri.decode(appLink.getQueryParameter("data"));
                //get to rc_index_url to retrieve eduTalkRCConfig
                //eduTalkRCConfig = EduTalkService.fetchRCConfig(rc_index_url);
                eduTalkRCConfig = EduTalkService.fetchRCConfig(rc_index_url+"&app=true");
            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(EduTalkBridgeAppLink.this::terminateWithError);
            }

            runOnUiThread(()->{
                setUI();
                setListeners();
            });
        }).start();
    }

    private void setUI(){
        sampleRateSpinner = findViewById(R.id.sampleRateSpinner);
        sampleRateSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, SAMPLE_RATES));
        sampleRateSpinner.setSelection(0);

        startSAButton = findViewById(R.id.startSAButton);
    }

    private void setListeners(){
        startSAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> lackingPermissions= PermissionManager.getLackingPermissions(EduTalkBridgeAppLink.this);
                if (lackingPermissions.size()>0){
                    PermissionManager.requestPermissionsInSetting(EduTalkBridgeAppLink.this, lackingPermissions);
                    return;
                }

                Intent saActivity=new Intent().setClass(EduTalkBridgeAppLink.this, EduTalkSmartphoneSA.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("eduTalkRCConfig", eduTalkRCConfig);
                bundle.putInt("sampleRate", (Integer) sampleRateSpinner.getSelectedItem());
                saActivity.putExtras(bundle);
                startActivity(saActivity);
                finish();
            }
        });
    }

    private void checkUpdate(){
        if (VersionManager.hasNewVersion()){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("please update app to latest version !");
            builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //open Download Url in browser
                    Intent intent=Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER);
                    intent.setData(Uri.parse(VersionManager.getDownloadUrl()));
                    startActivity(intent);
                }
            });
            builder.setCancelable(false);
            DialogQueue.showDialog(builder.create());
        }
    }

    private void terminateWithError(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("there are some errors in your app link !");
        builder.setPositiveButton("exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EduTalkBridgeAppLink.this.finish();
            }
        });
        builder.setCancelable(false);
        DialogQueue.showDialog(builder.create());
    }
}
