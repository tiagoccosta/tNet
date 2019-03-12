package com.tcc.androidchatapp;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.content.*;
import android.view.View.*;
import android.view.*;

import tNet.*;
import tNet.core.*;
import tNet.tools.*;
import tNet.events.*;
import java.util.*;

public class MainActivity extends Activity 
{
	
	NetworkManager manager;
	TextView textViewServerIP;
	TextView textViewConsole;
	EditText editTextAddressToConnect;
	Button btnHost;
	Button btnClient;
	Button btnSendMsg;
	EditText editTextMsgToSend;
	ProgressDialog pd;
	boolean clientInitialized = false;
	boolean chatInitialized = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		InitializeChat();
		
		
	}
	
	void InitializeChat(){
		btnHost = findViewById(R.id.btn_CreateHost);
		btnClient = findViewById(R.id.btn_JoinClient);

		textViewServerIP = findViewById(R.id.textView_HostAddress);
		textViewConsole = findViewById(R.id.textView_console);
		editTextAddressToConnect = findViewById(R.id.editText_ClientAddress);
		
		btnSendMsg = findViewById(R.id.btn_SendMsg);
		editTextMsgToSend = findViewById(R.id.editText_MsgToSend);
		
		btnHost.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				startServer();
			}
		});
		btnClient.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					startClient();
				}
			});
		btnSendMsg.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					sendMessage();
				}
			});
		
		manager = new NetworkManager();
		
		/*{
			@Override
			public void onStartServer(){
				super.onStartServer();
				chatInitialized = true;
				
				pd.dismiss();
				PrintChatMsg("--->>> SERVER STARTED <<<--- \n"+
							 " *IP: "+NetworkManager.getIpAddress()+"\n"+
							 " *PORT: "+manager.getPort());
							 
				runOnUiThread(new Runnable(){
						public void run(){

							btnHost.setText("Stop Server");
							btnHost.setOnClickListener(new OnClickListener(){
									@Override
									public void onClick(View v){
										StopServer();
									}	
								});
						}
					});
			}
			@Override
			public void onStartClient(ConnectionID conn){
				super.onStartClient(conn);
				chatInitialized = true;
				clientInitialized = true;
				
				pd.dismiss();
				PrintChatMsg("--->>> CLIENTE CREATED <<<--- \n"+
							 " *Is Server: "+manager.isServer());
				
				manager.sendToAll(
					"hello Chat!"
				);
				
				runOnUiThread(new Runnable(){
					public void run(){
				
						btnClient.setText("Stop Client");
						btnClient.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								StopClient();
							}	
						});
					}
				});
			}
			
			@Override
			public void OnReceiveCustomizedData(NetworkMessage msg, boolean isReceivedInServer){
				super.OnReceiveCustomizedData(msg, isReceivedInServer);
				
				if(!isReceivedInServer){
					PrintChatMsg("Client "+msg.getSenderID().id+" -> "+(String)msg.getData());
				}else{
					if(!isClient()){
						PrintChatMsg("(Server) Client "+msg.getSenderID().id+" -> "+(String)msg.getData());
					}
				}
					
			}
			
			@Override
			public void onServerStop(){
				PrintChatMsg("Server  closed!");
				runOnUiThread(new Runnable(){
						public void run(){

							btnHost.setText("Start Sever");
							btnHost.setOnClickListener(new OnClickListener(){
									@Override
									public void onClick(View v){

										pd = new ProgressDialog(MainActivity.this);
										pd.setMessage("Initializing server...");
										pd.setCancelable(false);
										pd.show();
										StartServer(8080);
									}	
								});
						}
					});
			}
			
			@Override
			public void onClientStop(int  disconnectionCode){
				super.onClientStop(disconnectionCode);
				switch(disconnectionCode){
					case DisconnectionCode.NO_RESPONSE:{
							PrintChatMsg("Disconnected with no server response!");
						break;
					}
					case DisconnectionCode.DISCONNECT_BY_REQUEST:{
							PrintChatMsg("Disconnection autorized!");
							break;
						}
					case DisconnectionCode.DISCONNECT_BY_SERVER:{
							PrintChatMsg("Disconnected by server!");
							break;
						}
				}
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
										StartClient(address,8080);
									}	
								});
						}
					});
			}
			
			@Override
			public void errorOnStartServer(int code){
				pd.dismiss();
				PrintChatMsg("--->>> Error to start Server <<<--- \n"+
							 " *IP: "+NetworkManager.getIpAddress()+"\n"+
							 " *PORT: "+manager.getPort());
				
			}
			
			@Override
			public void errorOnStartClient(int code){
				super.errorOnStartClient(code);

				pd.dismiss();
				PrintChatMsg("--->>> Error to start client <<<--- \n"+
							 " *IP: "+NetworkManager.getIpAddress()+"\n"+
							 " *PORT: "+manager.getPort());

			}
		
		};
		*/
		manager.setOnStartServerListener(
			new OnConnectionStartListener(){
				public void run(){
					chatInitialized = true;

					pd.dismiss();
					printChatMsg("--->>> SERVER STARTED <<<--- \n"+
								 " *IP: "+NetworkManager.getIpAddress()+"\n"+
								 " *PORT: "+manager.getPort());
								 
					runOnUiThread(new Runnable(){
							public void run(){

								btnHost.setText("Stop Server");
								btnHost.setOnClickListener(new OnClickListener(){
										@Override
										public void onClick(View v){
											manager.StopServer();
										}	
									});
							}
						});
				}
			}
		);
		manager.setOnStartClientListener(
			new OnConnectionStartListener(){
				public void run(){
					chatInitialized = true;
					clientInitialized = true;

					pd.dismiss();

					printChatMsg("--->>> CLIENTE CREATED <<<--- \n"+
								 " *Is Server: "+manager.isServer());
								 
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
		manager.setErrorListennerOnStartServer(
			new ConnectionInterruptListener(){
				public void run(int code){
					pd.dismiss();
					printChatMsg("Error on start server. Error code: "+code);
				}
			}
		);
		manager.setErrorListennerOnStartClient(
			new ConnectionInterruptListener(){
				public void run(int code){
					pd.dismiss();
					printChatMsg("Error on start client. Error code: "+code);
				}
			}
		);
		manager.setOnStopServerListener(
			new ConnectionInterruptListener(){
				public void run(int code){
					switch(code){
						case tNet.constants.DisconnectionCode.DISCONNECT_BY_REQUEST:{
								printChatMsg("Server stop. Disconnected by request.");
								break;
							}
						case tNet.constants.DisconnectionCode.DISCONNECT_BY_SERVER:{
								printChatMsg("Server stop. Disconnected by server.");
								break;
							}
						case tNet.constants.DisconnectionCode.NO_RESPONSE:{
								printChatMsg("Server stop. No server response.");
								break;
							}
					}

					chatInitialized = false;
					runOnUiThread(new Runnable(){
							public void run(){

								btnHost.setText("Start Sever");
								btnHost.setOnClickListener(new OnClickListener(){
										@Override
										public void onClick(View v){
											startServer();
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
					switch(code){
						case tNet.constants.DisconnectionCode.DISCONNECT_BY_REQUEST:{
								printChatMsg("Client stop. Disconnected by request.");
								break;
							}
						case tNet.constants.DisconnectionCode.DISCONNECT_BY_SERVER:{
								printChatMsg("Client stop. Disconnected by server.");
								break;
							}
						case tNet.constants.DisconnectionCode.NO_RESPONSE:{
								printChatMsg("Client stop. No server response.");
								break;
							}
					}
					
					clientInitialized = false;
					runOnUiThread(new Runnable(){
							public void run(){

								btnClient.setText("Start Client");
								btnClient.setOnClickListener(new OnClickListener(){
										@Override
										public void onClick(View v){
											startClient();
										}	
									});
							}
						});
				}

			}
		);
		manager.setOnUpdateClientListListener(
			new OnUpdateClientListListener(){
				public void run(List<ConnectionID> clients){
					String result = "Client list updated \n";
					for(ConnectionID id :clients){
						result += "Client "+id.toInt()+" \n";
					}
					printChatMsg(result);
				}
			}
		);
		manager.setOnReceiveDataInServerListener(
			new OnReceiveDataListener(){
				public void run(NetworkMessage msg){
					if(!manager.isClient())
						printChatMsg("(Server) Client "+msg.getSenderID().id+" -> "+msg.getData());
				}
			}
		);
		manager.setOnReceiveDataInClientListener(
			new OnReceiveDataListener(){
				public void run(NetworkMessage msg){
					printChatMsg("Client "+msg.getSenderID().id+" -> "+msg.getData());
				}
			}
		);

		textViewServerIP.setText(NetworkManager.getIpAddress());
		
    }
	
	public void startServer(){
		if(chatInitialized)
			return;
		pd = new ProgressDialog(MainActivity.this);
		pd.setMessage("Initializing server...");
		pd.setCancelable(false);
		pd.show();
		
		manager.startServer (8080);
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
	
	void sendMessage(){
		if(!clientInitialized)
			return;
		String msgText = editTextMsgToSend.getText().toString();
		manager.sendToAll(
			msgText);
		editTextMsgToSend.setText("");
	}
	
	void printChatMsg(final String msg){
		runOnUiThread(new Runnable(){
				public void run(){
					textViewConsole.setText(msg+"\n \n"+
											textViewConsole.getText());
				}
			});
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		//manager.AppQuiting();
	}

	@Override
	protected void onPause()
	{
		// TODO: Implement this method
		super.onPause();
	}
	
	
	
}
