package com.noxalus.vgbt.entities;

public class GameSerie extends BaseEntity
{
    private boolean selected;

    public GameSerie(Integer id, String name)
    {
        super(id, name);

        selected = true;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean value)
    {
        selected = value;
    }
}
