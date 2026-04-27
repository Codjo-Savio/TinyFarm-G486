package com.api.tinyfarm.security;

import com.api.tinyfarm.dto.CooperativeSaleRequest;
import com.api.tinyfarm.dto.MarketBuyRequest;
import com.api.tinyfarm.model.Stock;
import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.model.User;
import com.api.tinyfarm.repository.AnimalRepository;
import com.api.tinyfarm.repository.TransactionRepository;
import com.api.tinyfarm.security.oauth.CustomOAuth2User;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("securityAuthorizationService")
public class SecurityAuthorizationService {

    private final AnimalRepository animalRepository;
    private final TransactionRepository transactionRepository;

    public SecurityAuthorizationService(
            AnimalRepository animalRepository,
            TransactionRepository transactionRepository) {
        this.animalRepository = animalRepository;
        this.transactionRepository = transactionRepository;
    }

    public boolean canAccessUser(Authentication authentication, Long userId) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }

        User currentUser = extractCurrentUser(authentication);
        if (currentUser == null) {
            return true;
        }

        return Objects.equals(currentUser.getId(), userId);
    }

    public boolean ownsAnimal(Authentication authentication, Long animalId) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }

        User currentUser = extractCurrentUser(authentication);
        if (currentUser == null) {
            return true;
        }

        return animalRepository.findById(animalId)
                .map(animal -> Objects.equals(animal.getUserId(), currentUser.getId()))
                .orElse(false);
    }

    public boolean canAccessStock(Authentication authentication, Stock stock) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        if (stock == null || stock.getId() == null) {
            return true;
        }
        return canAccessUser(authentication, stock.getId().getUserId());
    }

    public boolean canBuyFromMarket(Authentication authentication, MarketBuyRequest request) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        if (request == null || request.getBuyerId() == null) {
            return true;
        }
        return canAccessUser(authentication, request.getBuyerId());
    }

    public boolean canSellToCooperative(Authentication authentication, CooperativeSaleRequest request) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        if (request == null || request.getSellerId() == null) {
            return true;
        }
        return canAccessUser(authentication, request.getSellerId());
    }

    public boolean canAccessTransaction(Authentication authentication, Long transactionId) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }

        User currentUser = extractCurrentUser(authentication);
        if (currentUser == null) {
            return true;
        }

        return transactionRepository.findById(transactionId)
                .map(transaction -> isTransactionParticipant(transaction, currentUser.getId()))
                .orElse(false);
    }

    public boolean canSubmitTransaction(Authentication authentication, Transaction transaction) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        if (transaction == null) {
            return true;
        }
        if (isAdmin(authentication)) {
            return true;
        }

        User currentUser = extractCurrentUser(authentication);
        if (currentUser == null) {
            return true;
        }

        return isTransactionParticipant(transaction, currentUser.getId());
    }

    private boolean isTransactionParticipant(Transaction transaction, Long userId) {
        return Objects.equals(transaction.getBuyer(), userId)
                || Objects.equals(transaction.getSeller(), userId);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private User extractCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        if (principal instanceof CustomOAuth2User oAuth2User) {
            return oAuth2User.getUser();
        }
        return null;
    }
}
