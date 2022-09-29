package com.example.lab17_flowskashitsin;

import android.graphics.Bitmap;
import android.graphics.Color;

public class GaussianBlurHelper implements Runnable{
    public int x0;
    public int y0;
    public int xn;
    public int yn;
    public int k;
    public Bitmap orig;
    public static int w = GlobalImage.w;
    public static int h = GlobalImage.w;

    public GaussianBlurHelper(int inx0, int iny0, int toXn, int toYn, int ink, Bitmap toOriginal) {
        x0 = inx0;
        y0 = iny0;
        xn = toXn;
        yn = toYn;
        k = ink;
        orig = toOriginal;
    }
    @Override
    public void run() {
        for (int y = y0; y < yn; y++) {
            for (int x = x0; x < xn; x++) {
                int red = 0;
                int green = 0;
                int blue = 0;
                double gaus = 0;
                for (int v = 0; v < k; v++)
                    for (int u = 0; u < k; u++) {
                        int px = u + x - k / 2;
                        int py = v + y - k / 2;
                        if (px < 0) px = 0;
                        if (py < 0) py = 0;
                        if (px >= w) px = w - 1;
                        if (py >= h) py = h - 1;
                        int c = orig.getPixel(px, py);
                        double epow = (u * u + v * v) / (2 * Math.pow(0.43, 2));
                        gaus += 1 / (2 * Math.PI * Math.pow(0.43,2)) * Math.pow(Math.E,-epow);
                        red += Color.red(c);
                        green += Color.green(c);
                        blue += Color.blue(c);

                    }
                red /= gaus * k * k;
                green /= gaus * k * k;
                blue /= gaus * k * k;
                if (Thread.currentThread().isInterrupted())
                {
                    return;
                }
                GlobalImage.finImage.setPixel(x, y, Color.rgb(red, green, blue));
            }

        }
    }
}
