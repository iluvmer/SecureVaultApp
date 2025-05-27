package com.meriem.securevaultapp.screens;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.meriem.securevaultapp.R;


public class MainActivityAfak extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_afak);

        CardView notepadCard = findViewById(R.id.notepadCard);
        CardView cleeCard = findViewById(R.id.cleeCard);
        CardView coeurCard = findViewById(R.id.coeurCard);
        CardView settingsCard = findViewById(R.id.settingsCard);

        notepadCard.setOnClickListener(this);
        cleeCard.setOnClickListener(this);
        coeurCard.setOnClickListener(this);
        settingsCard.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;

        switch (view.getId()) {
            case R.id.notepadCard:
                // activity li n3aytolha de chaque box
                intent = new Intent(this, .class);
                startActivity(intent);
                break;

            case R.id.cleeCard:
                intent = new Intent(this, .class);
                startActivity(intent);
                break;

            case R.id.coeurCard:
                intent = new Intent(this, .class);
                startActivity(intent);
                break;

            case R.id.settingsCard:
                intent = new Intent(this, .class);
                startActivity(intent);
                break;
        }
    }
}

