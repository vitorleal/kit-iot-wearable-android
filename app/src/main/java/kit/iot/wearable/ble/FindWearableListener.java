package kit.iot.wearable.ble;

import android.bluetooth.BluetoothDevice;

public interface FindWearableListener {
    public void connected(BluetoothDevice device);
    public void disconnected();
    public void notFound();
}