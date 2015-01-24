package com.noxalus.vgbt.tasks;

import java.util.ArrayList;

import com.noxalus.vgbt.entities.Question;

public interface AsyncResponse
{
    void processFinish(ArrayList<Question> output);
}
