package kit.iot.wearable.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * Kit IoT Wearable main class. Requires Android 5.0
 */
@TargetApi(21)
public class Wearable implements Parcelable {
    private static final String KIT_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String KIT_TX      = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final long SCAN_PERIOD = 5000;

    private static String name;
    private static Context context;
    private static BluetoothDevice device;
    private static Boolean connected = false;
    private static FindWearableListener listener;
    private Timer mTimer;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService bluetoothGattService;
    private int mData;
    private Commands cmd;

    /**
    * Create a new Wearable instance
    * @param context application context
    * @param name specify the Wearable name to connect
    */
    public Wearable(Context context, String name) {
        this.setName(name);
        this.setContext(context.getApplicationContext());
        this.mTimer = new Timer();
        this.cmd    = new Commands();

        checkBLE();
    }

    /**
     * Set on find Wearable listener
     * @param listener FindWearableListener
     */
    public void setOnFindWearableListner(FindWearableListener listener) {
        this.setListener(listener);
    }

    /**
     * Set the Wearable name
     */
    public void setName(String name) {
        Wearable.name = name;
    }

    /**
     * Get the Wearable name
     * @return name
     */
    public static String getName() {
        return Wearable.name;
    }

    /**
     * Set the fine device listener for the Wearable
     */
    private void setListener(FindWearableListener listener) {
        Wearable.listener = listener;
    }

    /**
     * Get the device listener for the Wearable
     * @return listener
     */
    public static FindWearableListener getListener() {
        return Wearable.listener;
    }

    /**
     * Set the Wearable name
     */
    private void setDevice(BluetoothDevice device) {
        Wearable.device = device;
    }

    /**
     * Clear Wearable device
     */
    private static void clearDevice() {
        Wearable.device = null;
    }

    /**
     * Get the Wearable name
     * @return name
     */
    public static BluetoothDevice getDevice() {
        return Wearable.device;
    }

    /**
     * Set context
     * @param context application context
     */
    private void setContext(Context context) {
        Wearable.context = context;
    }

    /**
     * Get context
     * @return context
     */
    public static Context getContext() {
        return Wearable.context;
    }

    /**
     * Set if the wearable is connected
     * @param connected if the wearable is connected
     */
    private static void setConnected(Boolean connected) {
        Wearable.connected = connected;
    }

    /**
     * Check if the Wearable is connected
     * @return boolean
     */
    public Boolean isConnected() {
        return Wearable.connected;
    }

    /**
     * Disconnect from BLE device
     */
    public static void disconnect(BluetoothGatt ble) {
        ble.disconnect();
        Wearable.clearDevice();
        Wearable.setConnected(false);
        getListener().disconnected();
    }

    /**
     * Turn the RGB led off
     */
    public void ledOFF() {
        this.sendCommand(this.cmd.LED_OFF);
    }

    /**
     * Turn the RGB led on - default to GREEN
     * @param color the color of the led to be turned on - RED, GREEN, BLUE - default to GREEN
     */
    public void ledON(String color) {
        switch (color.toLowerCase()) {
            case "red":
                this.sendCommand(this.cmd.LED_RED_HIGH);
                break;

            case "green":
                this.sendCommand(this.cmd.LED_GREEN_HIGH);
                break;

            case "blue":
                this.sendCommand(this.cmd.LED_BLUE_HIGH);
                break;

            default:
                this.sendCommand(this.cmd.LED_GREEN_HIGH);
                break;
        }
    }

    /**
     * Set a custom color to the RGB LED
     * @param color the color of the led to be turned on - RED, GREEN, BLUE - default to GREEN
     * @param val the value between 0 - 255
     */
    public void ledSetTo(String color, int val) {
        this.sendCommand(this.cmd.LED_CUSTOM(color, val));
    }

    /**
     * Play Melody
     */
    public void playMelody(String melody) {
        switch (melody.toLowerCase()) {
            case "mario":
                this.sendCommand(this.cmd.MELODY_MARIO);
                break;

            case "natal":
                this.sendCommand(this.cmd.MELODY_CHRISTMAS);
                break;

            case "imperial":
                this.sendCommand(this.cmd.MELODY_IMPERIAL);
                break;

            default:
                this.sendCommand(this.cmd.MELODY_MARIO);
                break;
        }
    }

    /**
    * Check if BLE is enabled and turned on in the phone
    */
    public void checkBLE() {
        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_LONG);

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast.setText("BLE not supported on this device");
            toast.show();
        }

        final BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, 1);
            toast.setText("Turn on the device bluetooth");
            toast.show();
        }
    }

    /**
     * Start looking for the BLE Wearable
     */
    public void findWearable() {
        if (getDevice() == null) {
            if(this.mTimer != null) {
                this.mTimer.cancel();
            }

            this.scanLeDevice();

            this.mTimer = new Timer();
            this.mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (getDevice() == null) {
                        getListener().notFound();

                    } else {
                        connect();
                    }
                }
            }, SCAN_PERIOD);

        } else {
            Toast.makeText(getContext(), "Wearable " + getName() + " is connected", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Connect to the Wearable device
     */
    private void connect() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(getDevice().getAddress());

        if (device == null) {
            logMessage("Unable to connect");
            Wearable.disconnect(bluetoothGatt);
            return;
        }

        bluetoothGatt = device.connectGatt(getContext(), false, gattCallback);
    }

    /**
     * Callback for the Bluetooth Gatt connect
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Wearable.setConnected(true);
                bluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Wearable.disconnect(bluetoothGatt);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> gattServices = bluetoothGatt.getServices();

                for (BluetoothGattService gattService : gattServices) {
                    if (KIT_SERVICE.equals(gattService.getUuid().toString())) {
                        bluetoothGattService = gattService;
                        getListener().connected(getDevice());
                    }
                }
            }
        }
    };

    /**
     * Send command to the Wearable device
     * @param command device command to the Wearable
     */
    public void sendCommand(String command) {
        if (bluetoothGattService == null) {
            return;
        }

        //Get GATT characteristic to send the command
        BluetoothGattCharacteristic gattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(KIT_TX));

        if (gattCharacteristic == null) {
            return;
        }

        //Transform the command to bytes
        byte[] temp = command.getBytes();
        byte[] tx = new byte[temp.length + 1];
        tx[0] = 0x00;

        System.arraycopy(temp, 0, tx, 1, temp.length);

        gattCharacteristic.setValue(tx);
        bluetoothGatt.writeCharacteristic(gattCharacteristic);
    }

    /**
     * Scan for the BLE device
     */
    private void scanLeDevice() {
        new Thread() {
            @Override
            public void run() {
                bluetoothAdapter.startLeScan(bleScanCallback);

                try {
                    Thread.sleep(SCAN_PERIOD);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                bluetoothAdapter.stopLeScan(bleScanCallback);
            }
        }.start();
    }

    /**
     * Callback for the BLE scan
     */
    private BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
        if (device != null) {
            if (Wearable.getName().equals(device.getName())) {
                setDevice(device);
            }
        }
        }
    };

    /**
     * Parcelable methods
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Wearable> CREATOR = new Parcelable.Creator<Wearable>() {
        public Wearable createFromParcel(Parcel in) {
            return new Wearable(in);
        }

        public Wearable[] newArray(int size) {
            return new Wearable[size];
        }
    };

    private Wearable(Parcel in) {
        mData = in.readInt();
    }

    /**
     * Log messages
     * @param message message to be logged
     */
    private void logMessage(String message) {
        Log.w("BLE", message);
    }
}
