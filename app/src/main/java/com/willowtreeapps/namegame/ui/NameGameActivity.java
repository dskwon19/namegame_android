package com.willowtreeapps.namegame.ui;

import android.os.Bundle;

import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.core.NameGameApplication;

import androidx.appcompat.app.AppCompatActivity;

public class NameGameActivity extends AppCompatActivity {

    private static final String FRAG_TAG = "NameGameFragmentTag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_game_activity);
        NameGameApplication.get(this).component().inject(this);
    }

}
