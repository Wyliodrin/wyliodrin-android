package com.wyliodrin.mobileapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wyliodrin.mobileapp.api.ServerConnection;
import com.wyliodrin.mobileapp.widgets.InputDataWidget;
import com.wyliodrin.mobileapp.widgets.SensorWidget;
import com.wyliodrin.mobileapp.widgets.SimpleButton;
import com.wyliodrin.mobileapp.widgets.GraphWidget;
import com.wyliodrin.mobileapp.widgets.SimpleSeekBar;
import com.wyliodrin.mobileapp.widgets.SimpleToggleButton;
import com.wyliodrin.mobileapp.widgets.Speedometer;
import com.wyliodrin.mobileapp.widgets.Thermometer;
import com.wyliodrin.mobileapp.widgets.Widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends FragmentActivity {
    private ArrayList<Widget> objects;
    private SharedPreferences shPref;

    private DrawerLayout mDrawerLayout;

    private JSONObject currentBoard;
    public static int boardId = -1;

    private List<String> boards;

    private static final String labelTag= "signal:";

    public static String currentBoardName = "";

    /**
     * On long click the widget will be removed
     */
    private View.OnLongClickListener widgetLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View view) {

            LayoutInflater mInflater = getLayoutInflater();
            View dialogContent = mInflater.inflate(R.layout.widget_options_dialog, null, false);

            final AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(DashboardActivity.this, R.style.CustomAlertDialogStyle))
                    .setTitle("Widget options").setView(dialogContent).show();

            dialog.findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {
                    dialog.dismiss();

                    int type = ((Widget) view).getType();
                    GraphWidget.GraphType graphType = null;

                    if (type == Widget.TYPE_GRAPH) {
                        GraphWidget graph = (GraphWidget) view;
                        graphType = graph.getGraphType();
                    }

                    showAddDialog(type, graphType, view);
                }
            });

            dialog.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {
                    dialog.dismiss();

                    new AlertDialog.Builder(new ContextThemeWrapper(DashboardActivity.this, R.style.CustomAlertDialogStyle)).setTitle("Remove widget?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            LinearLayout layout = (LinearLayout) findViewById(R.id.widgetsContainer);
                            layout.removeView(view);
                            objects.remove(view);

                            saveBoard(currentBoardName);

                        }
                    }).setNegativeButton("No", null).show();

                }
            });

            dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {
                    dialog.dismiss();
                }
            });

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_new_dashboard);

        objects = new ArrayList<Widget>();
        shPref = getSharedPreferences("dashboard", MODE_PRIVATE);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ImageView menuButton = (ImageView) findViewById(R.id.menu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOptionsMenu();
            }
        });

        WidgetsListAdapter adapter = new WidgetsListAdapter(this);

        ListView widgetsList = (ListView) findViewById(R.id.drawerList);

        adapter.add("Thermometer", R.drawable.thermometer_item, Widget.TYPE_THERMOMETER);
        adapter.add("Speedometer", R.drawable.speedometer_item, Widget.TYPE_SPEEDOMETER);
        adapter.add("Step graph", R.drawable.step_graph_item, Widget.TYPE_GRAPH);
        adapter.add("Bar graph", R.drawable.bar_graph_item, Widget.TYPE_GRAPH);
        adapter.add("Line and point graph", R.drawable.line_graph_item, Widget.TYPE_GRAPH);
        adapter.add("Button", R.drawable.button_item, Widget.TYPE_BUTTON);
        adapter.add("Toggle button", R.drawable.togglebutton_item, Widget.TYPE_TOGGLE_BUTTON);
        adapter.add("Seek bar", R.drawable.seekbar_item, Widget.TYPE_SEEK_BAR);
        adapter.add("Sensors", R.drawable.sensors_item, Widget.TYPE_SENSOR);

        widgetsList.setAdapter(adapter);

        widgetsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long type) {

                GraphWidget.GraphType graphType = null;
                if (pos == 2) {
                    graphType = GraphWidget.GraphType.StepGraph;
                } else if (pos == 3) {
                    graphType = GraphWidget.GraphType.BarGraph;
                } else if (pos == 4) {
                    graphType = GraphWidget.GraphType.LineGraph;
                }

                showAddDialog((int) type, graphType, null);
                mDrawerLayout.closeDrawers();
            }
        });

        Intent intent = getIntent();
        String boardString = intent.getStringExtra("board");
        boardId = intent.getIntExtra("board_id", -1);

        if (boardId == -1) {
            chooseDashboardName();
        }

        if(boardString != null && boardId != -1) {
            try {
                currentBoard = new JSONObject(boardString);

                JSONArray widgets = currentBoard.getJSONArray("objects");

                currentBoardName = currentBoard.optString("name", "");

                for (int i = 0; i < widgets.length(); i++) {
                    JSONObject widget = widgets.getJSONObject(i);
                    switch (widget.optInt("type", Widget.TYPE_NONE)) {
                        case Widget.TYPE_THERMOMETER:
                            Thermometer.addToBoard(this, (LinearLayout) findViewById(R.id.widgetsContainer), widgetLongClick, objects,
                                    widget.optInt("width", 200), widget.optInt("height", 300),
                                    widget.optDouble("maxDegree", 70), widget.optDouble("minDegree", -20),widget.optString("label", "label"), -1);
                            break;
                        case Widget.TYPE_GRAPH:
                            GraphWidget.GraphType type = null;
                            if (widget.opt("graph_type").equals("StepGraph")) {
                                type = GraphWidget.GraphType.StepGraph;
                            } else if (widget.opt("graph_type").equals("BarGraph")) {
                                type = GraphWidget.GraphType.BarGraph;
                            } else if (widget.opt("graph_type").equals("LineGraph")) {
                                type = GraphWidget.GraphType.LineGraph;
                            }

                            GraphWidget.addToBoard(this, (LinearLayout) findViewById(R.id.widgetsContainer),widgetLongClick,
                                    objects, widget.optInt("width", 200),widget.optInt("height", 300), widget.optInt("min", 0),widget.optInt("max", 100),widget.optString("title", "Step"),
                                    type, widget.optString("label"), -1);
                            break;
                        case Widget.TYPE_BUTTON:
                            SimpleButton.addToBoard(this, (LinearLayout) findViewById(R.id.widgetsContainer) ,widgetLongClick,
                                    objects, widget.optInt("width"), widget.optInt("height"), widget.optString("text_button"), widget.optString("label"), -1);
                            break;
                        case Widget.TYPE_TOGGLE_BUTTON:
                            SimpleToggleButton.addToBoard(this, (LinearLayout) findViewById(R.id.widgetsContainer) ,widgetLongClick,
                                    objects, widget.optInt("width"), widget.optInt("height"), widget.optString("text_button_on"), widget.optString("text_button_off"), widget.optString("label"), -1);
                            break;
                        case Widget.TYPE_SEEK_BAR:
                            SimpleSeekBar.addToBoard(this, (LinearLayout) findViewById(R.id.widgetsContainer) ,widgetLongClick,
                                objects, widget.optInt("width"), widget.optInt("max_value"), widget.optString("label"), -1);
                            break;
                        case Widget.TYPE_SPEEDOMETER:
                            Speedometer.addToBoard(this, (LinearLayout) findViewById(R.id.widgetsContainer), widgetLongClick,
                                    objects, widget.optInt("diameter"), widget.optInt("min_value"), widget.optInt("max_value"), widget.optString("label"), -1);
                            break;
                        case Widget.TYPE_SENSOR:
                            int sensorType = widget.optInt("sensor");
                            SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                            List<Sensor> deviceSensors = mSensorManager.getSensorList(sensorType);

                            if (deviceSensors.isEmpty()) return;

                            Sensor sensor = deviceSensors.get(0);

                            SensorWidget.addToBoard(this, (LinearLayout) findViewById(R.id.widgetsContainer) ,widgetLongClick,
                                    objects, sensor, widget.optInt("update_timeout"), widget.optInt("width"), widget.optString("label"), -1);
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        boards = ServerConnection.getInstance().getBoardsList();
        final Spinner boardsSpinner = (Spinner) findViewById(R.id.boardSpinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        boardsSpinner.setAdapter(spinnerAdapter);
        for (String board: boards) {
            spinnerAdapter.add(board);
        }
        spinnerAdapter.notifyDataSetChanged();

        boardsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ServerConnection.getInstance().setTo(boards.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        ServerConnection.getInstance().setDashboard(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getType() == Widget.TYPE_SENSOR) {
                SensorWidget sensorWidget = (SensorWidget) objects.get(i);
                sensorWidget.unregisterSensor();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ServerConnection.getInstance().setDashboard(null);
    }

    /**
     * Save the current board in SharedPreferences
     * @param name of the board
     */
    public void saveBoard(String name) {
        JSONArray array = new JSONArray();

        for(Widget widget : objects) {
            array.put(widget.toJson());
        }

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("objects", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String boards = shPref.getString("boards", "");

        JSONArray list;
        try {
            list = new JSONArray(boards);
        } catch (JSONException e) {
            list = new JSONArray();
            e.printStackTrace();
        }

        if(boardId != -1) {
            try {
                list.put(boardId, obj);
            } catch (JSONException e) {
            }
        } else {
            boardId = list.length();
            try {
                list.put(boardId, obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        shPref.edit().putString("boards", list.toString()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.rename_board:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(DashboardActivity.this, R.style.CustomAlertDialogStyle));
                alertDialogBuilder.setTitle("Dashboard name");

                LayoutInflater inflater = LayoutInflater.from(DashboardActivity.this);
                final View alert_dialog_xml = inflater.inflate(R.layout.alert_dialog_dashboard_name, null);
                EditText nameEditText = (EditText) alert_dialog_xml.findViewById(R.id.name);

                if (boardId != -1) {
                    nameEditText.setText(currentBoard.optString("name"), TextView.BufferType.EDITABLE);
                }
                alertDialogBuilder.setView(alert_dialog_xml);

                alertDialogBuilder
                        .setMessage("Choose dashboard name")
                        .setPositiveButton("Rename", null)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

                                EditText nameEditText = (EditText) alert_dialog_xml.findViewById(R.id.name);

                                String name = null;
                                if (nameEditText != null) {
                                    name = nameEditText.getText().toString();

                                    if (name.isEmpty())
                                        nameEditText.setError("Name is required");
                                }

                                if (!name.isEmpty()) {
                                    saveBoard(name);
                                    Toast.makeText(DashboardActivity.this, "Dashboard renamed.", Toast.LENGTH_SHORT).show();
                                    currentBoardName = name;
                                    alertDialog.dismiss();
                                }
                            }
                        });
                    }
                });

                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(DashboardActivity.this, "Dashboard " + currentBoardName + " saved.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void messageReceived(String label, final String message) {
        for (Widget widget : objects) {
            if (widget instanceof InputDataWidget) {
                final InputDataWidget w = (InputDataWidget) widget;

                if (label.startsWith(labelTag)) {
                    label = label.replace(labelTag, "");
                }

                if (w.getLabel().equals(label)) {
                    ((View) w).post(new Runnable() {
                        @Override
                        public void run() {
                            w.addData(message);
                        }
                    });
                }
            }
        }
    }

    public int getWidgetsCount (int type) {
        int count = 0;
        for (Widget widget : objects) {
            if (widget.getType() == type) {
                count++;
            }
        }
        return count;
    }

    public void showAddDialog(int type, GraphWidget.GraphType gType, View view) {
        switch(type) {
            case Widget.TYPE_THERMOMETER:
                Thermometer.showAlertDialog(DashboardActivity.this, (LinearLayout) findViewById(R.id.widgetsContainer),
                        widgetLongClick, objects, view);
                break;
            case Widget.TYPE_GRAPH:
                GraphWidget.showAddDialog(DashboardActivity.this, (LinearLayout) findViewById(R.id.widgetsContainer),
                        widgetLongClick, gType, objects, view);
                break;
            case Widget.TYPE_BUTTON:
                SimpleButton.showAddDialog(DashboardActivity.this, (LinearLayout) findViewById(R.id.widgetsContainer),
                        widgetLongClick, objects, view);
                break;
            case Widget.TYPE_SPEEDOMETER:
                Speedometer.showAddDialog(DashboardActivity.this, (LinearLayout) findViewById(R.id.widgetsContainer),
                        widgetLongClick, objects, view);
                break;
            case Widget.TYPE_TOGGLE_BUTTON:
                SimpleToggleButton.showAddDialog(DashboardActivity.this, (LinearLayout) findViewById(R.id.widgetsContainer),
                        widgetLongClick, objects, view);
                break;
            case Widget.TYPE_SEEK_BAR:
                SimpleSeekBar.showAddDialog(DashboardActivity.this, (LinearLayout) findViewById(R.id.widgetsContainer),
                        widgetLongClick, objects, view);
                break;
            case Widget.TYPE_SENSOR:
                SensorWidget.showAddDialog(DashboardActivity.this, (LinearLayout) findViewById(R.id.widgetsContainer),
                        widgetLongClick, objects, view);
                break;
        }

    }

    private void chooseDashboardName () {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(DashboardActivity.this, R.style.CustomAlertDialogStyle));
        alertDialogBuilder.setTitle("Dashboard name");

        LayoutInflater inflater = LayoutInflater.from(DashboardActivity.this);
        final View alert_dialog_xml = inflater.inflate(R.layout.alert_dialog_dashboard_name, null);
        EditText nameEditText = (EditText) alert_dialog_xml.findViewById(R.id.name);

        if (boardId != -1) {
            nameEditText.setText(currentBoard.optString("name"), TextView.BufferType.EDITABLE);
        }
        alertDialogBuilder.setView(alert_dialog_xml);

        alertDialogBuilder
                .setMessage("Choose dashboard name")
                .setPositiveButton("Save", null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {

                        EditText nameEditText = (EditText) alert_dialog_xml.findViewById(R.id.name);

                        String name = null;
                        if (nameEditText != null) {
                            name = nameEditText.getText().toString();

                            if (name.isEmpty())
                                nameEditText.setError("Name is required");
                        }

                        if (!name.isEmpty()) {
                            currentBoardName = name;
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        alertDialog.show();
    }
}
