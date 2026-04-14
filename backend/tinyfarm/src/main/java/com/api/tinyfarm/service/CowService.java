package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Animal;
import com.api.tinyfarm.model.Cow;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.CowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CowService {

    @Autowired
    private CowRepository cowRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private  AnimalService animalService;

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
        cow.setId(null);

        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        cow.setUserId(currentUser.getId());
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

    // --- Actions Journalières ---

    /**
     * nourri une vache avec de la paille
     * si impossible, elle sera nourrie avec de l'herbe
     */
    public Cow hayCow(Long cowId, Long userId){
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 5){
            user.setEcus(user.getEcus() - 5);
            userService.update(userId, user);

            cow.setHayToday(true);
            cow.setFedToday(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour nourrir la vahe avec de la paille !"
            );
        }
    }

    /**
     * nouri la vache avec de l'herbe
     */
    public Cow grassCow(Long cowId){
        Cow cow = findById(cowId);

        cow.setFedToday(true);
        return cowRepository.save(cow);
    }

    /**
     * abreuve une vache
     */
    public Cow waterCow(Long cowId, Long userId){
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 2){
            user.setEcus(user.getEcus() - 2);
            userService.update(userId, user);

            cow.setWateredToday(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour abreuver la vache !"
            );
        }
    }

    /**
     * nettoie une vache
     */
    public Cow cleanCow(Long cowId, Long userId){
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 3){
            user.setEcus(user.getEcus() - 3);
            userService.update(userId, user);

            cow.setClean(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour nettoyer la vache !"
            );
        }
    }

    /**
     * soigne une vache
     */
    public Cow healCow(Long cowId, Long userId){
        Cow cow = findById(cowId);
        User user = userService.findById(userId);

        if (user.getEcus() >= 6){
            user.setEcus(user.getEcus() - 6);
            userService.update(userId, user);

            cow.setHealthy(true);
            return cowRepository.save(cow);
        } else {
            throw new RuntimeException(
                "Pas assez d'écus pour soigner la vache !"
            );
        }
    }

    /**
     * gère la quantité de lait produite par les vaches d'un utilisateur
     */
    public void milking(Long cowID){
        Cow cow = findById(cowId);

        //si elle peut produire
        if (cow.getMilking()){
            // et qu'elle a été traite (i.e. elle n'a pas de lait)
            if (cow.getMilk() == 0){
                cow.setMilk(8);
            } else {
                cow.setMilk(cow.getMilk() + 4);
            }
        }

        if (cow.getMilk() == 16){
            cow.setMilking(false);
        }
        
        cowRepository.save(cow);
    }

    /**
     * ajuste le poid de la vache en fonction de ce qu'elle a mangé dans la journée
     */
    private Cow handleWeight(Long cowId){
        Cow cow = findById(cowId);

        if (cow.getWateredToday()){
            if(cow.getHayToday()){
                // paille herbe eau
                cow.setWeight(cow.getWeight() + 9);
            } else {
                //herbe eau
                cow.setWeight(cow.getWeight() + 6);
            }
            
        } else {
            if(cow.getHayToday()){
                //paille herbe
                cow.setWeight(cow.getWeight() + 8);
            } else {
                // herbe
                cow.setWeight(cow.getWeight() + 5);
            }
            
        }

        // eau seule ne fait rien

        // poids max = 750 kg
        if (cow.getWeight() > 750.0f) {
            cow.setWeight(750.0f);
        }
        
        return cowRepository.save(cow);
    }

    /**
     * gère la santé d'une vache séléctionnée
     */
    private Cow handleHealth(Long cowId){
        Cow cow = findById(cowId);

        if (cow.getHealthy() != null && !cow.getHealthy()){
            cow.setSickDays(cow.getSickDays() + 1);
        } else {
            cow.setSickDays(0);
            cow.setMilking(true);
        }

        return cowRepository.save(cow);
    }

    /**
     * gère la fin de journée
     */
    public void processEndOfDay(Long userId) {
        List<Cow> userCows = cowRepository.findByUserId(userId);

        for (Cow cow : userCows) {
            // 1. Santé
            cow = handleHealth(cow.getId());

            if (cow.getSickDays() == 4) {
                cowRepository.delete(cow);
                continue;
            }

            // 2. Poids
            cow = handleWeight(cow.getId());

            // reset journaliers
            cow.setFedToday(false);
            cow.setHayToday(false);
            cow.setWateredToday(false);
            cow.setClean(false); // elle devient sale
            cow.setMilking(false); // et ne peut donc plus faire de lait

            // 1 chance sur 5 de tomber malade
            if (Math.random() < 0.2){ 
                cow.setHealthy(false);
                cow.setMilking(false);
            }

            cowRepository.save(cow);
        }
    }

}
