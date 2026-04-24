package com.api.tinyfarm.controller;

import com.api.tinyfarm.service.EndOfTheDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/endofday")
public class EndOfDayController {
    @Autowired
    EndOfTheDayService endOfTheDayService;

    @PostMapping("/id/{id}")
    public ResponseEntity<Void> endOfDay(@PathVariable Long id){
        try {
            endOfTheDayService.process(id);
            return ResponseEntity.ok().build();
        }catch(Exception e){
            return ResponseEntity.notFound().build();
        }
    }
}
