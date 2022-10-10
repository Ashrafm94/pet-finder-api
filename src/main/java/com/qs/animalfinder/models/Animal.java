package com.qs.animalfinder.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Animal {

    private long id;
    private String url;
    private String type;
    private Map<String, Object> breeds;
    private String gender;
    private String age;
    private String name;
    private List<Map<String, String>> photos;
}
