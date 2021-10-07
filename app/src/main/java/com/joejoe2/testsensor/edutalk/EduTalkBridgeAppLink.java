package com.joejoe2.testsensor.edutalk;

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
import com.joejoe2.testsensor.R;
import com.joejoe2.testsensor.sa.EduTalkSmartphoneSA;
import com.joejoe2.testsensor.sensor.BaseSensorType;
import com.joejoe2.testsensor.sensor.streamsensor.StreamSensorType;
import com.joejoe2.testsensor.sensor.triggersensor.TriggerSensorType;
import com.joejoe2.testsensor.utils.PermissionManager;
import com.joejoe2.testsensor.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EduTalkBridgeAppLink extends AppCompatActivity {
    EduTalkRCConfig eduTalkRCConfig;
    HashMap<String, BaseSensorType> supportedDFSenosrs = new HashMap<>();
    final Integer[] SAMPLE_RATES = new Integer[]{1, 10, 25, 50, 100, 200, 300, 400, -1};

    TextView applinkTextView;
    Button startSAButton;
    Spinner sampleRateSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_link);
        PermissionManager.requestPermissions(this, PermissionManager.getLackingPermissions(this));
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
                eduTalkRCConfig = EduTalkService.fetchRCConfigNew(rc_index_url+"&app=true");
                supportedDFSenosrs = DFtoSensors(new JSONArray(eduTalkRCConfig.idf_list));
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
        applinkTextView = findViewById(R.id.applinkTextView);

        applinkTextView.setText("supported features are ready:\n\n"+ supportedDFSenosrs.keySet());

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
                bundle.putSerializable("selectedSensors", supportedDFSenosrs);
                bundle.putInt("sampleRate", (Integer) sampleRateSpinner.getSelectedItem());
                saActivity.putExtras(bundle);
                startActivity(saActivity);
                finish();
            }
        });
    }

    private HashMap<String, BaseSensorType> DFtoSensors(JSONArray idfs) throws JSONException {
        HashMap<String, BaseSensorType> df2sensors=new HashMap<>();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        outer:
        for (int i =0; i<idfs.length(); i++){
            /****************************************
             * there is a mismatch dfName in edutalk (idf_list vs joins) !!!
             * so replace _ with - temporarily
             * ***************************************
             * */
            String dfName=idfs.getJSONArray(i).getString(0).replace("_", "-");
            //check df is in StreamSensorType
            for (StreamSensorType streamSensorType: StreamSensorType.values()) {
                if (dfName.contains(streamSensorType.getDFAlias())&&Utils.isSensorAvailable(sensorManager, streamSensorType.getNativeSensorCode())){
                    streamSensorType.setNeedTimestamp(true);
                    df2sensors.put(dfName, streamSensorType);
                    continue outer;
                }
            }
            //check df is in RangeSensorType
            for (TriggerSensorType triggerSensorType: TriggerSensorType.values()) {
                if (dfName.contains(triggerSensorType.getDFAlias())){
                    df2sensors.put(dfName, triggerSensorType);
                    continue outer;
                }
            }
        }
        return df2sensors;
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
        builder.create().show();
    }
}
