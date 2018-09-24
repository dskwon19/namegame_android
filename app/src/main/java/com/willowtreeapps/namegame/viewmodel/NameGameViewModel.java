package com.willowtreeapps.namegame.viewmodel;

import android.app.Application;
import android.widget.Toast;

import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.core.ListRandomizer;
import com.willowtreeapps.namegame.core.NameGameApplication;
import com.willowtreeapps.namegame.network.api.ProfilesRepository;
import com.willowtreeapps.namegame.network.api.model.NameGame;
import com.willowtreeapps.namegame.network.api.model.Person;
import com.willowtreeapps.namegame.network.api.model.Profiles;
import com.willowtreeapps.namegame.util.AnalyticsUtil;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class NameGameViewModel extends AndroidViewModel {

    @Inject
    ProfilesRepository profilesRepository;

    @Inject
    ListRandomizer listRandomizer;

    private MutableLiveData<NameGame> nameGame = new MutableLiveData<>();
    private MutableLiveData<Boolean> isCorrect = new MutableLiveData<>();
    private Profiles profiles;

    public static final int LIMIT = 5;

    private ProfilesRepository.Listener profilesListener;

    public NameGameViewModel(Application application) {
        super(application);
        NameGameApplication.get(application).component().inject(this);
        if (nameGame.getValue() == null) {
            registerProfilesRepo();
        }
    }

    /**
     * Helper method to check if the user's selection is correct and update isCorrect live data accordingly
     *
     * @param person The person selected
     */
    public void onClick(Person person) {
        if (nameGame.getValue() != null) {
            if (nameGame.getValue().isCorrectPersonSelected(person)) {
                isCorrect.postValue(true);
                AnalyticsUtil.incrementCorrectAttempt(getApplication());
            } else {
                isCorrect.postValue(false);
            }
            AnalyticsUtil.incrementTotalAttempt(getApplication());
        }
    }

    /**
     * Method to load all profiles
     */
    private void registerProfilesRepo() {
        profilesListener = new ProfilesRepository.Listener() {
            @Override
            public void onLoadFinished(Profiles people) {
                profiles = people;
                createNewGame();
            }

            @Override
            public void onError(Throwable error) {
                Toast.makeText(getApplication(), R.string.error_fetch_profiles, Toast.LENGTH_SHORT).show();
            }
        };
        profilesRepository.register(profilesListener);
    }

    /**
     * Method to create a new game within NameGame session
     */
    public void createNewGame() {
        if (profiles != null) {
            List<Person> randomPeople = listRandomizer.pickN(profiles.getPeople(), LIMIT);
            Person randomPerson = listRandomizer.pickOne(randomPeople);

            nameGame.postValue(new NameGame(randomPeople, randomPerson));

            // We post a null value to prevent this live data from triggering during config change
            isCorrect.postValue(null);
        }
    }

    /**
     * Retrieve NameGame live data for viewers' consumption
     *
     * @return MutableLiveData NameGame
     */
    public MutableLiveData<NameGame> getNameGame() {
        return nameGame;
    }

    /**
     * Retrieve isCorrect live data for viewers' consumption
     *
     * @return MutableLiveData isCorrect
     */
    public MutableLiveData<Boolean> isCorrect() {
        return isCorrect;
    }

    @Override
    public void onCleared() {
        profilesRepository.unregister(profilesListener);
        super.onCleared();
    }
}
