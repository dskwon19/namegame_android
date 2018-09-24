package com.willowtreeapps.namegame.network.api.model;

import java.util.List;

public class NameGame {

    private Person correctPerson;
    private List<Person> randomPeople;

    public NameGame(List<Person> randomPeople, Person randomPerson) {
        loadNewGame(randomPeople, randomPerson);
    }

    private void loadNewGame(List<Person> randomPeople, Person randomPerson) {
        this.randomPeople = randomPeople;
        this.correctPerson = randomPerson;
    }

    public boolean isCorrectPersonSelected(Person person) {
        return person.getId().equals(correctPerson.getId());
    }

    public Person getCorrectPerson() {
        return correctPerson;
    }

    public List<Person> getRandomPeople() {
        return randomPeople;
    }
}
