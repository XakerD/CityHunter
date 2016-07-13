package ru.xakerd.cityhunter;



import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class InfoActivity extends AppCompatActivity implements View.OnClickListener, BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener {
   static final String EXTRA_ID="id",
                       EXTRA_TITLE="title";
    private String latitude, longitude, title, priceValue;
    private SliderLayout imageSlider;
    private String postId;
    private TextView description;
    private TextView btnPrice, btnLocation,btnTimeInfo;
    private LinearLayout infoLayout;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        progressDialog = new ProgressDialog(this, R.style.MyTheme);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();

        imageSlider = (SliderLayout) findViewById(R.id.info_slider);
        imageSlider.stopAutoCycle();

        btnLocation = (TextView) findViewById(R.id.info_btnLocation);
        btnLocation.setOnClickListener(this);

        btnPrice = (TextView) findViewById(R.id.info_btnPrice);
        btnPrice.setOnClickListener(this);

        description = (TextView) findViewById(R.id.info_description);
        btnTimeInfo = (TextView) findViewById(R.id.info_time);

        Bundle extras = getIntent().getExtras();
        postId = extras.getString(EXTRA_ID);
        String postTitle = extras.getString(EXTRA_TITLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
        assert toolbar != null;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        assert actionBar != null;
        actionBar.setTitle(postTitle);

        infoLayout = (LinearLayout) findViewById(R.id.infoLayout);
        if (isNetworkConnected())
        new ParseTask().execute();
        else
        { progressDialog.hide();
            Toast.makeText(this,getResources().getString(R.string.is_connected),Toast.LENGTH_SHORT).show();}

    }

    @Override
    public void onResume(){
        super.onResume();
        imageSlider.startAutoCycle();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_btnLocation:
                if (!TextUtils.isEmpty(latitude)) {
                    Intent mapIntent = new Intent(InfoActivity.this, MapsActivity.class);
                    mapIntent.putExtra(MapsActivity.EXTRA_LATITUDE, latitude);
                    mapIntent.putExtra(MapsActivity.EXTRA_LONGITUDE, longitude);
                    mapIntent.putExtra(EXTRA_TITLE, title);
                    startActivity(mapIntent);
                }
                break;
            case R.id.info_btnPrice:
                AlertDialog.Builder builder = new AlertDialog.Builder(InfoActivity.this);
                builder
                        .setMessage(priceValue)
                        .setCancelable(false)
                        .setNegativeButton("ОК",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            default:
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ((Button) v).getText()));
                startActivity(intent);
                break;

        }
    }

    private class ParseTask extends AsyncTask<Void, Void, Void> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        String postDescription;
        String[] caption,value,urlImages;
        @Override
        protected Void doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL(getResources().getString(R.string.url_info) + postId);

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
                JSONObject dataJsonObj;
                dataJsonObj = new JSONObject(resultJson);

                title = dataJsonObj.getString("title");

                postDescription = Html.fromHtml(dataJsonObj.getString("description")).toString();

                try {
                    JSONObject coordinates = dataJsonObj.getJSONObject("coordinates");
                    latitude = coordinates.getString("latitude");
                    longitude = coordinates.getString("longitude");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                JSONArray info;
                info = new JSONArray(dataJsonObj.getString("info"));
                caption = new String[info.length()];
                value = new String[info.length()];
                for (int i=0;i<info.length();i++) {
                    JSONObject objectInfo = info.getJSONObject(i);
                    caption[i] = objectInfo.getString("caption");
                    value[i] = objectInfo.getString("value");
                }

                JSONArray images = dataJsonObj.getJSONArray("images");
                urlImages = new String[images.length()];
                for (int i=0; i<urlImages.length;i++)
                urlImages[i]="http://" + images.get(i);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
                if (!TextUtils.isEmpty(postDescription)){
                    description.setText(postDescription);
                    description.setVisibility(View.VISIBLE);
                }

                for (int i=0;i<caption.length;i++){
                    switch (caption[i]){
                        case "Адрес":
                            btnLocation.setText(value[i]);
                            btnLocation.setVisibility(View.VISIBLE);
                            break;

                        case "Время работы":
                            btnTimeInfo.setText(value[i]);
                            btnTimeInfo.setVisibility(View.VISIBLE);
                            break;

                        case "Телефон":
                            String valuePhone[] = value[i].split(";");

                            final float scale = getResources().getDisplayMetrics().density;
                            LinearLayout.LayoutParams centerGravityParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            centerGravityParams.gravity = Gravity.CENTER;
                            centerGravityParams.setMargins(0, 0, 0, (int) (10 * scale + 0.5f));

                            final Context contextThemeWrapper = new ContextThemeWrapper(getApplicationContext(), R.style.myStyle);
                            Button[] btnPhone = new Button[valuePhone.length];
                            for (int j = 0; j < valuePhone.length; j++) {
                                btnPhone[j] = new Button(contextThemeWrapper);
                                btnPhone[j].setText(valuePhone[j]);
                                btnPhone[j].setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary));
                                btnPhone[j].setOnClickListener(InfoActivity.this);
                                infoLayout.addView(btnPhone[j], centerGravityParams);
                            }
                            break;

                        case "Цены":
                            priceValue = value[i];
                            btnPrice.setVisibility(View.VISIBLE);
                            break;

                    }

                }


                HashMap<String, String> image_maps = new HashMap<>();
                for (int i = 0; i < urlImages.length; i++) {
                    image_maps.put("image" + Integer.toString(i),
                            urlImages[i]);
                }
                for (String name : image_maps.keySet()) {
                    DefaultSliderView defaultSliderView = new DefaultSliderView(getApplicationContext());
                    defaultSliderView
                            .image(image_maps.get(name))
                            .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                            .setOnSliderClickListener(InfoActivity.this);
                    imageSlider.addSlider(defaultSliderView);
                }
                imageSlider.setPresetTransformer(SliderLayout.Transformer.Default);
                imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                imageSlider.setCustomAnimation(new DescriptionAnimation());
                imageSlider.setDuration(3000);
                imageSlider.addOnPageChangeListener(InfoActivity.this);
                imageSlider.startAutoCycle();
                progressDialog.hide();

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
        Log.e("Slider CityHunter", "Page Changed: " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    protected void onStop() {
        imageSlider.stopAutoCycle();
        super.onStop();
    }

    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null;
    }


}
