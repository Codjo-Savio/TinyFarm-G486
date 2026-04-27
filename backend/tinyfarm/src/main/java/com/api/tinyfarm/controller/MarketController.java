package com.api.tinyfarm.controller;

import com.api.tinyfarm.dto.MarketBuyRequest;
import com.api.tinyfarm.dto.PublishProductToTradeRequest;
import com.api.tinyfarm.model.Market;
import com.api.tinyfarm.service.MarketService;
import com.api.tinyfarm.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    @Autowired
    private MarketService marketService;
    @Autowired
    private StockService stockService;

    @GetMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #id)")
    public ResponseEntity<List<Market>> getByUserId(@PathVariable Long id) {
        try {
            List<Market> markets = marketService.findByUserId(id);

            if (markets.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(markets);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Market> getAllExceptOneOfTheConnectedUser(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(marketService.findByProductId(productId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/not")
    public ResponseEntity<List<Market>> getByProductId(@RequestParam Long uid) {
        try {
            return ResponseEntity.ok(marketService.findAllExceptOnesOfTheConnectedUser(uid));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/price/{price}")
    public ResponseEntity<List<Market>> getByPrice(@PathVariable float price) {
        try {
            return ResponseEntity.ok(marketService.findByPrice(price));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/quantity/{quantity}")
    public ResponseEntity<List<Market>> getByQuantity(@PathVariable int quantity) {
        try {
            return ResponseEntity.ok(marketService.findByQuantity(quantity));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Market> create(@RequestBody Market market) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                marketService.create(market)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PostMapping("/buy")
    @PreAuthorize("@securityAuthorizationService.canBuyFromMarket(authentication, #request)")
    public ResponseEntity<Void> buyFromMarket(@RequestBody MarketBuyRequest request) {
        try {
            marketService.buyFromMarket(
                request.getBuyerId(),
                request.getSellerId(),
                request.getProductId(),
                request.getQuantity()
            );
            return ResponseEntity.ok().build();
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PostMapping("/ad")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Market> publishToMarket(@RequestBody PublishProductToTradeRequest request) {
        try {
            Market market = stockService.publishToMarket(
                    request.getProductId(),
                    request.getQuantity(),
                    request.getUnitPrice());
            return ResponseEntity.status(HttpStatus.CREATED).body(market);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/uid/{uid}/pid/{pid}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #id)")
    public ResponseEntity<Market> update(
        @PathVariable Long uid,
        @PathVariable Long pid,
        @RequestBody Market market
    ) {
        try {
            return ResponseEntity.ok(marketService.update(uid, pid, market));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}/{productId}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #userId)")
    public ResponseEntity<Void> deleteByProductId(
        @PathVariable Long userId,
        @PathVariable Long productId
    ) {
        try {
            marketService.deleteProductById(userId, productId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/id/{uid}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #uid)")
    public ResponseEntity<Void> deleteById(@PathVariable Long uid) {
        try {
            marketService.deleteByID(uid);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
