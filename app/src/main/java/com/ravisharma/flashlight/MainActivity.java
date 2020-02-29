package com.ravisharma.flashlight;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private CameraManager mCameraManager;
    private String mCameraId;
    private ImageButton mTorchOnOffButton;
    private Boolean isTorchOn;
    private MediaPlayer mp;
    Switch timerSwitch;
    Spinner spTimer;

    boolean isBlinkOn = false;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }

        mTorchOnOffButton = (ImageButton) findViewById(R.id.btnSwitch);
        timerSwitch = findViewById(R.id.timerSwitch);
        spTimer = findViewById(R.id.sp_time);

        isTorchOn = false;

        boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {

            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error !!");
            alert.setMessage("Your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                    System.exit(0);
                }
            });
            alert.show();
            return;
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mTorchOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isTorchOn) {
                        turnOffFlashLight();
                        isTorchOn = false;
                    } else {
                        turnOnFlashLight();
                        isTorchOn = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        timerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    timerSwitch.setText("Off");
                    if (isBlinkOn && isTorchOn) {
                        isBlinkOn = false;
                    }
                } else {
                    timerSwitch.setText("On");
                    if (!isBlinkOn && isTorchOn) {
                        isBlinkOn = true;
                    }
                }
            }
        });

    }

    public void turnOnFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                playOnOffSound();
                mTorchOnOffButton.setImageResource(R.drawable.btn_switch_on);
                if (timerSwitch.isChecked()) {
                    isBlinkOn = true;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();

                            boolean ch = true;
                            while (true) {
                                try {
                                    String time = spTimer.getSelectedItem().toString();
                                    long blinkDelay = Long.parseLong(time); //Delay in ms
                                    if (isBlinkOn) {
                                        if (ch) {
                                            mCameraManager.setTorchMode(mCameraId, true);
                                            ch = false;
                                        } else {
                                            mCameraManager.setTorchMode(mCameraId, false);
                                            ch = true;
                                        }
                                        Thread.sleep(blinkDelay);
                                    } else if (isTorchOn) {
                                        mCameraManager.setTorchMode(mCameraId, true);
                                    } else {
                                        mCameraManager.setTorchMode(mCameraId, false);
                                        break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                } else {
                    mCameraManager.setTorchMode(mCameraId, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turnOffFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);
                playOnOffSound();
                isBlinkOn = false;
                mTorchOnOffButton.setImageResource(R.drawable.btn_switch_off);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playOnOffSound() {
        mp = MediaPlayer.create(MainActivity.this, R.raw.switch_on);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        });
        mp.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isTorchOn) {
            turnOffFlashLight();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTorchOn) {
            turnOffFlashLight();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTorchOn) {
            turnOnFlashLight();
        }
    }
}
