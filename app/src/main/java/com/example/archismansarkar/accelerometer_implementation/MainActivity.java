package com.example.archismansarkar.accelerometer_implementation;

import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.Activity;

import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;
import android.graphics.Paint;
import com.androidplot.util.Redrawer;
import android.view.View;
import android.widget.Button;

import java.lang.ref.*;
import java.util.Arrays;

import android.util.Log;
import android.widget.CheckBox;


////////////////////////////////////////////////////////////////////////////////////////
public class MainActivity extends Activity implements SensorEventListener {
    private XYPlot plot;
    private Redrawer redrawer;
    private SensorManager sensorManager;
    public textfile txt;
    public SGFilter sgfilterI;

    private static double xVal;
    private static double yVal;
    private static double zVal;

    private static double[] coefficient = {0.04472049689440971, -0.02484472049689429, -0.05001634521085299, -0.043151356652500555, -0.015152971943926091, 0.024529354075726505, 0.06789992885025861, 0.1084168221064168, 0.14099186585389278, 0.16199065438532362, 0.16923254427629142, 0.16199065438532362, 0.14099186585389278, 0.10841682210641679, 0.06789992885025861, 0.024529354075726505, -0.015152971943926091, -0.043151356652500555, -0.05001634521085299, -0.02484472049689429, 0.04472049689440971};

    private static int dataCounterX = 0;
    private static double[] dataSetX = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    private static int dataCounterY = 0;
    private static double[] dataSetY = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private static int dataCounterZ = 0;
    private static double[] dataSetZ = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    private static int filtCounterX = 0;
    private static double[] smoothenedDataX = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private static int filtCounterY = 0;
    private static double[] smoothenedDataY = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private static int filtCounterZ = 0;
    private static double[] smoothenedDataZ = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    private static Button bt1,bt2,bt3;
    private static CheckBox ch1;
    private static boolean filterState = false;

    private static int axis = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorManager.registerListener(this, sensorManager.getDefaultSensor
                (Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        txt.textfile_initialize();

        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        ECGModel ecgSeries = new ECGModel(2000, 200);

        // add a new series' to the xyplot:
        MyFadeFormatter formatter =new MyFadeFormatter(2000);
        formatter.setLegendIconEnabled(false);
        plot.addSeries(ecgSeries, formatter);
        plot.setRangeBoundaries(-20, 20, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, 2000, BoundaryMode.FIXED);

        // reduce the number of range labels
        plot.setLinesPerRangeLabel(3);

        // start generating ecg data in the background:
        ecgSeries.start(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)));

        // set a redraw rate of 30hz and start immediately:
        redrawer = new Redrawer(plot, 30, true);

        /////////////////////////////////////////////////////////////////////////////////////////////
        bt1 =(Button)findViewById(R.id.button1);
        bt1.setEnabled(false);

        bt2 =(Button)findViewById(R.id.button2);

