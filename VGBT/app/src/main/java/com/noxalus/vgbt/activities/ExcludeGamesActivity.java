package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.noxalus.vgbt.R;
import com.noxalus.vgbt.config.Config;
import com.noxalus.vgbt.entities.Game;
import com.noxalus.vgbt.entities.GameSerie;
import com.noxalus.vgbt.entities.Title;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ExcludeGamesActivity extends Activity
{
    MyCustomAdapter dataAdapter = null;
    ArrayList<Game> games;
    Integer gameSerieId;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclude_games);

        gameSerieId = getIntent().getIntExtra("gameSerieId", 0);

        games = Config.getInstance().getGameSeries().getGameSerie(gameSerieId).getGames();

        settings = getSharedPreferences("VGBT", 0);
        editor = settings.edit();

        displayListView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        displayListView();
    }

    private void getExcludeGames()
    {
        Set<String> excludeGamesSet = settings.getStringSet("excludeGames", null);
        ArrayList<Integer> savedExcludeGames = new ArrayList<>();

        if (excludeGamesSet != null)
        {
            for (String excludeGame : excludeGamesSet)
            {
                savedExcludeGames.add(Integer.parseInt(excludeGame));
            }
        }

        boolean isGameSerieExclude = isGameSerieExclude();
        for (Game game : games)
        {
            if (!isGameSerieExclude)
            {
                boolean isSelected = !savedExcludeGames.contains(game.getId());
                game.setSelected(isSelected);
            }
            else
            {
                game.setSelected(false);
            }
        }
    }

    private void saveExcludeGames()
    {
        Set<String> excludeGameSeries = settings.getStringSet("excludeGameSeries", null);
        Set<String> excludeGames = settings.getStringSet("excludeGames", null);
        Set<String> excludeTitles = settings.getStringSet("excludeTitles", null);

        if (excludeGameSeries == null)
            excludeGameSeries = new HashSet<>();

        if (excludeGames == null)
            excludeGames = new HashSet<>();

        if (excludeTitles == null)
            excludeTitles = new HashSet<>();

        int selectedGameNumber = 0;
        for (Game game : games)
        {
            if (!game.isSelected())
            {
                if (!excludeGames.contains(game.getId().toString()))
                    excludeGames.add(game.getId().toString());

                if (excludeTitles.size() > 0) {
                    // For all titles of this game, we remove them from the exclude list
                    for (Title title : game.getTitles()) {
                        excludeTitles.remove(title.getId().toString());
                    }
                }
            }
            else
            {
                selectedGameNumber++;
            }
        }

        // If game serie is exclude, we delete it from the exclude game serie list
        if (selectedGameNumber > 0 && isGameSerieExclude())
        {
            excludeGameSeries.remove(gameSerieId.toString());
        }
        else if (selectedGameNumber == 0)
        {
            if (!excludeGameSeries.contains(gameSerieId.toString()))
                excludeGameSeries.add(gameSerieId.toString());

            ArrayList<Game> allGamesFromGameSerie = Config.getInstance().getGamesFromGameSerieId(gameSerieId);
            for (Game game : allGamesFromGameSerie) {
                excludeGames.remove(game.getId().toString());
            }
        }

        editor.putStringSet("excludeGameSeries", excludeGameSeries);
        editor.putStringSet("excludeGames", excludeGames);
        editor.putStringSet("excludeTitles", excludeTitles);

        editor.commit();
    }

    private boolean isGameSerieExclude()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> excludeGameSeriesSet = settings.getStringSet("excludeGameSeries", null);

        if (excludeGameSeriesSet != null)
        {
            for (String excludeGameSerie : excludeGameSeriesSet)
            {
                if (this.gameSerieId == Integer.parseInt(excludeGameSerie))
                    return true;
            }
        }

        return false;
    }

    private void displayListView()
    {
        getExcludeGames();

        // Create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this, R.layout.checkbox_item, games);
        ListView listView = (ListView) findViewById(R.id.gamesListView);

        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Game game = (Game) parent.getItemAtPosition(position);

                Intent intent = new Intent(ExcludeGamesActivity.this, ExcludeTitlesActivity.class);
                intent.putExtra("gameSerieId", gameSerieId);
                intent.putExtra("gameId", game.getId());
                startActivity(intent);
            }
        });
    }

    private class MyCustomAdapter extends ArrayAdapter<Game>
    {

        private ArrayList<Game> gameList;

        public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Game> gameList)
        {
            super(context, textViewResourceId, gameList);
            this.gameList = new ArrayList<Game>();
            this.gameList.addAll(gameList);
        }

        private class ViewHolder
        {
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;

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
                        Game game = (Game) cb.getTag();

                        if (!cb.isChecked() && !Config.getInstance().isThereEnoughSelectedTitles(settings, 4, game.getId(), Config.ExcludeType.GAME))
                        {
                            cb.setChecked(true);

                            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_enough_title), Toast.LENGTH_LONG);
                            TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
                            if (textView != null)
                                textView.setGravity(Gravity.CENTER);

                            toast.show();
                        }
                        else
                        {
                            game.setSelected(cb.isChecked());
                            ((ExcludeGamesActivity) getContext()).saveExcludeGames();
                        }
                    }
                });
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            Game game = gameList.get(position);
            holder.name.setText(Html.fromHtml(game.getName() + " (<i>" + game.getTitles().size() + "</i>)"));
            holder.name.setChecked(game.isSelected());
            holder.name.setTag(game);

            return convertView;
        }

    }
}
