package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.noxalus.vgbt.R;
import com.noxalus.vgbt.config.Config;
import com.noxalus.vgbt.entities.Game;
import com.noxalus.vgbt.entities.Title;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ExcludeTitlesActivity extends Activity
{
    MyCustomAdapter dataAdapter = null;
    ArrayList<Title> titles;
    Integer gameSerieId;
    Integer gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclude_titles);

        gameSerieId = getIntent().getIntExtra("gameSerieId", 0);
        gameId = getIntent().getIntExtra("gameId", 0);

        titles = Config.getInstance().getTitlesFromGameId(gameId);

        displayListView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        displayListView();
    }

    private void getExcludeTitles()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> excludeGameSeriesSet = settings.getStringSet("excludeGameSeries", null);
        Set<String> excludeGamesSet = settings.getStringSet("excludeGames", null);
        Set<String> excludeTitlesSet = settings.getStringSet("excludeTitles", null);

        ArrayList<Integer> savedExcludeTitles = new ArrayList<Integer>();

        boolean gameSerieIsExcluded = false;
        boolean gameIsExcluded = false;

        if (excludeGameSeriesSet != null)
            gameSerieIsExcluded = excludeGameSeriesSet.contains(gameSerieId.toString());

        if (excludeGamesSet != null)
            gameIsExcluded = excludeGamesSet.contains(gameId.toString());

        if (excludeTitlesSet != null) {
            for (String excludeGame : excludeTitlesSet) {
                savedExcludeTitles.add(Integer.parseInt(excludeGame));
            }
        }

        boolean isTitleExclude = isTitleExclude();
        for (Title title : titles)
        {
            if (!gameSerieIsExcluded && !gameIsExcluded && !isTitleExclude) {
                boolean isSelected = !savedExcludeTitles.contains(title.getId());
                title.setSelected(isSelected);
            }
            else
            {
                title.setSelected(false);
            }
        }
    }

    private void saveExcludeTitles()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);
        SharedPreferences.Editor editor = settings.edit();

        Set<String> excludeGameSeries = settings.getStringSet("excludeGameSeries", null);
        Set<String> excludeGames = settings.getStringSet("excludeGames", null);
        Set<String> excludeTitles = settings.getStringSet("excludeTitles", null);

        boolean gameSerieIsExcluded = false;
        boolean gameIsExcluded = false;

        if (excludeGameSeries != null)
            gameSerieIsExcluded = excludeGameSeries.contains(gameSerieId.toString());
        else
            excludeGameSeries = new HashSet<>();

        if (excludeGames != null)
            gameIsExcluded = excludeGames.contains(gameId.toString());
        else
            excludeGames = new HashSet<>();

        if (excludeTitles == null)
            excludeTitles = new HashSet<>();

        int selectedTitlesNumber = 0;
        for (Title title : titles)
        {
            if (!title.isSelected()) {
                if (!excludeTitles.contains(title.getId().toString()))
                    excludeTitles.add(title.getId().toString());
            }
            else {
                selectedTitlesNumber++;
                excludeTitles.remove(title.getId().toString());
            }
        }

        ArrayList<Game> allGamesFromGameSerie = Config.getInstance().getGamesFromGameSerieId(gameSerieId);
        if (selectedTitlesNumber == 0)
        {
            // If we exclude all titles => we will exclude the game instead
            if (!gameIsExcluded)
                excludeGames.add(gameId.toString());

            for (Title title : titles) {
                excludeTitles.remove(title.getId().toString());
            }

            boolean allGamesAreExcluded = true;
            for (Game game : allGamesFromGameSerie)
            {
                if (!excludeGames.contains(game.getId().toString()))
                {
                    allGamesAreExcluded = false;
                    break;
                }
            }

            if (allGamesAreExcluded)
            {
                // If all games are excluded too, we exclude the game serie instead
                if (!gameSerieIsExcluded)
                    excludeGameSeries.add(gameSerieId.toString());

                for (Game game : allGamesFromGameSerie) {
                    excludeGames.remove(game.getId().toString());
                }
            }
        }
        else if (gameSerieIsExcluded)
        {
            // All games are excluded because of the game serie exclusion
            // we need to manually add all game from excludeGames list
            // except the one we checked
            for (Game game : allGamesFromGameSerie)
            {
                if (game.getId() != gameId && !excludeGames.contains(game.getId().toString()))
                    excludeGames.add(game.getId().toString());
            }

            excludeGameSeries.remove(gameSerieId.toString());
        }
        else if (gameIsExcluded)
        {
            excludeGames.remove(gameId.toString());
        }

        editor.putStringSet("excludeGameSeries", excludeGameSeries);
        editor.putStringSet("excludeGames", excludeGames);
        editor.putStringSet("excludeTitles", excludeTitles);

        editor.commit();
    }

    private boolean isTitleExclude()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> excludeTitleSet = settings.getStringSet("excludeTitles", null);

        if (excludeTitleSet != null)
        {
            for (String excludeTitle : excludeTitleSet)
            {
                if (this.gameId == Integer.parseInt(excludeTitle))
                    return true;
            }
        }

        return false;
    }

    private void displayListView()
    {
        getExcludeTitles();

        // Create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this, R.layout.checkbox_item, titles);
        ListView listView = (ListView) findViewById(R.id.titleListView);

        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
    }

    private class MyCustomAdapter extends ArrayAdapter<Title>
    {

        private ArrayList<Title> titleList;

        public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Title> titleList)
        {
            super(context, textViewResourceId, titleList);
            this.titleList = new ArrayList<Title>();
            this.titleList.addAll(titleList);
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
                        Title title = (Title) cb.getTag();
                        title.setSelected(cb.isChecked());

                        ((ExcludeTitlesActivity) getContext()).saveExcludeTitles();
                    }
                });
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            Title title = titleList.get(position);
            holder.name.setText(title.getName());
            holder.name.setChecked(title.isSelected());
            holder.name.setTag(title);

            return convertView;
        }

    }
}
