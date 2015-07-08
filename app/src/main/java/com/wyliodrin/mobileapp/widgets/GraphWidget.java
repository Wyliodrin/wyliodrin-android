package com.wyliodrin.mobileapp.widgets;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
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
import android.widget.ScrollView;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesFormatter;
import com.androidplot.xy.XYStepMode;
import com.wyliodrin.mobileapp.DashboardActivity;
import com.wyliodrin.mobileapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreea Stoican on 12/10/14.
 */
public class GraphWidget extends XYPlot implements InputDataWidget {

    private String title;
    private int width;
    private int height;
    private GraphType currentType;
    private String label;
    private int minY, maxY;

    @Override
    public String getLabel() {
        return label;
    }

    public int getMin() {
        return minY;
    }
    public int getMax() {
        return maxY;
    }

    public enum GraphType {
        StepGraph,
        BarGraph,
        LineGraph
    }

    private static final int maxNumberOfPoints = 10;

    private class GraphWidgetDataSeries implements XYSeries {

        List<Number> xValues;
        List<Number> yValues;

        GraphWidgetDataSeries() {
            xValues = new ArrayList<Number>();
            yValues = new ArrayList<Number>();
        }

        public void addPoint(double x, double y) {
            xValues.add(x);
            yValues.add(y);
            if(xValues.size() > maxNumberOfPoints) {
                xValues.remove(0);
                yValues.remove(0);
            }

            GraphWidget.this.redraw();
        }

        @Override
        public int size() {
            return xValues.size();
        }

        @Override
        public Number getX(int index) {
            return xValues.get(index);
        }

        @Override
        public Number getY(int index) {
            return yValues.get(index);
        }

        @Override
        public String getTitle() {
            return "my series";
        }
    }

    private GraphWidgetDataSeries series;

    public GraphWidget(Context context, AttributeSet attrs, GraphType graphType, int minY, int maxY) {
        super(context, attrs);

        series = new GraphWidgetDataSeries();
        XYSeriesFormatter formatter = null;

        if (graphType.equals(GraphType.StepGraph)) {
            StepFormatter stepFormatter = new StepFormatter(Color.argb(100, 0, 200, 0), Color.rgb(0, 80, 0));
            stepFormatter.getLinePaint().setStrokeWidth(3);
            stepFormatter.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

            formatter = stepFormatter;
        } else if(graphType.equals(GraphType.BarGraph)) {
            BarFormatter barFormatter = new BarFormatter(Color.argb(100, 0, 200, 0), Color.rgb(0, 80, 0));
            barFormatter.getLinePaint().setStrokeWidth(3);
            barFormatter.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

            formatter = barFormatter;
        } else if(graphType.equals(GraphType.LineGraph)) {
            LineAndPointFormatter lineFormatter = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, null, null);
            lineFormatter.getLinePaint().setStrokeWidth(3);
            lineFormatter.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
            formatter = lineFormatter;
        }


        setDomainStepMode(XYStepMode.SUBDIVIDE);
        setDomainStepValue(5);

        setRangeStepMode(XYStepMode.SUBDIVIDE);
        setRangeStepValue(10);

        setRangeValueFormat(new DecimalFormat("###.#"));
        setRangeBoundaries(minY, maxY, BoundaryMode.FIXED);

        DashPathEffect dashFx = new DashPathEffect(
                new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        getGraphWidget().getDomainGridLinePaint().setPathEffect(dashFx);
        getGraphWidget().getRangeGridLinePaint().setPathEffect(dashFx);

        this.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
        this.addSeries(series, formatter);
    }

