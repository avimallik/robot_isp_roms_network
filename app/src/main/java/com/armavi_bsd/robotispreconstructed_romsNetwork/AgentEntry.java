package com.armavi_bsd.robotispreconstructed_romsNetwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.armavi_bsd.robotispreconstructed_romsNetwork.adapter.AreaAdapter;
import com.armavi_bsd.robotispreconstructed_romsNetwork.adapter.BillingPersonAdapter;
import com.armavi_bsd.robotispreconstructed_romsNetwork.adapter.PackageAdapter;
import com.armavi_bsd.robotispreconstructed_romsNetwork.adapter.SubZoneAdapter;
import com.armavi_bsd.robotispreconstructed_romsNetwork.adapter.ZoneAdapter;
import com.armavi_bsd.robotispreconstructed_romsNetwork.databinding.ActivityAgentEntryBinding;
import com.armavi_bsd.robotispreconstructed_romsNetwork.dialogs.SuccessDialog;
import com.armavi_bsd.robotispreconstructed_romsNetwork.dialogs.SuccessDialogNewAgentEntry;
import com.armavi_bsd.robotispreconstructed_romsNetwork.dialogs.WarningDialog;
import com.armavi_bsd.robotispreconstructed_romsNetwork.model.AreaModel;
import com.armavi_bsd.robotispreconstructed_romsNetwork.model.BillingPersonModel;
import com.armavi_bsd.robotispreconstructed_romsNetwork.model.PackageModel;
import com.armavi_bsd.robotispreconstructed_romsNetwork.model.SubzoneModel;
import com.armavi_bsd.robotispreconstructed_romsNetwork.model.ZoneModel;
import com.armavi_bsd.robotispreconstructed_romsNetwork.navigationEndState.NavigationWithEndState;
import com.armavi_bsd.robotispreconstructed_romsNetwork.mikrotikStatusChecker.MikrotikStatusChecker;
import com.armavi_bsd.robotispreconstructed_romsNetwork.urlStorage.URLStorage;
import com.armavi_bsd.robotispreconstructed_romsNetwork.util.Intentkey;
import com.armavi_bsd.robotispreconstructed_romsNetwork.util.Pref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgentEntry extends AppCompatActivity {

    URLStorage urlStorage = new URLStorage();
    NavigationWithEndState navigationWithEndState;
    private Handler handler = new Handler();
    private MikrotikStatusChecker mikrotikStatusChecker;
    private Runnable mikrotikStatusUpdater;
    private ActivityAgentEntryBinding binding;
    Pref pref = new Pref();
    Intentkey intentkey = new Intentkey();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String userID, mikrotikIP, mikrotikID;
    String smsCheckTemp = "1";
    private ZoneAdapter adapter;
    private SubZoneAdapter adapterSubZone;
    private AreaAdapter adapterAreaAdapter;
    private PackageAdapter adapterPackage;
    private BillingPersonAdapter adapterBillingPerson;
    private List<ZoneModel> zoneList = new ArrayList<>();
    private List<SubzoneModel> subZoneList = new ArrayList<>();
    private List<AreaModel> areaList = new ArrayList<>();
    private List<PackageModel> packageList = new ArrayList<>();
    private List<BillingPersonModel> billingPersonList = new ArrayList<>();
    ProgressDialog progressDialog;
    String subZoneCount, zoneName, zoneId;
    String subZoneName, subZoneId, areaCount;
    String areaId, areaName;
    String packageName, netSpeed, billAmount;
    String billngPersonName, billingPersonId;
    String clientType, connectionType, clientStatus, gender, effectedVal;
    int subZoneCountNumber = 0, areaCountNumber;
    String subZoneListUrl, arealistUrl;

    String connectionTypeText;
    String[] connectionTypeTitle = {"Cat5e", "Optical Fiber", "Onu"};
    String[] connectionTypeValues = {"cat5e", "Fiber", "onu"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAgentEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Navigation to intent
        navigationWithEndState = new NavigationWithEndState();
        //SharedPreference task
        sharedPreferences = getSharedPreferences(pref.getPrefUserCred(),MODE_PRIVATE);
        editor = sharedPreferences.edit();

        userID = sharedPreferences.getString(pref.getPrefUserID(), "");
        mikrotikIP = sharedPreferences.getString(pref.getPrefMikrotikIP(),"");
        mikrotikID = sharedPreferences.getString(pref.getPrefMikrotikID(), "");

        binding.zoneSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchZone();
            }
        });

        binding.areaSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchArea();
            }
        });

        binding.subZoneSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchSubZone();
            }
        });

        binding.packageMbSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchPackage();
            }
        });

        binding.billingPersonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchBillingPerson();
            }
        });

        binding.datePickerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.spinnerConnectionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentClientType, View view, int positionConnectionType, long id) {
//                connectionType = parentClientType.getItemAtPosition(positionConnectionType).toString();
                connectionType = connectionTypeValues[positionConnectionType];
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.checkboxConnectionCharge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    binding.dueInputConnectionCharge.setVisibility(View.VISIBLE);
                }else{
                    binding.dueInputConnectionCharge.setVisibility(View.INVISIBLE);
                    binding.dueInputConnectionCharge.setText("");
                }
            }
        });

        binding.checkboxRunningMonthDue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    binding.dueInputRunningMonth.setVisibility(View.VISIBLE);
                }else {
                    binding.dueInputRunningMonth.setVisibility(View.INVISIBLE);
                    binding.dueInputRunningMonth.setText("");
                }
            }
        });

        binding.smsCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    smsCheckTemp = "1";
                }else {
                    smsCheckTemp = "0";
                }
            }
        });

        ////////Mikrotik Status checker////////
