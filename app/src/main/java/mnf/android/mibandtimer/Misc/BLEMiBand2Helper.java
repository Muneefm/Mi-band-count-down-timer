package mnf.android.mibandtimer.Misc;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import mnf.android.mibandtimer.AppController;
import mnf.android.mibandtimer.PreferenceHandler;

/**
 * Created by marmou on 12/06/2017.
 */

public class BLEMiBand2Helper extends Service {

    public static final String TAG = "Mario-MI-2";

    private Context myContext = null;
    private Handler myHandler = null;

    private BluetoothDevice activeDevice = null; // The mi band
    private boolean isConnectedToGatt = false; // the gatt connection
    private BluetoothGatt myGatBand = null;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicAlerta;

    byte notif;
    byte alert;
    String segundoSms;
    String tercerSms;
    byte[] mensaje;
    byte [] parametros;
    byte[] bytes;



    public BLEMiBand2Helper() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public BLEMiBand2Helper(Context context, Handler handler) {
        myContext = context;
        myHandler = handler;
    }




    public boolean isConnected() {
        return isConnectedToGatt;
    }

    /* =========  Handling Initializing  ============== */

    /**
     * Se conecta con la miBand por bluetoothLowWEnergy
     */

    public void connect() {

        Log.e("MiBand", "on Connect function called");



        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return;
        }


