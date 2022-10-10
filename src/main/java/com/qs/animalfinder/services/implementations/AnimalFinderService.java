package com.qs.animalfinder.services.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qs.animalfinder.models.Animal;
import com.qs.animalfinder.services.interfaces.IAnimalFinderService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AnimalFinderService implements IAnimalFinderService {
    private static final Logger logger = LoggerFactory.getLogger(AnimalFinderService.class);
    @Autowired
    private RestTemplate restTemplate;

    @Value("${petfinder.api.key}")
    private String API_KEY;

    @Value("${petfinder.secret}")
    private String API_SECRET;

    private final Map<String, List<Animal>> animalsInMemory = new HashMap<>();

    private Date lastFetchTime = null;

    @Override
    public String authorizeAndGetToken(){

        String url = "https://api.petfinder.com/v2/oauth2/token";
        Map<String, String> data = new HashMap<>();
        data.put("grant_type", "client_credentials");
        data.put("client_id", API_KEY);
        data.put("client_secret", API_SECRET);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse.getString("access_token");
        }

        return null;
    }

    private boolean shouldFetchAgain(String type){
        if (lastFetchTime == null ||
                !animalsInMemory.containsKey(type) ||
                CollectionUtils.isEmpty(animalsInMemory.get(type))) {
            return true;
        }

        long diff = new Date().getTime() - lastFetchTime.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(diff) >= 60;
    }

    private List<Animal> fetchAnimals(String type) throws JsonProcessingException {
        String token = authorizeAndGetToken();

        String url = "https://api.petfinder.com/v2/animals?type=" + type;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            String animals = jsonObject.getJSONArray("animals").toString();

            return new ObjectMapper().readValue(animals, new TypeReference<List<Animal>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
        }

        return null;
    }

    @Override
    public List<Animal> getAnimalsList(String type) {

        if (shouldFetchAgain(type)) {
            lastFetchTime = new Date();

            List<Animal> animalList = null;
            try {
                animalList = fetchAnimals(type);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            animalsInMemory.put(type, animalList);

            return animalList;
        } else {
            return animalsInMemory.get(type);
        }
    }

    @Override
    public List<Animal> getAllAnimals() {

        List<String> types = List.of("dog", "cat");
        List<Animal> animals = new ArrayList<>();

        types.forEach(type -> {
            if (shouldFetchAgain(type)) {
                lastFetchTime = new Date();

                List<Animal> animalList = null;
                try {
                    animalList = fetchAnimals(type);


                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                animalsInMemory.put(type, animalList);

                if (animalList != null) {
                    animals.addAll(animalList);
                }

            } else {
                animals.addAll(animalsInMemory.get(type));
            }
        });

        return animals;
    }
}
