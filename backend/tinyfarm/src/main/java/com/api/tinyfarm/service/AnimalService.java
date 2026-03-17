package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.repository.AnimalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    public AniamlService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public List<Animal> findAll() {
        return animalRepository.findAll();
    }

    public void deleteAllAnimal(){
       animalRepository.deleteAll();
    }

    public Animal findById(Long id) {
        return animalRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Animal introuvale : " + id));
    }

    public Animal create(Animal animal) {
        return animalRepository.save(animal);
    }

    public Animal update(Long id, Animal modificatedAnimal) {
        Animal existing = findById(id);
        existing.setClean(modificatedAnimal.getClean());
        existing.setAGender(modificatedAnimal.getAGender());
        existing.setHealthy(modificatedAnimal.getHealthy());
        existing.setAge(modificatedAnimal.getAge());
        existing.setWeight(modificatedAnimal.getWeight());
        return animalRepository.save(existing);
    }

    public void delete(Long id) {
        animalRepository.deleteById(id);
    }

    
}