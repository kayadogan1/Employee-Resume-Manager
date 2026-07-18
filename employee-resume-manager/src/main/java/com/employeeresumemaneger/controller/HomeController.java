package com.employeeresumemaneger.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String home() {
        log.info("[HOME-CONTROLLER] GET / -> Çalışan listesine yönlendiriliyor");
        return "redirect:/employees";
    }
}
