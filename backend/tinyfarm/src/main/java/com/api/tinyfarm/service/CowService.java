package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.CowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CowService {

    @Autowired
    private CowRepository cowRepository;
    @Autowired

    public List<Cow> findAll() {
        return cowRepository.findAll();
    }

    public Cow findById(Long id) {
        return cowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vache introuvable : " + id));
    }

    public Cow getByName(String name) {
        return cowRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Vache introuvable : " + name));
    }

    public Cow create(Cow cow) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
            cow.setUserId(currentUser.getId());
        }
        return cowRepository.save(cow);
    }

    public Cow update(Long id, Cow modifiedCow) {
        Cow existing = findById(id);
        existing.setCowType(modifiedCow.getCowType());
        existing.setName(modifiedCow.getName());
        existing.setMilking(modifiedCow.getMilking());
        return cowRepository.save(existing);
    }

    public void delete(Long id) {
        cowRepository.deleteById(id);
    }

    public void deleteByName(String name) {
        cowRepository.deleteByName(name);
    }

    public void deleteAll() {
        cowRepository.deleteAll();
    }
}
