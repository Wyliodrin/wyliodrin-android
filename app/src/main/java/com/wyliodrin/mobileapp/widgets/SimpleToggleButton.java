package com.wyliodrin.mobileapp.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import com.wyliodrin.mobileapp.DashboardActivity;
import com.wyliodrin.mobileapp.R;
import com.wyliodrin.mobileapp.api.ServerConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andreea Stoican on 20.04.2015.
 */
public class SimpleToggleButton extends ToggleButton implements OutputDataWidget {

    private String textON;
    private String textOFF;
    private int width;
    private int height;
    private String label;

    public SimpleToggleButton(final Context context) {
        super(context);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isChecked())
                    sendData("" + 1);
                else
                    sendData("" + 0);
            }
        });
    }

    public static void showAddDialog(final Activity activity, final LinearLayout layout, final View.OnLongClickListener onLongClick, final ArrayList<Widget> objects, final View view) {

        final ScrollView scroll = new ScrollView(activity);
        scroll.setBackgroundColor(android.R.color.transparent);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle));
        alertDialogBuilder.setTitle("Choose the button properties");

        LayoutInflater inflater= LayoutInflater.from(activity);
        final View alert_dialog_xml =inflater.inflate(R.layout.alert_dialog_simple_toggle_button, null);
        alertDialogBuilder.setView(alert_dialog_xml);

        alertDialogBuilder.setPositiveButton("Done", null);

        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        String width = "";
                        String height = "";
                        String textOn = "";
                        String textOff = "";
                        String label = "";

                        EditText widthEditText = (EditText) alert_dialog_xml.findViewById(R.id.width);
                        if (widthEditText != null) {
                            width = widthEditText.getText().toString();

                            if (width.isEmpty())
                                widthEditText.setError("Width is required");
                        }

                        EditText heightEditText = (EditText) alert_dialog_xml.findViewById(R.id.height);
                        if (heightEditText != null) {
                            height = heightEditText.getText().toString();

                            if (height.isEmpty())
                                heightEditText.setError("Height is required");
                        }

                        EditText textOnButtOn = (EditText) alert_dialog_xml.findViewById(R.id.textON);
                        if (textOnButtOn != null) {
                            textOn = textOnButtOn.getText().toString();

                            if (textOn.isEmpty())
                                textOnButtOn.setError("Text button on is required");
                        }

                        EditText textOnButtOff = (EditText) alert_dialog_xml.findViewById(R.id.textOFF);
                        if (textOnButtOff != null) {
                            textOff = textOnButtOff.getText().toString();

                            if (textOff.isEmpty())
                                textOnButtOff.setError("Text button off is required");
                        }

                        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
                        if (labelEditText != null) {
                            label = labelEditText.getText().toString();

                            if (label.isEmpty())
                                labelEditText.setError("Label is required");
                        }

                        if (!width.isEmpty() && !height.isEmpty() && !textOn.isEmpty() && !textOff.isEmpty() && !label.isEmpty()) {

                            int pos = -1;
                            if (view != null) {
                                LinearLayout layout = (LinearLayout) activity.findViewById(R.id.widgetsContainer);
                                pos = layout.indexOfChild(view);
                                layout.removeView(view);
                                objects.remove(view);
                            }

                            addToBoard(activity, layout, onLongClick, objects, Integer.parseInt(width), Integer.parseInt(height), textOn, textOff, label, pos);

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
        int count = ((DashboardActivity) activity).getWidgetsCount(TYPE_TOGGLE_BUTTON);
        if (count == 0)
            labelEditText.setText("toggleButton");
        else
            labelEditText.setText("toggleButton_" + count);

        if (view != null) {
            SimpleToggleButton toggleButton = (SimpleToggleButton) view;

            labelEditText.setText(toggleButton.getLabel());

            EditText widthEditText = (EditText) alert_dialog_xml.findViewById(R.id.width);
            EditText heightEditText = (EditText) alert_dialog_xml.findViewById(R.id.height);
            EditText textOnEditText = (EditText) alert_dialog_xml.findViewById(R.id.textON);
            EditText textOffEditText = (EditText) alert_dialog_xml.findViewById(R.id.textOFF);

            widthEditText.setText("" + toggleButton.getWidth());
            heightEditText.setText("" + toggleButton.getHeight());
            textOnEditText.setText(toggleButton.getTextOn());
            textOffEditText.setText(toggleButton.getTextOff());
        }

        alertDialog.show();
    }

    public static void addToBoard(Activity activity, LinearLayout layout, OnLongClickListener onLongClick, ArrayList<Widget> objects,
                                  int width, int height, String buttonTextOn, String buttonTextOff, String label, int pos) {

        SimpleToggleButton simpleToggleButton = new SimpleToggleButton(activity);
        simpleToggleButton.setBackgroundResource(R.drawable.togglebutton_bg);

        simpleToggleButton.setTextOn(buttonTextOn);
        simpleToggleButton.setTextOff(buttonTextOff);
        simpleToggleButton.setLabel(label);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(0, 5, 0, 0);
        simpleToggleButton.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        simpleToggleButton.setLayoutParams(params);
        simpleToggleButton.setOnLongClickListener(onLongClick);

        simpleToggleButton.width = width;
        simpleToggleButton.height = height;
        simpleToggleButton.textON = buttonTextOn;
        simpleToggleButton.textOFF = buttonTextOff;
        simpleToggleButton.setTextColor(Color.WHITE);

        if (pos != -1) {
            layout.addView(simpleToggleButton, pos);
            objects.add(pos, simpleToggleButton);
        } else {
            layout.addView(simpleToggleButton);
            objects.add(simpleToggleButton);
        }

        simpleToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    compoundButton.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                } else {
                    compoundButton.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                }
            }
        });

        ((DashboardActivity)activity).saveBoard(DashboardActivity.currentBoardName);
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj=new JSONObject();
        try {
            obj.put("type", TYPE_TOGGLE_BUTTON);
            obj.put("width", width);
            obj.put("height", height);
            obj.put("text_button_on", textON);
            obj.put("text_button_off", textOFF);
            obj.put("label", label);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getType() {
        return TYPE_TOGGLE_BUTTON;
    }

    @Override
    public void sendData(String message) {
        ServerConnection.getInstance().sendStringMessage(label, message);
    }

}