//        mikrotikStatusChecker = new MikrotikStatusChecker(this);
//        mikrotikStatusUpdater = new Runnable() {
//            @Override
//            public void run() {
//                mikrotikStatusChecker.fetchStatus(status -> {
//                    if (status == 1) {
//                        binding.mikrotikStatusTitle.setText("Mikrotik Connected");
//                        binding.mikrotikInfoContainer.setVisibility(View.VISIBLE);
//                        binding.queuePasswordEdi.setVisibility(View.VISIBLE);
//                        binding.mikrotikStatusTxt.setText(String.valueOf(status));
//                        binding.mikrotikStatusTitle.setBackgroundResource(R.drawable.green_back);
//                    } else if (status == 0) {
//                        binding.queuePasswordEdi.setText("");
//                        binding.mikrotikStatusTitle.setText("Mikrotik Disconnected");
//                        binding.mikrotikInfoContainer.setVisibility(View.GONE);
//                        binding.queuePasswordEdi.setVisibility(View.GONE);
//                        binding.mikrotikStatusTxt.setText(String.valueOf(status));
//                        binding.mikrotikStatusTitle.setBackgroundResource(R.drawable.red_back);
//                    } else {
//                        binding.mikrotikStatusTitle.setText("Error Fetching Status");
//                    }
//                });
//
//                // Re-run after 1 second
//                handler.postDelayed(this, 1000);
//            }
//        };
//        handler.post(mikrotikStatusUpdater);
        ///////////////////////////////////////

        binding.clenAgentEntryInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.remove(pref.getPrefMikrotikIP());
                editor.remove(pref.getPrefMikrotikCheckStatus());
                editor.remove(pref.getPrefMikrotikID());
                editor.apply();
                navigationWithEndState.navigateToActivity(AgentEntry.this, AdminPanel.class);
            }
        });

        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentStatus, View view, int positionStatus, long id) {
                if(positionStatus == 0){
                    clientStatus = "1";
                }else if(positionStatus == 1){
                    clientStatus = "0";
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

//        if(binding.customerName.getText().toString().isEmpty()){
//            WarningDialog.warningDialog(AgentEntry.this, "Some field is empty");
//        } else if(binding.mobileNumber.getText().toString().isEmpty()){
//            WarningDialog.warningDialog(AgentEntry.this, "Some field is empty");
//        } else if (binding.otherPhone.getText().toString().isEmpty()) {
//            WarningDialog.warningDialog(AgentEntry.this, "Some field is empty");
//        } else if (binding.datePickerText.getText().toString().isEmpty()) {
//            WarningDialog.warningDialog(AgentEntry.this, "Some field is empty");
//        } else if (binding.packageMbSelect.getText().toString().isEmpty()) {
//            WarningDialog.warningDialog(AgentEntry.this, "Some field is empty");
//        } else if (binding.mikrotikDisconnectEdi.getText().toString().isEmpty()) {
//            WarningDialog.warningDialog(AgentEntry.this, "Some field is empty");
//        } else if (binding.zoneSelect.getText().toString().isEmpty()) {
//            WarningDialog.warningDialog(AgentEntry.this, "Some field is empty");
//        } else if (binding.billingPersonIdText.getText().toString().equals("0")) {
//            WarningDialog.warningDialog(AgentEntry.this, "Some field is empty");
//        } else{
//            submitNewAgent();
//        }

        binding.addCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                submitNewAgent();
//                Toast.makeText(getApplicationContext(), connectionType, Toast.LENGTH_SHORT).show();
                fieldValidation();
//                Toast.makeText(getApplicationContext(), smsCheckTemp, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void submitNewAgent() {

        progressDialog = new ProgressDialog(AgentEntry.this);
        progressDialog.setMessage("Submitting New Customer!");
        progressDialog.setCancelable(true); // Allow cancel on back press
        progressDialog.show();

//        String urlAgentEntry = "http://192.168.0.122/roms/rest_api_mob_dx/robotispagententry.php";
        String urlAgentEntry = urlStorage.getHttpStd()
                + urlStorage.getBaseUrl()
                + urlStorage.getUserEntryIntersect();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlAgentEntry,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Volley inspection", response);
                        if(response.toString().equals("1")){
                            progressDialog.dismiss();
                            SuccessDialogNewAgentEntry.successDialogNewAgentEntry(AgentEntry.this, "Customer added successfully");
                        } else if (response.toString().equals("0")) {
                            progressDialog.dismiss();
                            WarningDialog.warningDialog(AgentEntry.this, "Some field is empty!");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle any error that occurs during the request
                        Toast.makeText(AgentEntry.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Create a map to hold the parameters for the POST request
                Map<String, String> params = new HashMap<>();
                params.put("ag_name", binding.customerName.getText().toString().trim());
                params.put("ag_mobile_no", binding.mobileNumber.getText().toString().trim());
                params.put("ag_email", binding.email.getText().toString().trim());
                params.put("ag_office_address", binding.address.getText().toString().trim());
                params.put("national_id", binding.nationalID.getText().toString().trim());
                params.put("mac_address", binding.macAddress.getText().toString().trim());
                params.put("regular_mobile", binding.otherPhone.getText().toString().trim());
                params.put("connection_date", binding.datePickerText.getText().toString().trim());
                params.put("mb", binding.packageMbSelect.getText().toString().trim());
                params.put("taka", binding.billAmt.getText().toString().trim());
                params.put("acc_amount_run", binding.runningMthAmt.getText().toString().trim());
                params.put("acc_amount_charge", binding.connectChargeAmt.getText().toString().trim());
                params.put("billing_person_id", binding.billingPersonIdText.getText().toString().trim());
                params.put("connection_charge_due", binding.dueInputConnectionCharge.getText().toString().trim());
                params.put("running_month_due", binding.dueInputRunningMonth.getText().toString().trim());
                params.put("ag_status", clientStatus);
                params.put("bill_date", binding.billDate.getText().toString().trim());
                params.put("entry_by", userID);
                params.put("update_by", userID);
                params.put("sms_switch", smsCheckTemp);
                params.put("ip", binding.ipAddress.getText().toString().trim());
                params.put("zone", binding.zoneIDText.getText().toString().trim());
                params.put("mikrotik_disconnect", binding.mikrotikDisconnectDay.getText().toString().trim());
                params.put("user_id", binding.userIDInput.getText().toString().trim());
                params.put("password", binding.passwordInput.getText().toString().trim());
                params.put("remarks", binding.remarkEdi.getText().toString().trim());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 10000;
            }
            @Override
            public int getCurrentRetryCount() {
                return 10000;
            }
            @Override
            public void retry(VolleyError error) throws VolleyError {
            }
        });
        RequestQueue requestQueueSendAlert = Volley.newRequestQueue(this);
        requestQueueSendAlert.add(stringRequest);
    }

    ////////////Zone Selection//////////
    private void fetchZone() {
        String url = urlStorage.getHttpStd()
                +urlStorage.getBaseUrl()
                +urlStorage.getZoneIntersect();
//        String url = "http://192.168.0.122/roms/rest_api_mob_dx/robotispzone.php";

        RequestQueue queue = Volley.newRequestQueue(AgentEntry.this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        zoneList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                zoneId = obj.getString("zone_id");
                                zoneName = obj.getString("zone_name");
                                zoneList.add(new ZoneModel(zoneId, zoneName));
                            }
                            showDialogZone();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonArrayRequest);
    }
    private void showDialogZone() {
        Dialog dialog = new Dialog(AgentEntry.this);
        dialog.setContentView(R.layout.dialog_zone_selection);

        RecyclerView recyclerViewZone = dialog.findViewById(R.id.recycler_view_zone);
        recyclerViewZone.setLayoutManager(new LinearLayoutManager(AgentEntry.this));

        adapter = new ZoneAdapter(zoneList, zoneModel -> {

            binding.zoneIDText.setText(zoneModel.getZoneId());
            binding.zoneSelect.setText(zoneModel.getZoneName());


            //Sub Zone visibility
            subZoneCountNumber = Integer.parseInt(binding.subZoneCountText.getText().toString());
            if(subZoneCountNumber > 0){
                binding.subZoneContainer.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Greater", Toast.LENGTH_SHORT).show();
            }else {
                binding.subZoneContainer.setVisibility(View.GONE);
                binding.areaContainer.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Shorter", Toast.LENGTH_SHORT).show();
            }
            /////////////////////
            dialog.dismiss();
        });

        recyclerViewZone.setAdapter(adapter);
        dialog.show();
    }
    ///////////////////////////////////////////

    //////////Sub Zone selection///////////////
    private void fetchSubZone() {

//        String urlSubZone = "http://192.168.0.126/newisp/rest_api_mob_dx/subZoneWithChildCount.php?parent_id="
//                +binding.zoneIDText.getText().toString();
        String urlSubZone = urlStorage.getHttpStd()
                + urlStorage.getBaseUrl()
                + urlStorage.getSubZoneEndpoint()
                + binding.zoneIDText.getText().toString();

        RequestQueue queue = Volley.newRequestQueue(AgentEntry.this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlSubZone, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        subZoneList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                subZoneId = obj.getString("zone_id");
                                subZoneName = obj.getString("zone_name");
                                areaCount = obj.getString("area_count");
                                subZoneList.add(new SubzoneModel(subZoneId, subZoneName, areaCount));
                            }
                            showDialogSubZone();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonArrayRequest);
    }
    private void showDialogSubZone() {
        Dialog dialogSubZone = new Dialog(AgentEntry.this);
        dialogSubZone.setContentView(R.layout.dialog_sub_zone_selection);

        RecyclerView recyclerViewSubZone = dialogSubZone.findViewById(R.id.recycler_view_sub_zone);
        recyclerViewSubZone.setLayoutManager(new LinearLayoutManager(AgentEntry.this));

        adapterSubZone = new SubZoneAdapter(subZoneList, subzoneModel -> {

            binding.subZoneIDText.setText(subzoneModel.getZoneId());
            binding.subZoneSelect.setText(subzoneModel.getZoneName());
            binding.areaCountText.setText(subzoneModel.getAreaCount());

            areaCountNumber = Integer.parseInt(binding.areaCountText.getText().toString());
            if(areaCountNumber > 0){
                binding.areaContainer.setVisibility(View.VISIBLE);
            }else{
                binding.areaContainer.setVisibility(View.GONE);
            }
            dialogSubZone.dismiss();
        });

        recyclerViewSubZone.setAdapter(adapterSubZone);
        adapterSubZone.notifyDataSetChanged();
//        subZoneList.clear();
        dialogSubZone.show();
    }
    ///////////////////////////////////////////

    /////Area Selection////////////////////////
    private void fetchArea() {
//        String urlArea = "http://192.168.0.126/newisp/rest_api_mob_dx/subZoneWithChildCount.php?parent_id="
//                +binding.subZoneIDText.getText().toString();
        String urlArea = urlStorage.getHttpsStd()
                + urlStorage.getBaseUrl()
                + urlStorage.getAreaEndpoint()
                + binding.subZoneIDText.getText().toString();

        RequestQueue queue = Volley.newRequestQueue(AgentEntry.this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlArea, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        areaList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                areaId = obj.getString("zone_id");
                                areaName = obj.getString("zone_name");
                                areaList.add(new AreaModel(areaId, areaName));
                            }
                            showDialogArea();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonArrayRequest);
    }
    private void showDialogArea() {
        Dialog dialogArea = new Dialog(AgentEntry.this);
        dialogArea.setContentView(R.layout.dialog_area);

        RecyclerView recyclerViewArea = dialogArea.findViewById(R.id.recycler_view_area);
        recyclerViewArea.setLayoutManager(new LinearLayoutManager(AgentEntry.this));

        adapterAreaAdapter = new AreaAdapter(areaList, areaModel -> {
            binding.areaIDText.setText(areaModel.getZoneId());
            binding.areaSelect.setText(areaModel.getZoneName());
            dialogArea.dismiss();
        });

        recyclerViewArea.setAdapter(adapterAreaAdapter);
        adapterSubZone.notifyDataSetChanged();
//        subZoneList.clear();
        dialogArea.show();
    }
    ///////////////////////////////////////////

    ////////Package selection////
    private void fetchPackage() {
//        String urlPackage = "http://192.168.0.122/roms/rest_api_mob_dx/robotispPackage.php";
        String urlPackage = urlStorage.getHttpStd()
                + urlStorage.getBaseUrl()
                + urlStorage.getPackageEndpoint();

        RequestQueue queue = Volley.newRequestQueue(AgentEntry.this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlPackage, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        packageList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                packageName = obj.getString("package_name");
                                netSpeed = obj.getString("net_speed");
                                billAmount = obj.getString("bill_amount");
                                packageList.add(new PackageModel(packageName, netSpeed, billAmount));
                            }
                            showDialogPackage();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonArrayRequest);
    }
    private void showDialogPackage() {
        Dialog dialogPackage = new Dialog(AgentEntry.this);
        dialogPackage.setContentView(R.layout.dialog_package);

        RecyclerView recyclerViewPackage = dialogPackage.findViewById(R.id.recycler_package);
        recyclerViewPackage.setLayoutManager(new LinearLayoutManager(AgentEntry.this));

        adapterPackage = new PackageAdapter(packageList, packageModel -> {
//            binding.areaIDText.setText(pack.getZoneId());
//            binding.areaSelect.setText(areaModel.getZoneName());
            binding.packageMbSelect.setText(packageModel.getNetSpeed());
            binding.billAmt.setText(packageModel.getBillAmount().toString().trim());
            binding.speed.setText(packageModel.getNetSpeed().toString().trim());
            dialogPackage.dismiss();
        });

        recyclerViewPackage.setAdapter(adapterPackage);
        adapterPackage.notifyDataSetChanged();
//        subZoneList.clear();
        dialogPackage.show();
    }
    /////////////////////////////

    ////Billing person selection/////
    private void fetchBillingPerson() {

        String urlBillingPerson = urlStorage.getHttpStd()
                + urlStorage.getBaseUrl()
                + urlStorage.getBillingPersonEndpoint();

        RequestQueue queue = Volley.newRequestQueue(AgentEntry.this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, urlBillingPerson, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        billingPersonList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                billngPersonName = obj.getString("FullName");
                                billingPersonId = obj.getString("UserId");
                                billingPersonList.add(new BillingPersonModel(billingPersonId, billngPersonName));
                            }
                            showDialogBillingPerson();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonArrayRequest);
    }
    private void showDialogBillingPerson() {
        Dialog dialogBillingPerson = new Dialog(AgentEntry.this);
        dialogBillingPerson.setContentView(R.layout.dialog_billing_person);

        RecyclerView recyclerViewBillingPerson = dialogBillingPerson.findViewById(R.id.recycler_billing_person);
        recyclerViewBillingPerson.setLayoutManager(new LinearLayoutManager(AgentEntry.this));

        adapterBillingPerson = new BillingPersonAdapter(billingPersonList, billingPersonModel -> {
            binding.billingPersonSelect.setText(billingPersonModel.getFullName());
            binding.billingPersonIdText.setText(billingPersonModel.getUserId());
            dialogBillingPerson.dismiss();
        });

        recyclerViewBillingPerson.setAdapter(adapterBillingPerson);
        adapterBillingPerson.notifyDataSetChanged();
//        subZoneList.clear();
        dialogBillingPerson.show();
    }
    //////////////////////////////////

    //Date Picker////////////////////
    // Method to show the DatePicker Dialog
    private void showDatePickerDialog() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and set a listener for when the date is picked
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AgentEntry.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // Adjust the month since the DatePicker returns 0-based months (0 = January)
                        selectedMonth = selectedMonth + 1;
                        // Format the date as a String
                        String formattedDate = formatDate(selectedYear, selectedMonth, selectedDay);
                        // Set the formatted date to the TextView
                        binding.datePickerText.setText(formattedDate);
                    }
                },
                year, month, day
        );
        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    // Helper method to format the selected date
    private String formatDate(int year, int month, int day) {
        // Format the date to "dd-MM-yyyy"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day); // Month is 0-based in Calendar
        return sdf.format(calendar.getTime());
    }
    //////////////////////////////////////////

    //Check field validations////
    void fieldValidation(){

        String regExMobileNumner = "^(\\+8801[3-9]\\d{8}|01[3-9]\\d{8})$";

        String mobileNumberVali = binding.mobileNumber.getText().toString();
        String emailVali = binding.email.getText().toString();

        if (TextUtils.isEmpty(binding.ipAddress.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(binding.passwordInput.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(binding.customerName.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mobileNumberVali)) {
            Toast.makeText(getApplicationContext(), "Mobile number cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (!mobileNumberVali.matches(regExMobileNumner)) {
            Toast.makeText(getApplicationContext(), "Invalid Mobile Number", Toast.LENGTH_SHORT).show();
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailVali).matches()){
            Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(binding.address.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(binding.nationalID.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(binding.macAddress.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(binding.datePickerText.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(binding.zoneIDText.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(binding.packageMbSelect.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(binding.billAmt.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(binding.runningMthAmt.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(binding.connectChargeAmt.getText())) {
            Toast.makeText(getApplicationContext(), "Some field is empty", Toast.LENGTH_SHORT).show();
        }else {
            // If none of the EditTexts are empty, show this message
//            Toast.makeText(getApplicationContext(), "All fields are filled", Toast.LENGTH_SHORT).show();
            submitNewAgent();
        }
    }
    ////////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the periodic updates when the activity is destroyed
        handler.removeCallbacks(mikrotikStatusUpdater);
    }

}