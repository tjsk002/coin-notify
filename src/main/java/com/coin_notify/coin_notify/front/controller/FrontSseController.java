package com.coin_notify.coin_notify.front.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontSseController {
    @GetMapping("/")
    public String showSSEPage() {
        return "index";
    }
}