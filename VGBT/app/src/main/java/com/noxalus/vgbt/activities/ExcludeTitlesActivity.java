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
        boolean gameSerieIsExcluded = excludeGameSeriesSet.contains(gameSerieId.toString());
        boolean gameIsExcluded = excludeGamesSet.contains(gameId.toString());

        if (excludeTitlesSet != null) {
            for (String excludeGame : excludeTitlesSet) {
                savedExcludeTitles.add(Integer.parseInt(excludeGame));
            }
        }

        Log.d("EXCLUDE", gameIsExcluded ? "true" : "false");
        Log.d("EXCLUDE", excludeGamesSet.toString());

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

        Set<String> excludeTitles = new HashSet<String>();
        Set<String> excludeGameSeriesSet = settings.getStringSet("excludeGameSeries", null);
        Set<String> excludeGamesSet = settings.getStringSet("excludeGames", null);
        boolean gameIsExcluded = excludeGamesSet.contains(gameId.toString());
        boolean gameSerieIsExcluded = excludeGameSeriesSet.contains(gameSerieId.toString());

        int selectedTitlesNumber = 0;
        for (Title title : titles)
        {
            if (!title.isSelected())
                excludeTitles.add(title.getId().toString());
            else
                selectedTitlesNumber++;
        }

        if (selectedTitlesNumber == 0)
        {
            excludeTitles.clear();
            excludeGamesSet.add(gameId.toString());
        }
        else if (gameSerieIsExcluded || gameIsExcluded)
        {
            excludeGameSeriesSet.remove(gameSerieId.toString());
            excludeGamesSet.remove(gameId.toString());
        }

        editor.putStringSet("excludeGameSeries", excludeGameSeriesSet);
        editor.putStringSet("excludeGames", excludeGamesSet);
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
                holder.code = (TextView) convertView.findViewById(R.id.code);
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
            holder.code.setText("");
            holder.name.setText(title.getName());
            holder.name.setChecked(title.isSelected());
            holder.name.setTag(title);

            return convertView;
        }

    }
}
