package com.armavi_bsd.robotispreconstructed_romsNetwork;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.armavi_bsd.robotispreconstructed_romsNetwork.databinding.ActivityAgentInfoBinding;

public class AgentInfo extends AppCompatActivity {

    ActivityAgentInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAgentInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.infoCustomerIp.setText(getIntent().getStringExtra("intent_agent_cus_ip"));
        binding.infoCustomerAddress.setText(getIntent().getStringExtra("intent_agent_address"));
        binding.infoCustomerMobile.setText(getIntent().getStringExtra("intent_agent_mobile"));
        binding.infoCustomerPackage.setText(getIntent().getStringExtra("intent_agent_package"));

        binding.infoCustomerBillAmount.setText(getIntent().getStringExtra("intent_agent_taka")
                +" Taka");

        binding.infoCustomerConnectionDate.setText(getIntent().getStringExtra("intent_agent_condate"));
        binding.infoCustomerZone.setText(getIntent().getStringExtra("intent_agent_zone_name"));
        binding.inforCustomerID.setText(getIntent().getStringExtra("intent_agent_cus_id"));
        binding.infoCustomerName.setText(getIntent().getStringExtra("intent_agent_name"));

        binding.infoCustomerCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+
                        binding.infoCustomerMobile
                                .getText()
                                .toString()));
                startActivity(intent);
            }
        });

        binding.infoIpClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                funcIPCopyInClipboard();
            }
        });

        binding.infoCustomerIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                funcIPCopyInClipboard();
            }
        });
    }

    void funcIPCopyInClipboard(){
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Customer IP", binding.infoCustomerIp.getText().toString().trim());
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getApplicationContext(), "Customer IP copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
}