package com.example.lab17_flowskashitsin;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.number.Precision;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    SurfaceView sw;
    Spinner selectedFlows;
    Integer countFlows;
    Slider selectedMatrix;
    RadioButton boxBlur;
    RadioButton gaussianBlur;
    Bitmap bmp;
    ArrayList<Runnable> runnable;
    ArrayList<Thread> threads;
    Thread mainProcT;
    Long time;
    TextView textTime;
    TextView textMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedFlows = findViewById(R.id.spinner_flows);
        sw = findViewById(R.id.surface_image);
        selectedMatrix = findViewById(R.id.slider_matrix);
        boxBlur = findViewById(R.id.rb_box_blur);
        gaussianBlur = findViewById(R.id.rb_gaussian_blur);
        textTime = findViewById(R.id.text_time_processing);
        textMatrix = findViewById(R.id.text_matrix);
        ArrayList<Integer> coresList = new ArrayList<>(8);
        for (int i = 1; i < 9; i++) {
            coresList.add(i);
        }
        ArrayAdapter<Integer> coresAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, coresList);
        selectedFlows.setAdapter(coresAdapter);
        selectedMatrix.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                textMatrix.setText("Матрица " + (int) selectedMatrix.getValue() + "x" + (int) selectedMatrix.getValue());
                return false;
            }
        });
    }

    public void onImageProcessing(View view)
    {
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.capybara);
        bmp = Bitmap.createScaledBitmap(bmp, 256, 256, false);
        Bitmap res = Bitmap.createBitmap(bmp.getWidth(),bmp.getHeight(),Bitmap.Config.ARGB_8888);
        GlobalImage.finImage = res.copy(Bitmap.Config.ARGB_8888, true);
        GlobalImage.w = GlobalImage.finImage.getWidth();
        GlobalImage.h = GlobalImage.finImage.getHeight();
        countFlows = Integer.parseInt(selectedFlows.getSelectedItem().toString());

        mainProcT = new Thread(() -> {
            runnable = new ArrayList<>();
            threads = new ArrayList<>();
            int neededRunnable = countFlows;
            int neededPixels = GlobalImage.h / countFlows;
            if (neededPixels * countFlows < GlobalImage.h)
                neededRunnable++;
            int needXY = 0;
            GlobalImage.endPixel = 0;
            if (boxBlur.isChecked())
            {
                for (int i = 0; i < neededRunnable; i++)
                {
                    if (needXY + neededPixels > GlobalImage.h)
                        runnable.add(new BoxBlurHelper(0,needXY,GlobalImage.w,GlobalImage.h, (int) selectedMatrix.getValue(), bmp));
                    else
                        runnable.add(new BoxBlurHelper(0,needXY,GlobalImage.w ,needXY + neededPixels, (int) selectedMatrix.getValue(), bmp));
                    needXY += neededPixels;

                }
            }
            if (gaussianBlur.isChecked())
            {
                int k = (int)selectedMatrix.getValue();
                if (selectedMatrix.getValue() < 11)
                    k = 11;
                for (int i = 0; i < neededRunnable; i++)
                {
                    if (needXY + neededPixels > GlobalImage.h)
                        runnable.add(new GaussianBlurHelper(0,needXY,GlobalImage.w,GlobalImage.h, k, bmp));
                    else
                        runnable.add(new GaussianBlurHelper(0,needXY,GlobalImage.w ,needXY + neededPixels, k, bmp));
                    needXY += neededPixels;

                }
            }
            time = System.currentTimeMillis();

            for (int i = 0; i < countFlows; i++)
            {
                threads.add(new Thread(runnable.get(i)));
                GlobalImage.endPixel += neededPixels;
                threads.get(i).start();
            }
            for (int i = 0; i < countFlows;i++)
            {
                try {
                    threads.get(i).join();
                    if (GlobalImage.endPixel < GlobalImage.h) {
                        threads.get(i).interrupt();
                        threads.set(i,new Thread(runnable.get(countFlows)));
                        threads.get(i).start();
                        threads.get(i).join();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            time = System.currentTimeMillis() - time;
            double calculatedTime = calculateTime(time);
            runOnUiThread(() -> {
                textTime.setText("Время на обработку: " + calculatedTime + "s");
                sw.setForeground(new BitmapDrawable(GlobalImage.finImage));
            });
        });

        mainProcT.start();

        }

        public void onStopProccessing(View view){
            for (int i = 0; i < countFlows; i++)
            {
                time = System.currentTimeMillis() - time;
                double calculatedTime = calculateTime(time);
                mainProcT.interrupt();
                if (threads.get(i).isAlive())
                {
                    threads.get(i).interrupt();
                }
                textTime.setText("Время на обработку: " + calculatedTime + "s");
                sw.setForeground(new BitmapDrawable(GlobalImage.finImage));
            }

        }

        double calculateTime(Long time)
        {
            double timed = Double.valueOf(time) / 1000;
            double t = BigDecimal.valueOf(timed).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
            return t;
        }
}