package ru.xakerd.cityhunter;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.app.*;
import android.os.AsyncTask;

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

public class CategoryActivity extends Activity {

    private ArrayAdapter<String> adapter;
    private ArrayList<String> categoryId = new ArrayList<>();
    private final ArrayList<String> categoryName = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView categoryList;

        progressDialog = new ProgressDialog(this, R.style.MyTheme);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();

        categoryList = (ListView) findViewById(R.id.listcategory);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, categoryName);

        categoryList.setAdapter(adapter);

        if (isNetworkConnected())
            new ParseTask().execute();
        else
        { progressDialog.hide();
            Toast.makeText(this,getResources().getString(R.string.is_connected),Toast.LENGTH_SHORT).show();}

        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CategoryActivity.this, ShebangActivity.class);
                intent.putExtra("category_id", categoryId.get(position));
                intent.putExtra("category_name", categoryName.get(position));
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
                URL url = new URL(getResources().getString(R.string.url_category));

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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            // выводим целиком полученную json-строку
            JSONArray dataJsonObj;


            try {
                dataJsonObj = new JSONArray(strJson);


                for (int i = 0; i < dataJsonObj.length(); i++) {
                    JSONObject category = dataJsonObj.getJSONObject(i);
                    String id = category.getString("id");
                    categoryId.add(id);
                    String name = category.getString("name");
                    categoryName.add(name.toUpperCase());

                }
                adapter.notifyDataSetChanged();

                progressDialog.hide();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null;
    }
}
