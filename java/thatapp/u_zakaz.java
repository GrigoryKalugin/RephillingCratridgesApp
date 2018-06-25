package com.example.a1.iturapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static com.example.a1.iturapp.u_prntr.prntrAdapter;

public class u_zakaz extends AppCompatActivity {

    static RecyclerView recyclerView;
    static private RecyclerView.LayoutManager layoutManager;
    static ZakazAdapter zakazAdapter;
    static private List <Zakaz> items;
    DBHelper dbHelper;
    SharedPreferences sForms;

    EditText Name, Phone, Email, Address, Comment;
    TextView oplata_i_dostavka, regions_header, regions_info, address_link,
            summa_z, summa_all, errNon, errNocp, errNoce, errNoa;
    List<String> regValues = new ArrayList<String>();
    List<String> regIds = new ArrayList<String>();
    RadioButton selfv, courier;
    Spinner regions;
    RelativeLayout summa_z_line, summa_all_line, Send;

    public Integer delivery = 0, freeDelivery = 0, deleted = 0, available = 0;
    public Double summaZ = 0.0, summaD = 0.0, summaAll = 0.0;
    Boolean bphone, bmail, baddress = false;
    String Curr_reg = "1";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_u_zakaz);
        Toolbar toolbar = (Toolbar) findViewById(R.id.zakaz_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recyclerView = (RecyclerView)findViewById(R.id.Zak_list);
        summa_z = (TextView) findViewById(R.id.zakaz_summa_z_tv);
        summa_all = (TextView) findViewById(R.id.zakaz_summa_all_tv);
        address_link = (TextView) findViewById(R.id.zakaz_address_link);
        summa_z_line = (RelativeLayout) findViewById(R.id.zakaz_summa_z_rl);
        summa_all_line = (RelativeLayout) findViewById(R.id.zakaz_summa_all_rl);
        selfv = (RadioButton) findViewById(R.id.zakaz_radio_selfv);
        courier = (RadioButton) findViewById(R.id.zakaz_radio_courier);
        regions = (Spinner) findViewById(R.id.zakaz_regions_spinner);
        regions_header = (TextView) findViewById(R.id.zakaz_regions_header);
        regions_info = (TextView) findViewById(R.id.zakaz_regions_info);

        dbHelper = new DBHelper(this);
        newAdapter();

        //Radio доставка
        selfv.setChecked(true);
        regions.setVisibility(View.GONE);
        regions_header.setVisibility(View.GONE);
        address_link.setVisibility(View.GONE);
        summa_all.setVisibility(View.GONE);
        courier.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    regions.setVisibility(View.VISIBLE);
                    regions_header.setVisibility(View.VISIBLE);
                    address_link.setVisibility(View.VISIBLE);
                    summa_all.setVisibility(View.VISIBLE);
                    regions_info.setText(getString(R.string.u_zakaz_loading_data));
                    new ParseTaskParams().execute();
                    baddress = true;
                }
            }
        });
        selfv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    regions.setVisibility(View.GONE);
                    regions_header.setVisibility(View.GONE);
                    address_link.setVisibility(View.GONE);
                    regions_info.setText(R.string.u_zakaz_regions_free);
                    //summa_all.setVisibility(View.GONE);
                    summaD = 0.0;
                    delivery = 0;
                    freeDelivery = 0;
                    regions_info.setText(getString(R.string.u_zakaz_loading_data));
                    new ParseTaskParams().execute();
                    baddress = false;
                }
            }
        });

        //Спиннер "Регионы"
        new ParseTaskList().execute();
        regions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected,
                                       int selectedItemPosition, long selectedId) {
                if (regIds != null) {
                    Curr_reg = regIds.get(regions.getSelectedItemPosition());
                    regions_info.setText(getString(R.string.u_zakaz_loading_data));
                    new ParseTaskParams().execute();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Name = (EditText) findViewById(R.id.zakaz_name_edit);
        Phone = (EditText) findViewById(R.id.zakaz_phone_edit);
        Email = (EditText) findViewById(R.id.zakaz_email_edit);
        Address = (EditText) findViewById(R.id.zakaz_address_edit);
        Comment = (EditText) findViewById(R.id.zakaz_zakaz_edit);
        Send = (RelativeLayout) findViewById(R.id.zakaz_send_rl);
        loadText();

        //Обработка ввода текста
        inputTextWatcher inputTextWatcher= new inputTextWatcher(Phone);
        Phone.addTextChangedListener(inputTextWatcher);

        errNon = (TextView) findViewById(R.id.zakaz_name_error);
        errNocp = (TextView) findViewById(R.id.zakaz_phone_error);
        errNoce = (TextView) findViewById(R.id.zakaz_email_error);
        errNoa = (TextView) findViewById(R.id.zakaz_address_error);

        errNon.setVisibility(View.GONE);
        errNocp.setVisibility(View.GONE);
        errNoce.setVisibility(View.GONE);
        errNoa.setVisibility(View.GONE);

        //Ссылка "Оплата и доставка"
        oplata_i_dostavka = (TextView) findViewById(R.id.oplata_i_dostavka);
        String textWithLink = "<a href=\"" + getString(R.string.oplata_i_dostavka_link) + "\">"
                + getString(R.string.oplata_i_dostavka) + "</a>";
        oplata_i_dostavka.setText(Html.fromHtml(textWithLink, null, null));
        oplata_i_dostavka.setLinksClickable(true);
        oplata_i_dostavka.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = oplata_i_dostavka.getText();
        if (text instanceof Spannable) {
            oplata_i_dostavka.setText(u_kourier.reformatText(text));
        }

        //Кнопка "Отправить заказ"
        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((selfv.isChecked() || (courier.isChecked() && summaZ >= delivery))
                        && summaZ > 0) {
                    beforeSend();
                } else {
                    AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(u_zakaz.this);
                    mDialogBuilder
                            .setTitle(R.string.u_zakaz_send_error_title)
                            .setMessage(R.string.u_zakaz_send_no_min_cost)
                            .setCancelable(true)
                            .setNegativeButton(R.string.back,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alertDialog = mDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
    }

    //Заполнение заказа
    public List<Zakaz> fillData(){
        items = new ArrayList();
        summaZ = 0.0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("Zakaztable", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int pidColIndex = c.getColumnIndex("pid");
            int nameColIndex = c.getColumnIndex("name");
            int priceColIndex = c.getColumnIndex("price");
            int imageColIndex = c.getColumnIndex("image");
            int bigimageColIndex = c.getColumnIndex("bigimage");
            int kolColIndex = c.getColumnIndex("kol");
            int stateColIndex = c.getColumnIndex("state");
            do {
                items.add(new Zakaz(c.getInt(pidColIndex), c.getString(nameColIndex), c.getDouble(priceColIndex),
                        c.getString(imageColIndex), c.getString(bigimageColIndex), c.getInt(kolColIndex),
                        c.getString(stateColIndex)));

                summaZ += c.getFloat(priceColIndex) * c.getInt(kolColIndex);

            } while (c.moveToNext());
        }
        deleted = c.getCount();
        c.close();
        summa_z.setText(getText(R.string.u_zakaz_summa_z).toString() + " " + String.valueOf(summaZ) + " руб.");
        newSummaAll();
        return items;
    }

    //Получение списка регионов
    private class ParseTaskList extends AsyncTask<Void, Integer, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        URL url;

        @Override
        protected String doInBackground(Void... params) {

            try {
                url = new URL("ссылка на список доступных регионов доставки");

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
            }
            return resultJson;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            JSONObject dataJsonObj = null;

            try {
                regValues.clear();
                regIds.clear();

                dataJsonObj = new JSONObject(strJson);
                JSONObject regionsdata = dataJsonObj.getJSONObject("data");
                JSONArray regionslist = regionsdata.getJSONArray("destinations");

                for (int i = 0; i < regionslist.length(); i++) {
                    JSONObject regions = regionslist.getJSONObject(i);
                    regValues.add(regions.getString("name"));
                    regIds.add(regions.getString("id"));
                }

                ArrayAdapter<String> adapter_se = new ArrayAdapter<String>(u_zakaz.this,
                        android.R.layout.simple_spinner_item, regValues);
                adapter_se.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                regions.setAdapter(adapter_se);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Получение параметров доставки для региона
    private class ParseTaskParams extends AsyncTask<Void, Integer, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        URL url;

        @Override
        protected String doInBackground(Void... params) {

            String strSum = "1500.0";
            try {
                url = new URL("ссылка на получение цены доставки" +
                        "cart_total=" + strSum + "&destination_id=" + Curr_reg);

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
            }
            return resultJson;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            JSONObject dataJsonObj = null;

            try {
                dataJsonObj = new JSONObject(strJson);
                JSONObject paramsdata = dataJsonObj.getJSONObject("data");
                JSONObject paramslist = paramsdata.getJSONObject("shipping");

                available = paramslist.getInt("is_available");
                Double cost = paramslist.getDouble("cost");
                String info = paramslist.getString("string");
                delivery = paramslist.getInt("order_for_delivery");
                freeDelivery = paramslist.getInt("order_for_free_delivery");
                String infoText = info;
                if (delivery > 0) {
                    infoText += "\n" + getString(R.string.u_zakaz_regions_cost_delivery) + " "
                            + String.valueOf(delivery) + " руб.";
                }
                if (freeDelivery > 0) {
                    infoText += "\n" + getString(R.string.u_zakaz_regions_cost_free_delivery) + " "
                            + String.valueOf(freeDelivery) + " руб.";
                }
                regions_info.setText(infoText);
                summaD = cost;
                newSummaAll();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Адаптер recyclerView, пересчет суммы
    public void newAdapter() {
        items = fillData();
        zakazAdapter = new ZakazAdapter(this, items);
        recyclerView.setAdapter(zakazAdapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        zakazAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                summaZ = 0.0;
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor c = db.query("Zakaztable", null, null, null, null, null, null);
                if (c.moveToFirst()) {
                    int priceColIndex = c.getColumnIndex("price");
                    int kolColIndex = c.getColumnIndex("kol");
                    do {
                        summaZ += c.getInt(priceColIndex) * c.getInt(kolColIndex);
                    } while (c.moveToNext());
                }
                if (deleted > c.getCount()){

                    deleted = c.getCount();
                }
                c.close();
                summa_z.setText(getText(R.string.u_zakaz_summa_z).toString() + " " + String.valueOf(summaZ) + " руб.");
                if (selfv.isChecked()) {
                    newSummaAll();
                } else {
                    regions_info.setText(getString(R.string.u_zakaz_loading_data));
                    new ParseTaskParams().execute();
                }
                newAdapter();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                Toast.makeText(u_zakaz.this, "click!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                Toast.makeText(u_zakaz.this, "delete!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Пересчет общей суммы
    public void newSummaAll () {
        if (selfv.isChecked() || (courier.isChecked() && summaZ >= freeDelivery)) {
            summaAll = summaZ;
            delivery = 0;
            freeDelivery = 0;
            regions_info.setText(R.string.u_zakaz_regions_free);
            summa_all.setVisibility(View.GONE);
        } else {
            if (summaZ >= delivery) {
                summaAll = summaZ + summaD;
                summa_all.setVisibility(View.VISIBLE);
                summa_all.setText(getText(R.string.u_zakaz_summa_all).toString() + " "
                        + String.valueOf(summaAll) + " руб.");
            } else {
                summa_all.setText(R.string.u_zakaz_regions_sryno);
            }
        }
    }

    //Проверка заполнения полей перед отправкой
    public  void beforeSend() {
        Boolean bname = false;
        bphone = false;
        bmail = false;
        Name.setText(Name.getText().toString().trim());
        Phone.setText(Phone.getText().toString().trim());
        Email.setText(Email.getText().toString().trim());
        Address.setText(Address.getText().toString().trim());
        Comment.setText(Comment.getText().toString().trim());
        if (Name.length() == 0) {
            bname = false;
            errNon.setVisibility(View.VISIBLE);
        } else {
            bname = true;
            errNon.setVisibility(View.GONE);
        }
        if (Phone.length() <= 2 && Email.length() == 0) {
            bphone = false;
            bmail = false;
            errNocp.setText(getString(R.string.error_no_phonemail));
            errNoce.setText(getString(R.string.error_no_phonemail));
            errNocp.setVisibility(View.VISIBLE);
            errNoce.setVisibility(View.VISIBLE);
        } else {
            if (Phone.length() != 13 ||
                    Phone.getText().toString().startsWith("8(") == false ||
                    Phone.getText().toString().indexOf("+", 0) != -1 ||
                    Phone.getText().toString().indexOf("-", 0) != -1 ||
                    Phone.getText().toString().indexOf(".", 0) != -1 ||
                    Phone.getText().toString().indexOf(",", 0) != -1 ||
                    Phone.getText().toString().indexOf("#", 0) != -1 ||
                    Phone.getText().toString().indexOf("*", 0) != -1) {
                bphone = false;
                errNocp.setText(getString(R.string.error_no_correct_phone));
                errNocp.setVisibility(View.VISIBLE);
            } else {
                bphone = true;
                errNocp.setVisibility(View.GONE);
            }
            if (Email.getText().toString().indexOf("@", 1) == -1 ||
                    Email.getText().toString().indexOf(".", 1) == -1 ||
                    Email.getText().toString().startsWith("@") == true ||
                    Email.getText().toString().endsWith("@") == true ||
                    Email.getText().toString().startsWith(".") == true ||
                    Email.getText().toString().endsWith(".") == true) {
                bmail = false;
                errNoce.setText(getString(R.string.error_no_correct_email));
                errNoce.setVisibility(View.VISIBLE);
            } else {
                bmail = true;
                errNoce.setVisibility(View.GONE);
            }
        }
        if (baddress && Address.length() == 0) {
            errNoa.setVisibility(View.VISIBLE);
        } else {
            errNoa.setVisibility(View.GONE);
        }

        if (bname && (!baddress || (baddress && Address.length() != 0)) && (
                (bmail && bphone) || (!bmail && bphone) || (bmail && !bphone))) {
            errNoa.setVisibility(View.GONE);
            errNoce.setVisibility(View.GONE);
            new SendZakaz().execute();
            //saveText();
        } else {
            if (!bmail && !bphone)
                Toast.makeText(u_zakaz.this, "Заполните поле контактных данных", Toast.LENGTH_LONG).show();
            if (bmail && !bphone)
                errNocp.setVisibility(View.GONE);
            if (!bmail && bphone)
                errNoce.setVisibility(View.GONE);
            if (!bname)
                Toast.makeText(u_zakaz.this, "Заполните поле Имя", Toast.LENGTH_LONG).show();
            if (baddress && Address.length() == 0)
                Toast.makeText(u_zakaz.this, "Введите адрес доставки", Toast.LENGTH_LONG).show();
        }
    }

    //Отпарвить заказ
    public class SendZakaz extends AsyncTask<String, String, String> {

        ProgressDialog WaitingDialog;
        String[] formTexts;
        String region, address, answerHTTP = "";
        Integer is_delivery;
        String server = "ссылка на адрес принимающего заказ";

            @Override
            protected void onPreExecute() {

                if (courier.isChecked()) {
                    is_delivery = 1;
                    region = Curr_reg;
                    address = Address.getText().toString();
                } else {
                    is_delivery = 0;
                    region = "0";
                    address = "";
                }

                formTexts = new String[] {
                        Name.getText().toString(),
                        Phone.getText().toString(),
                        Email.getText().toString(),
                        region,
                        address,
                        Comment.getText().toString()};
                super.onPreExecute();

                WaitingDialog = ProgressDialog.show(u_zakaz.this, getString(R.string.u_zakaz_wait_dialog_title),
                        getString(R.string.u_zakaz_sending_data));
            }

            @Override
            protected String doInBackground(String... params) {

                List<Integer> productsId = new ArrayList<Integer>();
                List<Integer> productsQuantity = new ArrayList<Integer>();

                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor c = db.query("Zakaztable", null, null, null, null, null, null);
                if (c.moveToFirst()) {
                    int pidColIndex = c.getColumnIndex("pid");
                    int kolColIndex = c.getColumnIndex("kol");
                    do {
                        productsId.add(c.getInt(pidColIndex));
                        productsQuantity.add(c.getInt(kolColIndex));
                    } while (c.moveToNext());
                }
                Integer prodQ = c.getCount();
                c.close();
                dbHelper.close();

                try{

                    String postMessage =
                            "{" +
                                    " \"is_delivery\": " + String.valueOf(is_delivery) +
                                    ", \"form_fields\": " +
                                        "{" +
                                            " \"2\": \"" + formTexts[0].toString() + "\"" +
                                            ", \"17\": \"" + formTexts[1].toString() + "\"" +
                                            ", \"8\": \"" + formTexts[2].toString() + "\"" +
                                            ", \"42\": " + formTexts[3].toString() +
                                            ", \"4\": \"" + formTexts[4].toString() + "\"" +
                                            ", \"21\": \"" + formTexts[5].toString() + "\"" +
                                        "}" +
                                    ", \"products\": [";

                    for (int i = 0; i < prodQ; i++) {
                        postMessage += "{\"id\": " + productsId.get(i).toString() + "" +
                                       ", \"quantity\": " + productsQuantity.get(i).toString() + "}";
                        if(i<prodQ-1){
                            postMessage += ", ";
                        }
                    }
                        postMessage += "]}";

                    answerHTTP = performPostCall(server, postMessage);
                    return answerHTTP;

                } catch (Exception e){
                    e.printStackTrace();
                    return "error";
                }
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                JSONObject dataJsonObj = null;

                if (result == "error") {
                    Toast.makeText(u_zakaz.this, getString(R.string.error_server_conn), Toast.LENGTH_LONG).show();
                } else
                try {
                    dataJsonObj = new JSONObject(result);
                    JSONObject resultdata = dataJsonObj.getJSONObject("data");
                    if ( dataJsonObj.getInt("result") == 1) {

                        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(u_zakaz.this);
                        mDialogBuilder
                                .setTitle(getString(R.string.u_zakaz_send_complete_title))
                                .setMessage(getString(R.string.u_zakaz_send_complete_text_beg) + "\n" +
                                        getString(R.string.u_zakaz_send_complete_text_end) + " " +
                                        String.valueOf(resultdata.getInt("order_id")))
                                .setCancelable(true)//false)
                                .setPositiveButton(getString(R.string.okay),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int id) {
                                                Intent intent = new Intent(u_zakaz.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        });
                        AlertDialog alertDialog = mDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        Toast.makeText(u_zakaz.this, "Отрицательный ответ сервера", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(u_zakaz.this, getString(R.string.error_server_conn), Toast.LENGTH_LONG).show();
                }
                saveText();
                WaitingDialog.dismiss();
            }
        }
    //POST
    public String performPostCall(String requestURL, String postMessage) {
            URL url;
            String response = "";
            try {
                url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write (postMessage);

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";

                }
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        }

    //Обработчик ввода номера телефона
    public class inputTextWatcher implements TextWatcher {

        public EditText editText;
        Integer length = 2;
        Boolean one = false;

        public inputTextWatcher(EditText userInput){
            super();
            editText = userInput;
        }
        public void afterTextChanged(Editable s) {
            if(editText.getText().length() == 5 && length < editText.length() && !one) {
                editText.setText(editText.getText().toString() + ")");
                editText.setSelection(6);
                one = true;
            }
            length  = editText.length();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){

        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    //Ссылка "Оплата и доставка"
    public static class CustomerTextClick extends ClickableSpan
    {
        String Url;

        public CustomerTextClick(String url)
        {
            Url = url;
        }

        @Override
        public void onClick(View widget)
        {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(Url));
            widget.getContext().startActivity(i);
        }
    }

    public static SpannableStringBuilder reformatText(CharSequence text)
    {
        int end = text.length();
        Spannable sp = (Spannable) text;
        URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        for (URLSpan url : urls)
        {
            style.removeSpan(url);
            u_kourier.CustomerTextClick click = new u_kourier.CustomerTextClick(url.getURL());
            style.setSpan(click, sp.getSpanStart(url), sp.getSpanEnd(url),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return style;
    }

    //Сохранение и загрузка данных полей
    void saveText() {

        Log.d("classInJson", "Start!" );

        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        String nName = Name.getText().toString();
        String nPhone = Phone.getText().toString();
        String nEmail = Email.getText().toString();
        String nAddr = Address.getText().toString();

        Log.d("classInJson", nName + "; " + nPhone + "; " + nEmail + "; " + nAddr + ".");

        c = db.query("UserData", null, null, null, null, null, null);
        if (c.getCount() == 0) {
            cv.put("name", nName);
            cv.put("phone", nPhone);
            cv.put("email", nEmail);
            cv.put("address", nAddr);
            db.insert("UserData", null, cv);

            Log.d("classInJson", "Insert: " + nName + "; " + nPhone + "; " + nEmail + "; " + nAddr + ".");

        } else {
            c.moveToFirst();
            String sName = c.getString(1);
            String sPhone = c.getString(2);
            String sEmail = c.getString(3);
            String sAddr = c.getString(4);
            if (sName != nName) {
                cv.put("name", nName);
                db.update("UserData", cv, null, null);

                Log.d("classInJson", "nName");

            }
            if (sPhone != nPhone && bphone == true) {
                cv.put("phone", nPhone);
                db.update("UserData", cv, null, null);

                Log.d("classInJson", "nPhone");

            }
            if (sEmail != nEmail && bmail == true) {
                cv.put("email", nEmail);
                db.update("UserData", cv, null, null);

                Log.d("classInJson", "nEmail");

            }
            if (sAddr != nAddr) {
                cv.put("address", nAddr);
                db.update("UserData", cv, null, null);

                Log.d("classInJson", "nAddr");

            }

            Log.d("classInJson", "New: " + nName + "; " + nPhone + "; " + nEmail + "; " + nAddr + ".");
        }
        c.close();
        dbHelper.close();
    }
    void loadText() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        c = db.query("UserData", null, null, null, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
            Name.setText(c.getString(1));
            Phone.setText(c.getString(2));
            Email.setText(c.getString(3));
            Address.setText(c.getString(4));
        }
        c.close();
        dbHelper.close();
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

