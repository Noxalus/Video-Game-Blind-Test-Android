package com.noxalus.vgbt.tasks;

import com.noxalus.vgbt.entities.GameSerie;
import java.util.ArrayList;

public interface GetGameSeriesAsyncResponse
{
    void processFinish(ArrayList<GameSerie> output);
}
