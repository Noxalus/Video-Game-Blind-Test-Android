package com.noxalus.vgbt.tasks;

import com.noxalus.vgbt.entities.Game;
import java.util.ArrayList;

public interface GetGamesAsyncResponse
{
    void processFinish(ArrayList<Game> output);
}
