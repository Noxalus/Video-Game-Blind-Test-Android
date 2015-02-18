package com.noxalus.vgbt.entities;

import java.util.ArrayList;

public class Game extends SelectableEntity
{
    private Integer gameSerieId;
    private ArrayList<Title> titles;

    public Game(Integer id, String name, int gameSerieId)
    {
        super(id, name);

        this.gameSerieId = gameSerieId;
        this.titles = new ArrayList<Title>();
    }

    public void addTitle(Title title)
    {
        titles.add(title);
    }

    public ArrayList<Title> getTitles()
    {
        return titles;
    }

    public Integer getGameSerieId()
    {
        return gameSerieId;
    }
}
