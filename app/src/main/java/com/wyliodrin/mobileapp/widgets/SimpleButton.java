package com.wyliodrin.mobileapp.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.wyliodrin.mobileapp.DashboardActivity;
import com.wyliodrin.mobileapp.R;
import com.wyliodrin.mobileapp.api.ServerConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andreea Stoican on 07.04.2015.
 */
public class SimpleButton extends Button implements OutputDataWidget {

    private String textButton;
    private int width;
    private int height;
    private String label;

    public SimpleButton(Context context) {
        super(context);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData("pressed");
            }
        });
    }

    public static void showAddDialog(final Activity activity, final LinearLayout layout, final View.OnLongClickListener onLongClick,
                                     final ArrayList<Widget> objects, final View view) {

        final ScrollView scroll = new ScrollView(activity);
        scroll.setBackgroundColor(android.R.color.transparent);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle));
        alertDialogBuilder.setTitle("Choose the button properties");

        LayoutInflater inflater= LayoutInflater.from(activity);
        final View alert_dialog_xml =inflater.inflate(R.layout.alert_dialog_simple_button, null);
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
                        String text = "";
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

                        EditText textButton = (EditText) alert_dialog_xml.findViewById(R.id.text);
                        if (textButton != null) {
                            text = textButton.getText().toString();

                            if (text.isEmpty())
                                textButton.setError("Text button is required");
                        }

                        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
                        if (labelEditText != null) {
                            label = labelEditText.getText().toString();

                            if (label.isEmpty())
                                labelEditText.setError("Label is required");
                        }

                        if (!width.isEmpty() && !height.isEmpty() && !text.isEmpty() && !label.isEmpty()) {

                            int pos = -1;
                            if (view != null) {
                                LinearLayout layout = (LinearLayout) activity.findViewById(R.id.widgetsContainer);
                                pos = layout.indexOfChild(view);
                                layout.removeView(view);
                                objects.remove(view);
                            }

                            addToBoard(activity, layout, onLongClick, objects, Integer.parseInt(width), Integer.parseInt(height), text, label, pos);

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
        int count = ((DashboardActivity) activity).getWidgetsCount(TYPE_BUTTON);
        if (count == 0)
            labelEditText.setText("button");
        else
            labelEditText.setText("button_" + count);

        if (view != null) {
            SimpleButton btn = (SimpleButton) view;


            EditText widthEditText = (EditText) alert_dialog_xml.findViewById(R.id.width);
            EditText heightEditText = (EditText) alert_dialog_xml.findViewById(R.id.height);
            EditText btnTextEditText = (EditText) alert_dialog_xml.findViewById(R.id.text);

            widthEditText.setText("" + btn.getWidth());
            heightEditText.setText("" + btn.getHeight());
            btnTextEditText.setText(btn.getText());

            labelEditText.setText(btn.getLabel());
        }

        alertDialog.show();
    }

    public static void addToBoard(Activity activity, LinearLayout layout, OnLongClickListener onLongClick, ArrayList<Widget> objects,
                                  int width, int height, String buttonText, String label, int pos) {

        SimpleButton simpleButton = new SimpleButton(activity);
        simpleButton.setText(buttonText);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(0, 5, 0, 0);

        simpleButton.setLayoutParams(params);
        simpleButton.setOnLongClickListener(onLongClick);
        simpleButton.setLabel(label);

        simpleButton.setTextColor(Color.WHITE);
        simpleButton.setBackgroundResource(R.drawable.simple_button_background);


        simpleButton.width = width;
        simpleButton.height = height;
        simpleButton.textButton = buttonText;

        if (pos != -1) {
            layout.addView(simpleButton, pos);
            objects.add(pos, simpleButton);
        } else {
            layout.addView(simpleButton);
            objects.add(simpleButton);
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj=new JSONObject();
        try {
            obj.put("type", TYPE_BUTTON);
            obj.put("width", width);
            obj.put("height", height);
            obj.put("text_button", textButton);
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
        return TYPE_BUTTON;
    }

    @Override
    public void sendData(String message) {
        ServerConnection.getInstance().sendStringMessage(label, message);
    }
}
