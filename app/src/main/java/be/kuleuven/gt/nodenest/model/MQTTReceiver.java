package be.kuleuven.gt.nodenest.model;

import android.os.Parcelable;

import androidx.appcompat.app.AppCompatActivity;

import kotlin.text.UStringsKt;

// interface for all activities meant to use mqtt
public interface MQTTReceiver {
    public void ReceiveFromMQTT(String topic, String payload);
    public void RetryConnection();
}
