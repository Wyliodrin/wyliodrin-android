package com.wyliodrin.mobileapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WidgetsListAdapter extends BaseAdapter {

    private List<WidgetItem> items;
    private Context context;
    private LayoutInflater mInflater;

    WidgetsListAdapter(Context c) {
        context = c;
        items = new ArrayList<WidgetItem>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return items.get(i).type;
    }

    public void add(String name, int resId, int type) {
        items.add(new WidgetItem(name, resId, type));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v;

        if (view != null) {
            v = view;
        } else {
            v = mInflater.inflate(R.layout.widgets_item_layout, viewGroup, false);
        }

        WidgetItem item = items.get(i);

        ImageView img = (ImageView) v.findViewById(R.id.img_widget);
        img.setImageResource(item.imgResource);

        TextView text = (TextView) v.findViewById(R.id.text_widget);
        text.setText(item.name);

        return v;
    }
}
