package com.noxalus.vgbt.entities;

public class SelectableEntity extends BaseEntity
{
    private boolean selected;

    SelectableEntity(Integer id, String name)
    {
        super(id, name);

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
}
