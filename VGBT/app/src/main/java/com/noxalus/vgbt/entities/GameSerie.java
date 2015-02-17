package com.noxalus.vgbt.entities;

import java.util.ArrayList;

public class GameSerie extends SelectableEntity
{
    private ArrayList<Game> games;

    public GameSerie(Integer id, String name)
    {
        super(id, name);

        this.games = new ArrayList<Game>();
    }

    public void addGame(Game game)
    {
        games.add(game);
    }

    public int gameNumber() { return games.size(); }

    public ArrayList<Game> getGames()
    {
        return games;
    }
}
