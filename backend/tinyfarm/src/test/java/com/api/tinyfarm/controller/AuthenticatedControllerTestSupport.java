package com.api.tinyfarm.controller;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

abstract class AuthenticatedControllerTestSupport {

    protected RequestPostProcessor authenticated() {
        return user("test-user").roles("USER");
    }
}
