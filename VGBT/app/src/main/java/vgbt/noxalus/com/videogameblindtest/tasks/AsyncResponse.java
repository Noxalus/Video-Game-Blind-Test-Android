package vgbt.noxalus.com.videogameblindtest.tasks;

import java.util.ArrayList;

import vgbt.noxalus.com.videogameblindtest.entities.Question;

public interface AsyncResponse
{
    void processFinish(ArrayList<Question> output);
}
