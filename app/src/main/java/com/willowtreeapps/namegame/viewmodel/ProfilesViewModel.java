package com.willowtreeapps.namegame.viewmodel;

import com.willowtreeapps.namegame.network.api.model.Person;
import com.willowtreeapps.namegame.network.api.model.Profiles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfilesViewModel extends ViewModel {

    private MutableLiveData<Person> person = new MutableLiveData<>();
    private Profiles profiles;

    public ProfilesViewModel(Profiles profiles) {
        this.profiles = profiles;
    }

    public void onClick(int i) {
        person.setValue(profiles.getPeople().get(i));
    }

    public LiveData<Person> getPerson() {
        return person;
    }
}
