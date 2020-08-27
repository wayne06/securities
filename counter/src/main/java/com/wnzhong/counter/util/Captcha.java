package com.wnzhong.counter.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Captcha {

    private String code;

    private BufferedImage bufferedImage;

    private Random random = new Random();

    public Captcha(int width, int height, int codeCount, int lineCount) {
        // 生成图像
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 背景色
        Graphics g = bufferedImage.getGraphics();
        g.setColor(getRandomColor(200, 250));
        g.fillRect(0, 0, width, height);

        // 干扰线
        for (int i = 0; i < lineCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = xs + random.nextInt(width);
            int ye = ys + random.nextInt(height);
            g.setColor(getRandomColor(1, 255));
            g.drawLine(xs, ys, xe, ye);
        }

        // 噪点
        float yawRate = 0.01f;
        int area = (int) (yawRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            bufferedImage.setRGB(x, y, random.nextInt(255));
        }

        // 添加字符
        Font font = new Font("Fixedsys", Font.BOLD, height - 5);
        g.setFont(font);
        code = getRandomStr(codeCount);
        for (int i = 0; i < codeCount; i++) {
            g.setColor(getRandomColor(1, 255));
            g.drawString(code.substring(i, i + 1), i * width / codeCount + 3, height - 8);
        }
    }

    private String getRandomStr(int codeCount) {
        String str = "ABCDEFGHJKMNOPQRSTUVWXYZabcdefghjkmnopqrstuvwxyz1234567890";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeCount; i++) {
            sb.append(str.charAt((int) (Math.random() * (str.length() - 1))));
        }
        return sb.toString();
    }

    private Color getRandomColor(int fc, int bc) {
        fc = (fc > 255 ? 255 : fc);
        bc = (bc > 255 ? 255 : bc);
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
