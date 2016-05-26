package edu.cs65.caregiver.caregiver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import edu.cs65.caregiver.caregiver.meapsoft.FFT;


/**
 * Created by ellenli on 5/25/16.
 */
public class SensorService extends Service implements SensorEventListener {

    private final IBinder mBinder = new MyLocalBinder();
    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private ArrayList<Double> featVect = new ArrayList<>();
    private static ArrayBlockingQueue<Double> mAccBuffer;
    private OnSensorChangedTask mAsyncTask;
    private Context mContext = this;

    public static final int ACCELEROMETER_BUFFER_CAPACITY = 2048;
    public static final int ACCELEROMETER_BLOCK_CAPACITY = 64;
    public static final String BROADCAST_ACTION = "edu.cs65.caregiver.caregiver.SOME_MESSAGE";
    public static final String BROADCAST_LABEL_CHANGE = "edu.cs65.caregiver.caregiver.WEKA_UPDATE";

    public void onCreate() {
        super.onCreate();

        mAccBuffer = new ArrayBlockingQueue<Double>(ACCELEROMETER_BUFFER_CAPACITY);
        setUpSensor();
    }

    private void setUpSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);

        mAsyncTask = new OnSensorChangedTask();
        mAsyncTask.execute(new Void[0]);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            double m = Math.sqrt(x * x + y * y + z * z);

            // add to queue
            try {
                mAccBuffer.add(new Double(m));
            } catch (IllegalStateException e) {
                ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(
                        mAccBuffer.size() * 2);

                mAccBuffer.drainTo(newBuf);
                mAccBuffer = newBuf;
                mAccBuffer.add(new Double(m));
            }
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        mAsyncTask.cancel(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sensorManager.unregisterListener(this);

        super.onDestroy();
    }

    private class OnSensorChangedTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {

            Double[] toClassify = new Double[ACCELEROMETER_BLOCK_CAPACITY + 1];

            int blockSize = 0;
            FFT fft = new FFT(ACCELEROMETER_BLOCK_CAPACITY);
            double[] accBlock = new double[ACCELEROMETER_BLOCK_CAPACITY];
            double[] re = accBlock;
            double[] im = new double[ACCELEROMETER_BLOCK_CAPACITY];

            double max = Double.MIN_VALUE;

            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled() == true) {
                        return null;
                    }

                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().doubleValue();

                    // once app has 64 readings
                    if (blockSize == ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0;
                        max = .0;

                        for (double val : accBlock) {
                            if (max < val) {
                                max = val;
                            }
                        }

                        fft.fft(re, im);

                        for (int i = 0; i < re.length; i++) {
                            // Compute each coefficient
                            double mag = Math.sqrt(re[i] * re[i] + im[i]* im[i]);
                            toClassify[i] = mag;
                            im[i] = .0; // Clear the field
                        }

                        // Finally, append max after frequency components
                        toClassify[ACCELEROMETER_BLOCK_CAPACITY] = max;
                        int label = (int) WekaClassifier.classify(toClassify);

                        switch ((int) label) {
                            case 0:
                                // nothing wrong
                                break;
                            case 1:
                                Intent fallIntent = new Intent(mContext, FallActivity.class);
                                fallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(fallIntent);
                                break;
                        }

                        Intent i = new Intent(BROADCAST_LABEL_CHANGE);
                        sendBroadcast(i);
                        featVect.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class MyLocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }
}