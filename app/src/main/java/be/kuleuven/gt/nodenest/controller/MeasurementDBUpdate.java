package be.kuleuven.gt.nodenest.controller;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import be.kuleuven.gt.nodenest.view.SensorPressureActivity;

public class MeasurementDBUpdate {
    private static String POST_URL_DEVICE = "https://studev.groept.be/api/a23PT103/modifyLastMeasurement/";
    private static String POST_URL_MEASUREMENT = "https://studev.groept.be/api/a23PT103/addMeasurement/";
    //updating table measurements in the db with newly arrived value
    public static void updateDBmeasurement(Context context, int deviceID, String value) {
        String POST_URL = POST_URL_MEASUREMENT + value + '/' + String.valueOf(deviceID);

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        StringRequest submitRequest = new StringRequest(
                Request.Method.GET,
                POST_URL,
                response -> {
                    //Toast.makeText(context, "Log successful.", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    //Toast.makeText(context, "Log failed. " + error, Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(submitRequest);
    }
    //updating table devices, column latestmeasurement in the db with newly arrived value
    public static void updateDBdevice(Context context, int deviceID, String value) {
        String POST_URL = POST_URL_DEVICE + value + '/' + String.valueOf(deviceID);

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        StringRequest submitRequest = new StringRequest(
                Request.Method.GET,
                POST_URL,
                response -> {
                    //Toast.makeText(context, "Log successful.", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    //Toast.makeText(context, "Log failed. " + error, Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(submitRequest);
    }
}
