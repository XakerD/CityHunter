package ru.xakerd.cityhunter;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.*;
import android.os.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.view.View;
import android.widget.*;

import java.util.*;

public class MainActivity extends Activity {
    String LOG_TAG;
    ListView listcategory;
    ArrayAdapter<String> adapter;
    ArrayList<String> category_id = new ArrayList<String>();
    final ArrayList<String> category_name = new ArrayList<String>();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this, R.style.MyTheme);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();

        listcategory = (ListView) findViewById(R.id.listcategory);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, category_name);

        listcategory.setAdapter(adapter);

        if (isNetworkConnected())
            new ParseTask().execute();
        else
        { progressDialog.hide();
            Toast.makeText(this,"Подключитесь к сети",Toast.LENGTH_SHORT).show();}
        listcategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ActivityShebang.class);
                intent.putExtra(ActivityShebang.category_id, category_id.get(position));
                intent.putExtra("category_name", category_name.get(position));
                startActivity(intent);
            }
        });
    }

    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL("http://api.cityhunter.kz/menu");

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
            Log.d(LOG_TAG, strJson);

            JSONArray dataJsonObj = null;


            try {
                dataJsonObj = new JSONArray(strJson);


                for (int i = 0; i < dataJsonObj.length(); i++) {
                    JSONObject category = dataJsonObj.getJSONObject(i);
                    String cid = category.getString("id");
                    category_id.add(cid);
                    String name = category.getString("name");
                    category_name.add(name.toUpperCase());

                }
                adapter.notifyDataSetChanged();

                progressDialog.hide();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null;
    }
}
