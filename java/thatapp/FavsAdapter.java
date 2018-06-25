package com.example.a1.iturapp;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FavsAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Favs> objects;
    ArrayList<Favs> favses = new ArrayList<Favs>();

    FavsAdapter(Context context, ArrayList<Favs> favses) {
        ctx = context;
        objects = favses;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.drawer_list_item, parent, false);
        }

        Favs p = getFavs(position);

        ((TextView) view.findViewById(R.id.drawer_item_text)).setText(p.name);
        ((ImageView) view.findViewById(R.id.drawer_item_image)).setImageResource(p.image);
        ((ImageView) view.findViewById(R.id.drawer_item_bothline)).setImageResource(p.line);
        ((TextView) view.findViewById(R.id.drawer_item_subtext)).setText(p.subname);

        return view;
    }

    // товар по позиции
    Favs getFavs(int position) {
        return ((Favs) getItem(position));
    }
}