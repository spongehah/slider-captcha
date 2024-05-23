package com.hah.demo.service;

import java.util.List;
import java.util.Map;

public interface ISliderCaptchaService {

    Map<String, String> getSliderImage(Integer width, Integer height);

    boolean verifyCode(Long startTime, Long endTime, Integer left, List<Integer> trail);
}
