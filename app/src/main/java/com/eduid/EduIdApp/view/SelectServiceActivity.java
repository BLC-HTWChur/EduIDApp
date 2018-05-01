package com.eduid.EduIdApp.view;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.eduid.EduIdApp.R;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.controller.Profile.LoginManagement;
import com.eduid.EduIdApp.controller.ProtocolDiscovery.RSDCallback;
import com.eduid.EduIdApp.controller.ProtocolDiscovery.RSDManager;
import com.eduid.EduIdApp.controller.ResponseService;
import com.eduid.EduIdApp.controller.ServiceManagement.AssertionManager;
import com.eduid.EduIdApp.model.EduIdDB;
import com.eduid.EduIdApp.model.dataobjects.EduIDService;
import com.eduid.EduIdApp.model.dataobjects.ServicesAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

/**
 * Created by Yann Cuttaz on 14.12.16.
 */
public class SelectServiceActivity extends ListActivity {

    /**
     * View params
     */
    private EduIdDB eduIdDB = null;
    private ArrayList<EduIDService> platforms;
    private String[] stringServicesList = {};
    private ArrayList<EduIDService> services;
    private ArrayList<EduIDService> selectedItems = new ArrayList<EduIDService>();

    /**
     * Service response
     */
    private ArrayList<String> authorizedServicesToken = new ArrayList<>();
    private boolean isBound = false;
    private ServiceConnection connection;//receives callbacks from bind and unbind invocations

    /**
     * Intent params
     */
//    private String thirdPartyAppID = null;
    private String serviceName = null;
    private String[] protocolList = null;
    private String client_id;
    private String third_party_app_id;
    private String app_name;
    private String token;
    private boolean single;

    /**
     *  Search Bar EdiText
     */
    private EditText filterSearchBar;
    private ServicesAdapter mAdapter;

