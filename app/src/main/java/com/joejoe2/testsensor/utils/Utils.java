package com.joejoe2.testsensor.utils;

import android.os.Environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import de.siegmar.fastcsv.writer.CsvWriter;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Utils {
    public static float calculateSensorFrequency(long lastTimeNS, long currentTimeNS) {
        return 1 / ((currentTimeNS - lastTimeNS) / 1000000000.0f);
    }

    public static float calculateSensorIntervalMS(long lastTimeNS, long currentTimeNS) {
        return (currentTimeNS - lastTimeNS)/ 1000000.0f;
    }

    public static void saveTestSampleTimeData(ArrayList<Float> sampleTimes, String filename){
        try {
            Path path = Files.createTempFile(filename, ".csv");
            try (CsvWriter csv = CsvWriter.builder().build(path, StandardCharsets.UTF_8)) {
                csv.writeRow("ms");
                for (float f : sampleTimes) {
                    csv.writeRow(String.valueOf(f));
                }
            }
            Files.copy(path, Paths.get(Environment.getExternalStorageDirectory().toString()+"/"+filename+".csv"), REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
