package com.example.itdevjr1.kiosk;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by paolohilario on 1/29/18.
 */

public class ActivitySingleton {

    private static ActivitySingleton instance;
    JSONArray arrayClients;
    JSONObject objectClient;
    JSONObject appointmentObjectBranch;
    JSONArray arrayServices;
    JSONArray arrayProducts;
    JSONObject objectAppointments;
    TextView lblTotalPrice,lblTotalQty,lblCartLabel;
    JSONObject objectWaiver;
    JSONArray arrayRestrictedServiceWhenMenstrual;
    JSONObject objectClientAppointment  = new JSONObject();
    JSONArray arraySelectedItems        = new JSONArray();
    boolean clientCycleStatus           = false;
    Socket mSocket;
    Utilities utilities;
    RecyclerView.Adapter recyclerAdapter;
    RecyclerView recyclerView;
    //for walk in, call in and chat appointment(waiver)
    JSONObject objectClientWaiver;



    public static ActivitySingleton Instance() {
        if (instance == null) {
            instance = new ActivitySingleton();
        }
        return instance;
    }

    //set utilities as Global variable
    public void setUtilityClass(Utilities utilities){
        this.utilities = utilities;
    }

    public Utilities getUtilityClass(){
        return this.utilities;
    }

    public void setClientData(JSONObject objectParse){
        this.objectClient = objectParse;
    }

    public JSONObject getClientData(){
        return this.objectClient;
    }

    public void setClientsArray(JSONArray arrayClientss){
        this.arrayClients = arrayClientss;
    }

    public JSONArray getClientsArray(){
        return this.arrayClients;
    }

    ///waiver array
    public void setWaivers(JSONObject objectWaivers){
        this.objectWaiver = objectWaivers;
    }

    public JSONObject getWaivers(){
        return this.objectWaiver;
    }


    public String getClientGender(){
        String gender = null;
        gender = this.objectClient.optString("client_gender","female");
        return gender;
    }

    //set & get Label of total Qty
    public void setLabelQuantity(TextView lblQty){
        this.lblTotalQty = lblQty;
    }

    public TextView getLabelQuantity(){
        return this.lblTotalQty;
    }
    //set & get Label of total price
    public void setLabelPrice(TextView lblTotalPrices){
        this.lblTotalPrice = lblTotalPrices;
    }

    public TextView getLabelPrice(){
        return this.lblTotalPrice;
    }


    //Items of transactions(Initial) Service & Products
    public void setArrayItems(JSONArray arraySelectedItems){
        this.arraySelectedItems = arraySelectedItems;
    }

    public JSONArray getSelectedArrayItems(){
        return this.arraySelectedItems;
    }

    //appointment object of client
    public void setObjectAppointment(JSONObject objectClientAppointment){
        this.objectClientAppointment = objectClientAppointment;
    }

    public JSONObject getObjectAppointment(){
        return this.objectClientAppointment;
    }

    public void  setRecyclerAppointmentList(RecyclerView.Adapter recyclerAdapters,RecyclerView recyclerView){
        this.recyclerAdapter = recyclerAdapters;
        this.recyclerView    = recyclerView;
    }

    public RecyclerView.Adapter getRecyclerAppointmentList(){
        return this.recyclerAdapter;
    }

    public RecyclerView getRecyclerView(){
        return this.recyclerView;
    }

    //encapsulate socket
    public void setSocketApplication(Socket mSockets){
        this.mSocket = mSockets;
    }
    public Socket getSocketApplication(){
        return this.mSocket;
    }

    //cart label if empty
    public void setLblCartLabel(TextView lblCartLabel){
        this.lblCartLabel = lblCartLabel;
    }
    public TextView getLblCartLabel(){
        return this.lblCartLabel;
    }
     //restricted services when client is on her period
//    public void arraySetRestrictedServices(JSONArray arrayRestricted){
//        this.arrayRestrictedServiceWhenMenstrual = arrayRestricted;
//    }
//
//    public JSONArray arrayGetRestrictedServices(){
//        return this.arrayRestrictedServiceWhenMenstrual;
//    }


    public void setClientCycleStatus(Boolean clientCycleStatus){
        this.clientCycleStatus = clientCycleStatus;
    }

    public Boolean getClientCycleStatus(){
        return this.clientCycleStatus;
    }


//    // Parameters: value(id),label,gender
//
//    //for whole Appointment(final)
//    public void setAppointmentsObject(JSONObject object){
//        this.objectAppointments = object;
//    }
//    public JSONObject getAppointmentsObject(){
//        return objectAppointments;
//    }
//
//    //for Waiver
//    public void setArrayWaiver(JSONArray arrayWaivers){
//        this.arrayWaiver = arrayWaivers;
//    }
//
//
//
//

//
//

//


//    //boolean monthly cycle
//    public void setIfClientHasMonthlyCycle(boolean ifClientHasMonthlyCycles){
//        this.ifClientHasMonthlyCycle = ifClientHasMonthlyCycles;
//    }
//    public Boolean getIfClientHasMonthlyCycle(){
//        return this.ifClientHasMonthlyCycle;
//    }
//

//
//
    //for walk in, call in and chat appointment(waiver)
    public void setWalkINObjectWaiver(JSONObject objectWaivers){
        this.objectClientWaiver = objectWaivers;
    }

    public JSONObject getWalkINObjectWaiver(){
       return this.objectClientWaiver;
    }



//    public void setWalkINClientItems(JSONArray array){
//        this.arrayClientItems = array;
//    }
//    public JSONArray getWalkINClientItems(){
//        return this.arrayClientItems;
//    }
//    public void setWalkINRestrictedItems(JSONArray array){
//        this.arrayRestrictedItems = array;
//    }
//    public JSONArray getWalkINRestrictedItems(){
//        return this.arrayRestrictedItems;
//    }


    public void resetAllDetails(){

        this.objectClient            = new JSONObject();
        this.arraySelectedItems      = new JSONArray();
        this.appointmentObjectBranch = new JSONObject();
        this.arrayServices           = new JSONArray();
        this.arrayProducts           = new JSONArray();
        this.objectWaiver            = new JSONObject();
        this.objectClientAppointment = new JSONObject();
        this.objectAppointments      = new JSONObject();
        this.arrayRestrictedServiceWhenMenstrual = new JSONArray();
        this.clientCycleStatus       = false;
        this.objectClientWaiver      = new JSONObject();

    }

}
