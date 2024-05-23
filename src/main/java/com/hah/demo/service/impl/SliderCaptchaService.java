package com.hah.demo.service.impl;

import com.hah.demo.service.ISliderCaptchaService;
import com.hah.demo.utils.DrawCaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.*;

@Service
public class SliderCaptchaService implements ISliderCaptchaService {

    @Autowired
    private HttpServletRequest request;

    @Override
    public Map<String, String> getSliderImage(Integer width, Integer height) {
        String imageFilePath = ClassUtils.getDefaultClassLoader().getResource("static").getPath();
        File bgImageFile = new File(imageFilePath + "/images/");
        File slideImageFile = new File(imageFilePath + "/slide/slide1.png");// 若要无边框选择slide.png
        if (bgImageFile.isDirectory()) {
            File[] files = bgImageFile.listFiles();
            Random random = new Random();
            int i = random.nextInt(files.length);
            bgImageFile = files[i];
        }
        int[] point = DrawCaptchaUtil.randomAnchorPoint(width, height);
        HttpSession session = request.getSession();
        session.setAttribute(session.getId(), point[0]);
        String bgImageBase64 = DrawCaptchaUtil.getBgImageBase64(bgImageFile, slideImageFile, point, width, height);
        String slideImageBase64 = DrawCaptchaUtil.getSlideImageBase64(bgImageFile, slideImageFile, point, width, height);
        Map<String, String> images = new HashMap<>();
        images.put("bgImage", bgImageBase64);
        images.put("slideImage", slideImageBase64);
        return images;
    }

    @Override
    public boolean verifyCode(Long startTime, Long endTime, Integer left, List<Integer> trail) {
        try {
            // 校验时间
            long dif = endTime - startTime;
            if (dif > 10000L) {// 只允许拖10s
                return false;
            }
            // 校验最后落点
            HttpSession session = request.getSession();
            Integer offset = (Integer) session.getAttribute(session.getId());
            // 获取offset后立即删除，防止重复验证
            session.removeAttribute(session.getId());
            Integer padding = offset - left;
            if (padding > 5 || padding < -5) {
                return false;
            }
            // 校验y轴轨迹
            int sum = 0;
            for (Integer num : trail) {
                sum += num;
            }
            double avg = sum * 1.0 / trail.size();
            double sum2 = 0.0;
            for (Integer data : trail) {
                sum2 += Math.pow(data - avg, 2);
            }
            double stddev = sum2 / trail.size();
            return stddev != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
