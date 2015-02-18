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
    Integer gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclude_titles);

        gameId = getIntent().getIntExtra("gameId", 0);

        titles = Config.getInstance().getTitlesFromGameId(gameId);

        displayListView();
    }

    private void getExcludeTitles()
    {
        /*
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> excludeGamesSet = settings.getStringSet("excludeGames", null);
        ArrayList<Integer> savedExcludeGames = new ArrayList<Integer>();

        if (excludeGamesSet != null) {
            for (String excludeGame : excludeGamesSet) {
                savedExcludeGames.add(Integer.parseInt(excludeGame));
            }
        }

        boolean isGameSerieExclude = isGameSerieExclude();
        for (Game game : games)
        {
            if (!isGameSerieExclude) {
                boolean isSelected = !savedExcludeGames.contains(game.getId());
                game.setSelected(isSelected);
            }
            else
            {
                game.setSelected(false);
            }
        }
        */
    }

    private void saveExcludeTitles()
    {
        /*
        SharedPreferences settings = getSharedPreferences("VGBT", 0);
        SharedPreferences.Editor editor = settings.edit();

        // If game serie is exclude, we delete it from the exclude game serie list
        if (isGameSerieExclude()) {
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
        */
    }

    private boolean isTitleExclude()
    {
        SharedPreferences settings = getSharedPreferences("VGBT", 0);

        Set<String> excludeTitleSet = settings.getStringSet("excludeTitles", null);

        if (excludeTitleSet != null) {
            for (String excludeTitle : excludeTitleSet) {
                if (this.gameId == Integer.parseInt(excludeTitle))
                    return true;
            }
        }

        return false;
    }

    private void displayListView()
    {
        // Create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this, R.layout.checkbox_item, titles);
        ListView listView = (ListView) findViewById(R.id.titleListView);

        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
    }

    private class MyCustomAdapter extends ArrayAdapter<Title> {

        private ArrayList<Title> titleList;

        public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Title> titleList) {
            super(context, textViewResourceId, titleList);
            this.titleList = new ArrayList<Title>();
            this.titleList.addAll(titleList);
        }

        private class ViewHolder {
            TextView code;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = null;

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.checkbox_item, null);

                holder = new ViewHolder();
                holder.code = (TextView) convertView.findViewById(R.id.code);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Game game = (Game) cb.getTag();
                        game.setSelected(cb.isChecked());

                        ((ExcludeTitlesActivity)getContext()).saveExcludeTitles();
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Title title = titleList.get(position);
            holder.code.setText("");
            holder.name.setText(title.getName());
            holder.name.setChecked(title.isSelected());
            holder.name.setTag(title);

            return convertView;
        }

    }
}
