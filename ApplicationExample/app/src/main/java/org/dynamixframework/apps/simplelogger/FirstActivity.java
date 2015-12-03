/*
// * Copyright (C) The Ambient Dynamix Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dynamixframework.apps.simplelogger;

import java.util.Date;

import org.ambientdynamix.api.application.BundleContextInfo;
import org.ambientdynamix.api.application.ContextHandler;
import org.ambientdynamix.api.application.ContextHandlerCallback;
import org.ambientdynamix.api.application.ContextListener;
import org.ambientdynamix.api.application.ContextPluginInformation;
import org.ambientdynamix.api.application.ContextRequestCallback;
import org.ambientdynamix.api.application.ContextResult;
import org.ambientdynamix.api.application.ContextSupportCallback;
import org.ambientdynamix.api.application.ContextSupportInfo;
import org.ambientdynamix.api.application.DynamixConnector;
import org.ambientdynamix.api.application.DynamixFacade;
import org.ambientdynamix.api.application.IBundleContextInfo;
import org.ambientdynamix.api.application.IContextInfo;
import org.ambientdynamix.api.application.IContextListener;
import org.ambientdynamix.api.application.IContextRequestCallback;
import org.ambientdynamix.api.application.ISessionListener;
import org.ambientdynamix.api.application.IdResult;
import org.ambientdynamix.api.application.Result;
import org.ambientdynamix.api.application.SessionCallback;
import org.ambientdynamix.api.application.SessionListener;
import org.ambientdynamix.contextplugins.ambientsound.IAmbientSoundContextInfo;
import org.ambientdynamix.contextplugins.barcode.IBarcodeContextInfo;
import org.ambientdynamix.contextplugins.batterylevel.IBatteryLevelInfo;
import org.ambientdynamix.contextplugins.nfc.INfcTag;
import org.ambientdynamix.contextplugins.pedometer.IPedometerStepInfo;
import org.ambientdynamix.contextplugins.sampleplugin.ISamplePluginContextInfo;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple Dynamix project that demonstrates how to use Dynamix 2.x from a native Android app.
 * 
 * @author Darren Carlson
 */
