package com.tony.rabbitmqprovider.controller;

import com.tony.rabbitmqprovider.service.RegisterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/register")
public class RegisterController {
    @Resource
    private RegisterService registerService;

    @GetMapping
    public void register(String name) {
        registerService.register(name);

    }

}
