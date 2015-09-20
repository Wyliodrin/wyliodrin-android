package com.wyliodrin.mobileapp.widgets;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.wyliodrin.mobileapp.DashboardActivity;
import com.wyliodrin.mobileapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andreea Stoican on 31.03.2015.
 */
public class Thermometer extends RelativeLayout implements InputDataWidget {

    private int width;
    private int height;

    private double min;
    private double max;

    private String label;

    private ArrayList<TextView> labels;
    private TextView currentValueTextView;


    private double value;

    public Thermometer(Context context) {
        this(context, null);
    }

    public Thermometer(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        labels = new ArrayList<TextView>();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.thermometer_layout, this, true);

        currentValueTextView = new TextView(getContext());
        addView(currentValueTextView);

    }
    public Thermometer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setLimits(double min, double max) {
        this.max = max;
        this.min = min;

        for (TextView text : labels) {
            removeView(text);
        }
        labels.clear();

        double step = (max - min) / 9;
        double x = max;
        for (int i=0; i < 10; i++) {

            if (x < min)
                x = min;

            TextView text = new TextView(getContext());
            text.setText("" + (int) x);
            text.setTextColor(R.color.black);

            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(ALIGN_PARENT_RIGHT);
            params.topMargin = (int) (0.1 * height) + i * (int) (0.075 * height);
            text.setLayoutParams(params);

            addView(text);
            labels.add(text);

            x -= step;
        }

        setValue(min);
    }

    public void setValue(double value) {
        this.value = value;

        if(value < min) value = min;
        if(value > max) value = max;

        currentValueTextView.setText((int) value + " °C");
        currentValueTextView.setTextColor(R.color.black);

        LayoutParams paramsTextView = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsTextView.addRule(ALIGN_PARENT_BOTTOM);
        paramsTextView.bottomMargin = (int) (0.06 * height);
        paramsTextView.leftMargin = (int) (0.21 * width);
        currentValueTextView.setLayoutParams(paramsTextView);

        ImageView bar = (ImageView) findViewById(R.id.thermometer_bar);

        LayoutParams params = (LayoutParams) bar.getLayoutParams();
        params.width = (int)(0.226 * width);
        params.height = (int) ((0.065 * height) + ((value - min) / (max - min) * 0.67 * height));
        params.leftMargin = (int) (0.152 * width);
        params.bottomMargin = (int) (0.145 * height);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bar.setLayoutParams(params);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void addData(String message) {
        try {
            setValue(Double.parseDouble(message));
        } catch (Exception e) {
            try {
                setValue(Integer.parseInt(message));
            } catch (Exception e2) {
                setValue(min);
            }
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void showAlertDialog(final Activity activity, final LinearLayout layout, final OnLongClickListener onLongClick,
                                       final ArrayList<Widget> objects, final View view) {

        ScrollView scroll = new ScrollView(activity);
        scroll.setBackgroundColor(android.R.color.transparent);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle));
        alertDialogBuilder.setTitle("Choose the thermometer properties");

        LayoutInflater inflater= LayoutInflater.from(activity);
        final View alert_dialog_xml =inflater.inflate(R.layout.alert_dialog_thermometer, null);
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
                        String minDegree = "";
                        String maxDegree = "";
                        String label = "";

                        EditText widthEditText = (EditText) alert_dialog_xml.findViewById(R.id.thermometer_width);
                        if (widthEditText != null) {
                            width = widthEditText.getText().toString();

                            if (width.isEmpty())
                                widthEditText.setError("Width is required");
                        }

                        EditText heightEditText = (EditText) alert_dialog_xml.findViewById(R.id.thermometer_height);
                        if (heightEditText != null) {
                            height = heightEditText.getText().toString();

                            if (height.isEmpty())
                                heightEditText.setError("Height is required");
                        }

                        EditText minDegreeEditText = (EditText) alert_dialog_xml.findViewById(R.id.thermometer_min);
                        if (minDegreeEditText != null) {
                            minDegree = minDegreeEditText.getText().toString();

                            if (minDegree.isEmpty())
                                minDegreeEditText.setError("Min degree is required");
                        }

                        EditText maxDegreeEditText = (EditText) alert_dialog_xml.findViewById(R.id.thermometer_max);
                        if (maxDegreeEditText != null) {
                            maxDegree = maxDegreeEditText.getText().toString();

                            if (maxDegree.isEmpty())
                                maxDegreeEditText.setError("Max degree is required");
                        }

                        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
                        if (labelEditText != null) {
                            label = labelEditText.getText().toString();

                            if (label.isEmpty())
                                labelEditText.setError("Label is required");
                        }

                        if (!width.isEmpty() && !height.isEmpty() && !minDegree.isEmpty() && !maxDegree.isEmpty() && !label.isEmpty()) {

                            int pos = -1;
                            if (view != null) {
                                LinearLayout layout = (LinearLayout) activity.findViewById(R.id.widgetsContainer);
                                pos = layout.indexOfChild(view);
                                layout.removeView(view);
                                objects.remove(view);
                            }

                            addToBoard(activity, layout, onLongClick, objects,
                                    Integer.parseInt(width), Integer.parseInt(height),
                                    Float.parseFloat(maxDegree), Float.parseFloat(minDegree), label, pos);

                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        EditText widthEditText = (EditText) alert_dialog_xml.findViewById(R.id.thermometer_width);
        int newWidth = (int) (width * 0.3);
        widthEditText.setText(Integer.toString(newWidth));

        EditText heightEditText = (EditText) alert_dialog_xml.findViewById(R.id.thermometer_height);
        int newHeight = (int) (height * 0.5);
        heightEditText.setText(Integer.toString(newHeight));

        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
        int count = ((DashboardActivity) activity).getWidgetsCount(TYPE_THERMOMETER);
        if (count == 0)
            labelEditText.setText("thermometer");
        else
            labelEditText.setText("thermometer_" + count);

        if (view  != null) {
            Thermometer thermo = (Thermometer) view;
            labelEditText.setText(thermo.getLabel());

            widthEditText.setText("" + thermo.getWidth());
            heightEditText.setText("" + thermo.getHeight());

            EditText minEditText = (EditText) alert_dialog_xml.findViewById(R.id.thermometer_min);
            minEditText.setText("" + (int) thermo.getMin());

            EditText maxEditText = (EditText) alert_dialog_xml.findViewById(R.id.thermometer_max);
            maxEditText.setText("" + (int) thermo.getMax());
        }

        alertDialog.show();
    }

    public static void addToBoard(Activity activity, LinearLayout layout, OnLongClickListener onLongClick, ArrayList<Widget> objects,
                             int width, int height, double maxDegree, double minDegree, String label, int pos) {

        Thermometer thermometer = new Thermometer(activity);
        thermometer.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        thermometer.setPadding(2, 2, 2, 2);
        thermometer.setSize(width, height);
        thermometer.setLimits(minDegree, maxDegree);
        thermometer.setLabel(label);
        if (pos != -1) {
            layout.addView(thermometer, pos);
            objects.add(pos, thermometer);
        } else {
            layout.addView(thermometer);
            objects.add(thermometer);
        }
        thermometer.setOnLongClickListener(onLongClick);

        ((DashboardActivity)activity).saveBoard(DashboardActivity.currentBoardName);
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj=new JSONObject();
        try {
            obj.put("type", TYPE_THERMOMETER);
            obj.put("width", width);
            obj.put("height", height);
            obj.put("minDegree", min);
            obj.put("maxDegree", max);
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
    public int getType() {
        return TYPE_THERMOMETER;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
