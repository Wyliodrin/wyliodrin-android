package com.wyliodrin.mobileapp.widgets;

import org.json.JSONObject;

/**
 * Created by andreea on 12/10/14.
 */
public interface Widget {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_THERMOMETER = 1;
    public static final int TYPE_GRAPH = 2;
    public static final int TYPE_BUTTON = 3;
    public static final int TYPE_TOGGLE_BUTTON = 4;
    public static final int TYPE_SEEK_BAR = 5;
    public static final int TYPE_SPEEDOMETER = 6;
    public static final int TYPE_SENSOR = 7;

    public JSONObject toJson ();
    public void setLabel (String label);
    public String getLabel();
    public int getType ();
}
