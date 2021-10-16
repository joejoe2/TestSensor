package com.edutalk.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Environment;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

import de.siegmar.fastcsv.writer.CsvWriter;
import iottalk.AppID;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Utils {
    public static AppID getDeviceAddress(Context context, String host, String deviceModel, String deviceName, boolean usePersistent){
        if (!usePersistent)return new AppID();

        SharedPreferences sharedPreferences = context.getSharedPreferences("DeviceAddress", Context.MODE_PRIVATE);
        String key = host+"|"+deviceModel+"|"+deviceName;
        String deviceAddress = sharedPreferences.getString(key, null);
        if (deviceAddress!=null){
            return new AppID(deviceAddress.replace("-", ""), true);
        }else {
            AppID appID = new AppID(UUID.randomUUID().toString().replace("-", ""), true);
            SharedPreferences.Editor editor =  sharedPreferences.edit();
            editor.putString(key, appID.getUUID().toString());
            editor.apply();
            return appID;
        }
    }

    public static void saveTimeStampData(ArrayList<Long> sampleTimes, ArrayList<Long> sendTimes, String filename){
        try {
            Path path = Files.createTempFile(System.nanoTime()+filename, ".csv");
            try (CsvWriter csv = CsvWriter.builder().build(path, StandardCharsets.UTF_8)) {
                csv.writeRow("sample", "send", "send delay");
                for (int i=0; i<sampleTimes.size(); i++) {
                    csv.writeRow(String.valueOf(sampleTimes.get(i)), String.valueOf(sendTimes.get(i)), String.valueOf(sendTimes.get(i)-sampleTimes.get(i)));
                }
                double sampleRate = sampleTimes.size()/((sampleTimes.get(sendTimes.size()-1)-sampleTimes.get(0))/1000.0);
                csv.writeRow("avg sample rate:", String.valueOf(sampleRate));
            }
            Files.copy(path, Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+filename+".csv"), REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSensorAvailable(SensorManager sensorManager, int nativeSensorCode){
        return sensorManager.getDefaultSensor(nativeSensorCode)!=null;
    }
}
