package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.repository.TransactionRepository;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    public Transaction findByBuyer(Long buyer) {
        return transactionRepository
            .findByBuyer(buyer)
            .orElseThrow(() ->
                new RuntimeException("Transaction introuvable : " + buyer)
            );
    }

    public Transaction findBySeller(Long seller) {
        return transactionRepository
            .findBySeller(seller)
            .orElseThrow(() ->
                new RuntimeException("Transaction introuvable : " + seller)
            );
    }

    public Transaction findByProduct(Long product) {
        return transactionRepository
            .findByProduct(product)
            .orElseThrow(() ->
                new RuntimeException("Transaction introuvable : " + product)
            );
    }

    public Transaction findById(Long id) {
        return transactionRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Transaction introuvable : " + id)
            );
    }

    public Transaction create(Transaction transaction) {
        // A transaction must represent a real exchange between two distinct users.
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction manquante");
        }
        if (transaction.getId() != null && transactionRepository.existsById(transaction.getId())) {
            throw new IllegalArgumentException("Transaction déjà existante : " + transaction.getId());
        }
        if (transaction.getSeller() == null || transaction.getBuyer() == null) {
            throw new IllegalArgumentException("Acheteur/Vendeur manquant pour la transaction");
        }
        if (transaction.getSeller().equals(transaction.getBuyer())) {
            throw new IllegalArgumentException("Le vendeur et l'acheteur doivent être différents");
        }
        if (transaction.getProduct() == null) {
            throw new IllegalArgumentException("Produit manquant pour la transaction");
        }
        if (transaction.getQuantity() == null || transaction.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantité transaction invalide");
        }
        if (transaction.getTotalPrice() < 0) {
            throw new IllegalArgumentException("Montant transaction invalide");
        }
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Transaction update(Long id, Transaction modifiedT) {
        Transaction existingT = findById(id);
        existingT.setBuyer(modifiedT.getBuyer());
        existingT.setSeller(modifiedT.getSeller());
        existingT.setProduct(modifiedT.getProduct());
        existingT.setQuantity(modifiedT.getQuantity());
        existingT.setTotalPrice(modifiedT.getTotalPrice());

        return transactionRepository.save(existingT);
    }

    public void deleteById(Long id) {
        transactionRepository.deleteById(id);
    }

    public void deleteAll() {
        transactionRepository.deleteAll();
    }
}
