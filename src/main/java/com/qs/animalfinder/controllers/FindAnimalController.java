package com.qs.animalfinder.controllers;


import com.qs.animalfinder.models.Animal;
import com.qs.animalfinder.services.implementations.AnimalFinderService;
import com.qs.animalfinder.services.interfaces.IAnimalFinderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/find-animal")
@CrossOrigin
public class FindAnimalController {

    private static final Logger logger = LoggerFactory.getLogger(FindAnimalController.class);

    @Autowired
    private IAnimalFinderService animalFinderService;

    @GetMapping("/find-all")
    public ResponseEntity<List<Animal>> findAll() {
        logger.info("entering findAll");

        List<Animal> animalList = animalFinderService.getAllAnimals();
        return new ResponseEntity<List<Animal>>(animalList, HttpStatus.OK);
    }

    @GetMapping("/find-all-by-type/{type}")
    public ResponseEntity<List<Animal>> findAllByType(@PathVariable String type) {
        logger.info("entering findAllByType with type '{}'", type);

        List<Animal> animalList = animalFinderService.getAnimalsList(type);
        return new ResponseEntity<List<Animal>>(animalList, HttpStatus.OK);
    }
}
