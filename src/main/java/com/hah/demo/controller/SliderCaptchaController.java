package com.hah.demo.controller;

import com.hah.demo.service.ISliderCaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@Controller
public class SliderCaptchaController {

    @Autowired
    private ISliderCaptchaService sliderCaptchaService;

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/getSliderCaptcha")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getSliderCaptcha(@RequestParam(value = "width", defaultValue = "280") Integer width,
                                                                @RequestParam(value = "height", defaultValue = "155") Integer height) {
        return ResponseEntity.ok(sliderCaptchaService.getSliderImage(width, height));
    }

    @PostMapping("/verifyCode")
    @ResponseBody
    public boolean verifyCode(Long startTime, Long endTime, Integer left, ArrayList<Integer> trail) {
        return sliderCaptchaService.verifyCode(startTime, endTime, left, trail);
    }
}