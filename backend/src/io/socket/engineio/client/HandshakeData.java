package io.socket.engineio.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class HandshakeData {

    public String sid;
    public String[] upgrades;
    public long pingInterval;
    public long pingTimeout;

    /*package*/ HandshakeData(String data) {
        this(new JsonParser().parse(data).getAsJsonObject());
    }

    /*package*/ HandshakeData(JsonObject data) {
        JsonArray upgrades = data.getAsJsonArray("upgrades");
        int length = upgrades.size();
        String[] tempUpgrades = new String[length];
        for (int i = 0; i < length; i ++) {
            tempUpgrades[i] = upgrades.get(i).getAsString();
        }

        this.sid = data.get("sid").getAsString();
        this.upgrades = tempUpgrades;
        this.pingInterval = data.get("pingInterval").getAsLong();
        this.pingTimeout = data.get("pingTimeout").getAsLong();
    }
}