    private void setProperties(String title, int width, int height, int minY, int maxY, GraphType currentType) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.minY = minY;
        this.maxY = maxY;
        this.currentType = currentType;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void showAddDialog(final Activity activity, final LinearLayout layout, final View.OnLongClickListener onLongClick,
                                     final GraphType graphType, final ArrayList<Widget> objects, final View view) {

        ScrollView scroll = new ScrollView(activity);
        scroll.setBackgroundColor(android.R.color.transparent);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle));
        alertDialogBuilder.setTitle("Choose the graph properties");

        LayoutInflater inflater= LayoutInflater.from(activity);
        final View alert_dialog_xml =inflater.inflate(R.layout.alert_dialog_graph, null);
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
                        String height = "";
                        String title = "";
                        String label = "";
                        String minValue = "";
                        String maxValue = "";

                        EditText widthEditText = (EditText) alert_dialog_xml.findViewById(R.id.width);
                        if (widthEditText != null) {
                            width = widthEditText.getText().toString();

                            if (width.isEmpty())
                                widthEditText.setError("Width is required");
                        }

                        EditText minValueEditText = (EditText) alert_dialog_xml.findViewById(R.id.min);
                        if (minValueEditText != null) {
                            minValue = minValueEditText.getText().toString();

                            if (minValue.isEmpty())
                                minValueEditText.setError("Min value is required");
                        }

                        EditText maxValueEditText = (EditText) alert_dialog_xml.findViewById(R.id.max);
                        if (maxValueEditText != null) {
                            maxValue = maxValueEditText.getText().toString();

                            if (maxValue.isEmpty())
                                maxValueEditText.setError("Max value is required");
                        }

                        EditText heightEditText = (EditText) alert_dialog_xml.findViewById(R.id.height);
                        if (heightEditText != null) {
                            height = heightEditText.getText().toString();

                            if (height.isEmpty())
                                heightEditText.setError("Height is required");
                        }

                        EditText titleEditText = (EditText) alert_dialog_xml.findViewById(R.id.title);
                        if (titleEditText != null) {
                            title = titleEditText.getText().toString();

                            if (title.isEmpty())
                                titleEditText.setError("Title is required");
                        }

                        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
                        if (labelEditText != null) {
                            label = labelEditText.getText().toString();

                            if (label.isEmpty())
                                labelEditText.setError("Label is required");
                        }

                        if (!width.isEmpty() && !height.isEmpty() && !title.isEmpty() && !label.isEmpty()) {

                            int pos = -1;
                            if (view != null) {
                                LinearLayout layout = (LinearLayout) activity.findViewById(R.id.widgetsContainer);
                                pos = layout.indexOfChild(view);
                                layout.removeView(view);
                                objects.remove(view);
                            }

                            addToBoard(activity, layout, onLongClick, objects,
                                    Integer.parseInt(width), Integer.parseInt(height), Integer.parseInt(minValue), Integer.parseInt(maxValue),
                                    title, graphType, label, pos);
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
        int newWidth = (int) (width * 0.8);
        widthEditText.setText(Integer.toString(newWidth));

        EditText heightEditText = (EditText) alert_dialog_xml.findViewById(R.id.height);
        int newHeight = (int) (height * 0.5);
        heightEditText.setText(Integer.toString(newHeight));

        EditText labelEditText = (EditText) alert_dialog_xml.findViewById(R.id.label);
        int count = ((DashboardActivity) activity).getWidgetsCount(TYPE_GRAPH);
        if (count == 0)
            labelEditText.setText("graph");
        else
            labelEditText.setText("graph_" + count);

        if (view != null) {
            GraphWidget graph = (GraphWidget) view;

            labelEditText.setText(graph.getLabel());
            widthEditText.setText("" + graph.getWidth());
            heightEditText.setText("" + graph.getHeight());

            EditText minEditText = (EditText) alert_dialog_xml.findViewById(R.id.min);
            EditText maxEditText = (EditText) alert_dialog_xml.findViewById(R.id.max);
            minEditText.setText("" + graph.getMin());
            maxEditText.setText("" + graph.getMax());
        }

        alertDialog.show();
    }

    public static void addToBoard(Activity activity, LinearLayout layout, OnLongClickListener onLongClick, ArrayList<Widget> objects,
                                  int width, int height, int minY, int maxY, String title, GraphType graphType, String label, int pos) {

        final GraphWidget graph = new GraphWidget(activity, graphType, minY, maxY);

        graph.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        graph.setOnLongClickListener(onLongClick);
        graph.setProperties(title, width, height, minY, maxY, graphType);
        graph.setTitle(title);
        graph.setLabel(label);

        if (pos != -1) {
            layout.addView(graph, pos);
            objects.add(pos, graph);
        } else {
            layout.addView(graph);
            objects.add(graph);
        }
    }

    public GraphWidget(Context context, GraphType type, int minY, int maxY) {
        this(context, null, type, minY, maxY);
    }

    @Override
    public void addData(String message) {
        double x = (double) System.currentTimeMillis() / 1000;
        double y = 0;
        try {
            y = Double.parseDouble(message);
        } catch (Exception e) { }
        addPoint(x, y);
    }

    public void addPoint(double x, double y) {
        series.addPoint(x, y);
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj=new JSONObject();
        try {
            obj.put("type", TYPE_GRAPH);
            obj.put("graph_type", currentType);
            obj.put("title", title);
            obj.put("width", width);
            obj.put("height", height);
            obj.put("min", minY);
            obj.put("max", maxY);
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
        return TYPE_GRAPH;
    }

    public GraphType getGraphType() {
        return currentType;
    }

}
