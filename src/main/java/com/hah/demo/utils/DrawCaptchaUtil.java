package com.hah.demo.utils;

import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 验证码绘制类
 *
 * @Anthor: spongehah
 * @Date: 2024.05.23
 */
public class DrawCaptchaUtil {
    private static final int SLIDE_CANVAS_WIDTH = 50;
    private static final int SLIDE_IMAGE_WIDTH = 50;
    private static final int SLIDE_IMAGE_HEIGHT = 50;

    /**
     * 绘制背景图片
     *
     * @param bgImagePath    背景图片路径文件
     * @param slideImagePath 滑块形状路径文件
     * @param point          滑块坐标: x=point[0],y=point[1]
     * @param canvasWidth    画布宽度
     * @param canvasHeight   画布高度
     * @return
     * @throws IOException
     */
    public static String getBgImageBase64(File bgImagePath, File slideImagePath, int[] point, Integer canvasWidth, Integer canvasHeight) {
        Graphics2D bgg = null;
        InputStream inputStream = null;
        try {
            // 绘制bgImg
            BufferedImage bgImg = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            bgg = (Graphics2D) bgImg.getGraphics();
            bgg.fillRect(0, 0, canvasWidth, canvasHeight);
            inputStream = Files.newInputStream(Paths.get(bgImagePath.getAbsolutePath()));
            BufferedImage read = ImageIO.read(inputStream);
            bgg.drawImage(read, 0, 0, canvasWidth, canvasHeight, null, null);
            // 设置为透明覆盖 很重要
            float num = ThreadLocalRandom.current().nextInt(45, 55) / 100.00f;
            bgg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, num));
            // 覆盖阴影
            bgg.drawImage(drawSlideImageShadow(slideImagePath), point[0], point[1], null, null);

            return ImageToBase64Util.bufferedImageToBase64(bgImg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bgg != null) {
                    bgg.dispose();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 绘制滑块
     *
     * @param bgImageFile    背景图片路径文件
     * @param slideImagePath 滑块形状路径文件
     * @param point          滑块坐标: x=point[0],y=point[1]
     * @param canvasHeight   画布高度
     * @return 滑块图片
     * @throws IOException
     */
    public static String getSlideImageBase64(File bgImageFile, File slideImagePath, int[] point, Integer canvasWidth, Integer canvasHeight) {
        Graphics2D bgg = null;
        Graphics2D slg = null;
        Graphics2D g2 = null;
        InputStream inputStream = null;
        try {
            // 绘制bgImg
            BufferedImage bgImg = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            bgg = (Graphics2D) bgImg.getGraphics();
            bgg.fillRect(0, 0, canvasWidth, canvasHeight);
            inputStream = Files.newInputStream(Paths.get(bgImageFile.getAbsolutePath()));
            BufferedImage read = ImageIO.read(inputStream);
            bgg.drawImage(read, 0, 0, canvasWidth, canvasHeight, null, null);

            BufferedImage sliderImg = new BufferedImage(SLIDE_CANVAS_WIDTH, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            slg = sliderImg.createGraphics();
            slg.getDeviceConfiguration().createCompatibleImage(SLIDE_CANVAS_WIDTH, canvasHeight, Transparency.TRANSLUCENT);
            slg = sliderImg.createGraphics();
            BufferedImage slideImage = bgImg.getSubimage(point[0], point[1], SLIDE_IMAGE_WIDTH, SLIDE_IMAGE_HEIGHT);
            inputStream = Files.newInputStream(Paths.get(slideImagePath.getAbsolutePath()));
            BufferedImage slide = ImageIO.read(inputStream);
            g2 = slide.createGraphics();
            // 设置为透明覆盖 很重要
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.85f));

            g2.drawImage(slideImage, 0, 0, null);
            slg.drawImage(slide, 0, point[1], null);
            return ImageToBase64Util.bufferedImageToBase64(sliderImg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bgg != null) {
                    bgg.dispose();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (g2 != null) {
                    g2.dispose();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (null != slg) {
                    slg.dispose();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 绘制背景图上的阴影
     */
    private static BufferedImage drawSlideImageShadow(File slideImagePath) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(Paths.get(slideImagePath.getAbsolutePath()));
            BufferedImage slide = ImageIO.read(inputStream);
            return slide;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 生成随机坐标点 x > 滑块画布宽度 y < 滑块画布高度-滑块图片高度
     */
    public static int[] randomAnchorPoint(Integer canvasWidth, Integer canvasHeight) {
        // 设置x坐标从 slideCanvasWidth 开始至 canvasWidth - slideCanvasWidth 范围随机
        int x = ThreadLocalRandom.current().nextInt(SLIDE_CANVAS_WIDTH, canvasWidth - SLIDE_IMAGE_WIDTH - 10);
        if (x < SLIDE_IMAGE_WIDTH) {
            // 随机生成x点小于图片宽度+上图片宽度
            x += SLIDE_CANVAS_WIDTH;
        }
        // 设置y坐标
        int y = ThreadLocalRandom.current().nextInt(canvasHeight - SLIDE_IMAGE_HEIGHT);
        int[] point = {x, y};
        return point;
    }

    static class ImageToBase64Util {

        /**
         * BufferedImage 对象转BASE64
         */
        private static String bufferedImageToBase64(BufferedImage image) {
            ByteArrayOutputStream out = null;
            try {
                out = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", out);
                byte[] bytes = out.toByteArray();
                BASE64Encoder base64Encoder = new BASE64Encoder();
                return base64Encoder.encodeBuffer(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}