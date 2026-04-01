package com.api.tinyfarm.service;

import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.repository.TransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

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
