package com.willowtreeapps.namegame.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.core.ListRandomizer;
import com.willowtreeapps.namegame.core.NameGameApplication;
import com.willowtreeapps.namegame.network.api.ProfilesRepository;
import com.willowtreeapps.namegame.network.api.model.NameGame;
import com.willowtreeapps.namegame.network.api.model.Person;
import com.willowtreeapps.namegame.util.CircleBorderTransform;
import com.willowtreeapps.namegame.util.Ui;
import com.willowtreeapps.namegame.viewmodel.NameGameViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class NameGameFragment extends Fragment {

    private static final Interpolator OVERSHOOT = new OvershootInterpolator();

    @Inject
    ListRandomizer listRandomizer;
    @Inject
    Picasso picasso;
    @Inject
    ProfilesRepository profilesRepository;

    private TextView title;
    private ViewGroup container;
    private List<ImageView> faces = new ArrayList<>(6);
    private NameGameViewModel nameGameViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NameGameApplication.get(getActivity()).component().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        nameGameViewModel = ViewModelProviders.of(this).get(NameGameViewModel.class);
        nameGameViewModel.init();
        return inflater.inflate(R.layout.name_game_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        title = view.findViewById(R.id.title);
        container = view.findViewById(R.id.face_container);
        prepareViewLoad();
        setNameGameListeners();
    }

    /**
     * Method to prepare the views before loading NameGame data
     */
    private void prepareViewLoad() {
        //Hide the views until data loads
        title.setAlpha(0);

        int n = container.getChildCount();
        for (int i = 0; i < n; i++) {
            ImageView face = (ImageView) container.getChildAt(i);
            faces.add(face);

            //Hide the views until data loads
            face.setScaleX(0);
            face.setScaleY(0);
        }
    }

    /**
     * Method to set listeners related to the NameGame
     */
    private void setNameGameListeners() {
        nameGameViewModel.getNameGame().observe(NameGameFragment.this, new Observer<NameGame>() {
            @Override
            public void onChanged(NameGame nameGame) {
                setViews(nameGame);
            }
        });
        nameGameViewModel.isCorrect().observe(NameGameFragment.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean correct) {
                if (correct != null) {
                    if (correct) {
                        Toast.makeText(getContext(), "CORRECT", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "WRONG", Toast.LENGTH_SHORT).show();
                    }
                    nameGameViewModel.createNewGame();
                }
            }
        });
    }

    /**
     * Method to set views related to the NameGame
     *
     * @param nameGame The current NameGame
     */
    private void setViews(NameGame nameGame) {
        setImages(faces, nameGame.getRandomPeople());
        animateFacesIn();

        title.setText(String.format("%s %s", nameGame.getCorrectPerson().getFirstName(), nameGame.getCorrectPerson().getLastName()));
        title.setAlpha(1);
    }

    /**
     * A method for setting the images from people into the imageviews
     */
    private void setImages(List<ImageView> faces, List<Person> randomPeople) {
        final List<Person> people = randomPeople;
        int imageSize = (int) Ui.convertDpToPixel(100, getContext());
        int n = faces.size();

        for (int i = 0; i < n; i++) {
            ImageView face = faces.get(i);
            final int finalI = i;
            face.setOnClickListener(null);
            face.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onPersonSelected(people.get(finalI));
                }
            });

            // Picasso is unhappy without the protocol explicitly stated
            picasso.load("http:" + people.get(i).getHeadshot().getUrl())
                    .placeholder(R.drawable.ic_face_white_48dp)
                    .resize(imageSize, imageSize)
                    .transform(new CircleBorderTransform())
                    .into(face);
        }
    }

    /**
     * A method to animate the faces into view
     */
    private void animateFacesIn() {
        title.animate().alpha(1).start();
        for (int i = 0; i < faces.size(); i++) {
            ImageView face = faces.get(i);
            face.animate().scaleX(1).scaleY(1).setStartDelay(800 + 120 * i).setInterpolator(OVERSHOOT).start();
        }
    }

    /**
     * A method to handle when a person is selected
     *
     * @param person The person that was selected
     */
    private void onPersonSelected(@NonNull Person person) {
        nameGameViewModel.onClick(person);
    }

}
