package com.noxalus.vgbt.entities;

public class Game extends SelectableEntity
{
    private Integer gameId;

    public Game(Integer id, String name, int gameId)
    {
        super(id, name);

        this.gameId = gameId;
    }

    public Integer getGameId()
    {
        return gameId;
    }
}