public class FirstActivity extends Activity {
	private final String TAG = this.getClass().getSimpleName();
	private DynamixFacade dynamix;
	private ContextHandler handler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "ON CREATE for: Dynamix Simple Logger");
		setContentView(R.layout.main);
		TextView activityLabel = (TextView) findViewById(R.id.activity_label);
		activityLabel.setText("Dynamix Simple Logger");
		// Setup the connect button
		Button btnConnect = (Button) findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doConnect();
			}
		});
		// Setup the barcode scan button
		Button btnInteractiveAcquisition = (Button) findViewById(R.id.btnInteractiveAcquisition);
		btnInteractiveAcquisition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (handler != null) {
					try {
						/*
						 * Here we demonstrate making a context request using the previously installed context handler.
						 * In this case, we are requesting that the barcode plug-in perform a scan and return the
						 * results to the callback. Note that both the plug-in id and context type are required since
						 * plug-ins may support multiple context types. In addition, plug-in version information can
						 * also be passed into the 'contextRequest' method. Omitting version information implies that
						 * the latest version of the plug-in should be used.
						 */
						handler.contextRequest("org.ambientdynamix.contextplugins.barcode",
								"org.ambientdynamix.contextplugins.barcode", new ContextRequestCallback() {
									public void onSuccess(ContextResult result) throws RemoteException {
										/*
										 * Results have been successfully returned to the callback. Log results.
										 */
										logContextResult(result);
									}

									public void onFailure(String message, int errorCode) throws RemoteException {
										/*
										 * The context request failed.
										 */
										Log.w(TAG, "Barcode scan fail: " + message);
									};
								});
					} catch (Exception e) {
						// Log any errors
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		// Setup the battery level scan button
		Button btnProgrammaticAcquisition = (Button) findViewById(R.id.btnProgrammaticAcquisition);
		btnProgrammaticAcquisition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (handler != null) {
					try {
						/*
						 * Here we demonstrate making another context request using the previously created context
						 * handler. In this case, we are requesting that the battery level plug-in perform a scan and
						 * return the results to the callback. Note that some plug-ins (like the battery level plug-in)
						 * support both event broadcasting as well as programmatic interaction. If you look at the
						 * registerContextSupport method, we are passing in a listener for battery events, which will be
						 * automatically updated with new data as the plug-in detects changes in battery state. The
						 * battery level plug-in also supports the 'contextRequest' method, which returns the battery
						 * level at the moment the method is called.
						 */
						handler.contextRequest("org.ambientdynamix.contextplugins.batterylevel",
								"org.ambientdynamix.contextplugins.batterylevel", new IContextRequestCallback.Stub() {
									@Override
									public void onSuccess(ContextResult result) throws RemoteException {
										/*
										 * Results have been successfully returned to the callback. Log results.
										 */
										logContextResult(result);
									}

									@Override
									public void onFailure(String message, int arg1) throws RemoteException {
										/*
										 * The context request failed.
										 */
										Log.i(TAG, "Battery level fail: " + message);
									}
								});
					} catch (Exception e) {
						// Log any errors
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		// Setup the example command button
		Button btnExampleCommand = (Button) findViewById(R.id.btnExamplePlugin);
		btnExampleCommand.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (handler != null) {
					String CONTEXT_TYPE_COMMAND = "org.example.dynamixplugin.commandtest";
					/*
					 * Example of how to interact with the example plug-in using commands.
					 */
					try {
						// First, request that the plug-in respond with success
						Bundle requestSuccess = new Bundle();
						requestSuccess.putString("requestCommand", "sendSuccess");
						handler.contextRequest("org.example.dynamixplugin", CONTEXT_TYPE_COMMAND, requestSuccess,
								new ContextRequestCallback() {
									public void onSuccess(ContextResult result) throws RemoteException {
										Log.i(TAG, "Received success from: " + result.getResultSource().getPluginId());
									};

									public void onFailure(String message, int errorCode) throws RemoteException {
										Log.i(TAG, "Received fail: " + message);
									};
								});
						// Next, request that the plug-in echo some text back to us
						Bundle echoRequest = new Bundle();
						echoRequest.putString("requestCommand", "echoRequest");
						echoRequest.putString("echoText", "Echo text generated at: " + new Date().toString());
						handler.contextRequest("org.example.dynamixplugin", CONTEXT_TYPE_COMMAND, echoRequest,
								new ContextRequestCallback() {
									public void onSuccess(ContextResult result) throws RemoteException {
										// Make sure we have attached IContextInfo
										if (result.hasIContextInfo()) {
											/*
											 * Results are attached. In this case, the result is send using a
											 * IBundleContextInfo, which can be handled as follows:
											 */
											if (result.getIContextInfo() instanceof IBundleContextInfo) {
												IBundleContextInfo resultBundle = (IBundleContextInfo) result
														.getIContextInfo();
												String echoResponse = resultBundle.getData().getString("echoResponse");
												Log.i(TAG, "Received echo from "
														+ result.getResultSource().getPluginId() + ": " + echoResponse);
											}
										} else
											Log.w(TAG, "No data in context result from: " + result.getResultSource());
									};

									public void onFailure(String message, int errorCode) throws RemoteException {
										Log.i(TAG, "Received fail: " + message);
									};
								});
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
		});
		// Setup the plug-in config button
		Button btnConfigPlugin = (Button) findViewById(R.id.btnConfigExamplePlugin);
		btnConfigPlugin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dynamix != null) {
					try {
						dynamix.openDefaultContextPluginConfigurationView("org.example.dynamixplugin");
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				} else
					Log.w(TAG, "Dynamix not connected.");
			}
		});
		// Setup the Dynamix info button
		Button btnGetDynamixInfo = (Button) findViewById(R.id.getDynamixInfo);
		btnGetDynamixInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (dynamix != null) {
						// Log Dynamix Version
						Log.i(TAG, "Dynamix Framework Version: " + dynamix.getDynamixVersion());
						// Log all known plug-ins
						for (ContextPluginInformation info : dynamix.getAllContextPluginInformation()) {
							Log.i(TAG, "Plug-in Name: " + info.getPluginName());
							Log.i(TAG, "Plug-in ID: " + info.getPluginId());
							Log.i(TAG, "Plug-in Install Status: " + info.getInstallStatus());
						}
					}
					if (handler != null) {
						// Log information about the handler's installed context support.
						for (ContextSupportInfo support : handler.getContextSupport()) {
							Log.i(TAG, "Context Support Type: " + support.getContextType());
							Log.i(TAG, "Context Support Plug-ins: " + support.getPlugins());
						}
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		// Setup the disconnect button
		Button btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		btnDisconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DynamixConnector.closeConnection();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/*
		 * Close our connection with Dynamix so the Activity doesn't leak its service connection.
		 */
		DynamixConnector.closeConnection();
		Log.i(TAG, "ON DESTROY for Dynamix Simple Logger");
	}

	/*
	 * Utility method that handles connecting to Dynamix.
	 */
	private void doConnect() {
		try {
			/*
			 * An example of using the DynamixConnector to easily access the Dynamix framework running on the device.
			 * Here we pass in a SessionCallback and wait for the onSuccess event to be raised. We then create a context
			 * handler, passing in a ContextHandlerCallback and waiting for onSuccess before registering for context
			 * support.
			 */
			DynamixConnector.openConnection(FirstActivity.this, true, sessionListener, new SessionCallback() {
				@Override
				public void onSuccess(DynamixFacade facade) throws RemoteException {
					/*
					 * At this point, the Dynamix session has been opened for the app, since we passed 'true' as the
					 * second argument to 'DynamixConnector.openConnection'.
					 */
					Log.i(TAG, "openConnection.onSuccess with " + facade);
					// Store the facade
					dynamix = facade;
					// Check for context handler
					if (handler != null)
						// We have a handler, so register for context support
						registerForContextSupport(handler);
					else {
						// We don't have a handler, so create one
						dynamix.createContextHandler(new ContextHandlerCallback() {
							@Override
							public void onSuccess(ContextHandler handler) throws RemoteException {
								/*
								 * The handler was successfully created. Setup context support.
								 */
								Log.i(TAG, "createContextHandler.onSuccess with " + handler);
								// Store the handler globally
								FirstActivity.this.handler = handler;
								// Register for context support
								registerForContextSupport(handler);
							}

							@Override
							public void onFailure(String message, int errorCode) throws RemoteException {
								/*
								 * The handler failed to be created. Log the error message and close the connection with
								 * Dynamix.
								 */
								Log.w(TAG, "createContextHandler.onFailure " + message);
								DynamixConnector.closeConnection();
							}
						});
					}
				}

				@Override
				public void onFailure(String message, int errorCode) throws RemoteException {
					/*
					 * Failed to open a session with Dynamix. Close the connection.
					 */
					Log.w(TAG, "openConnection.onFailure " + message);
					DynamixConnector.closeConnection();
				}
			});
		} catch (RemoteException e) {
			// Log any errors
			Log.e(TAG, e.toString());
		}
	}

	/*
	 * Utility method that demonstrates several ways of adding context support. This example, omits plug-in versions,
	 * which installs the latest plug-in; however, there are several handler methods that support setting up context
	 * support using a specific version of a plug-in.
	 */
	private void registerForContextSupport(ContextHandler handler) throws RemoteException {
		Log.i(TAG, "registerForContextSupport for " + handler);
		/*
		 * Method 1: We can simply call 'addContextSupport' with a plug-in id and context type. In this case, we won't
		 * know exactly when the context support will be added (if at all) as the method operates asynchronously and
		 * we're not passing in a callback. The 'addContextSupport' methods without a callback return a Result object,
		 * which indicates whether or not the request was accepted by Dynamix (not whether the request succeeded). If
		 * you need information about the installation of context support, use the 'addContextSupport' method with a
		 * callback as shown below.
		 */
		logResult(handler.addContextSupport("org.example.dynamixplugin", "org.example.dynamixplugin.commandtest"));
		/*
		 * Method 2: We can also call 'addContextSupport' passing in a ContextSupportCallback, which provides a
		 * notification when the context support has been added (including installation progress updates). Note that you
		 * may override only the callback events you wish to receive (e.g., implementing 'onSuccess' and 'onFailure' but
		 * not 'onProgress' or 'onWarning').
		 */
		handler.addContextSupport("org.ambientdynamix.contextplugins.barcode",
				"org.ambientdynamix.contextplugins.barcode", new ContextSupportCallback() {
					@Override
					public void onSuccess(ContextSupportInfo supportInfo) throws RemoteException {
						Log.i(TAG, "addContextSupportWithListenerAndCallback.onSuccess: " + supportInfo);
					}

					@Override
					public void onFailure(String message, int errorCode) throws RemoteException {
						Log.w(TAG, "addContextSupportWithListenerAndCallback.onFailure: " + message);
					}

					@Override
					public void onProgress(int progress) throws RemoteException {
						Log.i(TAG, "addContextSupportWithListenerAndCallback.onProgress: " + progress);
					}
				});
		/*
		 * Method 3: In addition to passing in a ContextSupportCallback, you can also provide an IContextListener, which
		 * will receive ongoing events from plug-ins that support listeners.
		 */
		handler.addContextSupport("org.ambientdynamix.contextplugins.batterylevel",
				"org.ambientdynamix.contextplugins.batterylevel", contextListener, new ContextSupportCallback() {
					@Override
					public void onSuccess(ContextSupportInfo sup) throws RemoteException {
						Log.i(TAG, "addContextSupportWithListenerAndCallback.onSuccess: " + sup);
					}

					@Override
					public void onFailure(String message, int arg1) throws RemoteException {
						Log.w(TAG, "addContextSupportWithListenerAndCallback.onFailure: " + message);
					}
					/*
					 * Note that we don't override 'onProgress' in this example. You can override only the events you
					 * need from any concrete callback or listener. Raw interfaces require that you implement all
					 * methods (i.e., IContextSupportCallback vs ContextSupportCallback). Dynamix provides concrete
					 * implementations of all callbacks and listeners, and are the recommended classes to use.
					 */
				});
	}

	/*
	 * Example listener that receives important ongoing information about our session with Dynamix.
	 */
	private ISessionListener sessionListener = new SessionListener() {
		@Override
		public void onSessionOpened(String sessionId) throws RemoteException {
			Log.i(TAG, "onSessionOpened");
		}

		@Override
		public void onSessionClosed() throws RemoteException {
			Log.i(TAG, "onSessionClosed");
			dynamix = null;
			handler = null;
		}

		@Override
		public void onContextPluginDisabled(org.ambientdynamix.api.application.ContextPluginInformation plug)
				throws RemoteException {
			Log.i(TAG, "onContextPluginDisabled: " + plug);
		}

		@Override
		public void onContextPluginEnabled(org.ambientdynamix.api.application.ContextPluginInformation plug)
				throws RemoteException {
			Log.i(TAG, "onContextPluginEnabled: " + plug);
		}
		// Other events omitted for brevity. See JavaDocs for details.
	};
	/*
	 * Example listener that is used to receive ongoing information from the battery level plug-in. In this case,
	 * whenever the battery level changes, this listener will automatically receive an context result event.
	 */
	private IContextListener contextListener = new ContextListener() {
		@Override
		public void onContextResult(ContextResult result) throws RemoteException {
			logContextResult(result);
		}

		@Override
		public void onContextListenerRemoved() throws RemoteException {
			Log.w(TAG, "onContextListenerRemoved");
		}

		@Override
		public void onContextSupportRemoved(ContextSupportInfo support, String message, int errorCode)
				throws RemoteException {
			Log.w(TAG, "onContextSupportRemoved: " + support + " - " + message);
		}
	};

	/*
	 * Utility method that logs various information about the context result.
	 */
	private void logContextResult(ContextResult result) throws RemoteException {
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
		}
		// Check for native IContextInfo
		if (result.hasIContextInfo()) {
			Log.i(TAG, "Event contains native IContextInfo: " + result.getIContextInfo());
			IContextInfo nativeInfo = result.getIContextInfo();
			/*
			 * Note: At this point you can cast the IContextInfo into its native type and then call its methods. In
			 * order for this to work, you'll need to have the proper Java classes for the IContextInfo data types on
			 * your app's classpath. If you don't, event.hasIContextInfo() will return false and event.getIContextInfo()
			 * would return null, meaning that you'll need to rely on the string representation of the context info. To
			 * use native context data-types, simply download the data-types JAR for the Context Plug-in you're
			 * interested in, include the JAR(s) on your build path, and you'll have access to native context type
			 * objects instead of strings.
			 */
			Log.i(TAG, "IContextInfo implimentation class: " + nativeInfo.getImplementingClassname());
			if (nativeInfo instanceof IBatteryLevelInfo) {
				IBatteryLevelInfo info = (IBatteryLevelInfo) nativeInfo;
				Log.i(TAG, "Received IBatteryLevelInfo with level: " + info.getBatteryLevel());
			}
			// Example of using IPedometerStepInfo
			if (nativeInfo instanceof IPedometerStepInfo) {
				IPedometerStepInfo stepInfo = (IPedometerStepInfo) nativeInfo;
				Log.i(TAG, "Received IPedometerStepInfo with RmsStepForce: " + stepInfo.getRmsStepForce());
			}
			// Example of using ISamplePluginContextInfo
			if (nativeInfo instanceof ISamplePluginContextInfo) {
				ISamplePluginContextInfo info = (ISamplePluginContextInfo) nativeInfo;
				Log.i(TAG, "Received ISamplePluginContextInfo with sample data: " + info.getSampleData());
			}
			// Example of using IAmbientSoundContextInfo
			if (nativeInfo instanceof IAmbientSoundContextInfo) {
				IAmbientSoundContextInfo info = (IAmbientSoundContextInfo) nativeInfo;
				Log.i(TAG, "Received IAmbientSoundContextInfo with dB value: " + info.getDbValue());
			}
			// Example of using INfcTag
			if (nativeInfo instanceof INfcTag) {
				INfcTag tagInfo = (INfcTag) nativeInfo;
				Log.i(TAG, "Received INfcTag with tag value: " + tagInfo.getTagIdAsString());
			}
			// Example of using IBarcodeContextInfo
			if (nativeInfo instanceof IBarcodeContextInfo) {
				IBarcodeContextInfo info = (IBarcodeContextInfo) nativeInfo;
				Log.i(TAG, "Received IBarcodeContextInfo with format  " + info.getBarcodeFormat() + " and value "
						+ info.getBarcodeValue());
			}
			// Example of using BundleContextInfo within the SampleContextPlugin
			if (result.getResultSource().getPluginId()
					.equalsIgnoreCase("org.ambientdynamix.contextplugins.sampleplugin")) {
				// Ensure the event contains a BundleContextInfo
				if (result.getIContextInfo() instanceof BundleContextInfo) {
					BundleContextInfo bundleInfo = (BundleContextInfo) result.getIContextInfo();
					for (String key : bundleInfo.getData().keySet()) {
						Log.i(TAG, "BundleContextInfo " + key + " contained data " + bundleInfo.getData().get(key));
					}
				}
			}
			// Check for other interesting types, if needed...
		} else
			Log.i(TAG, "Event does NOT contain native IContextInfo... we need to rely on the string representation!");
	}

	/*
	 * Utility method that outputs the result of Dynamix method calls.
	 */
	private void logResult(Result result) {
		if (result != null) {
			if (result.wasSuccessful()) {
				if (result instanceof IdResult) {
					Log.i(TAG, "Request was accepted by Dynamix: " + ((IdResult) result).getId());
				} else {
					Log.i(TAG, "Request was accepted by Dynamix");
				}
			} else {
				Log.w(TAG,
						"Request failed! Message: " + result.getMessage() + " | Error code: " + result.getErrorCode());
			}
		} else
			Log.w(TAG, "Result was null!");
	}
}