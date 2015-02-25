package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.noxalus.vgbt.R;
import com.noxalus.vgbt.config.Config;
import com.noxalus.vgbt.entities.Game;
import com.noxalus.vgbt.entities.GameSerie;
import com.noxalus.vgbt.entities.GameSeries;
import com.noxalus.vgbt.entities.Title;
import com.noxalus.vgbt.tasks.GetGameSeriesAsyncResponse;
import com.noxalus.vgbt.tasks.GetGameSeriesAsyncTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ExcludeGameSeriesActivity extends Activity implements GetGameSeriesAsyncResponse
{
    MyCustomAdapter dataAdapter = null;
    public GetGameSeriesAsyncResponse delegate = null;
    GameSeries gameSeries;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclude_game_series);

        SharedPreferences settings = getSharedPreferences("VGBT", 0);
        editor = settings.edit();

        boolean newExtracts = settings.getBoolean("newExtracts", true);

        if (newExtracts)
            getGameSeries();
        else
        {
            Config.getInstance().loadGameSeries(getApplicationContext());
            gameSeries = Config.getInstance().getGameSeries();
            displayListView();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (gameSeries != null)
            displayListView();
    }

    private void getExcludeGameSeries()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> excludeGameSeriesSet = settings.getStringSet("excludeGameSeries", null);
        ArrayList<Integer> savedExcludeGameSeries = new ArrayList<Integer>();

        if (excludeGameSeriesSet != null)
        {
            for (String excludeGameSerie : excludeGameSeriesSet)
            {
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

        Set<String> excludeGameSeries = settings.getStringSet("excludeGameSeries", null);
        Set<String> excludeGame = settings.getStringSet("excludeGames", null);
        Set<String> excludeTitle = settings.getStringSet("excludeTitles", null);

        if (excludeGameSeries == null)
            excludeGameSeries = new HashSet<>();
        if (excludeGame == null)
            excludeGame = new HashSet<>();
        if (excludeTitle == null)
            excludeTitle = new HashSet<>();

        for (GameSerie gameSerie : gameSeries)
        {
            if (!gameSerie.isSelected())
            {
                if (!excludeGameSeries.contains(gameSerie.getId().toString()))
                    excludeGameSeries.add(gameSerie.getId().toString());

                if (excludeGame.size() > 0) {
                    // For all games of this game series, we remove them from the exclude list
                    for (Game game : gameSerie.getGames()) {
                        if (excludeTitle != null) {
                            // For all titles of this game, we remove them from the exclude list
                            for (Title title : game.getTitles()) {
                                excludeTitle.remove(title.getId().toString());
                            }
                        }

                        excludeGame.remove(game.getId().toString());
                    }
                }
            }
            else
                excludeGameSeries.remove(gameSerie.getId().toString());
        }

        editor.putStringSet("excludeGameSeries", excludeGameSeries);
        editor.putStringSet("excludeGames", excludeGame);
        editor.putStringSet("excludeTitles", excludeTitle);

        editor.commit();
    }

    private void getGameSeries()
    {
        // AsyncTask can't be executed multiple times
        // we need to create a new instance each time
        GetGameSeriesAsyncTask getGameSeriesAsyncTask = new GetGameSeriesAsyncTask();
        getGameSeriesAsyncTask.delegate = this;

        getGameSeriesAsyncTask.execute(getResources().getString(R.string.api) + "?gameSerie=-42");
    }

    @Override
    public void processFinish(GameSeries output)
    {
        editor.putBoolean("newExtracts", false);
        editor.commit();

        gameSeries = output;

        Config.getInstance().setGameSeries(gameSeries);
        Config.getInstance().saveGameSeries(getApplicationContext());

        displayListView();
    }

    private void displayListView()
    {
        getExcludeGameSeries();

        // Create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this, R.layout.checkbox_item, gameSeries);
        ListView listView = (ListView) findViewById(R.id.gameSerieExpandableListView);

        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                GameSerie gameSerie = (GameSerie) parent.getItemAtPosition(position);

                Intent intent = new Intent(ExcludeGameSeriesActivity.this, ExcludeGamesActivity.class);
                intent.putExtra("gameSerieId", gameSerie.getId());
                startActivity(intent);
            }
        });

    }

    public class MyCustomAdapter extends ArrayAdapter<GameSerie>
    {
        private ArrayList<GameSerie> gameSerieList;

        public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<GameSerie> gameSerieList)
        {
            super(context, textViewResourceId, gameSerieList);
            this.gameSerieList = new ArrayList<GameSerie>();
            this.gameSerieList.addAll(gameSerieList);
        }

        private class ViewHolder
        {
            TextView code;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = null;

            if (convertView == null)
            {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.checkbox_item, null);

                holder = new ViewHolder();
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        CheckBox cb = (CheckBox) v;
                        GameSerie gameSerie = (GameSerie) cb.getTag();

                        gameSerie.setSelected(cb.isChecked());

                        ((ExcludeGameSeriesActivity) getContext()).saveExcludeGameSeries();
                    }
                });
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            GameSerie gameSerie = gameSerieList.get(position);
            holder.name.setText(Html.fromHtml(gameSerie.getName() + " (<i>" + gameSerie.getGames().size() + "</i>)"));
            holder.name.setChecked(gameSerie.isSelected());
            holder.name.setTag(gameSerie);

            return convertView;
        }
    }
}
