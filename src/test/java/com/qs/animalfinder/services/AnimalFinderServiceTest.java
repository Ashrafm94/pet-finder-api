package com.qs.animalfinder.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qs.animalfinder.models.Animal;
import com.qs.animalfinder.services.interfaces.IAnimalFinderService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnimalFinderServiceTest {

    @MockBean
    private RestTemplate restTemplate;
    @Autowired
    private IAnimalFinderService animalFinderService;

    private final String API_KEY = "FAKE_API_KEY";
    private final String API_SECRET = "FAKE_API_SECRET";
    private final String FAKE_TOKEN = "token_123456678";


    @BeforeEach
    public void setUp(){
        ReflectionTestUtils.setField(animalFinderService, "API_KEY", API_KEY);
        ReflectionTestUtils.setField(animalFinderService, "API_SECRET", API_SECRET);
    }

    @Test
    @Order(1)
    public void testAuthorizeAndGetToken() throws JSONException {
        mockRestTemplateTokenRequest();

        String token = animalFinderService.authorizeAndGetToken();

        assertEquals(token, FAKE_TOKEN);
    }

    @Test
    @Order(2)
    public void testGetAnimalListFromAPI() throws JSONException, JsonProcessingException {
        String type = "dog";

        //Set up mocks for token
        mockRestTemplateTokenRequest();
        mockFetchAnimalsRequest(type);

        //First Time should GET Data from API (we will mock this)
        animalFinderService.getAnimalsList(type);

        //Should Call API
        verifyVisitRestTemplateWithXTimes(1, type);
    }

    @Test
    @Order(3)
    public void testGetAnimalListFromHashMap() throws JSONException, JsonProcessingException {
        String type = "dog";

        //Should Get It from the HashMap
        animalFinderService.getAnimalsList(type);

        //Should Return from HashMap
        verifyVisitRestTemplateWithXTimes(0, type);
    }

    @Test
    @Order(4)
    public void testGetAllAnimalsFromAPI() throws JSONException, JsonProcessingException {
        //Set up mocks for token
        ReflectionTestUtils.setField(animalFinderService, "animalsInMemory", new HashMap<>());
        mockRestTemplateTokenRequest();
        mockFetchAnimalsRequest("dog");
        mockFetchAnimalsRequest("cat");

        //First Time should GET Data from API (we will mock this)
        animalFinderService.getAllAnimals();

        //Should Call API
        verifyVisitRestTemplateWithXTimes(1, "dog");
        verifyVisitRestTemplateWithXTimes(1, "cat");
    }

    @Test
    @Order(5)
    public void testGetAllAnimalsFromHashMap() throws JSONException, JsonProcessingException {
        animalFinderService.getAllAnimals();
        verifyVisitRestTemplateWithXTimes(0, "dog");
        verifyVisitRestTemplateWithXTimes(0, "cat");
    }

    private List<Animal> getAnimalsList(String type){
        List<Animal> animals = new ArrayList<>();
        animals.add(new Animal(1, "url", type, new HashMap<>(), "Female", "Old", "Rocky", new ArrayList<>()));

        return animals;
    }

    private String convertListToJsonArray(List<Animal> animals) throws JsonProcessingException {
        return new ObjectMapper()
                .writeValueAsString(animals);
    }

    private void verifyVisitRestTemplateWithXTimes(int times, String type) throws JSONException, JsonProcessingException {
        String url = "https://api.petfinder.com/v2/animals?type=" + type;
        verify(restTemplate, times(times)).exchange(url, HttpMethod.GET, getGetAnimalsRequest(), String.class);
    }

    private void mockFetchAnimalsRequest(String type) throws JSONException, JsonProcessingException {

        String url = "https://api.petfinder.com/v2/animals?type=" + type;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("animals", new JSONArray(convertListToJsonArray(getAnimalsList(type))));

        ResponseEntity<String> mockedResponse = new ResponseEntity<>(jsonObject.toString(), HttpStatus.OK);

        when(restTemplate.exchange(url, HttpMethod.GET, getGetAnimalsRequest(), String.class))
                .thenReturn(mockedResponse);
    }

    private HttpEntity<Map<String, String>> getGetAnimalsRequest(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + FAKE_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return new HttpEntity<>(null, headers);
    }

    private void mockRestTemplateTokenRequest() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("access_token", FAKE_TOKEN);

        ResponseEntity<String> mockedResponse = new ResponseEntity<>(jsonObject.toString(), HttpStatus.OK);

        String url = "https://api.petfinder.com/v2/oauth2/token";
        Map<String, String> data = new HashMap<>();
        data.put("grant_type", "client_credentials");
        data.put("client_id", API_KEY);
        data.put("client_secret", API_SECRET);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(data, headers);

        when(restTemplate.postForEntity(url, requestEntity, String.class)).thenReturn(mockedResponse);
    }
}
