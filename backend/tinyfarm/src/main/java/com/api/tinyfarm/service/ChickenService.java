package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Chicken;
import com.api.tinyfarm.repository.ChickenRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChickenService {

    private final ChickenRepository chickenRepository;

    public ChickenService(ChickenRepository chickenRepository) {
        this.chickenRepository = chickenRepository;
    }

    public List<Chicken> findAll() {
        return chickenRepository.findAll();
    }


    public Chicken findById(Long id) {
        return chickenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Poulet introuvale : " + id));
    }

    public Chicken getByName(String name) {
        return chickenRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Poulet introuvale : " + name));
    }

    public Chicken create(Chicken chicken) {
        return chickenRepository.save(chicken);
    }

    public Chicken update(Long id, Chicken modificatedChicken) {
        Chicken existing = findById(id);
        existing.setChickenType(modificatedChicken.getChickenType());
        existing.setName(modificatedChicken.getName());
        existing.setFasting(modificatedChicken.getFasting());
        return chickenRepository.save(existing);
    }

    public void delete(Long id) {
        chickenRepository.deleteById(id);
    }

    public void deleteByName(String name){
        chickenRepository.deleteByName(name);
    }

    public void deleteAll(){
        chickenRepository.deleteAll();
    }
}

