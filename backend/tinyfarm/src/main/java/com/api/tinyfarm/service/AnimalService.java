package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.repository.AnimalRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public List<Animal> findAll() {
        return animalRepository.findAll();
    }

    public void deleteAllAnimals() {
        animalRepository.deleteAll();
    }

    public Animal findById(Long id) {
        return animalRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Animal introuvable : " + id)
                );
    }

    public Animal create(Animal animal) {
        return animalRepository.save(animal);
    }

    public Animal update(Long id, Animal animal) {
        Animal existingAnimal = findById(id);
        existingAnimal.setClean(animal.getClean());
        existingAnimal.setHealthy(animal.getHealthy());
        existingAnimal.setAge(animal.getAge());
        existingAnimal.setWeight(animal.getWeight());
        existingAnimal.setGender(animal.getGender());
        return animalRepository.save(existingAnimal);
    }

    public void delete(Long id) {
        animalRepository.deleteById(id);
    }
}
