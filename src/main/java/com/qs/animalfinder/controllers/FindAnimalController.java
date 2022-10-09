package com.qs.animalfinder.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.qs.animalfinder.models.Animal;
import com.qs.animalfinder.services.AnimalFinderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/find-animal")
@CrossOrigin
public class FindAnimalController {

    private static final Logger logger = LoggerFactory.getLogger(FindAnimalController.class);

    private final AnimalFinderService animalFinderService;

    public FindAnimalController(AnimalFinderService animalFinderService) {
        this.animalFinderService = animalFinderService;
    }

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
