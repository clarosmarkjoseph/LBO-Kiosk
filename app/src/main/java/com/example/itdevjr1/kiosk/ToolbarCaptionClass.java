package com.example.itdevjr1.kiosk;

import java.util.ArrayList;

/**
 * Created by paolohilario on 2/20/18.
 */

public class ToolbarCaptionClass {

    public ToolbarCaptionClass(){

    }

    public ArrayList<String> returnCaptions(int position){
        ArrayList<String> arrayList = new ArrayList<>();
        if(position == 0){
            arrayList.add("Hair Free Morning!");
            arrayList.add("How may I assist you?");
        }
        if(position == 1){
            arrayList.add("Is this your first time in Lay Bare?");
            arrayList.add("Select answer");
        }
        if(position == 2){
            arrayList.add("Registering New User!");
            arrayList.add("Please fill-up the following: Full name, gender, birthdate, contact no., email address");
        }
        if(position == 3){
            arrayList.add("Identify Yourself");
            arrayList.add("Select one of the options below to identify yourself");
        }
        if(position == 4){
            arrayList.add("Verify User's Account");
            arrayList.add("Please login your Lay Bare Online credentials to continue");
        }
        if(position == 5){
            arrayList.add("Scan your QR Code");
            arrayList.add("Please attach / scan your QR code from your card or App");
        }
        if(position == 6){
            arrayList.add("Search Your account");
            arrayList.add("You may search your account. Please select on how do we identify you.");
        }
        if(position == 7){
            arrayList.add("Search Results");
            arrayList.add("See results of your parameters and select your account");
        }
        if(position == 8){
            arrayList.add("Step 1: Services / Products");
            arrayList.add("Select your preferred Services / Products in this branch");
        }
        if(position == 9){
            arrayList.add("Step 2: Waiver Form");
            arrayList.add("Fill up your Waiver Agreement Form");
        }
        if(position == 10){
            arrayList.add("Step 3: Terms & Conditions");
            arrayList.add("Please agree to our terms and conditions");
        }
        if(position == 11){
            arrayList.add("Administrator Dashboard: Settings");
            arrayList.add("Select actions to the Kiosk");
        }
        if(position == 12){
            arrayList.add("Branch Log - In");
            arrayList.add("Please Log - in your Branch Supervisor's Account");
        }
        if(position == 13){
            arrayList.add("System is on Stand-by");
            arrayList.add("Branch Kiosk is offline. Please check your connection and try again");
        }
        if(position == 14){
            arrayList.add("Search Appointment for signing a waiver");
            arrayList.add("Appointments via SMS, Call-in, CHAT without a waiver can sign a waiver");
        }
        if(position == 17){
            arrayList.add("Waiver Questions");
            arrayList.add("Fill up your waiver form to proceed.");
        }
        if(position == 18){
            arrayList.add("Sign a waiver (Today's Appointment)");
            arrayList.add("You are now currently signing your waiver");
        }
        if(position == 15){
            arrayList.add("Acknowledgement Signature");
            arrayList.add("Hi, please acknowledge your transaction details and signed it as a confirmation");
        }

        if(position == 16){
            arrayList.add("Registering Device to Lay Bare System");
            arrayList.add("Please enter the serial number below to Lay Bare Online System(Branch Supervisor's Desktop)");
        }

        return arrayList;
    }



}
