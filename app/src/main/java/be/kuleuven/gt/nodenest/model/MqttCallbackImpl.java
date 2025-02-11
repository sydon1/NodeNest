package be.kuleuven.gt.nodenest.model;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.util.Log;

import be.kuleuven.gt.nodenest.view.LoginActivity;

public class MqttCallbackImpl implements MqttCallback {
    public LoginActivity handler;
    public MqttCallbackImpl(LoginActivity mainActivity) {
        this.handler = mainActivity;
    }
    public boolean connect(String domain, int port,
                           MqttConnectOptions mqttConnectOptions){
        String TAG = "TAGmqtt";
        Log.d(TAG, "connect was invoked");
        try {
            client = new MqttClient("tcp://" + domain +":" + port,
                    MqttClient.generateClientId(),
                    new MemoryPersistence() );
            client.setCallback(this);
            client.connect(mqttConnectOptions);
            return true;
        } catch (MqttException e) {
            return false;
        }
    }
    public boolean my_subscribe(String topic){
        String TAG = "TAGmqtt";
        Log.d(TAG, "subscribe was invoked");
        try {
            client.subscribe(topic);
            return true;
        } catch (MqttException e) {
            return false;
        }
    }
    public boolean my_unsubscribe(String topic){
        String TAG = "TAGmqtt";
        Log.d(TAG, "unsubscribe was invoked");
        try {
            client.unsubscribe(topic);
            return true;
        } catch (MqttException e) {
            return false;
        }
    }
    public boolean my_publish(String topic, String message){
        String TAG = "TAGmqtt";
        Log.d(TAG, "publish was invoked");
        try {
            Log.d(TAG, "publish was succesfull");
            client.publish(topic, new MqttMessage(message.getBytes()));
            return true;
        } catch (MqttException e) {
            Log.d(TAG, "publish was unsuccesfull");
            return false;
        }
    }
    MqttClient client;
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
    }

    @Override
    public void connectionLost(Throwable cause) {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
    public boolean disConnect() {
        String TAG = "TAGmqtt";
        Log.d(TAG, "disconnect was invoked");
        try {
            client.disconnect();
            return true;
        } catch (MqttException e) {
            return false;
        } }
}
