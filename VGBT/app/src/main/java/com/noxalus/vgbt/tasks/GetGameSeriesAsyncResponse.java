package com.noxalus.vgbt.tasks;

import com.noxalus.vgbt.entities.GameSeries;

public interface GetGameSeriesAsyncResponse
{
    void processFinish(GameSeries output);
}
