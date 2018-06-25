package com.example.a1.iturapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.design.widget.Snackbar.make;

public class u_prntr extends AppCompatActivity{

    private static RecyclerView recyclerView;
    private TextView emptyList;
    private RecyclerView.LayoutManager layoutManager;
    static PrntrAdapter prntrAdapter;
    private List<Product> items;
    DBHelper dbHelper;
    boolean fav = false;
    Integer favstate = 0;
    Snackbar zakSnack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_u_prntr);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String Curr_prntr = (String) getIntent().getSerializableExtra("Curr_prntr");
        toolbar.setTitle(Curr_prntr);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_u_prntr);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findFavs(fav = true);
                String starState = "Что-то пошло не так...";
                if (favstate == 1) {
                    starState = getString(R.string.new_favorite);
                } else if (favstate == 2) {
                    starState = getString(R.string.no_favorite);
                } else if (favstate == 3) {
                    starState = getString(R.string.full_favorite);
                }
                Toast.makeText(u_prntr.this, starState,Toast.LENGTH_LONG).show();
            }
        });

        emptyList = (TextView) findViewById(R.id.u_prntr_emptylist);
        emptyList.setVisibility(View.INVISIBLE);

        recyclerView = (RecyclerView)findViewById(R.id.Usl_list);
        dbHelper = new DBHelper(this);
        findFavs(fav = false);
        items = new ArrayList();
        items = fillData();

        prntrAdapter = new PrntrAdapter(this, items);
        recyclerView.setAdapter(prntrAdapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        prntrAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                ShowZakazSnack();
            }
        });
    }

    // 3аполнение
    public List <Product> fillData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("Storetable", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int pidColIndex = c.getColumnIndex("pid");
            int nameColIndex = c.getColumnIndex("name");
            int priceColIndex = c.getColumnIndex("price");
            int imageColIndex = c.getColumnIndex("image");
            int bigimageColIndex = c.getColumnIndex("bigimage");
            int stateColIndex = c.getColumnIndex("state");
            int isservColIndex = c.getColumnIndex("isserv");
            do {
                items.add(new Product(c.getInt(pidColIndex), c.getString(nameColIndex), c.getDouble(priceColIndex),
                        c.getString(imageColIndex), c.getString(bigimageColIndex), c.getString(stateColIndex),
                        c.getInt(isservColIndex)));
            } while (c.moveToNext());
        } else {
            emptyList.setVisibility(View.VISIBLE);
        }
        c.close();
        dbHelper.close();
        return items;
    }

    //поиск в избранном
    void findFavs (boolean fav) {
        String Curr_prntr = (String) getIntent().getSerializableExtra("Curr_prntr");
        String Curr_id = (String) getIntent().getSerializableExtra("Curr_id");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_u_prntr);
        ContentValues cv = new ContentValues();
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        c = db.query("Favtable", null, null, null, null, null, null);
        Integer kolfavs = c.getCount();
        //проверка на повторы
        c = db.query("Favtable", null, "name = ?",
                new String[]{Curr_prntr}, null, null, null);
        if (c.getCount() == 0) {
            if (fav) {
                //проверка на мaксимальное число избранного
                if (kolfavs < (Integer.parseInt(getString(R.string.maxfavs)))) {
                    cv.put("name", Curr_prntr);
                    cv.put("favid", Curr_id);
                    db.insert("Favtable", null, cv);
                    fab.setImageResource(R.drawable.ic_star_white_24dp);
                    favstate = 1;
                } else {
                    favstate = 3;
                }
            } else {
                fab.setImageResource(R.drawable.ic_star_border_white_24dp);
            }
        } else {
            if (fav) {
                //удаление из избранного
                db.delete("Favtable", "name = \"" + Curr_prntr + "\"", null);
                fab.setImageResource(R.drawable.ic_star_border_white_24dp);
                favstate = 2;
            } else {
                fab.setImageResource(R.drawable.ic_star_white_24dp);
            }
        }
        c.close();
    }

    //Snackbar перехода к заказу
    void ShowZakazSnack () {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("Zakaztable", null, null, null, null, null, null);
        if (c.getCount() != 0) {
            zakSnack = Snackbar.make(findViewById(R.id.u_prntr_root), getString(R.string.prntr_snackbar_text)
                    + " " + String.valueOf(c.getCount()), Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                    .setAction(getString(R.string.prntr_snackbar_go), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(u_prntr.this, u_zakaz.class);
                            startActivity(intent);
                        }
                    });
            View snackbarView = zakSnack.getView();
            snackbarView.setBackgroundColor(Color.argb(200, 49, 27, 146));
            zakSnack.show();
            recyclerView.setPadding(0, 0, 0, 65);
        } else {
            recyclerView.setPadding(0, 0, 0, 0);
        }
        c.close();
        dbHelper.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        ShowZakazSnack();
    }

    //К заказу
    public void KZakazu (MenuItem item){
        Intent intent = new Intent(this, u_zakaz.class);
        startActivity(intent);
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
