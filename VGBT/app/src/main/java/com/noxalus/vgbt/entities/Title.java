package com.noxalus.vgbt.entities;

public class Title extends SelectableEntity
{
    private Integer gameId;

    public Title(Integer id, String name, int gameId)
    {
        super(id, name);

        this.gameId = gameId;
    }
}
