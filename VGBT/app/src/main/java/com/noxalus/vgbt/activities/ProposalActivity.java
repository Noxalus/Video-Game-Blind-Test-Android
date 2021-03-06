package com.noxalus.vgbt.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.noxalus.vgbt.R;

public class ProposalActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal);

        final EditText proposalsEditText = (EditText) findViewById(R.id.proposalsEditText);

        final Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendProposals(proposalsEditText.getText().toString());
            }
        });
    }

    private void sendProposals(String message)
    {
        // Call API to send mail
    }
}