    /**
     *  Loading indicator
     */
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.services_list);

        filterSearchBar = findViewById(R.id.searchBarText);
        mDialog = new SpotsDialog(this);
    }

    @Override
    public void onBackPressed() {
        // Nothing
    }

    @Override
    protected void onStop() {
        super.onStop();

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(eduIdDB == null) eduIdDB = new EduIdDB(getApplicationContext());

        /**
         * Accept button
         */
        Button acceptButton = (Button) findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Loading indicator (AlertDialog)
                 */
                Log.d("EDUID", "Accept button pressed, selected Services: " + selectedItems.size());
                if(selectedItems.size() <= 0){
                    return;
                }
                mDialog.show();

                /**
                 * Assertion
                 */
                new AssertionManager(getApplicationContext()).doAssertion(SelectServiceActivity.this.selectedItems, new AssertionManager.AssertionCallback() {
                    @Override
                    public void onAssertionFinish(ArrayList<EduIDService> authorizedServices) {
                        /**
                         * Build data to return on third party app
                         */
                        JSONObject dataToSend = null;
                        try {

                            Bundle bundle = new Bundle();

                            /**
                             * Check if list isn't empty
                             */
                            if(authorizedServices.size() > 0){
                                dataToSend = getDataToSend(authorizedServices);
                                bundle.putString("dataJSON", dataToSend.toString());
                            }
                            else{
                                bundle.putString("dataJSON", "[]");
                            }

                            Config.debug("data returned: " + bundle.getString("dataJSON"));

                           mDialog.dismiss();
                            /**
                             * Send back data to Third Party App
                             */
                            new ResponseService(getApplicationContext(), SelectServiceActivity.this.third_party_app_id, bundle);


                        } catch (JSONException e) {
                            sendDataError("Error on build JSON data.");
                        }
                    }

                    @Override
                    public void onAssertionError(String errorMessage) {
                        mDialog.dismiss();
                        sendDataError("Assertion error.");
                    }
                });


                /**
                 * Get Protocols Authorization
                 */
//                SelectServiceActivity.this.authorizeApplication();
            }
        });

        /**
         * Reject button
         */
        Button rejectButton = (Button) findViewById(R.id.rejectButton);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Return with an error
                 */
                sendDataError("User reject academic services.");
            }
        });


        rejectButton.post(new Runnable() {
            @Override
            public void run() {
                checkIntents();
            }
        });

    }



    /**
     * Build the services list
     */
    private void buildServicesList(){
        this.platforms = this.services;
        this.stringServicesList = new String[this.platforms.size()];
        for (int i = 0; i < this.platforms.size() ; i++){
            this.stringServicesList[i] = this.platforms.get(i).getEngineName();
        }
    }

    /**
     * Set list Adapter
     */
    private void bindDataList(ArrayList<EduIDService> services){
        this.services = services;
//        buildServicesList();
        //ArrayAdapter<EduIDService> adapter = new ArrayAdapter<EduIDService>(this, R.layout.list_layout, services);
        mAdapter = new ServicesAdapter(this, services);
        setListAdapter(mAdapter);

        /**
         * Set Listener for the search bar
         */
        filterSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().toLowerCase(Locale.getDefault());
                mAdapter.filter(text);
                Log.d("EDUID", "Searc bar text changed : " + text );
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        EduIDService selectedService = (EduIDService)getListView().getItemAtPosition(position);
        //ServicesAdapter adapter = (ServicesAdapter) getListAdapter();
        /**
         * Check if the service is already selected
         */
        if(selectedItems.contains(selectedService)){ // Already selected
            //v.setBackgroundColor(Color.TRANSPARENT);
            this.selectedItems.remove(selectedService);
            ImageView checkBox = v.findViewById(R.id.checkBoxView);
            checkBox.setImageResource(R.drawable.animated_check);
            mAdapter.removeSelected(selectedService);
        }
        else{
            //v.setBackgroundColor(Color.GRAY);
            this.selectedItems.add(selectedService);
            ImageView checkBox = v.findViewById(R.id.checkBoxView);
            ((Animatable) checkBox.getDrawable()).start();
            mAdapter.addSelected(selectedService);
        }
    }



    /**
     * If the view is opened by an external intent, we check it
     */
    public void checkIntents(){


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();



//        Config.debug(action + " - " + type);

        if (Intent.ACTION_VIEW.equals(action) && type != null) {

            /**
             * Get request params
             */
            try {
                this.third_party_app_id = intent.getStringExtra("app_id");
                this.serviceName = intent.getStringExtra("serviceName");
                this.client_id = intent.getStringExtra("client_id");
                this.app_name = intent.getStringExtra("app_name");
                this.protocolList = intent.getStringArrayExtra("protocols");
                this.token = intent.hasExtra("token") ? intent.getStringExtra("token") : "";
                this.single = intent.hasExtra("single") ? intent.getBooleanExtra("single", false) : null;
            }
            catch (Exception exc){
                sendDataError("Request parameters error!");
            }

            /**
             * If user logged-in, Show services. Otherwise send error to Third Party App
             */
            if(!new LoginManagement(this.getApplicationContext()).isLogged()) {
                sendDataError("User not logged in.");
            } else{

                /**
                 * Authorize Protocols
                 */
                if ("application/json".equals(type) && serviceName.equals("authorizeProtocols")) {

                    /**
                     * Loading message
                     */
                    //Toast.makeText(getApplicationContext(), "Loading ...", Toast.LENGTH_SHORT).show();

                    /**
                     * Services authentication
                     */
                    new RSDManager(getApplicationContext()).getRSD(this.protocolList, new RSDCallback() {
                        @Override
                        public void rsdAuthenticationFinished(ArrayList<EduIDService> services) {
                            SelectServiceActivity.this.bindDataList(services);
                        }

                        @Override
                        public void rsdAuthenticationError(String errorMessage) {
                            /**
                             * Return with an error
                             */
                            sendDataError(errorMessage);

                        }
                    });

                }
            }
        }
    }

    /**
     * Send back to third party app a message with the error
     * @param errorMessage
     */
    private void sendDataError(String errorMessage){
        /**
         * Prepare data to send
         */
        Bundle bundle = new Bundle();
        bundle.putString("dataJSON", getDataError(errorMessage).toString());

        /**
         * Send back data to Third Party App
         */
        new ResponseService(getApplicationContext(), SelectServiceActivity.this.third_party_app_id, bundle);

    }

    /**
     * Prepare the data to send when an error occured
     * @param errorMessage
     * @return
     */
    private JSONObject getDataError(String errorMessage){
        JSONObject ret = new JSONObject();
        try {
            ret.put("errorMessage", errorMessage);
        } catch (JSONException e) {
        }
        return ret;
    }

    /**
     * Get the json data to send
     * @return Data to send
     */
    private JSONObject getDataToSend(ArrayList<EduIDService> authorizedServices) throws JSONException {

        if(eduIdDB == null) {
            eduIdDB = new EduIdDB(this.getApplicationContext());
        }

        /**
         * Get protocols name
         */
        JSONArray services = new JSONArray();

        for (EduIDService service :
                authorizedServices) {
            services.put(service.toJSON());
        }


        /**
         * Build data on JSON Object
         */
        JSONObject dataJSON = new JSONObject();
        dataJSON.put("services", services);
        return dataJSON;

    }



}