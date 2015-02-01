package com.noxalus.vgbt.entities;

public class BaseEntity
{
    private Integer id;
    private String name;

    BaseEntity(Integer id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public Integer getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }
}
