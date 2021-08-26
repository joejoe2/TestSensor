package com.joejoe2.testsensor.edutalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.joejoe2.testsensor.R;
import com.joejoe2.testsensor.sa.EduTalkSmartphoneSA;
import com.joejoe2.testsensor.sensor.SensorType;
import com.joejoe2.testsensor.sensor.streamsensor.StreamSensorType;
import com.joejoe2.testsensor.sensor.triggersensor.RangeSensorType;
import com.joejoe2.testsensor.utils.PermissionUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class EduTalkBridgeAppLink extends AppCompatActivity {
    String CSMEndpoint;
    String deviceName, deviceModel;
    String BIND_RC_URL;
    EduTalkRCConfig eduTalkRCConfig;
    HashMap<String, SensorType> selectedSensors = new HashMap<>();
    final Integer[] SAMPLE_RATES = new Integer[]{1, 10, 25, 50, 100, 200};

    TextView applinkTextView;
    Button startSAButton;
    Spinner sampleRateSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_link);
        PermissionUtils.requestPermissions(this, PermissionUtils.getLackingPermissions(this));

        //use firebase DynamicLinks to open app, and receive query param
        FirebaseApp.initializeApp(this);
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        onReceivedAppLink(pendingDynamicLinkData);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        terminateWithError();
                    }
                });
    }

    private void onReceivedAppLink(PendingDynamicLinkData pendingDynamicLinkData){
        //link format: (should be encoded !)
        // https://edutalkapp.page.link/?link=https://joejoe2.github.io/applink/edutalk/Smartphone
        // ?data=http%3A%2F%2Fphyedu.iottalk.tw%2Flecture%2F6%2Frc%2F%3Ftoken%3D99d42d77825346e4ad24a7653c06ffdb
        // &apn=com.joejoe2.testsensor
        // &afl=https://play.google.com/store/apps/details?id%3Dgogolook.callgogolook2
        // &efr=1
        // the query param "data" is in format (some rc index):
        // http://phyedu.iottalk.tw/lecture/6/rc/?token=99d42d77825346e4ad24a7653c06ffdb !!!
        PendingDynamicLinkData appLinkData = pendingDynamicLinkData;
        new Thread(()->{
            try {
                //receive param
                String rc_index_url = Uri.decode(appLinkData.getLink().getQueryParameter("data"));
                //get to rc_index_url to retrieve eduTalkRCConfig
                eduTalkRCConfig=EduTalkService.getRCConfig(rc_index_url);
                CSMEndpoint = eduTalkRCConfig.csm_url;
                selectedSensors = getSelectedSensorsFromData(new JSONArray(eduTalkRCConfig.idf_list));
                deviceName=eduTalkRCConfig.device_name;
                deviceModel=eduTalkRCConfig.device_model;
                BIND_RC_URL=eduTalkRCConfig.rc_bind;
            }catch (Exception e){
                e.printStackTrace();
                terminateWithError();
            }finally {
                runOnUiThread(()->{
                    setUI();
                    setListeners();
                });
            }
        }).start();
    }

    private void setUI(){
        applinkTextView = findViewById(R.id.applinkTextView);
        applinkTextView.setText("supported features are ready:\n\n"+selectedSensors.keySet());
        sampleRateSpinner = findViewById(R.id.sampleRateSpinner);
        sampleRateSpinner.setAdapter(new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_dropdown_item, SAMPLE_RATES));
        sampleRateSpinner.setSelection(0);
        startSAButton = findViewById(R.id.startSAButton);
    }

    private void setListeners(){
        startSAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> lackingPermissions= PermissionUtils.getLackingPermissions(EduTalkBridgeAppLink.this);
                if (lackingPermissions.size()>0){
                    PermissionUtils.requestPermissionsInSetting(EduTalkBridgeAppLink.this, lackingPermissions);
                    return;
                }

                Intent saActivity=new Intent().setClass(EduTalkBridgeAppLink.this, EduTalkSmartphoneSA.class);
                Bundle bundle = new Bundle();
                bundle.putString("CSMEndpoint", CSMEndpoint);
                bundle.putString("deviceModel", deviceModel);
                bundle.putString("deviceName", deviceName);
                bundle.putSerializable("selectedSensors", selectedSensors);
                bundle.putString("BIND_RC_URL", BIND_RC_URL);
                bundle.putInt("sampleRate", (Integer) sampleRateSpinner.getSelectedItem());
                saActivity.putExtras(bundle);
                startActivity(saActivity);
                finish();
            }
        });
    }

    private HashMap<String, SensorType> getSelectedSensorsFromData(JSONArray idfs) throws JSONException {
        HashMap<String, SensorType>sensors=new HashMap<>();

        outer:
        for (int i =0; i<idfs.length(); i++){
            String dfName=idfs.getJSONArray(i).getString(0);
            //check is in StreamSensorType
            for (StreamSensorType sensorType: StreamSensorType.values()) {
                if (dfName.contains(sensorType.semanticAlias())){
                    /****************************************
                    * there is a mismatch bug in edutalk !!!
                     * so replace _ with - temporarily,
                     * and notice that edutalk need timestamp in StreamSensor !
                     * ***************************************
                    * */
                    sensors.put(dfName.replace("_", "-"), new SensorType(sensorType, true));
                    continue outer;
                }
            }
            //check is in RangeSensorType
            for (RangeSensorType sensorType: RangeSensorType.values()) {
                if (dfName.contains(sensorType.semanticAlias())){
                    sensors.put(dfName, new SensorType(sensorType));
                    continue outer;
                }
            }
        }
        return sensors;
    }

    private void terminateWithError(){
        runOnUiThread(()->{
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("there are some errors in your app link !");
            builder.setPositiveButton("exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EduTalkBridgeAppLink.this.finish();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        });
    }
}
