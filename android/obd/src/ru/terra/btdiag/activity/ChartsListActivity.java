package ru.terra.btdiag.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;
import roboguice.activity.RoboListActivity;
import roboguice.inject.ContentView;
import ru.terra.btdiag.R;

/**
 * Date: 23.12.14
 * Time: 12:42
 */

@ContentView(R.layout.a_charts_list)
public class ChartsListActivity extends RoboListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<AvailableCommandNames>(this, android.R.layout.simple_list_item_1, AvailableCommandNames.values()));
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(ChartsListActivity.this, ChartActivity.class).putExtra("command", getListAdapter().getItem(position).toString()));
            }
        });
    }
}
