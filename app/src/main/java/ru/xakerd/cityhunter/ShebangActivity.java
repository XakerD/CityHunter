package ru.xakerd.cityhunter;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;



public class ShebangActivity extends Activity {
    static final String EXTRA_CATEGORY_ID ="category_id",
                        EXTRA_CATEGORY_NAME ="category_name";
    private String categoryId;
    private ArrayList<HashMap<String, Object>> shebang;
    private final String title = "title",
            description = "description",
            address = "address", image = "image", is_rec = "is_rec", id = "id";
    private HashMap<String, Object> hm;
    private MyAdapter adapter;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shebang);
        Bundle extras = getIntent().getExtras();
        String categoryName;

        categoryId = extras.getString(EXTRA_CATEGORY_ID);
        categoryName = extras.getString(EXTRA_CATEGORY_NAME);

        Toolbar toolbar = (Toolbar) findViewById(R.id.shebang_toolbar);
        if (!categoryId.isEmpty()){
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            toolbar.setTitle(categoryName);
            toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left);
        }
        else
        toolbar.setVisibility(View.GONE);



        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShebangActivity.super.onBackPressed();
            }
        });

        progressDialog = new ProgressDialog(this, R.style.MyTheme);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();
        ListView listShebang;
        listShebang = (ListView) findViewById(R.id.shebang_listView);


        shebang = new ArrayList<>();
        hm = new HashMap<>();
        adapter = new MyAdapter(this, shebang, R.layout.shebang_list_item,
                new String[]{
                        title,
                        address,
                        description,
                        image,
                        is_rec
                },
                new int[]{
                        R.id.shebang_title,
                        R.id.shebang_address,
                        R.id.shebang_description,
                        R.id.shebang_image,
                        R.id.shebang_is_rec

                });
        View v = getLayoutInflater().inflate(R.layout.item_listview_footer, null);
        listShebang.addFooterView(v,null,false);
        listShebang.addHeaderView(v,null,false);
        listShebang.setAdapter(adapter);

        if (isNetworkConnected())
            new ParseTask().execute();
        else
        { progressDialog.hide();
            Toast.makeText(this,getResources().getString(R.string.is_connected),Toast.LENGTH_SHORT).show();}
        listShebang.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ShebangActivity.this, InfoActivity.class);
                HashMap hashMap = shebang.get(position-1);
                intent.putExtra(InfoActivity.EXTRA_ID, hashMap.get(ShebangActivity.this.id).toString());
                intent.putExtra(InfoActivity.EXTRA_TITLE,hashMap.get(title).toString());
                startActivity(intent);
            }
        });
    }

    private class ParseTask extends AsyncTask<Void, Void, Void> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        @Override
        protected Void doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                String resultJson;

                URL url;
                if (!categoryId.isEmpty())
                    url = new URL(getResources().getString(R.string.url_shebang) + categoryId);
                else
                    url = new URL(getResources().getString(R.string.url_new_shebang));

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                resultJson = builder.toString();
                //обработка json
                JSONArray dataJsonObj;
                dataJsonObj = new JSONArray(resultJson);


                for (int i = 0; i < dataJsonObj.length(); i++) {
                    JSONObject objShebang = dataJsonObj.getJSONObject(i);
                    String post_id = objShebang.getString("id");
                    String post_title = Html.fromHtml(objShebang.getString("title")).toString();
                    String post_is_rec = objShebang.getString("is_rec");
                    String post_thumb = objShebang.getString("thumb");
                    String post_description = Html.fromHtml(objShebang.getString("short_description")).toString();
                    String post_address = objShebang.getString("address");
                    hm = new HashMap<>();
                    hm.put(title, post_title);
                    hm.put(address, post_address);
                    hm.put(description, post_description);
                    hm.put(image, post_thumb);
                    hm.put(is_rec,post_is_rec);
                    hm.put(id,post_id);
                    if (Boolean.valueOf(post_is_rec))
                        ShebangActivity.this.shebang.add(0,hm);
                    else
                        ShebangActivity.this.shebang.add(hm);



                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
                adapter.notifyDataSetChanged();
                progressDialog.hide();
        }
    }

    private class MyAdapter extends SimpleAdapter {

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
                v = vi.inflate(R.layout.shebang_list_item, null);
            }
            TextView text1 = (TextView) v.findViewById(R.id.shebang_title);
            text1.setText(results.get(position).get("title").toString());


            TextView text2 = (TextView) v.findViewById(R.id.shebang_description);
            text2.setText(results.get(position).get("description").toString());
            if (TextUtils.isEmpty(results.get(position).get("description").toString()))
                text2.setVisibility(View.GONE);
            else
                text2.setVisibility(View.VISIBLE);

            TextView text3 = (TextView) v.findViewById(R.id.shebang_address);
            text3.setText(results.get(position).get("address").toString());
            if (results.get(position).get("address").toString().equals("null"))
                text3.setVisibility(View.GONE);
            else
                text3.setVisibility(View.VISIBLE);

            ImageView imageIsRec = (ImageView) v.findViewById(R.id.shebang_is_rec);
            if (Boolean.valueOf(results.get(position).get("is_rec").toString()))
            {imageIsRec.setImageResource(R.drawable.recommend);
            imageIsRec.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSunset));
            imageIsRec.setVisibility(View.VISIBLE);}
            else
            imageIsRec.setVisibility(View.GONE);

            ImageView imageThumb = (ImageView) v.findViewById(R.id.shebang_image);
            Picasso.with(context)
                    .load("http://" + results.get(position)
                            .get("image").toString())
                    .into(imageThumb);
            return v;
        }
    }

    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null;
    }


}
