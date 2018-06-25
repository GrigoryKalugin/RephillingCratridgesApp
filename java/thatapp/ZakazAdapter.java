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
import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.method.Touch;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.a1.iturapp.u_zakaz.recyclerView;
import static com.example.a1.iturapp.u_zakaz.zakazAdapter;


public class ZakazAdapter extends RecyclerView.Adapter<ZakazAdapter.ViewHolder> {

    View view;
    Context context;
    LayoutInflater lInflater;
    private List<Zakaz> items;
    DBHelper dbHelper;
    public ZakazAdapter(Context context, List<Zakaz> items) {
        this.context = context;
        this.items = items;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dbHelper = new DBHelper(context);
    }
    TextView Next, Prev;
    int pos = 1;

    /**
     * Создание новых View и ViewHolder элемента списка, которые впоследствии могут переиспользоваться.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zakaz_item, viewGroup, false);
        return new ViewHolder(v);
    }

    /**
     * Заполнение виджетов View данными из элемента списка с номером i
     */
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        final Zakaz item = items.get(i);
        viewHolder.name.setText(item.name);
        viewHolder.price.setText(Double.toString(item.price) + " руб.");
        new DownloadImageTask(viewHolder.image).execute(item.image);
        viewHolder.kol.setText(Integer.toString(item.kol));
        viewHolder.state.setText(item.state);

        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogImageOpen (item.name, item.bigimage);
            }
        });

        //-1
        viewHolder.before.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor c = null;
                c = db.query("Zakaztable", null, "name = ?", new String[] {item.name}, null, null, null);
                if (c.getCount() == 0) {
                    Toast.makeText(context, "Ошибка! товар с таким именем не найден", Toast.LENGTH_LONG).show();
                } else {
                    c.moveToFirst();
                    if (c.getInt(4) > 1) {
                        Integer sum = c.getInt(4) - 1;
                        cv.put("kol", sum);
                        db.update("Zakaztable", cv, "name = ?", new String[]{item.name});
                        //viewHolder.kol.setText(String.valueOf(sum));
                        //zakazAdapter.notifyItemChanged(-1);
                        zakazAdapter.notifyDataSetChanged();
                    }
                }
                    c.close();
            }
        });

        //+1
        viewHolder.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor c = null;
                c = db.query("Zakaztable", null, "name = ?", new String[] {item.name}, null, null, null);
                if (c.getCount() == 0) {
                    Toast.makeText(context, "Ошибка! товар с таким именем не найден", Toast.LENGTH_LONG).show();
                } else {
                    c.moveToFirst();
                    Integer sum = c.getInt(4) + 1;
                    cv.put("kol", sum);
                    db.update("Zakaztable", cv, "name = ?", new String[]{item.name});
                    //viewHolder.kol.setText(String.valueOf(sum));
                    //zakazAdapter.notifyItemChanged(-1);
                    zakazAdapter.notifyDataSetChanged();
                }
                    c.close();
            }
        });

        //ручной ввод
        viewHolder.kol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pos = item.kol;
                dialogOpen(item.name, item.price);
            }
        });

        //Delete
        viewHolder.remove.setOnTouchListener(new View.OnTouchListener() {
            Integer j = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewHolder.deleteBar.setProgress(j);
                viewHolder.deleteBarBot.setProgress(j);
                j += 7;
                if (event.getAction() == 1 || event.getAction() == 3) {
                    j = 0;
                    viewHolder.deleteBar.setProgress(j);
                    viewHolder.deleteBarBot.setProgress(j);
                } else if (j>= viewHolder.deleteBar.getMax()) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete("Zakaztable", "name = \"" + item.name + "\"", null);
                    //items.remove(i);
                    j = 0;
                    //zakazAdapter.notifyItemRemoved(i);
                    zakazAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
    }

    //Диалог изменения количества
    void dialogOpen(final String nm, final Double prc) {
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
        Next.setText(String.valueOf(pos + 1));
        Prev.setText(String.valueOf(pos - 1));

        NameTov.setText (nm + "; (" + prc + " руб/ед)");
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
                                ContentValues cv = new ContentValues();
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                Cursor c = null;
                                c = db.query("Zakaztable", null, "name = ?", new String[] {nm}, null, null, null);
                                if (c.getCount() == 0) {
                                    Toast.makeText(context, "Ошибка! товар с таким именем не найден", Toast.LENGTH_LONG).show();
                                } else {
                                    c.moveToFirst();
                                    Integer sum = pos;
                                    cv.put("kol", sum);
                                    db.update("Zakaztable", cv, "name = ?", new String[]{nm});
                                    zakazAdapter.notifyDataSetChanged();
                                }
                                c.close();
                            }
                        })
                .setNegativeButton(R.string.abort,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
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
        private TextView kol;
        private TextView state;

        private ImageView remove;
        private ImageView before;
        private ImageView next;

        private ProgressBar deleteBar;
        private ProgressBar deleteBarBot;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.zakaz_view_name);
            price = (TextView) itemView.findViewById(R.id.zakaz_view_cost);
            image = (ImageView) itemView.findViewById(R.id.zakaz_view_img);
            kol = (TextView) itemView.findViewById(R.id.zakaz_view_kol);
            state = (TextView) itemView.findViewById(R.id.zakaz_view_stat);

            remove = (ImageView) itemView.findViewById(R.id.zakaz_view_deletebutton);
            before = (ImageView) itemView.findViewById(R.id.zakaz_arrow_before);
            next = (ImageView) itemView.findViewById(R.id.zakaz_arrow_next);

            deleteBar = (ProgressBar) itemView.findViewById(R.id.zakaz_item_delprogress);
            deleteBarBot = (ProgressBar) itemView.findViewById(R.id.zakaz_item_delprogress_bot);
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

