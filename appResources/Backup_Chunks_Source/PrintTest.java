package com.armavi_bsd.robotispreconstructed_romsNetwork;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.armavi_bsd.robotispreconstructed_romsNetwork.navigationEndState.NavigationState;
import com.armavi_bsd.robotispreconstructed_romsNetwork.util.Intentkey;
import com.armavi_bsd.robotispreconstructed_romsNetwork.util.Pref;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class PrintTest extends AppCompatActivity {

    Button printBtn;
    TextView nameTx, designationTx, status;
    TextView dispPrintDate, dispName, dispID, dispPhone,
            dispAddress, dispPackage, dispPackageBill, dispPaidBill,
            dispDueBill, dispCDate, dispRecivedBy;

    ImageView dispCompanyIcon;

    SharedPreferences sharedPreferences;
    Pref pref = new Pref();
    Intentkey intentkey = new Intentkey();
    Handler handlerPrinterAddress = new Handler();

    NavigationState navigationState = new NavigationState();

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

    String printAgentName, printTotalDues, printAgentAddress,
            printAgentPackage, printPaidBill, printReceiverBy,
            printAgentPhone, printDate, printCusID, printPackageBill;

    Bitmap bitmap;
    BitmapDrawable drawable;
    byte[] imageData;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_print_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //UI Declaration
        printBtn = (Button) findViewById(R.id.btn_printer_test);
        nameTx = (TextView) findViewById(R.id.nameTx);
        designationTx = (TextView) findViewById(R.id.designationTx);
        status = (TextView) findViewById(R.id.status);
        ////////////////


        sharedPreferences = getSharedPreferences(pref.getPrefUserCred(), MODE_PRIVATE);
        PRINTER_ADDRESS = sharedPreferences.getString(pref.getPrefPrintingDeviceMAC(), "");

        //Intent values
        printAgentName = getIntent().getExtras().getString(intentkey.getPosAgentNameIntentKey());
        printTotalDues = getIntent().getExtras().getString(intentkey.getPosAgentTotalDueIntentKey());
        printCusID = getIntent().getExtras().getString(intentkey.getPosCusIDIntentKey());
        printAgentAddress = getIntent().getExtras().getString(intentkey.getPosAgentAddressIntentKey());
        printAgentPhone = getIntent().getExtras().getString(intentkey.getPosAgentMobileNoIntentKey());
        printPaidBill = getIntent().getExtras().getString(intentkey.getPosAmountIntentKey());
        printAgentPackage = getIntent().getExtras().getString(intentkey.getPosMBIntentKey());
        printDate = getIntent().getExtras().getString(intentkey.getPosDateIntentKey());
        printReceiverBy = getIntent().getExtras().getString(intentkey.getPosEntryUserNameIntentKey());
        printPackageBill = getIntent().getExtras().getString(intentkey.getPosPackageBill());
        /////////

        //Receipt value display
        dispPrintDate = (TextView) findViewById(R.id.dispPrintDate);
        dispPrintDate.setText(printDate);

        dispName = (TextView) findViewById(R.id.dispName);
        dispName.setText(printAgentName);

        dispID = (TextView) findViewById(R.id.dispID);
        dispID.setText(printCusID);

        dispPhone = (TextView) findViewById(R.id.dispPhone);
        dispPhone.setText(printAgentPhone);

        dispAddress = (TextView) findViewById(R.id.dispAddress);
        dispAddress.setText(printAgentAddress);

        dispPackage = (TextView) findViewById(R.id.dispPackage);
        dispPackage.setText(printAgentPackage);

        dispPackageBill = (TextView) findViewById(R.id.dispPackageBill);
        dispPackageBill.setText(printPackageBill);

        dispPaidBill = (TextView) findViewById(R.id.dispPaidBill);
        dispPaidBill.setText(printPaidBill);

        dispDueBill = (TextView) findViewById(R.id.dispDueBill);
        dispDueBill.setText(printTotalDues);

        dispCDate = (TextView) findViewById(R.id.dispCDate);
        dispCDate.setText(printDate);

        dispRecivedBy = (TextView) findViewById(R.id.dispRecivedBy);
        dispRecivedBy.setText(printReceiverBy);
        //////////////////////

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Bluetooth is not supported
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToPrinter();
        }

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // If the TextView is still empty, start ActivityTwo again
//                if(PRINTER_ADDRESS.trim().equals("")){
////                    status.setText("Printer address is not found");
//                    Toast.makeText(getApplicationContext(), "Select Printer from <Bluetooth Devices> page", Toast.LENGTH_SHORT).show();
//                    navigationState.navigateToActivity(getApplicationContext(), BluetoothDeviceList.class);
//                }else if(!PRINTER_ADDRESS.trim().equals("")){
////                    connectToPrinter();
//                    status.setText("Printer Address has established successfully");
//                }
//            }
//        }, 1000);

        printBtn.setOnClickListener(new View.OnClickListener() {
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

    // Method to connect to the printer
//    private void connectToPrinter() {
//        if (PRINTER_ADDRESS != null) {
//            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(PRINTER_ADDRESS);
//            mBluetoothConnectProgressDialog = ProgressDialog.show(this, "Connecting...", "Connecting to printer...", true, false);
//
//            new Thread(this::connectPrinter).start();
//        } else {
//            Toast.makeText(this, "Printer address is not found", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void connectToPrinter() {
        if (PRINTER_ADDRESS != "") {
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(PRINTER_ADDRESS);
            mBluetoothConnectProgressDialog = ProgressDialog.show(this, "Connecting...", "Connecting to printer...", true, false);
            new Thread(this::connectPrinter).start();
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
            mHandler.sendEmptyMessage(0);
        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
            mHandler.sendEmptyMessage(1); // Failure
        }
    }

    // Handler to update UI after connection
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            if (msg.what == 0) {
                status.setText("Connected");
                printTest();
            } else {
                status.setText("Connection Failed");
                status.setTextColor(Color.RED);
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


                String nameVal = printAgentName + "\n";
                String dateVal = printDate + "\n";
                String customerIDVal = printCusID + "\n";
                String phoneVal = printAgentPhone + "\n";
                String addressVal = printAgentAddress + "\n";
                String packageVal = printAgentPackage + "\n";
                String paidBillVal = printPaidBill + "\n";
                String dueBillVal = printTotalDues + "\n";
                String receivedByVal = printReceiverBy + "\n";
                String packageBillVal = printPackageBill + "\n";

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


    //Image print///

    ///////////////

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