package com.example.a1.iturapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.apache.harmony.awt.internal.nls.Messages.getString;
import static com.example.a1.iturapp.u_prntr1.prntrAdapter;

public class PrntrAdapter extends RecyclerView.Adapter<PrntrAdapter.ViewHolder> {

    Context context;
    LayoutInflater lInflater;
    private List<Product> items;
    int pos = 1;
    Boolean exit = false;
    DBHelper dbHelper;
    public PrntrAdapter(Context context, List<Product> items) {
        this.context = context;
        this.items = items;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dbHelper = new DBHelper(context);
    }
    TextView Next, Prev;

    /**
     * Создание новых View и ViewHolder элемента списка, которые впоследствии могут переиспользоваться.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.store_item, viewGroup, false);
        return new ViewHolder(v);
    }

    /**
     * Заполнение виджетов View данными из элемента списка с номером i
     */
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        final Product item = items.get(i);
        viewHolder.name.setText(item.name);
        viewHolder.price.setText(Double.toString(item.price) + " руб.");
        new DownloadImageTask(viewHolder.image).execute(item.image);
        if (item.isserv == 1) {
            viewHolder.state.setText(item.state);
        } else {
            viewHolder.state.setText("Наличие: " + item.state);
        }
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogImageOpen (item.name, item.bigimage);
            }
        });
        viewHolder.kzakazu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogOpen(item.pid, item.name, item.price, item.image, item.bigimage, item.state);
            }
        });
    }

    //Загрузка изображения
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap Icon = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                Icon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Icon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Реализация класса ViewHolder, хранящего ссылки на виджеты.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView price;
        private ImageView image;
        private TextView state;
        private RelativeLayout kzakazu;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.list_view_name);
            price = (TextView) itemView.findViewById(R.id.list_view_cost);
            image = (ImageView) itemView.findViewById(R.id.list_view_img);
            state = (TextView) itemView.findViewById(R.id.list_view_stat);
            kzakazu = (RelativeLayout) itemView.findViewById(R.id.list_view_button);
        }
    }

    //Полноразмерное изображение
    void dialogImageOpen(final String imgname, final String imgaddr) {
        LayoutInflater li = LayoutInflater.from(context);
        View uPrntrDialog = li.inflate(R.layout.bigimage_dialog, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
        mDialogBuilder.setView(uPrntrDialog);
        final ImageView bigImg = (ImageView) uPrntrDialog.findViewById(R.id.bigimage_dialig_iv);
        new DownloadImageTask(bigImg).execute(imgaddr);

        mDialogBuilder
                .setTitle(imgname)
                .setCancelable(true)
                .setNegativeButton(R.string.close_menu,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.show();
    }

    //Диалог добавления в корзину
    void dialogOpen(final Integer pid, final String nm, final Double prc, final String img,
                    final String bimg, final String stt) {
        LayoutInflater li = LayoutInflater.from(context);
        View uPrntrDialog = li.inflate(R.layout.u_prntr_dialog, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
        mDialogBuilder.setView(uPrntrDialog);

        //Настраиваем отображение поля для ввода текста в открытом диалоге:
        final EditText userInput = (EditText) uPrntrDialog.findViewById(R.id.dialog_count);
        final TextView NameTov = (TextView) uPrntrDialog.findViewById(R.id.dialog_name);
        final RelativeLayout Plus = (RelativeLayout) uPrntrDialog.findViewById(R.id.dialog_plus_rl);
        final RelativeLayout Minus = (RelativeLayout) uPrntrDialog.findViewById(R.id.dialog_minus_rl);
        Next = (TextView) uPrntrDialog.findViewById(R.id.dialog_count_next);
        Prev = (TextView) uPrntrDialog.findViewById(R.id.dialog_count_prev);

        NameTov.setText (nm + "; (" + stt + ", " + prc + " руб/ед)");
        userInput.setText (Integer.toString(pos));

        Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pos++;
                userInput.setText (Integer.toString (pos));
                Next.setText (Integer.toString(pos+1));
                Prev.setText (Integer.toString(pos-1));
            }
        });
        Minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(pos>1) {
                pos--;
                userInput.setText (Integer.toString (pos));
                Next.setText (Integer.toString(pos+1));
                Prev.setText (Integer.toString(pos-1));
                }
            }
        });
        inputTextWatcher inputTextWatcher= new inputTextWatcher(userInput);
        userInput.addTextChangedListener(inputTextWatcher);

        //Настраиваем сообщение в диалоговом окне:
        mDialogBuilder
                .setTitle(R.string.dialog_kol)
                .setCancelable(false)
                .setPositiveButton(R.string.dobavit,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                exit = false;
                                pos = Integer.parseInt(userInput.getText().toString());
                                newZakaz(pid, nm, prc, pos, img, bimg, stt);
                                pos = 1;
                                Next.setText(Integer.toString(pos+1));
                                Prev.setText(Integer.toString(pos-1));
                                prntrAdapter.notifyDataSetChanged();
                            }
                        })
                .setNeutralButton(R.string.dobavit_i_zakaz,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        exit = true;
                        pos = Integer.parseInt(userInput.getText().toString());
                        newZakaz(pid, nm, prc, pos, img, bimg, stt);
                        pos = 1;
                        Next.setText(Integer.toString(pos+1));
                        Prev.setText(Integer.toString(pos-1));
                    }
                })
                .setNegativeButton(R.string.abort,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                pos = 1;
                                Next.setText(Integer.toString(pos+1));
                                Prev.setText(Integer.toString(pos-1));
                                dialog.cancel();
                            }
                        });

        //Создаем AlertDialog:
        AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.show();
    }

    //Обработчик ввода текста
    public class inputTextWatcher implements TextWatcher {

        public EditText editText;

        public inputTextWatcher(EditText userInput){
            super();
            editText = userInput;
        }
        public void afterTextChanged(Editable s) {
            if(editText.getText().length() == 3) {
                editText.setText(editText.getText().subSequence(0, editText.getText().length() - 1));
            }
            if(editText.getText().length() >= 1) {
                pos = Integer.parseInt(editText.getText().toString());
                Next.setText(Integer.toString(pos + 1));
                Prev.setText(Integer.toString(pos - 1));
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){

        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    //Добавление в корзину
    void newZakaz (Integer pid, String nm, Double prc, Integer kol, String img, String bimg, String stt) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        //проверка на повторы
        c = db.query("Zakaztable", null, "name = ?", new String[] {nm}, null, null, null);
        if (c.getCount() == 0) {
            cv.put("pid", pid);
            cv.put("name", nm);
            cv.put("price", prc);
            cv.put("kol", kol);
            cv.put("image", img);
            cv.put("bigimage", bimg);
            cv.put("state", stt);
            db.insert("Zakaztable", null, cv);
        } else {
            c.moveToFirst();
            Integer sum = c.getInt(4)+kol;
            cv.put("kol", sum);
            db.update("Zakaztable", cv, "name = ?", new String[] { nm });
        }
        c.close();
        dbHelper.close();
        if (exit == true) {
            Intent intent = new Intent(context, u_zakaz.class);
            context.startActivity(intent);
        }
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