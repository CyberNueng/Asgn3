package cs355.asgn3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;

import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener {
    SensorManager sensorManager;
    Handler hdr = new Handler();
    float x, y, z;
    int POLL_INTERVAL = 500;
    int shake_threshold = 30;
    boolean shown_dialog = false;
    BroadcastReceiver batteryInfoReceiver;
    PowerManager powerManager;
    WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        wl = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "WakelockTag");
    }
    public void onSensorChanged(SensorEvent event){
        int type = event.sensor.getType();
        if(type==Sensor.TYPE_ACCELEROMETER){
            x=event.values[0];
            y=event.values[1];
            z=event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private Runnable pollTask = new Runnable() {
        public void run() {
            showDialog();
            hdr.postDelayed(pollTask,POLL_INTERVAL);
        }
    };

    public void showDialog() {
        if( (Math.abs(x)>shake_threshold) || (Math.abs(y)>shake_threshold) || (Math.abs(z)>shake_threshold) ) {
            if(!shown_dialog) {
                shown_dialog = true;
                final AlertDialog.Builder viewDialog = new AlertDialog.Builder(this);
                viewDialog.setIcon(android.R.drawable.btn_star_big_on);
                Resources res = getResources();
                String resultArray[] = res.getStringArray(R.array.resultSet);
                Random r = new Random();
                int i = r.nextInt(27);
                viewDialog.setTitle("ใบที่: "+(i+1)); viewDialog.setMessage(resultArray[i]);
                viewDialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); shown_dialog = false;
                            }
                        });
                viewDialog.show();
            }//end if
        }//end if
    }//end method

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(
                Sensor.TYPE_AMBIENT_TEMPERATURE),
                SensorManager.SENSOR_DELAY_NORMAL);
        this.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        /*if (!wl.isHeld()) {
            wl.acquire();
        }*/
        hdr.postDelayed(pollTask, POLL_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        /*if (wl.isHeld()) {
            wl.release();
        }*/
        hdr.removeCallbacks(pollTask);
    }
}

