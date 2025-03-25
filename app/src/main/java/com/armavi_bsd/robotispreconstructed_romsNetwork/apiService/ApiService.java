package com.armavi_bsd.robotispreconstructed_romsNetwork.apiService;

import com.armavi_bsd.robotispreconstructed_romsNetwork.model.AgentModel;
import com.armavi_bsd.robotispreconstructed_romsNetwork.model.BillCollectionModel;
import com.armavi_bsd.robotispreconstructed_romsNetwork.model.ComplainModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
//    @GET("rest_api_mob_dx/testCustomerView.php")
//    Call<List<AgentModel>> getAgents();

    @GET("rest_api_mob_dx/robotispagentdisplay.php")
    Call<List<AgentModel>> getAgents();

    @GET("rest_api_mob_dx/robotispcomplainview.php")
    Call<List<ComplainModel>> getComplain();

    @GET("rest_api_mob_dx/robotispviewpayment.php")
    Call<List<BillCollectionModel>> getBillCollection();
}
