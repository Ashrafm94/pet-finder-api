package com.qs.animalfinder.config;

import com.qs.animalfinder.services.implementations.AnimalFinderService;
import com.qs.animalfinder.services.interfaces.IAnimalFinderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewayConfigurations {
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public IAnimalFinderService getAnimalFinderService(){
        return new AnimalFinderService();
    }
}
