package ru.xakerd.cityhunter;


import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


/**
 * Created by User on 31.05.2016.
 */

public class ActivityShebang extends Activity implements View.OnClickListener {
    static String category_id,category_name;
    ListView lv;
    private ArrayList<HashMap<String, Object>> myshebang;
    private static final String title = "mytitle",
            description = "mydescription",
            address = "myaddress", image = "myimage", is_rec = "is_rec", idpost = "id";
    private HashMap<String, Object> hm;
    MyAdapter adapter;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shebang);
        ImageButton imageButton =(ImageButton) findViewById(R.id.btnBack);
        imageButton.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView)toolbar.findViewById(R.id.toolbar_title);

        progressDialog = new ProgressDialog(this, R.style.MyTheme);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();

        lv = (ListView) findViewById(R.id.lv);
        Bundle extras = getIntent().getExtras();
        category_id = extras.getString(category_id);
        category_name = extras.getString("category_name");

        toolbar_title.setText(category_name);
        myshebang = new ArrayList<HashMap<String, Object>>();
        hm = new HashMap<String, Object>();
        adapter = new MyAdapter(this, myshebang, R.layout.list_shebang,
                new String[]{
                        title,
                        address,
                        description,
                        image,
                        is_rec
                },
                new int[]{
                        R.id.textTitle,
                        R.id.textAdres,
                        R.id.textDesk,
                        R.id.imageView,
                        R.id.imageRec

                });
        View v = getLayoutInflater().inflate(R.layout.item_listviw_footer, null);
        lv.addFooterView(v,null,false);
        lv.addHeaderView(v,null,false);
        lv.setAdapter(adapter);

        if (isNetworkConnected())
            new ParseTask().execute();
        else
        { progressDialog.hide();
            Toast.makeText(this,"Подключитесь к сети",Toast.LENGTH_SHORT).show();}
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ActivityShebang.this, ActivityInfo.class);
                HashMap hashMap = myshebang.get(position-1);
                intent.putExtra(ActivityInfo.post_id, hashMap.get(idpost).toString());


                intent.putExtra("title",hashMap.get(title).toString());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onBackPressed();
    }


    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL("http://api.cityhunter.kz/posts?category_id=" + category_id + "[&type=new]");

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
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            // выводим целиком полученную json-строку


            JSONArray dataJsonObj = null;


            try {
                dataJsonObj = new JSONArray(strJson);


                for (int i = 0; i < dataJsonObj.length(); i++) {
                    JSONObject shebang = dataJsonObj.getJSONObject(i);
                    String post_id = shebang.getString("id");
                    String post_title = Html.fromHtml(shebang.getString("title")).toString();
                    String post_is_rec = shebang.getString("is_rec");
                    String post_thumb = shebang.getString("thumb");
                    String post_description = Html.fromHtml(shebang.getString("short_description")).toString();
                    String post_address = shebang.getString("address");
                    hm = new HashMap<String, Object>();
                    hm.put(title, post_title);
                    hm.put(address, post_address);
                    hm.put(description, post_description);
                    hm.put(image, post_thumb);
                    hm.put(is_rec,post_is_rec);
                    hm.put(idpost,post_id);
                    if (Boolean.valueOf(post_is_rec))
                    myshebang.add(0,hm);
                    else
                        myshebang.add(hm);



                }

                adapter.notifyDataSetChanged();
                progressDialog.hide();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class MyAdapter extends SimpleAdapter {

        private ArrayList<HashMap<String, Object>> results;
        private Context context;

        public MyAdapter(Context context,
                         ArrayList<HashMap<String, Object>> data, int resource,
                         String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.results = data;
            this.context = context;
        }

        public View getView(int position, View view, ViewGroup parent) {
            View v = view;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_shebang, null);
            }
            TextView text1 = (TextView) v.findViewById(R.id.textTitle);
            text1.setText(results.get(position).get("mytitle").toString());


            TextView text2 = (TextView) v.findViewById(R.id.textDesk);
            text2.setText(results.get(position).get("mydescription").toString());
            if (TextUtils.isEmpty(results.get(position).get("mydescription").toString()))
                text2.setVisibility(View.GONE);
            else
                text2.setVisibility(View.VISIBLE);

            TextView text3 = (TextView) v.findViewById(R.id.textAdres);
            text3.setText(results.get(position).get("myaddress").toString());
            if (results.get(position).get("myaddress").toString() == "null")
                text3.setVisibility(View.GONE);
            else
                text3.setVisibility(View.VISIBLE);

            ImageView image_is_rec = (ImageView) v.findViewById(R.id.imageRec);
            if (Boolean.valueOf(results.get(position).get("is_rec").toString()))
            {image_is_rec.setImageResource(R.drawable.recommend);
            image_is_rec.setBackgroundColor(Color.parseColor("#d5f9f518"));
            image_is_rec.setVisibility(View.VISIBLE);}
            else
            image_is_rec.setVisibility(View.GONE);

            ImageView image = (ImageView) v.findViewById(R.id.imageView);
            //"cityhunter.kz/uploads/images/1441439294.jpg",
            //Picasso.with(context).load(results.get(position).get("myimage").toString()).error(R.mipmap.ic_launcher).into(image);
            Picasso.with(context)
                    .load("http://" + results.get(position)
                            .get("myimage").toString())
                    .into(image);
            return v;
        }
    }

    boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null;
    }


}
