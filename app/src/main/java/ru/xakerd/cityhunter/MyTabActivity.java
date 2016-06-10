package ru.xakerd.cityhunter;


import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * Created by User on 05.06.2016.
 */
public class MyTabActivity extends TabActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newz);

        // create the TabHost that will contain the Tabs
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);


        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");


        // Set the Tab name and Activity
        // that will be opened when particular Tab will be selected
        tab1.setIndicator("Каталог");
        tab1.setContent(new Intent(this, MainActivity.class));

        tab2.setIndicator("Новое");
        Intent intent = new Intent(this,NewActivity.class);
        tab2.setContent(intent);



        /** Add the tabs  to the TabHost to display. */
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);


    }
    @Override
    public void onResume(){
        super.onResume();
        TabHost tabhost = getTabHost();
        for(int i=0;i<tabhost.getTabWidget().getChildCount();i++)
        {
            TextView tv = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#ffffff"));
        }
        // Возобновите все приостановленные обновления UI,
        // потоки или процессы, которые были "заморожены",
        // когда данный объект был неактивным.
    }

}
