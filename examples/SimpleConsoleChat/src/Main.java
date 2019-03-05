import java.util.*;
import tNet.*;
import tNet.core.*;
import tNet.events.*;


public class Main
{
	
	static NetworkManager manager;
	static boolean chatInitialized = false;
	static boolean clientInitialized = false;
	static Scanner input;
	
	public static void main(String[] args)
	{
		PrintChatMsg("This is a chat application to show library tNet",false);
		
		PrintInfo();
		
		input = new Scanner(System.in);

		InitializeManager();
		loop();
		
	}
	
	
	static void loop(){
		while(true){
			String s = input.nextLine();
			boolean isCommand = false;
			if(s.charAt(0)=='$'){
				//System.out.println("Is Command action => "+s);
				isCommand = true;
				String[] command;
				if(s.contains(":")){
					command = s.split(":");
					//System.out.println("Command => "+command[0]);
					//System.out.println("Action => "+command[1]);
				}else{
					command = new String[]{s};
				}

				if(command[0].equals( "$StartServer") || command[0].equals( "$1")){
					String port = command[1];
					manager.startServer(Integer.parseInt(port));
				}
				if(command[0].equals( "$StartHost") || command[0].equals( "$2")){
					String port = command[1];
					manager.startHost(Integer.parseInt(port));
				}
				if(command[0].equals("$StartClient") || command[0].equals( "$3")){
					String[] info = command[1].split(";");
					String ip = info[0];
					int port = Integer.parseInt(info[1]);
					//System.out.println("Intializing client. IP: "+ip+" |Port: "+port); 
					manager.startClient(ip,port);
				}
				if(command[0].equals( "$KickClient") || command[0].equals( "$4")){
					String port = command[1];
					manager.DisconnectClient(new ConnectionID( Integer.parseInt(port)));
				}
				if(command[0].equals("$StopServer") || command[0].equals( "$5")){
					manager.StopServer();
				}
				if(command[0].equals("$StopClient") || command[0].equals( "$6")){
					manager.StopClient();
				}
				if(command[0].equals("$ShowClientList") || command[0].equals( "$7")){
					ConnectionID[] clients = manager.getClients();
					String result = "All clients: \n";
					for(ConnectionID client :clients){
						if(client.id == 0){result+= "(SERVER)";}
						result += "Client "+client.toInt()+" \n";
					}
					PrintChatMsg(result);
				}
				if(command[0].equals("$Help") || command[0].equals( "$0")){
					PrintInfo();
				}
			}
			if(!isCommand){
				if(chatInitialized){
					manager.sendToAll(s);
				}
			}
		}
	}
	
	static void PrintInfo(){
		String info = "Commands: \n"
			+ " 0) $Help \n"
			+ " 1) $StartServer:Port \n"
			+ " 2) $StartHost:Port \n"
			+ " 3) $StartClient:IP|Port \n"
			+ " 4) $KickClient:ID \n"
			+ " 5) $StopServer \n"
			+ " 6) $StopClient \n"
			+ " 7) $ShowClientList";
		PrintChatMsg(info,true);
	}
	
	static void PrintChatMsg(String msg){
		PrintChatMsg(msg,true);
	}
	static void PrintChatMsg(String msg, boolean dropLine){
		if(!dropLine){
			System.out.println("\n" + msg);
		}else{
			System.out.println("\n" + msg + "\n");
		}
		
	}
	static void InitializeManager(){
		manager = new NetworkManager();
		manager.setOnStartServerListener(
			new OnConnectionStartListener(){
				public void run(){
					chatInitialized = true;

					PrintChatMsg("--->>> SERVER STARTED <<<--- \n"+
								 " *IP: "+NetworkManager.getIpAddress()+"\n"+
								 " *PORT: "+manager.getPort());
				}
			}
		);
		manager.setOnStartClientListener(
			new OnConnectionStartListener(){
				public void run(){
					chatInitialized = true;
					clientInitialized = true;

					PrintChatMsg("--->>> CLIENTE CREATED <<<--- \n"+
								 " *Is Server: "+manager.isServer());
				}
			}
		);
		manager.setErrorListennerOnStartServer(
			new ConnectionInterruptListener(){
				public void run(int code){
					PrintChatMsg("Error on start server. Error code: "+code);
				}
			}
		);
		manager.setErrorListennerOnStartClient(
			new ConnectionInterruptListener(){
				public void run(int code){
					PrintChatMsg("Error on start client. Error code: "+code);
				}
			}
		);
		manager.setOnStopServerListener(
			new ConnectionInterruptListener(){
				public void run(int code){
					switch(code){
						case tNet.tools.DisconnectionCode.DISCONNECT_BY_REQUEST:{
								PrintChatMsg("Server stop. Disconnected by request.");
								break;
							}
						case tNet.tools.DisconnectionCode.DISCONNECT_BY_SERVER:{
								PrintChatMsg("Server stop. Disconnected by server.");
								break;
							}
						case tNet.tools.DisconnectionCode.NO_RESPONSE:{
								PrintChatMsg("Server stop. No server response.");
								break;
							}
					}
				}
			}
		);
		manager.setOnStopClientListener(
			new ConnectionInterruptListener(){
				public void run(int code){
					switch(code){
						case tNet.tools.DisconnectionCode.DISCONNECT_BY_REQUEST:{
								PrintChatMsg("Client stop. Disconnected by request.");
								break;
							}
						case tNet.tools.DisconnectionCode.DISCONNECT_BY_SERVER:{
								PrintChatMsg("Client stop. Disconnected by server.");
								break;
							}
						case tNet.tools.DisconnectionCode.NO_RESPONSE:{
								PrintChatMsg("Client stop. No server response.");
								break;
							}
					}
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
					PrintChatMsg(result);
				}
			}
		);
		manager.setOnReceiveDataInServerListener(
			new OnReceiveDataListener(){
				public void run(NetworkMessage msg){
					if(!manager.isClient())
						PrintChatMsg("(Server) Client "+msg.getSenderID().id+" -> "+msg.getData());
				}
			}
		);
		manager.setOnReceiveDataInClientListener(
			new OnReceiveDataListener(){
				public void run(NetworkMessage msg){
					PrintChatMsg("Client "+msg.getSenderID().id+" -> "+msg.getData());
				}
			}
		);
		/*
			@Override
			public void onStartServer(){
				super.onStartServer();
				chatInitialized = true;

				PrintChatMsg("--->>> SERVER STARTED <<<--- \n"+
							 " *IP: "+NetworkManager.getIpAddress()+"\n"+
							 " *PORT: "+manager.getPort());

			}
			@Override
			public void onStartClient(ConnectionID conn){
				super.onStartClient(conn);
				chatInitialized = true;
				clientInitialized = true;

				PrintChatMsg("--->>> CLIENTE CREATED <<<--- \n"+
							 " *Is Server: "+manager.isServer());

			}

			@Override
			public void OnReceiveCustomizedData(NetworkMessage msg, boolean isReceivedInServer){
				super.OnReceiveCustomizedData(msg, isReceivedInServer);

				if(!isReceivedInServer){
					PrintChatMsg("Client "+msg.getSenderID().id+" -> "+msg.getData());
				}else{
					PrintChatMsg("(Server) Client "+msg.getSenderID().id+" -> "+msg.getData());
				}

			}
		};
		*/
	}
}
