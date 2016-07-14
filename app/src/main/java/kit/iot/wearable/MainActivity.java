package kit.iot.wearable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.pnikosis.materialishprogress.ProgressWheel;

import kit.iot.wearable.ble.FindWearableListener;
import kit.iot.wearable.ble.Wearable;


public class MainActivity extends Activity {
    NotificationManagerCompat notificationManger;
    int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Get user preferences
         */
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String name = preferences.getString("wearableName", "");

        /*
         * Interface controllers
         */
        final Button findWearable  = (Button) findViewById(R.id.findWearable);
        final SeekBar ledRed       = (SeekBar) findViewById(R.id.ledRed);
        final SeekBar ledGreen     = (SeekBar) findViewById(R.id.ledGreen);
        final SeekBar ledBlue      = (SeekBar) findViewById(R.id.ledBlue);
        final Button ledOff        = (Button) findViewById(R.id.ledOff);
        final ProgressWheel loader = (ProgressWheel) findViewById(R.id.loader);
        final Spinner playMelody   = (Spinner) findViewById(R.id.playMelody);

        /*
         * Create new Wearable instance
         */
        final Wearable kit = new Wearable(this, name);
        final FrameLayout cmdFrame = (FrameLayout) findViewById(R.id.commandsFrame);


        if (kit.isConnected()) {
            findWearable.setVisibility(View.GONE);
            cmdFrame.setVisibility(View.VISIBLE);
        }

        /*
         * Set on find wearable listener, on connected, on disconnected and id no wearable was found
         */
        kit.setOnFindWearableListner(new FindWearableListener() {
            @Override
            public void connected(BluetoothDevice device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createConnectedNotification();

                        findWearable.setVisibility(View.GONE);
                        cmdFrame.setVisibility(View.VISIBLE);
                        loader.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void disconnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelConnectedNotification();

                        findWearable.setVisibility(View.VISIBLE);
                        cmdFrame.setVisibility(View.GONE);
                        loader.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void notFound() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelConnectedNotification();

                        findWearable.setVisibility(View.VISIBLE);
                        cmdFrame.setVisibility(View.GONE);
                        loader.setVisibility(View.GONE);
                    }
                });
            }
        });

        /*
         * On button click start searching for the Wearable
         */
        findWearable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loader.setVisibility(View.VISIBLE);
                findWearable.setVisibility(View.GONE);
                kit.setName(preferences.getString("wearableName", "wV3"));
                kit.findWearable();
            }
        });

        /*
         * On melody selected
         */
        playMelody.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        kit.playMelody("mario");
                        break;

                    case 2:
                        kit.playMelody("imperial");
                        break;

                    case 3:
                        kit.playMelody("natal");
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        /*
         * Interface controllers listeners
         */
        ledRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                kit.ledSetTo("RED", progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ledGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                kit.ledSetTo("GREEN", progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ledBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                kit.ledSetTo("BLUE", progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ledOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                kit.ledOFF();

                ledRed.setProgress(0);
                ledGreen.setProgress(0);
                ledBlue.setProgress(0);
            }
        });
    }


    /*
     * Menu header options
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, WearableSettingsActivity.class);
                startActivityForResult(i, 1);
                break;
        }

        return true;
    }

    /*
     * Create a persistent connected notification
     */
    public void createConnectedNotification() {
        notificationManger = NotificationManagerCompat.from(getApplicationContext());

        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, viewIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.connected_notif_title));
        builder.setContentText(getString(R.string.connected_notif_content));
        builder.setContentIntent(notificationPendingIntent);
        builder.setSmallIcon(R.drawable.ic_wearable_icon);
        builder.setPriority(-1);
        builder.extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true));
        builder.setOngoing(true);

        notificationManger.notify(NOTIFICATION_ID, builder.build());
    }

    /*
     * Cancel persistent connected notification
     */
    public void cancelConnectedNotification() {
        notificationManger = NotificationManagerCompat.from(getApplicationContext());
        notificationManger.cancel(NOTIFICATION_ID);
    }
}