        PreferenceHandler pref  = new PreferenceHandler(AppController.getInstance());
        isConnectedToGatt = false;

        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {

            activeDevice = mBluetoothAdapter.getRemoteDevice(pref.getMacAddress());
        } catch (Exception e) {
            activeDevice = null;
            e.printStackTrace();
        }
        if (activeDevice != null) {
            if (!connectGatt()) {
                isConnectedToGatt = false;
                Log.w(TAG, "No se puede conectar con mi Band 2");
                Log.e("MiBand", "Gatt not  connected");

            }else{
                Log.e("MiBand", "Gatt  connected");

            }
        }
    }

    /**
     * se conecta con la pulsera
     * @return el método connect
     */
    public boolean connectGatt() {
        if (activeDevice == null)
            return false;

        // closeGatt();//older gatt??
        myGatBand = activeDevice.connectGatt(myContext, false, myGattCallback);//false

        if (myGatBand == null)
            return false;

        return myGatBand.connect();
    }

    /**
     * Desconexión de la MiBand
     */
    public void DisconnectGatt() {
        if (myGatBand != null && isConnectedToGatt) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    myGatBand.disconnect();
                    myGatBand.close();
                    myGatBand = null;
                    isConnectedToGatt = false;
                }
            });
        }
    }

    private BluetoothGattCallback myGattCallback = new BluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(Config.TAG_BAND, "onServicesDiscovered callback");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Servicio y caracteristica necessaria para el enviio de texto
                characteristic = gatt.getService(Consts.UUID_SERVICE_1811).getCharacteristic(Consts.UUID_CHARACTERISTIC_2A46);
                //servicio para alertar de la notificación
                characteristicAlerta=gatt.getService(Consts.UUID_SERVICE_1802).getCharacteristic(Consts.UUID_CHARACTERISTIC_2A06);

                isConnectedToGatt = true;
            }
            Log.d(TAG, "Service discovered with status " + status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(Config.TAG_BAND, "onConnectionStateChange callback");

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(TAG, "Gatt state: connected");
                    gatt.discoverServices();
                    isConnectedToGatt = true;
                    raiseonConnect();
                    break;
                default:
                    Log.d(TAG, "Gatt state: not connected");
                    isConnectedToGatt = false;
                    raiseonDisconnect();
                    break;
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Config.TAG_BAND, "onCharacteristicWrite callback");

            Log.d(TAG, "Write successful: " + Arrays.toString(characteristic.getValue()));
            raiseonWrite(gatt, characteristic, status);
            super.onCharacteristicWrite(gatt, characteristic, status);


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Config.TAG_BAND, "onCharacteristicRead callback");

            Log.d(TAG, "Read successful: " + Arrays.toString(characteristic.getValue()));
            raiseonRead(gatt, characteristic, status);
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(Config.TAG_BAND, "onCharacteristicChanged callback");

            Log.d(TAG, "Button press");


            Log.d(TAG, " - Notification UUID: " + characteristic.getUuid().toString());
            Log.d(TAG, " - Notification value: " + Arrays.toString(characteristic.getValue()));
            raiseonNotification(gatt, characteristic);
            super.onCharacteristicChanged(gatt, characteristic);
        }


    };

    /**
     * Envia texto como llamada
     * @param value mensaje
     */
    public void sendCall(String value){
        if (!isConnectedToGatt) {
            connect();
        }
        try {
            byte notif=Consts.llamada;
            byte alert=Consts.alert1;


            byte [] parametros=new byte[]{notif,alert};
            byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);

            byte[] mensaje= unirBytes(parametros,bytes);

            characteristic.setValue(mensaje);
            myGatBand.writeCharacteristic(characteristic);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  Envía texto a la miBand desde la aplicación
     * @param value mensaje para enviar a la miBand
     */
    public void sendSms(String value) {
        if (!isConnectedToGatt) {
            connect();
        }
        try {
            notif=Consts.mensaje;
            alert=Consts.alert1;

            parametros=new byte[]{notif,alert};
            bytes = value.getBytes(StandardCharsets.US_ASCII);
            mensaje= unirBytes(parametros,bytes);

            characteristic.setValue(mensaje);
            Log.e(Config.TAG_BAND,"character = "+characteristic);
            myGatBand.writeCharacteristic(characteristic);



            /********
             * EJEMPLO: ESTO ENVIA LA PALABRA TEST CON EL ICONO DE SMS SIEMPRE QUE EL SERVICIO SEA 1811 Y LA CARACTERISRICA 2a46.
             * characteristic.setValue(new byte[]{5,1,84,101,115,116});
             *                                        T   e   s   t
             *                                        5=icono de sms
             *                                        1=nivel de alerta
             * myGatBand.writeCharacteristic(characteristic);
             *********/


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface BLEAction {
        void onDisconnect();

        void onConnect();

        void onRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);


    }



    /* =========  Handling Events  ============== */

    private ArrayList<BLEAction> listeners = new ArrayList<BLEAction>();

    public void addListener(BLEAction toAdd) {
        listeners.add(toAdd);
    }

    public void removeListener(BLEAction toDel) {
        listeners.remove(toDel);
    }

    public void raiseonNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        // Notify everybody that may be interested.
        for (BLEAction listener : listeners)
            listener.onNotification(gatt, characteristic);
    }

    public void raiseonRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        // Notify everybody that may be interested.
        for (BLEAction listener : listeners)
            listener.onRead(gatt, characteristic, status);
    }

    public void raiseonWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        // Notify everybody that may be interested.
        for (BLEAction listener : listeners)
            listener.onWrite(gatt, characteristic, status);
    }

    public void raiseonDisconnect() {
        // Notify everybody that may be interested.
        for (BLEAction listener : listeners)
            listener.onDisconnect();
    }

    public void raiseonConnect() {
        // Notify everybody that may be interested.
        for (BLEAction listener : listeners)
            listener.onConnect();



    }


    /* =========  Handling Data  ============== */

    public void readData(UUID service, UUID Characteristics) {
        if (!isConnectedToGatt || myGatBand == null) {
            Log.d(TAG, "Cant read from BLE, not initialized.");
            return;
        }

        Log.d(TAG, "* Getting gatt service, UUID:" + service.toString());
        BluetoothGattService myGatService =
                myGatBand.getService(service /*Consts.UUID_SERVICE_GENERIC*/);
        if (myGatService != null) {
            Log.d(TAG, "* Getting gatt Characteristic. UUID: " + Characteristics.toString());

            BluetoothGattCharacteristic myGatChar
                    = myGatService.getCharacteristic(Characteristics /*Consts.UUID_CHARACTERISTIC_DEVICE_NAME*/);
            if (myGatChar != null) {
                Log.d(TAG, "* Reading data");

                boolean status = myGatBand.readCharacteristic(myGatChar);
                Log.d(TAG, "* Read status :" + status);
            }
        }
    }

    public void writeData(UUID service, UUID Characteristics, byte[] data) {
        if (!isConnectedToGatt || myGatBand == null) {
            Log.d(TAG, "Cant read from BLE, not initialized.");
            return;
        }

        Log.d(TAG, "* Getting gatt service, UUID:" + service.toString());
        BluetoothGattService myGatService =
                myGatBand.getService(service /*Consts.UUID_SERVICE_HEARTBEAT*/);
        if (myGatService != null) {
            Log.d(TAG, "* Getting gatt Characteristic. UUID: " + Characteristics.toString());

            BluetoothGattCharacteristic myGatChar
                    = myGatService.getCharacteristic(Characteristics /*Consts.UUID_START_HEARTRATE_CONTROL_POINT*/);
            if (myGatChar != null) {
                Log.d(TAG, "* Writing trigger");
                myGatChar.setValue(data /*Consts.BYTE_NEW_HEART_RATE_SCAN*/);

                boolean status = myGatBand.writeCharacteristic(myGatChar);
                Log.d(TAG, "* Writting trigger status :" + status);
            }
        }
    }

    public void getNotifications(UUID service, UUID Characteristics) {
        if (!isConnectedToGatt || myGatBand == null) {
            Log.d(TAG, "Cant get notifications from BLE, not initialized.");
            return;
        }

        Log.d(TAG, "* Getting gatt service, UUID:" + service.toString());
        BluetoothGattService myGatService =
                myGatBand.getService(service/*Consts.UUID_SERVICE_MIBAND_SERVICE*/);
        if (myGatService != null) {
            Log.d(TAG, "* Getting gatt Characteristic. UUID: " + Characteristics.toString());

            BluetoothGattCharacteristic myGatChar= myGatService.getCharacteristic(Characteristics/*Consts.UUID_BUTTON_TOUCH*/);
            if (myGatChar != null) {
                Log.d(TAG, "* Statring listening");

                // second parametes is for starting\stopping the listener.
                boolean status = myGatBand.setCharacteristicNotification(myGatChar, true);
                Log.d(TAG, "* Set notification status :" + status);
            }
        }else{
            Log.e("MiBand", "myGatChar is null = "+myGatBand);

        }
    }

    /**
     * Get notification but also set descriptor to Enable notification. You need to wait couple of
     * seconds before you could use it (at least in the mi band 2)
     *
     * @param service
     * @param Characteristics
     */
    public void getNotificationsWithDescriptor(UUID service, UUID Characteristics, UUID Descriptor) {


        Log.e("MiBand", "getNotificationsWithDescriptor");

        if (!isConnectedToGatt || myGatBand == null) {
            Log.d(TAG, "Cant get notifications from BLE, not initialized.");
            return;
        }

        Log.d(TAG, "* Getting gatt service, UUID:" + service.toString());
        BluetoothGattService myGatService =
                myGatBand.getService(service/*Consts.UUID_SERVICE_MIBAND_SERVICE*/);
        if (myGatService != null) {
            Log.d(TAG, "* Getting gatt Characteristic. UUID: " + Characteristics.toString());

            BluetoothGattCharacteristic myGatChar
                    = myGatService.getCharacteristic(Characteristics/*Consts.UUID_BUTTON_TOUCH*/);
            if (myGatChar != null) {
                Log.e("MiBand", "myGatChar is not null");

                Log.d(TAG, "* Statring listening");

                // second parametes is for starting\stopping the listener.
                boolean status = myGatBand.setCharacteristicNotification(myGatChar, true);
                Log.d(TAG, "* Set notification status :" + status);

                BluetoothGattDescriptor myDescriptor
                        = myGatChar.getDescriptor(Descriptor/*Consts.UUID_DESCRIPTOR_UPDATE_NOTIFICATION*/);
                if (myDescriptor != null) {
                    Log.d(TAG, "Writing decriptor: " + Descriptor.toString());
                    myDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    status = myGatBand.writeDescriptor(myDescriptor);
                    Log.d(TAG, "Writing decriptors result: " + status);
                }
            }
        }else{
            Log.e("MiBand", "myGatChar is null");

        }
    }



    /**
     * Alerta en forma de icono de sms para avisar de que va a llegar un sms
     */

    public void alerta(){
        characteristicAlerta.setValue(new byte []{1,1});
        myGatBand.writeCharacteristic(characteristicAlerta);
    }

    /**
     * une los bytes estaticos con los del sms
     * @param a categoria y notificacion
     * @param b mensaje
     * @return
     */
    public byte[] unirBytes(byte[] a,byte[] b){

        byte[] mensaje= new byte[a.length+b.length];
        System.arraycopy(a,0,mensaje,0,a.length);
        System.arraycopy(b,0,mensaje,a.length,b.length);

        return mensaje;
    }



}