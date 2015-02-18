package com.noxalus.vgbt.entities;

import java.util.ArrayList;

public class GameSeries extends ArrayList<GameSerie>
{
    public GameSerie getGameSerie(int id)
    {
        for (GameSerie gameSerie : this)
        {
            if (gameSerie.getId() == id)
                return gameSerie;
        }

        return null;
    }
}
