package com.ufc.great.testingdynamix;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.ambientdynamix.api.application.ContextHandler;
import org.ambientdynamix.api.application.ContextHandlerCallback;
import org.ambientdynamix.api.application.ContextResult;
import org.ambientdynamix.api.application.ContextSupportCallback;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.DynamixConnector;
import org.ambientdynamix.api.application.DynamixFacade;
import org.ambientdynamix.api.application.IContextListener;
import org.ambientdynamix.api.application.IContextRequestCallback;
import org.ambientdynamix.api.application.ISessionListener;
import org.ambientdynamix.api.application.SessionCallback;
import org.ambientdynamix.api.application.SessionListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "lana";
    private DynamixFacade dynamix;
    private ISessionListener sessionListener;
    private IContextListener contextListener;
    private ContextHandler handler;
    private String sound_result = "00";

    private Button buttonGetContextSound;
    private TextView textViewResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connect();

        textViewResult = (TextView) findViewById(R.id.result);


        buttonGetContextSound = (Button) findViewById(R.id.button);
        buttonGetContextSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (buttonGetContextSound.getText().equals(getResources().getString(R.string.on)))
//                    buttonGetContextSound.setText(getResources().getString(R.string.off));
//                else
//                    buttonGetContextSound.setText(getResources().getString(R.string.on));

                // Make a context request using the handler
                try {
                    handler.contextRequest("org.ambientdynamix.contextplugins.ambientsound",
                            "org.ambientdynamix.contextplugins.ambientsound", new IContextRequestCallback.Stub() {
                                @Override
                                public void onSuccess(ContextResult result) throws RemoteException {
                                    Log.i(TAG, "Recebendo contexto");
                                    logInfos(result);
                                }

                                @Override
                                public void onFailure(String message, int arg1) throws RemoteException {
                                    Log.i(TAG, "Can't get context Request.");
                                }
                            });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                //if (buttonGetContextSound.getText().equals(getResources().getString(R.string.off))){
                    float sound = Float.parseFloat(sound_result);
                    textViewResult.setText(String.format("%.2f", sound)+"db");

            }

        });
    }


    private void logInfos(ContextResult result) {

        /*
        * Log some information about the incoming event
        */
        Log.i(TAG, "onContextResult received from plugin: " + result.getResultSource());
        Log.i(TAG, "-------------------");
        Log.i(TAG, "Event context type: " + result.getContextType());
        Log.i(TAG, "Event timestamp " + result.getTimeStamp().toLocaleString());
        if (result.expires())
            Log.i(TAG, "Event expires at " + result.getExpireTime().toLocaleString());
        else
            Log.i(TAG, "Event does not expire");
        /*
         * To illustrate how string-based context representations are accessed, we log each contained in the event.
         */
        for (String format : result.getStringRepresentationFormats()) {
            Log.i(TAG,
                    "Event string-based format: " + format + " contained data: "
                            + result.getStringRepresentation(format));
            sound_result = result.getStringRepresentation(format);

        }


    }


    private void connect() {

        Log.i(TAG,"iniciando conexão!");

        sessionListener = new SessionListener() {
            @Override
            public void onSessionOpened(String sessionId) throws RemoteException {
                Log.i(TAG, "onSessionOpened");
            }

            @Override
            public void onSessionClosed() throws RemoteException {
                Log.i(TAG, "onSessionClosed");
            }

            @Override
            public void onContextPluginDisabled(org.ambientdynamix.api.application.ContextPluginInformation plug) throws RemoteException {
                Log.i(TAG, "onContextPluginDisabled: " + plug);
            }

            @Override
            public void onContextPluginEnabled(org.ambientdynamix.api.application.ContextPluginInformation plug) throws RemoteException {
                Log.i(TAG, "onContextPluginEnabled: " + plug);
            }
            // Other events omitted for brevity. See JavaDocs for details.
        };


        try {
            DynamixConnector.openConnection(MainActivity.this, true, sessionListener, new SessionCallback() {
                public void onSuccess(DynamixFacade facade) throws RemoteException {
                    // Handle connection success
                    Log.i(TAG, "Tá conectado! :) ");

                    // Store the facade
                    dynamix = facade;

                    // Use the Dynamix Facade Object to create Context Handlers
                    dynamix.createContextHandler(new ContextHandlerCallback() {
                        @Override
                        public void onSuccess(ContextHandler handler) throws RemoteException {
                            Log.i(TAG, "createContextHandler.onSuccess with " + handler);

                            MainActivity.this.handler = handler;

                            handler.addContextSupport("org.ambientdynamix.contextplugins.ambientsound",
                                    "org.ambientdynamix.contextplugins.ambientsound", contextListener, new ContextSupportCallback() {
                                        @Override
                                        public void onSuccess(ContextSupportInfo sup) throws RemoteException {
                                            Log.i(TAG, "addContextSupportWithListenerAndCallback.onSuccess: " + sup);
                                        }

                                        @Override
                                        public void onFailure(String message, int arg1) throws RemoteException {
                                            Log.w(TAG, "addContextSupportWithListenerAndCallback.onFailure: " + message);
                                        }
                                    });

                        }

                        @Override
                        public void onFailure(String message, int errorCode) throws RemoteException {
                            Log.w(TAG, "createContextHandler.onFailure " + message);
                            DynamixConnector.closeConnection();
                        }
                    });
                }

                public void onFailure(String message, int errorCode) throws RemoteException {
                    // Handle connection failure
                    Log.i(TAG, "Não conectou! :( ");
                }

            });


        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

}
