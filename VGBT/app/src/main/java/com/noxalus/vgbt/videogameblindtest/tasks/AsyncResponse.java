package com.noxalus.vgbt.videogameblindtest.tasks;

import java.util.ArrayList;

import com.noxalus.vgbt.videogameblindtest.entities.Question;

public interface AsyncResponse
{
    void processFinish(ArrayList<Question> output);
}
