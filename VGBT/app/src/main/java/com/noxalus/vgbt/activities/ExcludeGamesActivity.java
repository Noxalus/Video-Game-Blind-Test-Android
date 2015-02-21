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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ExcludeGamesActivity extends Activity
{
    MyCustomAdapter dataAdapter = null;
    ArrayList<Game> games;
    Integer gameSerieId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclude_games);

        gameSerieId = getIntent().getIntExtra("gameSerieId", 0);

        games = Config.getInstance().getGameSeries().getGameSerie(gameSerieId).getGames();

        displayListView();
    }

    private void getExcludeGames()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> excludeGamesSet = settings.getStringSet("excludeGames", null);
        ArrayList<Integer> savedExcludeGames = new ArrayList<Integer>();

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
        SharedPreferences settings = getSharedPreferences("VGBT", 0);
        SharedPreferences.Editor editor = settings.edit();

        // If game serie is exclude, we delete it from the exclude game serie list
        if (isGameSerieExclude())
        {
            Set<String> excludeGameSeries = settings.getStringSet("excludeGameSeries", null);

            excludeGameSeries.remove(gameSerieId.toString());

            editor.putStringSet("excludeGameSeries", excludeGameSeries);
        }

        Set<String> excludeGames = new HashSet<String>();

        for (Game game : games)
        {
            if (!game.isSelected())
                excludeGames.add(game.getId().toString());
        }

        editor.putStringSet("excludeGames", excludeGames);
        editor.commit();
    }

    private boolean isGameSerieExclude()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> excludeGameSeriesSet = settings.getStringSet("excludeGameSeries", null);
        ArrayList<Integer> savedExcludeGameSeries = new ArrayList<Integer>();

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
                holder.code = (TextView) convertView.findViewById(R.id.code);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        CheckBox cb = (CheckBox) v;
                        Game game = (Game) cb.getTag();
                        game.setSelected(cb.isChecked());

                        ((ExcludeGamesActivity) getContext()).saveExcludeGames();
                    }
                });
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            Game game = gameList.get(position);
            holder.code.setText("");
            holder.name.setText(Html.fromHtml(game.getName() + " (<i>" + game.getTitles().size() + "</i>)"));
            holder.name.setChecked(game.isSelected());
            holder.name.setTag(game);

            return convertView;
        }

    }
}
