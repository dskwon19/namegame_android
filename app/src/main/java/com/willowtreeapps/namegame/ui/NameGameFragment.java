package com.willowtreeapps.namegame.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.core.ListRandomizer;
import com.willowtreeapps.namegame.core.NameGameApplication;
import com.willowtreeapps.namegame.network.api.ProfilesRepository;
import com.willowtreeapps.namegame.network.api.model.NameGame;
import com.willowtreeapps.namegame.network.api.model.Person;
import com.willowtreeapps.namegame.util.AnalyticsUtil;
import com.willowtreeapps.namegame.util.CircleBorderTransform;
import com.willowtreeapps.namegame.util.Ui;
import com.willowtreeapps.namegame.viewmodel.NameGameViewModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import static com.willowtreeapps.namegame.viewmodel.NameGameViewModel.LIMIT;

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
    private TextView data;
    private ImageView more;
    private ImageView correctFace;
    private TextView isCorrect;
    private TextView correctName;
    private ViewGroup correctContainer;
    private Button nextRound;

    private List<ImageView> faces = new ArrayList<>(LIMIT);
    private NameGameViewModel nameGameViewModel;

    private final Random rand = new Random();
    private final Handler handler = new Handler();
    private Runnable hintRunnable;
    private final int delay = 5000;
    private boolean hintEnabled = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NameGameApplication.get(getActivity()).component().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        nameGameViewModel = ViewModelProviders.of(this).get(NameGameViewModel.class);
        return inflater.inflate(R.layout.name_game_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        title = view.findViewById(R.id.title);
        container = view.findViewById(R.id.face_container);
        data = view.findViewById(R.id.data);
        more = view.findViewById(R.id.more);
        correctFace = view.findViewById(R.id.correct_face);
        correctName = view.findViewById(R.id.correct_name);
        isCorrect = view.findViewById(R.id.is_correct);
        correctContainer = view.findViewById(R.id.correct_container);
        nextRound = view.findViewById(R.id.next);

        prepareViewLoad();
        setNameGameListeners();
    }

    /**
     * Method to prepare the views before loading NameGame data
     */
    private void prepareViewLoad() {
        //Hide the views until data loads
        title.setAlpha(0);
        faces.clear();

        int n = container.getChildCount();
        for (int i = 0; i < n; i++) {
            ImageView face = (ImageView) container.getChildAt(i);
            faces.add(face);

            //Hide the views until data loads
            face.setScaleX(0);
            face.setScaleY(0);
        }

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMoreMenu(view);
            }
        });
    }

    /**
     * Initialize game menu popup
     *
     * @param view The anchor view
     */
    private void initMoreMenu(View view) {
        final PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        if (hintEnabled) {
            popup.getMenu().findItem(R.id.hint_mode).setTitle(getString(R.string.turn_off_hint_mode));
        } else {
            popup.getMenu().findItem(R.id.hint_mode).setTitle(getString(R.string.turn_on_hint_mode));
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.reset_data:
                        AnalyticsUtil.resetData(getContext());
                        setDataView();
                        break;
                    case R.id.hint_mode:
                        hintEnabled = !hintEnabled;
                        setViews(nameGameViewModel.getNameGame().getValue());
                        break;
                }
                return false;
            }
        });
        popup.show();
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
                    setCorrectView(correct);
                }
            }
        });
    }

    /**
     * Method to set views related to the correct results
     *
     * @param correct Boolean if the user is correct
     */
    private void setCorrectView(Boolean correct) {
        if (correct) {
            isCorrect.setText(R.string.correct_text);
        } else {
            isCorrect.setText(R.string.incorrect_text);
        }
        correctContainer.setVisibility(View.VISIBLE);

        // Disable click when displaying the correct face
        for (int i = 0; i < faces.size(); i++) {
            faces.get(i).setOnClickListener(null);
        }
        // Stops hint mode from running
        handler.removeCallbacks(hintRunnable);
    }

    /**
     * Method to set views related to the NameGame
     *
     * @param nameGame The current NameGame
     */
    private void setViews(NameGame nameGame) {
        if (nameGame != null) {
            setImages(faces, nameGame.getRandomPeople());
            animateFacesIn();

            String name = String.format("%s %s", nameGame.getCorrectPerson().getFirstName(), nameGame.getCorrectPerson().getLastName());

            title.setText(name);
            title.setAlpha(1);

            setDataView();
            prepareCorrectView(nameGame, name);

            toggleHintMode(nameGame, hintEnabled);
        }
    }

    /**
     * Method to start hint mode
     *
     * @param nameGame The current NameGame
     */
    private void toggleHintMode(NameGame nameGame, boolean enabled) {
        if (enabled) {
            final Set<Integer> generated = new LinkedHashSet<>();
            // We'll leave two for the user to select
            while (generated.size() < LIMIT - 2) {
                Integer next = rand.nextInt(LIMIT);
                if (next != nameGame.getRandomPeople().indexOf(nameGame.getCorrectPerson())) {
                    generated.add(next);
                }
            }
            final Iterator<Integer> iter = generated.iterator();

            hintRunnable = new Runnable() {
                @Override
                public void run() {
                    faces.get(iter.next()).setVisibility(View.GONE);
                    if (iter.hasNext()) {
                        handler.postDelayed(this, delay);
                    }
                }
            };
            handler.postDelayed(hintRunnable, delay);
        } else {
            handler.removeCallbacks(hintRunnable);
        }
    }

    /**
     * Method to initialize view displaying the correct face and name
     *
     * @param nameGame The current NameGame
     * @param name The name to display
     */
    private void prepareCorrectView(NameGame nameGame, String name) {
        int imageSize = (int) Ui.convertDpToPixel(120, getContext());
        correctContainer.setVisibility(View.GONE);
        nextRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareViewLoad();
                nameGameViewModel.createNewGame();
            }
        });

        correctName.setText(name);
        picasso.load("http:" + nameGame.getCorrectPerson().getHeadshot().getUrl())
                .placeholder(R.drawable.ic_face_white_48dp)
                .resize(imageSize, imageSize)
                .transform(new CircleBorderTransform())
                .into(correctFace);
    }

    /**
     * Method to set views related to data and analytics
     */
    private void setDataView() {
        int correct = AnalyticsUtil.getCorrectAttempts(getContext());
        int total = AnalyticsUtil.getTotalAttempts(getContext());

        int percent = 0;
        if (total > 0 ){
            percent = (correct * 100) / total;
        }

        data.setText(String.format(Locale.US, "%d/%d (%d%%)", correct, total, percent));
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
            face.setVisibility(View.VISIBLE);
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
