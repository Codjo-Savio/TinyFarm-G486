package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.repository.AnimalRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public List<Animal> getAll() {
        return animalRepository.findAll();
    }

    public void deleteAllAnimals() {
        animalRepository.deleteAll();
    }

    public Animal findById(Long id) {
        // Now using standard JpaRepository findById instead of converting to int for UID,
        // which makes it standard and prevents ID mismatch issues.
        return animalRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Animal introuvable : " + id)
            );
    }

    public Animal findByUid(int uid) {
        return animalRepository
            .findByUID(uid)
            .orElseThrow(() ->
                new RuntimeException("Animal introuvable avec UID : " + uid)
            );
    }

    public Animal create(Animal animal) {

        return animalRepository.save(animal);
    }

    public Animal update(Long id, Animal animal) {
        Animal existingAnimal = findById(id);

        // Update fields
        existingAnimal.setClean(animal.getClean());
        existingAnimal.setHealthy(animal.getHealthy());
        existingAnimal.setAge(animal.getAge());
        existingAnimal.setWeight(animal.getWeight());
        existingAnimal.setAGender(animal.getAGender());

        return animalRepository.save(existingAnimal);
    }

    public void delete(Long id) {
        animalRepository.deleteById(id);
    }

    // Nouvelles méthodes utilisant le repository corrigé

    public List<Animal> findByClean(Boolean clean) {
        return animalRepository.findByClean(clean);
    }

    public List<Animal> findByHealthy(Boolean healthy) {
        return animalRepository.findByHealthy(healthy);
    }

    public List<Animal> findByAge(int age) {
        return animalRepository.findByAge(age);
    }

    public List<Animal> findByWeight(float weight) {
        return animalRepository.findByWeight(weight);
    }

    public List<Animal> findByGender(Boolean gender) {
        return animalRepository.findByGender(gender);
    }
}
