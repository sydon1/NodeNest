package be.kuleuven.gt.nodenest.model;

import be.kuleuven.gt.nodenest.controller.MqttCallbackImpl;

// singleton containing information about the mqtt connection and connecting with mqtt handlers
public final class Session {
    private static Session instance;
    public MqttCallbackImpl mqttCallback;

    private Session(MqttCallbackImpl mqttCallback) {
        this.mqttCallback = mqttCallback;
    }

    public static Session getInstance(MqttCallbackImpl mqttCallback) {
        if (instance == null) {
            instance = new Session(mqttCallback);
        }
        return instance;
    }
}
