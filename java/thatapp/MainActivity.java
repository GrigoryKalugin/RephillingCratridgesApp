package com.example.a1.iturapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    Button btnFind, btnSendPhoto;
    ImageButton btnNoConn;
    RelativeLayout NoConn;
    Spinner spinner_pr, spinner_se, spinner_mo;
    ProgressBar proizvBar, seriaBar, modelBar;
    DBHelper dbHelper;
    Context ctx;
    FavsAdapter favsAdapter;

    TextView favName;
    private DrawerLayout myDrawerLayout;
    private ListView myDrawerList;
    private ActionBarDrawerToggle myDrawerToggle;
    // navigation drawer title
    private CharSequence myDrawerTitle;
    private CharSequence myTitle;

    Integer spin = 1, num = 0, favCount = 0, favRepCount = 0;
    String shosenPr = "0", shosenPrd = "0", Curr_prntr = "", Curr_id = "";
    List<String> prntNames = new ArrayList<String>();
    List<String> prntNIds = new ArrayList<String>();
    List<String> prntFamilies = new ArrayList<String>();
    List<String> prntFIds = new ArrayList<String>();
    List<String> prntDevices = new ArrayList<String>();
    List<String> prntDIds = new ArrayList<String>();
    ArrayList<Favs> items = new ArrayList<Favs>();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PackageManager packageManager = getPackageManager();
                boolean telephonySupported = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
                boolean gsmSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_GSM);
                boolean cdmaSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA);
                if (telephonySupported || gsmSupported || cdmaSupported) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("номер телефона"));
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.main_no_telephone), Toast.LENGTH_LONG);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        ctx = this;

        //Панель избранного
        myTitle = getTitle();
        myDrawerTitle = getResources().getString(R.string.menu);
        myDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        myDrawerList = (ListView) findViewById(R.id.left_drawer);
        favName = (TextView) findViewById(R.id.drawer_item_text);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        myDrawerToggle = new ActionBarDrawerToggle(this, myDrawerLayout,
                R.string.open_menu,
                R.string.close_menu
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(myTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(myDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        myDrawerLayout.setDrawerListener(myDrawerToggle);

        if (savedInstanceState == null) {
            displayView(0);
        }

        myDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        spinner_pr = (Spinner)findViewById(R.id.spinner_proizvoditel);
        spinner_se = (Spinner)findViewById(R.id.spinner_seria);
        spinner_mo = (Spinner)findViewById(R.id.spinner_model);

        proizvBar = (ProgressBar) findViewById(R.id.progress_bar_poizv);
        seriaBar = (ProgressBar) findViewById(R.id.progress_bar_seria);
        modelBar = (ProgressBar) findViewById(R.id.progress_bar_model);

        btnFind = (Button) findViewById(R.id.btnFind);
        btnSendPhoto = (Button) findViewById(R.id.btnPrnPhoto);

        NoConn = (RelativeLayout) findViewById(R.id.main_no_conn_btn_rl);
        btnNoConn = (ImageButton) findViewById(R.id.main_no_conn_btn);
        btnNoConn.setVisibility(View.GONE);
        NoConn.setVisibility(View.GONE);
        btnFind.setAlpha(1);

        //Старт!
        new ParseTask().execute();

        //Выбор производителя
        spinner_pr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected,
                                       int selectedItemPosition, long selectedId) {
                    num++;
                    if (num > 1) {
                        shosenPr = prntNIds.get(spinner_pr.getSelectedItemPosition());
                    }
                    if (shosenPr != "0") {
                        spin = 2;
                        new ParseTask().execute();
                    } else {
                        ArrayAdapter<?> adapter_se =
                                ArrayAdapter.createFromResource(ctx, R.array.spinner_seria, android.R.layout.simple_spinner_item);
                        adapter_se.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_se.setAdapter(adapter_se);
                        shosenPrd = "0";
                    }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Выбор серии
        spinner_se.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected,
                                       int selectedItemPosition, long selectedId) {
                    if (num > 1) {
                        shosenPrd = prntFIds.get(spinner_se.getSelectedItemPosition());
                    }
                    if (shosenPr != "0") {
                        spin = 3;
                        new ParseTask().execute();
                    } else {
                        ArrayAdapter<?> adapter_mo =
                                ArrayAdapter.createFromResource(ctx, R.array.spinner_model, android.R.layout.simple_spinner_item);
                        adapter_mo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_mo.setAdapter(adapter_mo);
                    }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Выбор модели
        spinner_mo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected,
                                       int selectedItemPosition, long selectedId) {
                    if (num > 1) {
                        Curr_id = prntDIds.get(spinner_mo.getSelectedItemPosition());
                    }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Переподключене
        btnNoConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasConnection(ctx)) {
                    Intent intent = new Intent(ctx, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Zakaztable", null, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        items.clear();
        items = fillData();
        favsAdapter = new FavsAdapter(this, items);
        myDrawerList.setAdapter(favsAdapter);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        btnFind.setAlpha(1);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if(hasConnection(ctx) == false) {
            btnNoConn.setVisibility(View.VISIBLE);
            NoConn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Заполнение спиннеpов
    private class ParseTask extends AsyncTask<Void, Integer, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        URL url;

        @Override
        protected String doInBackground(Void... params) {
            int counter = 0;
            try {

                publishProgress(++counter);

                if (spin == 1) {
                    url = new URL("Ссылка на список производителей");
                } else if (spin == 2) {
                    url = new URL("Ссылка на список серий"+shosenPr);
                } else if (spin == 3) {
                    url = new URL("Ссылка на список моделей"+shosenPrd);
                }

                publishProgress(++counter);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                publishProgress(++counter);

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                publishProgress(++counter);

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

                publishProgress(++counter);

                return resultJson;

            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        btnFind.setAlpha (Float.valueOf("0.75"));
        if (spin == 1) {
            proizvBar.setVisibility(View.VISIBLE);
            proizvBar.setProgress(values[0]);
        } else if (spin == 2) {
            seriaBar.setVisibility(View.VISIBLE);
            seriaBar.setProgress(values[0]);
        } else if (spin == 3) {
            modelBar.setVisibility(View.VISIBLE);
            modelBar.setProgress(values[0]);
        }
    }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            JSONObject dataJsonObj = null;
            if (strJson == "error") {
                noConnecttion();
            } else {
                try {
                    dataJsonObj = new JSONObject(strJson);
                    JSONObject prntrsdata = dataJsonObj.getJSONObject("data");


                    if (spin == 1) {
                        JSONArray prntrslistM = prntrsdata.getJSONArray("makers");
                        prntNames.add(getString(R.string.main_spinner_pr));
                        prntNIds.add("0");

                        for (int i = 0; i < prntrslistM.length(); i++) {
                            JSONObject maker = prntrslistM.getJSONObject(i);
                            prntNames.add(maker.getString("name"));
                            prntNIds.add(maker.getString("id"));
                        }
                        ArrayAdapter<String> adapter_pr = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item, prntNames);
                        adapter_pr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_pr.setAdapter(adapter_pr);
                        proizvBar.setVisibility(View.INVISIBLE);
                        seriaBar.setVisibility(View.INVISIBLE);
                        modelBar.setVisibility(View.INVISIBLE);
                        proizvBar.setProgress(0);

                    } else if (spin == 2) {
                        prntFamilies.clear();
                        prntFIds.clear();
                        JSONArray prntrslistF = prntrsdata.getJSONArray("families");

                        for (int i = 0; i < prntrslistF.length(); i++) {
                            JSONObject maker = prntrslistF.getJSONObject(i);
                            prntFamilies.add(maker.getString("name"));
                            prntFIds.add(maker.getString("id"));
                        }
                        ArrayAdapter<String> adapter_se = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item, prntFamilies);
                        adapter_se.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_se.setAdapter(adapter_se);
                        seriaBar.setVisibility(View.INVISIBLE);
                        seriaBar.setProgress(0);

                    } else if (spin == 3) {
                        prntDevices.clear();
                        prntDIds.clear();
                        JSONArray prntrslistD = prntrsdata.getJSONArray("devices");

                        for (int i = 0; i < prntrslistD.length(); i++) {
                            JSONObject maker = prntrslistD.getJSONObject(i);
                            prntDevices.add(maker.getString("name"));
                            prntDIds.add(maker.getString("id"));
                        }
                        ArrayAdapter<String> adapter_mo = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item, prntDevices);
                        adapter_mo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner_mo.setAdapter(adapter_mo);
                        modelBar.setVisibility(View.INVISIBLE);
                        modelBar.setProgress(0);
                    }
                    btnFind.setAlpha(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                    noConnecttion();
                }
            }
        }
    }

    //Найти принтер
    public void FindPrinter(View view) {
        if (btnFind.getAlpha() == 1) {
            new ToUPrntr().execute();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Дождитесь загрузки данных...", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //Заполнение списка товаров и услуг
    public class ToUPrntr extends AsyncTask<Void, Integer, String> {
    DBHelper dbHelper = new DBHelper(MainActivity.this);
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    ProgressDialog WaitingDialog;
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String resultJson = "";
    URL url;

    @Override
    protected void onPreExecute() {
        WaitingDialog = ProgressDialog.show(MainActivity.this, getString(R.string.main_wait_dialog_title),
                getString(R.string.main_wait_dialog_text));
    }

    @Override
    protected String doInBackground(Void... params) {
        try {

            db.delete("Storetable", null, null);
            url = new URL("ссылка на список товаров для модели" + Curr_id);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            resultJson = buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return resultJson;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String strJson) {
        super.onPostExecute(strJson);
        JSONObject dataJsonObj = null;

        if (strJson == "error") {
            noConnecttion();
            WaitingDialog.dismiss();
        } else {
            try {
                dataJsonObj = new JSONObject(strJson);
                JSONObject productsdata = dataJsonObj.getJSONObject("data");
                JSONArray productslist = productsdata.getJSONArray("products");

                Curr_prntr = productsdata.getString("device_name");

                for (int i = 0; i < productslist.length(); i++) {
                    JSONObject products = productslist.getJSONObject(i);
                    cv.put("pid", products.getInt("id"));
                    cv.put("name", products.getString("name"));
                    cv.put("price", products.getInt("price"));
                    cv.put("state", products.getString("stock"));
                    cv.put("image", products.getString("thumbnail"));
                    cv.put("bigimage", products.getString("picture"));
                    cv.put("isserv", products.getString("is_service"));
                    db.insert("Storetable", null, cv);
                }

                WaitingDialog.dismiss();
                dbHelper.close();
                Intent intent = new Intent(MainActivity.this, u_prntr.class);
                intent.putExtra("Curr_prntr", Curr_prntr);
                intent.putExtra("Curr_id", Curr_id);
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
                noConnecttion();
                WaitingDialog.dismiss();
            }
        }
    }
}

    //Отправить фото
    public void SendPhoto (View view) {
        LayoutInflater li = LayoutInflater.from(this);
        View sendPhotoDialog = li.inflate(R.layout.send_photo_dialog, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);
        mDialogBuilder.setView(sendPhotoDialog);

        final RelativeLayout Camera = (RelativeLayout) sendPhotoDialog.findViewById(R.id.send_dialog_camera);
        final RelativeLayout Gallery = (RelativeLayout) sendPhotoDialog.findViewById(R.id.send_dialog_gallery);

        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager cameraManager = getPackageManager();
                boolean cameraSupported = cameraManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
                if (cameraSupported) {
                    Intent intent = new Intent(MainActivity.this, SendPhotoCamera.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Камера отсутствует", Toast.LENGTH_LONG).show();
                }
            }
        });
        Gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, SendPhotoGallery.class);
                startActivity(intent);
            }
        });
        mDialogBuilder
                .setCancelable(true);

        AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.show();
    }

    //Как нас найти
    public void About (View view) {
        Intent intent = new Intent(MainActivity.this, About_us.class);
        startActivity(intent);
    }

    //Вызвать курьера
    public void KZakazu (View view){
        Intent intent = new Intent(MainActivity.this, u_kourier.class);
        startActivity(intent);
    }

    //Задать вопрос
    public void Question (MenuItem menu) {
        Intent intent = new Intent(MainActivity.this, Question.class);
        startActivity(intent);
    }

    //JivoChat
    public void LiveChat (MenuItem menu) {
        if (hasConnection(ctx)) {
            Intent intent = new Intent(MainActivity.this, JivoChat.class);
            startActivity(intent);
        } else {
            noConnecttion();
        }
    }

    //Нажатие на элемент боковой панели
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }
    //Обработка нажатия
    private void displayView(int position) {

        Integer Maxfavs = Integer.parseInt(getString(R.string.maxfavs));
        Integer MaxRepfavs = Integer.parseInt(getString(R.string.maxrepairs));

        if (position < favCount) {
            Curr_prntr = items.get(position).name;
            Curr_id = items.get(position).prntrid;
            new ToUPrntr().execute();
        }
        else if (favCount == 0 && position == (2 + Maxfavs)
                || (favCount > 0 && position == (1 + Maxfavs))) {
            LinearLayout repairLL = (LinearLayout) findViewById(R.id.btnRepair);
            repairLL.callOnClick();
        }
        else if (favCount == 0 && position > (2 + Maxfavs)
                || (favCount > 0 && position > (1 + Maxfavs))) {
            Curr_prntr = items.get(position).subname;
            Intent intent = new Intent(MainActivity.this, Repair.class);
            intent.putExtra("Repair_No", Curr_prntr);
            startActivity(intent);
        }
        else {
            myDrawerList.setSelection(0);
        }
    }

    //окно ввода № заявки
    public void repairDialog(View view) {
        LayoutInflater li = LayoutInflater.from(this);
        View findRepairDialog = li.inflate(R.layout.repair_prntr_dialog, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);
        mDialogBuilder.setView(findRepairDialog);
        final EditText r_editText = (EditText) findRepairDialog.findViewById(R.id.repair_edittext);
        r_editText.setMaxEms(Integer.parseInt(getString(R.string.repairlength)));
        mDialogBuilder
                .setTitle(R.string.repair_dialog_info)
                .setCancelable(true)
                .setPositiveButton(R.string.main_btn_find,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                Intent intent = new Intent(MainActivity.this, Repair.class);
                                intent.putExtra("Repair_No", r_editText.getText().toString());
                                startActivity(intent);
                            }
                        })
                .setNegativeButton(R.string.abort,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void setTitle(CharSequence title) {
        myTitle = title;
        getSupportActionBar().setTitle(myTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        myDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        myDrawerToggle.onConfigurationChanged(newConfig);
    }

    // Заполнение боковой панели
    public ArrayList <Favs> fillData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("Favtable", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int nameColIndex = c.getColumnIndex("name");
            int prntridColIndex = c.getColumnIndex("favid");
            do {
                items.add(new Favs(c.getString(nameColIndex), R.drawable.ic_print_black_24dp,
                        R.color.colorNoColor, null, c.getString(prntridColIndex)));
            } while (c.moveToNext());
        }
        for (Integer i = 0; i + c.getCount() < (Integer.parseInt(getString(R.string.maxfavs))); ++i) {
            items.add(new Favs("", R.color.colorNoColor, R.color.colorNoColor, null, ""));
            if (c.getCount() == 0 && i == (Math.floor((Integer.parseInt(getString(R.string.maxfavs)))/2))) {
                items.add(new Favs(getString(R.string.emptyfavs), R.drawable.ic_star_border_black_24dp,
                        R.color.colorNoColor, null, ""));
            }
        }
        items.add(new Favs("", R.color.colorNoColor, R.color.colorAccent, null, ""));
        items.add(new Favs(getString(R.string.find_repair), R.drawable.ic_search_black_24dp,
                R.color.colorNoColor, null, ""));
        favCount = c.getCount();
        c = db.query("FavReptable", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int nameColIndex = c.getColumnIndex("name");
            int repnoColIndex = c.getColumnIndex("repairno");
            do {
                items.add(new Favs(c.getString(nameColIndex), R.drawable.ic_build_black_24dp,
                        R.color.colorNoColor, c.getString(repnoColIndex), ""));
            } while (c.moveToNext());
        }
        favRepCount = c.getCount();
        c.close();
        dbHelper.close();
        return items;
    }

    // Проверка подключения
    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        return false;
    }
    // Нет соединения
    public  void noConnecttion () {
        Toast.makeText(ctx, getString(R.string.error_no_connection), Toast.LENGTH_LONG).show();
        btnNoConn.setVisibility(View.VISIBLE);
        NoConn.setVisibility(View.VISIBLE);

        spinner_pr.setSelection(0);

        ArrayAdapter<?> adapter_se =
                ArrayAdapter.createFromResource(ctx, R.array.spinner_seria, android.R.layout.simple_spinner_item);
        adapter_se.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_se.setAdapter(adapter_se);

        ArrayAdapter<?> adapter_mo =
                ArrayAdapter.createFromResource(ctx, R.array.spinner_model, android.R.layout.simple_spinner_item);
        adapter_mo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mo.setAdapter(adapter_mo);

        spin = 1;

        btnFind.setAlpha (Float.valueOf("0.75"));

        shosenPr = "0";
        shosenPrd = "0";
    }

	//создание локальной базы данных
    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table Storetable ("
                    + "id integer primary key autoincrement,"
                    + "pid INTEGER,"
                    + "name text,"
                    + "price REAL,"
                    + "image text,"
                    + "bigimage text,"
                    + "state text,"
                    + "isserv INTEGER" + ");");
            db.execSQL("create table Zakaztable ("
                    + "id integer primary key autoincrement,"
                    + "pid INTEGER,"
                    + "name text,"
                    + "price REAL,"
                    + "kol INTEGER,"
                    + "image text,"
                    + "bigimage text,"
                    + "state text" + ");");
            db.execSQL("create table Favtable ("
                    + "id integer primary key autoincrement,"
                    + "name text,"
                    + "favid text" + ");");
            db.execSQL("create table FavReptable ("
                    + "id integer primary key autoincrement,"
                    + "name text,"
                    + "repairno INTEGER" + ");");
            db.execSQL("create table UserData ("
                    + "id integer primary key autoincrement,"
                    + "name text,"
                    + "phone text,"
                    + "email text,"
                    + "address text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}