        bt3 =(Button)findViewById(R.id.button3);

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt1.setEnabled(false);
                bt2.setEnabled(true);
                bt3.setEnabled(true);
                axis = 1;
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt2.setEnabled(false);
                bt1.setEnabled(true);
                bt3.setEnabled(true);
                axis = 2;
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt3.setEnabled(false);
                bt2.setEnabled(true);
                bt1.setEnabled(true);
                axis = 3;
            }
        });

        ch1=(CheckBox)findViewById(R.id.checkBox1);

        ////////////////////////////////////////////////////////////////////////////////////////////
    }

    public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

        private int trailSize;

        public MyFadeFormatter(int trailSize) {
            this.trailSize = trailSize;
        }

        @Override
        public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
            // offset from the latest index:
            int offset;
            if(thisIndex > latestIndex) {
                offset = latestIndex + (seriesSize - thisIndex);
            } else {
                offset =  latestIndex - thisIndex;
            }

            float scale = 255f / trailSize;
            int alpha = (int) (255 - (offset * scale));
            getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
            return getLinePaint();
        }
    }
    public static class ECGModel implements XYSeries {

        private final Number[] data;
        private final long delayMs;
        private final Thread thread;
        private boolean keepRunning;
        private int latestIndex;

        private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

        /**
         *
         * @param size Sample size contained within this model
         * @param updateFreqHz Frequency at which new samples are added to the model
         */
        public ECGModel(int size, int updateFreqHz) {
            data = new Number[size];
            for(int i = 0; i < data.length; i++) {
                data[i] = 0;
            }

            // translate hz into delay (ms):
            delayMs = 1000 / updateFreqHz;

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (keepRunning) {
                            if (latestIndex >= data.length) {
                                latestIndex = 0;
                            }

                            if(axis == 1){
                                if(filterState== true){
                                    if(filtCounterX<=20){
                                        data[latestIndex] = 5*smoothenedDataX[filtCounterX];
                                        filtCounterX++;
                                        filtCounterY++;
                                        filtCounterZ++;
                                    }
                                    else if(filtCounterX>20){
                                        filtCounterX=0;
                                        filtCounterY=0;
                                        filtCounterZ=0;
                                    }
                                }
                                else {
                                    data[latestIndex] = xVal;
                                }
                            }
                            else if(axis == 2){
                                if(filterState == true){
                                    if(filtCounterY<=20){
                                        data[latestIndex] = 5*smoothenedDataY[filtCounterY];
                                        filtCounterX++;
                                        filtCounterY++;
                                        filtCounterZ++;
                                    }
                                    else if(filtCounterY>20){
                                        filtCounterX=0;
                                        filtCounterY=0;
                                        filtCounterZ=0;
                                    }
                                }
                                else {
                                    data[latestIndex] = yVal;
                                }
                            }
                            else if(axis == 3){
                                if(filterState == true){
                                    if(filtCounterZ<=20){
                                        data[latestIndex] = 5*smoothenedDataZ[filtCounterZ];
                                        filtCounterX++;
                                        filtCounterY++;
                                        filtCounterZ++;
                                    }
                                    else if(filtCounterZ>20){
                                        filtCounterX=0;
                                        filtCounterY=0;
                                        filtCounterZ=0;
                                    }
                                }
                                else {
                                    data[latestIndex] = zVal;
                                }
                            }

                            if(latestIndex < data.length - 1) {
                                data[latestIndex +1] = null;
                            }

                            if(rendererRef.get() != null) {
                                rendererRef.get().setLatestIndex(latestIndex);
                                Thread.sleep(delayMs);
                            } else {
                                keepRunning = false;
                            }
                            latestIndex++;
                        }
                    } catch (InterruptedException e) {
                        keepRunning = false;
                    }
                }
            });
        }

        public void start(final WeakReference<AdvancedLineAndPointRenderer> rendererRef) {
            this.rendererRef = rendererRef;
            keepRunning = true;
            thread.start();
        }

        @Override
        public int size() {
            return data.length;
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return data[index];
        }

        @Override
        public String getTitle() {
            return "Signal";
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        redrawer.finish();
        txt.textfile_close();
        finish();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        redrawer.finish();
        txt.textfile_close();
        finish();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

            filterState = ch1.isChecked();

            xVal = event.values[0];
            yVal = event.values[1];
            zVal = event.values[2];

            if(dataCounterX<=20)
            {
                dataSetX[dataCounterX]=xVal;
                dataCounterX++;
            }
            else if(dataCounterX>20)
            {
                smoothenedDataX= sgfilterI.smooth(dataSetX,coefficient);
                dataCounterX=0;
            }

            if(dataCounterY<=20)
            {
                dataSetY[dataCounterY]=yVal;
                dataCounterY++;
            }
            else if(dataCounterY>20)
            {
                smoothenedDataY= sgfilterI.smooth(dataSetY,coefficient);
                dataCounterY=0;
            }

            if(dataCounterZ<=20)
            {
                dataSetZ[dataCounterZ]=zVal;
                dataCounterZ++;
            }
            else if(dataCounterZ>20)
            {
                smoothenedDataZ= sgfilterI.smooth(dataSetZ,coefficient);
                dataCounterZ=0;
            }

            String xValue = String.valueOf(xVal);
            String yValue = String.valueOf(yVal);
            String zValue = String.valueOf(zVal);

            String data = "X: "+xValue+", Y: "+yValue+", Z: "+zValue + "\n";
            String datax = xValue+ "\n";
            String datay = yValue+ "\n";
            String dataz = zValue + "\n";

            txt.textfile_write(data,datax,datay,dataz);

        }
    }
}

