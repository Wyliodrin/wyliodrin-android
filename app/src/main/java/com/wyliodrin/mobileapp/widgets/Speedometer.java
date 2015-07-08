package com.wyliodrin.mobileapp.widgets;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.Typeface;
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
import android.widget.ScrollView;

import com.wyliodrin.mobileapp.DashboardActivity;
import com.wyliodrin.mobileapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andreea Stoican on 14.05.2015.
 */
public class Speedometer extends View implements InputDataWidget {
    private Paint paint, scalePaint;
    private Shader shader = null;
    private Paint innerCirclePaint, scaleTextPaint;

    private static final int totalLines = 60;
    private static final float degreesPerLine = 360.0f / totalLines;
    private int currentValue;

    private float minValue = 0;
    private float maxValue = 40;

    private Path indicatorPath;
    private Paint indicatorPaint;
    private Paint indicatorScrewPaint;
    private int diameter;

    private String label;

    public Speedometer(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();

        innerCirclePaint = new Paint();
        innerCirclePaint.setAntiAlias(true);
        innerCirclePaint.setColor(Color.WHITE);
        innerCirclePaint.setStyle(Paint.Style.FILL);
    }

    public void setLimits(float min, float max) {
        this.minValue = min;
        this.maxValue = max;
        this.currentValue = (int) min;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        diameter = getWidth();

        if (shader == null) {
            shader = new LinearGradient(0, 0, 0, diameter, Color.rgb(192,192,192), Color.rgb(112,112,112), Shader.TileMode.CLAMP);
            paint.setShader(shader);

            scalePaint = new Paint();
            scalePaint.setStyle(Paint.Style.STROKE);
            scalePaint.setColor(0x9f004d0f);
            scalePaint.setStrokeWidth(0.01f * diameter);
            scalePaint.setAntiAlias(true);

            scaleTextPaint = new Paint();
            scaleTextPaint.setTypeface(Typeface.SANS_SERIF);
            scaleTextPaint.setColor(0x9f004d0f);
            scaleTextPaint.setTextSize(0.07f * diameter);
            scaleTextPaint.setTextAlign(Paint.Align.CENTER);

            indicatorPath = new Path();
            indicatorPath.moveTo(0.5f * diameter, 0.5f * diameter + 0.2f * diameter);
            indicatorPath.lineTo(0.5f * diameter - 0.010f * diameter, 0.5f * diameter + 0.2f * diameter - 0.007f * diameter);
            indicatorPath.lineTo(0.5f * diameter - 0.002f * diameter, 0.5f * diameter - 0.32f * diameter);
            indicatorPath.lineTo(0.5f * diameter + 0.002f * diameter, 0.5f * diameter - 0.32f * diameter);
            indicatorPath.lineTo(0.5f * diameter + 0.010f * diameter, 0.5f * diameter + 0.2f * diameter - 0.007f * diameter);
            indicatorPath.lineTo(0.5f * diameter, 0.5f * diameter + 0.2f * diameter);
            indicatorPath.addCircle(0.5f * diameter, 0.5f * diameter, 0.025f * diameter, Path.Direction.CW);

            indicatorPaint = new Paint();
            indicatorPaint.setAntiAlias(true);
            indicatorPaint.setColor(0xff392f2c);
            indicatorPaint.setShadowLayer(0.01f, -0.005f, -0.005f, 0x7f000000);
            indicatorPaint.setStyle(Paint.Style.FILL);

            indicatorScrewPaint = new Paint();
            indicatorScrewPaint.setAntiAlias(true);
            indicatorScrewPaint.setColor(0xff493f3c);
            indicatorScrewPaint.setStyle(Paint.Style.FILL);
        }

        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2, paint);
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2 - (float) (0.02 * diameter), innerCirclePaint);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        int n = 1;
        int m = 3;
        for (int i = 0; i < totalLines; ++i) {
            float y1 = 0.15f * diameter;
            float y2 = y1 + 0.035f * diameter;
            boolean shown = false;

            if (i % 5 == 0) {
                if (n < 6 || n > 8) {
                    shown = true;
                    m ++;
                    if (n == 9)
                        m = 0;
                }

                n++;
            }

            float value = m * (maxValue - minValue) / 8 + minValue;

            if (shown)
                y1 -= 0.02 * diameter;

            canvas.drawLine(0.5f * diameter, y1, 0.5f * diameter, y2, scalePaint);

            if (shown) {
                String valueString = Integer.toString((int) value);
                canvas.drawText(valueString, 0.5f * diameter, y2 - 0.07f * diameter, scaleTextPaint);
            }

            canvas.rotate(degreesPerLine, 0.5f * diameter, 0.5f * diameter);
        }
        canvas.restore();

        float currentAngle = 240 * (currentValue - maxValue) / (maxValue - minValue) + 120;
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(currentAngle, 0.5f * diameter, 0.5f * diameter);
        canvas.drawPath(indicatorPath, indicatorPaint);
        canvas.restore();

        canvas.drawCircle(0.5f * diameter, 0.5f * diameter, 0.01f * diameter, indicatorScrewPaint);
    }

    public void setCurrentValue(int value) {
        currentValue = value;
        invalidate();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void showAddDialog(final Activity activity, final LinearLayout layout, final OnLongClickListener onLongClick, final ArrayList<Widget> objects, final View view) {

        ScrollView scroll = new ScrollView(activity);
        scroll.setBackgroundColor(android.R.color.transparent);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle));
        alertDialogBuilder.setTitle("Choose the speedometer properties");

        LayoutInflater inflater= LayoutInflater.from(activity);
        final View alert_dialog_xml =inflater.inflate(R.layout.alert_dialog_speedometer, null);
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
                        String diameterValue = "";
                        String minValue = "";
                        String maxValue = "";
                        String label = "";

                        EditText diameterEditText = (EditText) alert_dialog_xml.findViewById(R.id.speedometer_diameter);
                        if (diameterEditText != null) {
                            diameterValue = diameterEditText.getText().toString();

                            if (diameterValue.isEmpty())
                                diameterEditText.setError("Diameter is required");
                        }

                        EditText minValueEditText = (EditText) alert_dialog_xml.findViewById(R.id.speedometer_min);
                        if (minValueEditText != null) {
                            minValue = minValueEditText.getText().toString();

                            if (minValue.isEmpty())
                                minValueEditText.setError("Min value is required");
                        }

                        EditText maxValueEditText = (EditText) alert_dialog_xml.findViewById(R.id.speedometer_max);
                        if (maxValueEditText != null) {
                            maxValue = maxValueEditText.getText().toString();

                            if (maxValue.isEmpty())
                                maxValueEditText.setError("Max value is required");
                        }

                        EditText lableEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
                        if (lableEditText != null) {
                            label = lableEditText.getText().toString();

                            if (label.isEmpty())
                                lableEditText.setError("Label is required");
                        }

                        if (!diameterValue.isEmpty() && !minValue.isEmpty() && !maxValue.isEmpty() && !label.isEmpty()) {

                            int pos = -1;
                            if (view != null) {
                                LinearLayout layout = (LinearLayout) activity.findViewById(R.id.widgetsContainer);
                                pos = layout.indexOfChild(view);
                                layout.removeView(view);
                                objects.remove(view);
                            }

                            addToBoard(activity, layout, onLongClick, objects,
                                    Integer.parseInt(diameterValue),
                                    Float.parseFloat(minValue), Float.parseFloat(maxValue), label, pos);

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

        EditText diameterEditText = (EditText) alert_dialog_xml.findViewById(R.id.speedometer_diameter);
        int diameter = (int) (width * 0.9);
        diameterEditText.setText(Integer.toString(diameter));

        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
        int count = ((DashboardActivity) activity).getWidgetsCount(TYPE_SPEEDOMETER);
        if (count == 0)
            labelEditText.setText("speedometer");
        else
            labelEditText.setText("speedometer_" + count);

        if (view != null) {
            Speedometer speedometer = (Speedometer) view;

            EditText minValueEditText = (EditText) alert_dialog_xml.findViewById(R.id.speedometer_min);
            EditText maxValueEditText = (EditText) alert_dialog_xml.findViewById(R.id.speedometer_max);

            minValueEditText.setText("" + (int) speedometer.getMin());
            maxValueEditText.setText("" + (int) speedometer.getMax());

            diameterEditText.setText("" + speedometer.getDiameter());
            labelEditText.setText(speedometer.getLabel());
        }

        alertDialog.show();
    }

    public static void addToBoard(Activity activity, LinearLayout layout, OnLongClickListener onLongClick, ArrayList<Widget> objects,
                                  int diameter, float minValue, float maxValue, String label, int pos) {
        Speedometer speedometer = new Speedometer(activity, null);
        speedometer.setLayoutParams(new LinearLayout.LayoutParams(diameter, diameter));
        speedometer.setLimits(minValue, maxValue);
        speedometer.setLabel(label);
        speedometer.setPadding(2, 2, 2, 2);

        if (pos != -1) {
            layout.addView(speedometer, pos);
            objects.add(pos, speedometer);
        } else {
            layout.addView(speedometer);
            objects.add(speedometer);
        }

        speedometer.setOnLongClickListener(onLongClick);
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj=new JSONObject();
        try {
            obj.put("type", TYPE_SPEEDOMETER);
            obj.put("diameter", diameter);
            obj.put("min_value", minValue);
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
    public int getType() {
        return TYPE_SPEEDOMETER;
    }

    @Override
    public void addData(String message) {
        try {
            setCurrentValue(Integer.parseInt(message));
        } catch (Exception e) {
            setCurrentValue((int) minValue);
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    public float getMin() {
        return minValue;
    }

    public float getMax() {
        return maxValue;
    }

    public int getDiameter() {
        return diameter;
    }
}
