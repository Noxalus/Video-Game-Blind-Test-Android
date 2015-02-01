package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.noxalus.vgbt.R;
import com.noxalus.vgbt.entities.GameSerie;
import com.noxalus.vgbt.tasks.GetGameSeriesAsyncResponse;
import com.noxalus.vgbt.tasks.GetGameSeriesAsyncTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameSeriesActivity extends Activity implements GetGameSeriesAsyncResponse
{
    MyCustomAdapter dataAdapter = null;
    public GetGameSeriesAsyncResponse delegate = null;
    ArrayList<GameSerie> gameSeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_series);

        gameSeries = new ArrayList<>();

        getGameSeries();
    }

    private void getGameSeries()
    {
        // AsyncTask can't be executed multiple times
        // we need to create a new instance each time
        GetGameSeriesAsyncTask getGameSeriesAsyncTask = new GetGameSeriesAsyncTask();
        getGameSeriesAsyncTask.delegate = this;

        getGameSeriesAsyncTask.execute(getResources().getString(R.string.api) + "?gameSerie=0");
    }

    @Override
    public void processFinish(ArrayList<GameSerie> output) {
        gameSeries = output;

        getExcludeGameSeries();
        displayListView();
    }

    private void getExcludeGameSeries()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> excludeGameSeriesSet = settings.getStringSet("excludeGameSeries", null);
        ArrayList<Integer> savedExcludeGameSeries = new ArrayList<Integer>();

        if (excludeGameSeriesSet != null) {
            for (String excludeGameSerie : excludeGameSeriesSet) {
                savedExcludeGameSeries.add(Integer.parseInt(excludeGameSerie));
            }
        }

        for (GameSerie gameSerie : gameSeries)
        {
            boolean isSelected = !savedExcludeGameSeries.contains(gameSerie.getId());
            gameSerie.setSelected(isSelected);
        }
    }

    private void saveExcludeGameSeries()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);
        SharedPreferences.Editor editor = settings.edit();

        Set<String> excludeGameSeries = new HashSet<String>();

        for (GameSerie gameSerie : gameSeries)
        {
            if (!gameSerie.isSelected())
                excludeGameSeries.add(gameSerie.getId().toString());
        }

        editor.putStringSet("excludeGameSeries", excludeGameSeries);
        editor.commit();
    }

    private void displayListView()
    {
        // Create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this, R.layout.game_serie_info, gameSeries);
        ListView listView = (ListView) findViewById(R.id.gameSerieExpandableListView);

        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                GameSerie gameSerie = (GameSerie) parent.getItemAtPosition(position);

                Intent intent = new Intent(GameSeriesActivity.this, ExcludeGamesActivity.class);
                intent.putExtra("gameSerieId", gameSerie.getId());
                startActivity(intent);
            }
        });

    }

    public class MyCustomAdapter extends ArrayAdapter<GameSerie> {
        private ArrayList<GameSerie> gameSerieList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<GameSerie> gameSerieList) {
            super(context, textViewResourceId, gameSerieList);
            this.gameSerieList = new ArrayList<GameSerie>();
            this.gameSerieList.addAll(gameSerieList);
        }

        private class ViewHolder {
            TextView code;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.game_serie_info, null);

                holder = new ViewHolder();
                holder.code = (TextView) convertView.findViewById(R.id.code);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        GameSerie gameSerie = (GameSerie) cb.getTag();

                        gameSerie.setSelected(cb.isChecked());

                        ((GameSeriesActivity) getContext()).saveExcludeGameSeries();
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            GameSerie gameSerie = gameSerieList.get(position);
            holder.code.setText(" (" +  gameSerie.getId() + ")");
            holder.name.setText(gameSerie.getName());
            holder.name.setChecked(gameSerie.isSelected());
            holder.name.setTag(gameSerie);

            return convertView;
        }
    }
}
