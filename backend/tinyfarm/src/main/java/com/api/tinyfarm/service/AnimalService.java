package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.AnimalRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (animal == null) {
            throw new IllegalArgumentException("Animal manquant");
        }
        if (animal.getId() != null && animalRepository.existsById(animal.getId())) {
            throw new IllegalArgumentException("Animal déjà existant : " + animal.getId());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
            animal.setUserId(currentUser.getId());
        }
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
