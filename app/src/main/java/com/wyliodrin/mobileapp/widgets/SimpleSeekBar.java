package com.wyliodrin.mobileapp.widgets;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wyliodrin.mobileapp.DashboardActivity;
import com.wyliodrin.mobileapp.R;
import com.wyliodrin.mobileapp.api.ServerConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andreea Stoican on 21.04.2015.
 */
public class SimpleSeekBar extends RelativeLayout implements OutputDataWidget {

    private int maxValue;
    private int width;
    private String label;

    public SimpleSeekBar(Context context, int width, int maxValue) {
        this(context, null, width, maxValue);
    }

    public SimpleSeekBar(final Context context, AttributeSet attrs, int width, int maxValue) {
        super(context, attrs);

        this.width = width;
        this.maxValue = maxValue;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.seek_bar_layout, this, true);

        SeekBar seekBar = (SeekBar)findViewById(R.id.seek_bar);
        seekBar.setMax(maxValue);

        final TextView result = (TextView) findViewById(R.id.result);
        TextView minValueTextView = (TextView) findViewById(R.id.min);
        TextView maxValueTextView = (TextView) findViewById(R.id.max);

        result.setText("Value: 0");

        minValueTextView.setText("0");

        maxValueTextView.setText(maxValue + "");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                result.setText("Value:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String value = result.getText().toString().replace("Value:", "");
                sendData(value);
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void showAddDialog(final Activity activity, final LinearLayout layout, final View.OnLongClickListener onLongClick, final ArrayList<Widget> objects, final View view) {

        final ScrollView scroll = new ScrollView(activity);
        scroll.setBackgroundColor(android.R.color.transparent);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle));
        alertDialogBuilder.setTitle("Choose seekbar properties");

        LayoutInflater inflater= LayoutInflater.from(activity);
        final View alert_dialog_xml =inflater.inflate(R.layout.alert_dialog_seek_bar, null);
        alertDialogBuilder.setView(alert_dialog_xml);

        alertDialogBuilder.setPositiveButton("Done", null);

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
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
                        String maxValue = "";
                        String label = "";

                        EditText widthEditText = (EditText) alert_dialog_xml.findViewById(R.id.width);
                        if (widthEditText != null) {
                            width = widthEditText.getText().toString();

                            if (width.isEmpty())
                                widthEditText.setError("Width is required");
                        }

                        EditText maxValueButton = (EditText) alert_dialog_xml.findViewById(R.id.max_value);
                        if (maxValueButton != null) {
                            maxValue = maxValueButton.getText().toString();

                            if (maxValue.isEmpty())
                                maxValueButton.setError("Max value is required");
                        }

                        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
                        if (labelEditText != null) {
                            label = labelEditText.getText().toString();

                            if (label.isEmpty())
                                labelEditText.setError("Label is required");
                        }

                        if (!width.isEmpty() && !maxValue.isEmpty() && !label.isEmpty()) {

                            int pos = -1;
                            if (view != null) {
                                LinearLayout layout = (LinearLayout) activity.findViewById(R.id.widgetsContainer);
                                pos = layout.indexOfChild(view);
                                layout.removeView(view);
                                objects.remove(view);
                            }

                            addToBoard(activity, layout, onLongClick, objects, Integer.parseInt(width), Integer.parseInt(maxValue), label, pos);

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

        EditText widthEditText = (EditText) alert_dialog_xml.findViewById(R.id.width);
        widthEditText.setText(Integer.toString(width));

        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
        int count = ((DashboardActivity) activity).getWidgetsCount(TYPE_SEEK_BAR);
        if (count == 0)
            labelEditText.setText("seekbar");
        else
            labelEditText.setText("seekbar_" + count);

        if (view != null) {
            SimpleSeekBar seekBar = (SimpleSeekBar) view;

            labelEditText.setText(seekBar.getLabel());
            widthEditText.setText("" + seekBar.getWidth());

            EditText maxValueEditText = (EditText) alert_dialog_xml.findViewById(R.id.max_value);
            maxValueEditText.setText("" + seekBar.getMax());
        }

        alertDialog.show();
    }

    public static void addToBoard(Activity activity, LinearLayout layout, OnLongClickListener onLongClick, ArrayList<Widget> objects,
                                  int width, int max, String label, int pos) {
        SimpleSeekBar simpleSeekBar = new SimpleSeekBar(activity, width, max);

        simpleSeekBar.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        simpleSeekBar.setOnLongClickListener(onLongClick);
        simpleSeekBar.setLabel(label);

        if (pos != -1) {
            layout.addView(simpleSeekBar, pos);
            objects.add(pos, simpleSeekBar);
        } else {
            layout.addView(simpleSeekBar);
            objects.add(simpleSeekBar);
        }

        ((DashboardActivity)activity).saveBoard(DashboardActivity.currentBoardName);
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj=new JSONObject();
        try {
            obj.put("type", TYPE_SEEK_BAR);
            obj.put("width", width);
            obj.put("max_value", maxValue);
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
        return TYPE_SEEK_BAR;
    }

    @Override
    public void sendData(String message) {
        ServerConnection.getInstance().sendStringMessage(label, message);
    }

    public int getMax() {
        return maxValue;
    }
}
