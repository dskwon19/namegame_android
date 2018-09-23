package com.willowtreeapps.namegame.ui;

import android.os.Bundle;

import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.core.NameGameApplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class NameGameActivity extends AppCompatActivity {

    private static final String FRAG_TAG = "NameGameFragmentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_game_activity);
        NameGameApplication.get(this).component().inject(this);

        if (savedInstanceState == null) {
            NameGameFragment fragment = new NameGameFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.container, fragment, FRAG_TAG);
            fragmentTransaction.commit();
        }
    }

}
