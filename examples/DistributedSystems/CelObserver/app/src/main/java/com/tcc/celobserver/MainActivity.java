package com.tcc.celobserver;

import android.app.*;
import android.os.*;
import android.hardware.*;
import android.renderscript.*;
import android.content.*;
import android.util.*;
import android.widget.*;
import android.view.*;
import java.io.*;
import tNet.*;
import java.util.*;
import android.view.View.*;
import tNet.events.*;

public class MainActivity extends Activity 
{
	NetworkManager manager;
	//Camera camera;
	//CameraPreview cameraPreview;
	//Camera.Size size;
	Context context;

	LinearLayout imgView;
	
	EditText editTextAddressToConnect;
	//Button btnHost;
	Button btnClient;
	//Button btnSendMsg;
	//EditText editTextMsgToSend;
	ProgressDialog pd;

	private List<ConnectionID> cameras = new ArrayList<ConnectionID>();
	public ConnectionID[] getCameras () {
		synchronized(cameras){
			return cameras.toArray( new ConnectionID[cameras.size()]);

		}
	}


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		context = this;

		imgView = findViewById(R.id.imageView);

		btnClient = findViewById(R.id.btn_JoinClient);

		editTextAddressToConnect = findViewById(R.id.editText_ClientAddress);

		
		btnClient.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					startClient();
				}
			});


		manager = new NetworkManager();
	
		manager.setOnStartClientListener(
			new OnConnectionStartListener(){
				public void run(){
					pd.dismiss();
					manager.sendToAll("","request_cam_info");
					runOnUiThread(new Runnable(){
							public void run(){
								btnClient.setText("Stop Client");
								btnClient.setOnClickListener(new OnClickListener(){
										@Override
										public void onClick(View v){
											manager.StopClient();
										}	
									});
							}
						});
				}
			}
		);
		
		manager.setOnStopClientListener(
			new ConnectionInterruptListener(){
				public void run(int code){
					runOnUiThread(new Runnable(){
							public void run(){
								btnClient.setText("Start Client");
								btnClient.setOnClickListener(new OnClickListener(){
										@Override
										public void onClick(View v){
											pd = new ProgressDialog(MainActivity.this);
											pd.setMessage("Initializing client...");
											pd.setCancelable(false);
											pd.show();

											String address = editTextAddressToConnect.getText().toString();
											System.out.println("Starting client.... "+address);
											manager.startClient(address,8080);
										}	
									});
							}
						});
				}
			}
		);
		manager.setErrorListennerOnStartClient(
			new ConnectionInterruptListener(){
				public void run(int code){
					pd.dismiss();
				}
			}
		);
		manager.setErrorListennerOnStartServer(
			new ConnectionInterruptListener(){
				public void run(int code){
					pd.dismiss();
				}
			}
		);
		manager.setOnReceiveDataInClientListener(
			new OnReceiveDataListener(){
				public void run(NetworkMessage msg){
					if(msg.getKey().equals("info")){
						if(!cameras.contains(msg.getSenderID())){
							cameras.add(msg.getSenderID());
						}
						//manager.sendToClients("","cam_info", new ConnectionID[]{msg.getSenderID()});
					}
					if(msg.getKey().equals("image")){
						byte[] data = (byte[])msg.getData();
						setImage(data);
						//manager.sendToClients("","cam_info", new ConnectionID[]{msg.getSenderID()});
					}
				}
			});
		manager.setOnUpdateClientListListener( 
			new OnUpdateClientListListener(){
				public void run(List<ConnectionID> data){
					List<ConnectionID> clients = data;
					ConnectionID[] observersCopy = cameras.toArray(new ConnectionID[cameras.size()]);
					for(ConnectionID observer:observersCopy){
						if(clients.contains(observer)){
							cameras.remove(observer);
						}
					}
					//manager.sendToAll("","request_cam_info");
				}
			}
		);
		
        
    }


	public void setImage(final byte[] data){
		runOnUiThread(new Runnable(){
			public void run(){
				android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(data,0,data.length);
				
			}
		});
	}

	public void startClient(){
		//if(clientInitialized)
		//	return;
		pd = new ProgressDialog(MainActivity.this);
		pd.setMessage("Initializing client...");
		pd.setCancelable(false);
		pd.show();

		String address = editTextAddressToConnect.getText().toString();
		System.out.println("Starting client.... "+address);
		manager.startClient(address,8080);
	}

}


