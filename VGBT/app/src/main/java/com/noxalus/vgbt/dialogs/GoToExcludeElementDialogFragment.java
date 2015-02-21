package com.noxalus.vgbt.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.noxalus.vgbt.R;

public class GoToExcludeElementDialogFragment extends DialogFragment
{
    private Context context;
    private Intent yesIntent;
    private Intent noIntent;

    public GoToExcludeElementDialogFragment(Context context, Intent yesIntent, Intent noIntent)
    {
        this.context = context;
        this.yesIntent = yesIntent;
        this.noIntent = noIntent;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        TextView message = new TextView(context);
        message.setText("This is the first time that you launch a training game.\n\n" +
                "For this mode, you can exclude games or titles from the quiz going to the options screen.\n\n" +
                "Do you want to exclude elements now?");
        message.setGravity(Gravity.CENTER_HORIZONTAL);
        message.setTextSize(15);
        message.setTextColor(Color.BLACK);
        message.setPadding(20, 20, 20, 20);

        builder.setView(message);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                startActivity(yesIntent);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                startActivity(noIntent);
            }
        });

        return builder.create();
    }
}
