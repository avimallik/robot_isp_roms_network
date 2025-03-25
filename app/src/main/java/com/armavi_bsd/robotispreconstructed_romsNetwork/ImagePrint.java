package com.armavi_bsd.robotispreconstructed_romsNetwork;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.armavi_bsd.robotispreconstructed_romsNetwork.util.Pref;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ImagePrint extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Pref pref = new Pref();

    protected static final String TAG = "PrintTest";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    String PRINTER_ADDRESS;
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;

    Button testPrintBtn;
    ImageView testCompanyLogo;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_print);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(pref.getPrefUserCred(), MODE_PRIVATE);
        PRINTER_ADDRESS = sharedPreferences.getString(pref.getPrefPrintingDeviceMAC(), "");

        testPrintBtn = (Button) findViewById(R.id.testPrintBtn);
        testCompanyLogo = (ImageView) findViewById(R.id.testCompanyLogo);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Bluetooth is not supported
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        testPrintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                } else {
                    connectToPrinter();
                }
            }
        });
    }

    private void connectToPrinter() {
        if (PRINTER_ADDRESS != "") {
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(PRINTER_ADDRESS);
            mBluetoothConnectProgressDialog = ProgressDialog.show(this, "Connecting...", "Connecting to printer...", true, false);

        } else if(PRINTER_ADDRESS.trim().equals("")){
            Toast.makeText(this, "Printer address is not found, Select Printer first from Admin Panel!", Toast.LENGTH_SHORT).show();
//            navigationState.navigateToActivity(getApplicationContext(), BluetoothDeviceList.class);
        }
    }

    // Thread to connect to Bluetooth printer
    @SuppressLint("MissingPermission")
    private void connectPrinter() {
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();

        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
            mHandler.sendEmptyMessage(1); // Failure
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            if (msg.what == 0) {
                Log.e(TAG, "Connected");
                printTest();
            } else {
                Log.e(TAG, "Connection failed");
            }
        }
    };

    // Method to print test data
    public void printTest() {
        if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
            try {

                ///Image print///

                /////////////////

                OutputStream os = mBluetoothSocket.getOutputStream();

                //Headers
//                String header = "        Mega Speed Net\n";
                String header = "          Roms Network\n";
                String companyAddress = "New Jail Road Maijdee Sadar Noakhali.\n";
                String companyMobile = "Mobile: 01975321087, 01725733882\n";
                String companyWeb = "      Web:romsnetwork.com/\n";
                String companyFacebook = "    facebook.com/romsnetwork\n";
                String blank = "\n\n";

                String underLine = "********************************\n\n";
                String underLineHifen = "--------------------------------";
                String vio = "================================\n";

                //Titles
                String nameTitle = "Name: ";
                String dateTitle = "Print Date:";
                String idTitle = "ID: ";
                String phoneTitle = "Phone: ";
                String addressTitle = "Address: ";
                String packageTitle = "Package: ";
                String packageBillTitle = "Package Bill: ";
                String paidBillTitle = "Paid Bill: ";
                String dueBillTitle = "Due Bill: ";
                String cDateTitle = "C.Date: ";
                String receivedByTitle = "Received by: ";


                String nameVal = "Arunav Mallik" + "\n";
                String dateVal = "27.7.2025" + "\n";
                String customerIDVal = "AUS222" + "\n";
                String phoneVal = "01956273133" + "\n";
                String addressVal = "Surma-37, R/A" + "\n";
                String packageVal = "1000 MBPS" + "\n";
                String paidBillVal = "1000" + "\n";
                String dueBillVal = "0" + "\n";
                String receivedByVal = "Arm Avi" + "\n";
                String packageBillVal = "100" + "\n";

                //blank
                os.write(blank.getBytes());

                //Logo
//                os.write(imageData);

                //Header
                os.write(header.getBytes());

                //Company address
                os.write(companyAddress.getBytes());
                os.write(companyMobile.getBytes());
                os.write(companyWeb.getBytes());
                os.write(companyFacebook.getBytes());

                //Underline
                os.write(underLine.getBytes());

                //Date
                os.write(dateTitle.getBytes());
                os.write(dateVal.getBytes());

                //name
                os.write(nameTitle.getBytes());
                os.write(nameVal.getBytes());

                //Customer ID
                os.write(idTitle.getBytes());
                os.write(customerIDVal.getBytes());

                //Phone
                os.write(phoneTitle.getBytes());
                os.write(phoneVal.getBytes());

                //Address
                os.write(addressTitle.getBytes());
                os.write(addressVal.getBytes());

//                os.write(blank.getBytes());

                //Package
                os.write(packageTitle.getBytes());
                os.write(packageVal.getBytes());

                //Package bill
                os.write(packageBillTitle.getBytes());
                os.write(packageBillVal.getBytes());

//                os.write(blank.getBytes());

                //Paid bill
                os.write(paidBillTitle.getBytes());
                os.write(paidBillVal.getBytes());

                //Due bill
                os.write(dueBillTitle.getBytes());
                os.write(dueBillVal.getBytes());

                //c.data
                os.write(cDateTitle.getBytes());
                os.write(dateVal.getBytes());

                //Received by
                os.write(receivedByTitle.getBytes());
                os.write(receivedByVal.getBytes());

                os.write(blank.getBytes());

                os.write(underLineHifen.getBytes());
                os.write(blank.getBytes());

                // Paper settings (height and width)
                os.write(intToByteArray(29)); // GS
                os.write(intToByteArray(150)); // Height
                os.write(intToByteArray(170)); // Height

                os.write(intToByteArray(29)); // GS
                os.write(intToByteArray(119)); // Width
                os.write(intToByteArray(2)); // Width

            } catch (IOException e) {
                Log.e(TAG, "Error during printing", e);
            }
        } else {
            Toast.makeText(this, "Printer not connected", Toast.LENGTH_SHORT).show();
        }
    }

    public static byte[] intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();
        return new byte[]{b[3]};
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }
    }


}