package ru.xakerd.cityhunter;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.squareup.picasso.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by User on 01.06.2016.
 */
public class ActivityInfo extends Activity implements View.OnClickListener, BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {

    String latitude, longitude, address, title, price_value;
    private SliderLayout imageSlider;
    static String post_id,post_title;
    TextView description, textTime;
    TextView btnprice,btnlocation;
    LinearLayout linLayout;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        ImageButton imageButton =(ImageButton) findViewById(R.id.btnBack);
        imageButton.setOnClickListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView)findViewById(R.id.toolbar_title);
        progressDialog = new ProgressDialog(this, R.style.MyTheme);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();

        imageSlider = (SliderLayout) findViewById(R.id.slider);
        imageSlider.stopAutoCycle();
        btnlocation = (TextView) findViewById(R.id.btnlocation);
        btnprice = (TextView) findViewById(R.id.btnprice);
        btnprice.setOnClickListener(this);
        btnlocation.setOnClickListener(this);
        description = (TextView) findViewById(R.id.description);
        textTime = (TextView) findViewById(R.id.textTime);
        Bundle extras = getIntent().getExtras();
        post_id = extras.getString(post_id);
        post_title = extras.getString("title");
        toolbar_title.setText(post_title);
        linLayout = (LinearLayout) findViewById(R.id.linLayout);
        if (isNetworkConnected())
        new ParseTask().execute();
        else
        { progressDialog.hide();
            Toast.makeText(this,"Подключитесь к сети",Toast.LENGTH_SHORT).show();}

    }

    @Override
    public void onResume(){
        super.onResume();
        imageSlider.startAutoCycle();
        // Возобновите все приостановленные обновления UI,
        // потоки или процессы, которые были "заморожены",
        // когда данный объект был неактивным.
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.btnBack)
        {super.onBackPressed();
        return;}
        //Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+77027631259"));
        if ((v.getId() != R.id.btnlocation) && (v.getId() != R.id.btnprice)) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ((Button) v).getText()));

            startActivity(intent);
        } else {
            //String uri = "geo:"+ latitude + "," + longitude;
            //Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
            if (v.getId() == R.id.btnlocation) {
                if (!TextUtils.isEmpty(latitude)) {
                    Intent mapIntent = new Intent(ActivityInfo.this, MapsActivity.class);
                    mapIntent.putExtra("latitude", latitude);
                    mapIntent.putExtra("longitude", longitude);
                    mapIntent.putExtra("title", title);
                    startActivity(mapIntent);
                }
            }

            if (v.getId() == R.id.btnprice) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInfo.this);
                builder
                        .setMessage(price_value)
                        .setCancelable(false)
                        .setNegativeButton("ОК",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL("http://api.cityhunter.kz/posts/" + post_id);

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
            JSONObject dataJsonObj = null;
            try {
                dataJsonObj = new JSONObject(strJson);
                String post_description = Html.fromHtml(dataJsonObj.getString("description")).toString();
                description.setText(post_description);
                if (!TextUtils.isEmpty(post_description))
                    description.setVisibility(View.VISIBLE);

                JSONArray myinfo = new JSONArray(dataJsonObj.getString("info"));
                JSONObject phone = myinfo.getJSONObject(2);
                String valuephone[] = phone.getString("value").split(";");
                final Context contextThemeWrapper = new ContextThemeWrapper(getApplicationContext(), R.style.myStyle);
                Button[] mybtn = new Button[valuephone.length];

                JSONObject jsonaddress = myinfo.getJSONObject(1);
                address = jsonaddress.getString("value");
                btnlocation.setText(address);
                btnlocation.setVisibility(View.VISIBLE);

                try {
                    JSONObject jsontime = myinfo.getJSONObject(3);
                    String mytime = jsontime.getString("value");
                    textTime.setText(mytime);
                    textTime.setVisibility(View.VISIBLE);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                try {
                    JSONObject coordinates = dataJsonObj.getJSONObject("coordinates");
                    latitude = coordinates.getString("latitude");
                    longitude = coordinates.getString("longitude");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                try {
                    JSONObject json_price = myinfo.getJSONObject(4);
                    price_value = json_price.getString("value");
                    btnprice.setVisibility(View.VISIBLE);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                final float scale = getResources().getDisplayMetrics().density;
                LinearLayout.LayoutParams centerGravityParams = new LinearLayout.LayoutParams(
                        (int) (250 * scale + 0.5f), LinearLayout.LayoutParams.WRAP_CONTENT);
                centerGravityParams.gravity = Gravity.CENTER;

                centerGravityParams.setMargins(0, 0, 0, (int) (10 * scale + 0.5f));

                for (int i = 0; i < valuephone.length; i++) {
                    mybtn[i] = new Button(contextThemeWrapper);
                    mybtn[i].setText(valuephone[i]);
                    // mybtn[i].setPadding(0, 0, 0, (int) (10 * scale + 0.5f));
                    mybtn[i].setBackgroundColor(Color.parseColor("#4b5cb9"));
                    mybtn[i].setOnClickListener(ActivityInfo.this);
                    linLayout.addView(mybtn[i], centerGravityParams);

                }

                JSONArray images = dataJsonObj.getJSONArray("images");
                HashMap<String, String> image_maps = new HashMap<String, String>();
                title = dataJsonObj.getString("title");
                for (int i = 0; i < images.length(); i++) {
                    image_maps.put("name" + Integer.toString(i),
                            "http://" + images.get(i));
                }

                for (String name : image_maps.keySet()) {
                    DefaultSliderView defaultSliderView = new DefaultSliderView(getApplicationContext());
                    // initialize a SliderLayout
                    defaultSliderView
                            .image(image_maps.get(name))
                            .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                            .setOnSliderClickListener(ActivityInfo.this);


                    imageSlider.addSlider(defaultSliderView);
                }
                imageSlider.setPresetTransformer(SliderLayout.Transformer.Default);
                imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                imageSlider.setCustomAnimation(new DescriptionAnimation());
                imageSlider.setDuration(3000);
                imageSlider.addOnPageChangeListener(ActivityInfo.this);

                progressDialog.hide();
                imageSlider.startAutoCycle();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Log.e("Slider Demo", "Page Changed: " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    protected void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider
        // before activity or fragment is destroyed
        imageSlider.stopAutoCycle();
        super.onStop();
    }

    boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null;
    }


}
