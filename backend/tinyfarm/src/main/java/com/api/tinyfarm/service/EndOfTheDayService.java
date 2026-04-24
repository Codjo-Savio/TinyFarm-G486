package com.api.tinyfarm.service;

import com.api.tinyfarm.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
public class EndOfTheDayService {

    @Autowired
    ChickenService chickenService;
    @Autowired
    RabbitService rabbitService;
    @Autowired
    UserService userService;

    // process automatically the end of the day
    @Scheduled(cron = "0 0 0 * * *")
    public void processScheduled() {
        for (User user : userService.findAll()) {
            process(user.getId());
        }
    }

    public void process(Long userId){
        chickenService.processEndOfDay(userId);
        rabbitService.processEndOfDay(userId);
    }
}
