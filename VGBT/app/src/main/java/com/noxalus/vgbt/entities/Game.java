package com.noxalus.vgbt.entities;

public class Game extends BaseEntity
{
    private Integer gameId;
    private boolean selected;

    Game(Integer id, String name, int gameId)
    {
        super(id, name);

        this.gameId = gameId;
        this.selected = true;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean value)
    {
        selected = value;
    }

    public Integer getGameId()
    {
        return gameId;
    }
}
