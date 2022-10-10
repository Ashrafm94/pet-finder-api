package com.qs.animalfinder.services.interfaces;

import com.qs.animalfinder.models.Animal;

import java.util.List;

public interface IAnimalFinderService {

    public String authorizeAndGetToken();
    public List<Animal> getAnimalsList(String type);
    public List<Animal> getAllAnimals();
}
