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
					if(command.length>1){
						String port = command[1];
						manager.startServer(Integer.parseInt(port));
					}else{
						PrintChatMsg("Parameter invalid!");
					}
				}
				if(command[0].equals( "$KickClient") || command[0].equals( "$2")){
					if(command.length>1){
						String port = command[1];
						manager.DisconnectClient(new ConnectionID( Integer.parseInt(port)));
					}else{
						PrintChatMsg("Parameter invalid!");
					}
				}
				if(command[0].equals("$StopServer") || command[0].equals( "$3")){
					manager.StopServer();
				}
				if(command[0].equals("$SendMessageToAll") || command[0].equals( "$4")){
					if(command.length>1 && command[1].contains(";")){
						String[] info = command[1].split(";");
						String key = info[0];
						String data = info[1];
						manager.sendToAll(data,key);
					}else{
						PrintChatMsg("Parameter invalid!");
					}
				}
				if(command[0].equals("$SendMessageToClient") || command[0].equals( "$5")){
					if(command.length>1 && command[1].contains(";")){
						String[] info = command[1].split(";");
						String key = info[0];
						String data = info[1];
						ConnectionID id = new ConnectionID(Integer.parseInt(info[2]));
						manager.sendToClients(data,key, new ConnectionID[]{id});
					}else{
						PrintChatMsg("Parameter invalid!");
					}
				}
				if(command[0].equals("$ShowClientList") || command[0].equals( "$6")){
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
		}
	}

	static final String info = "Commands: \n"
	+ " 0) $Help \n"
	+ " 1) $StartServer:Port \n"
	+ " 2) $KickClient:ID \n"
	+ " 3) $StopServer \n"
	+ " 4) $SendMessageToAll:StringKey;StringData \n"
	+ " 5) $SendMessageToClient:StringKey;StringData;intID1 \n"
	+ " 6) $ShowClientList";
	
	static void PrintInfo(){
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
		manager.setErrorListennerOnStartServer(
			new ConnectionInterruptListener(){
				public void run(int code){
					PrintChatMsg("Error on start server. Error code: "+code);
				}
			}
		);
		manager.setOnStopServerListener(
			new ConnectionInterruptListener(){
				public void run(int code){
					switch(code){
						case tNet.constants.DisconnectionCode.DISCONNECT_BY_REQUEST:{
								PrintChatMsg("Server stop. Disconnected by request.");
								break;
							}
						case tNet.constants.DisconnectionCode.DISCONNECT_BY_SERVER:{
								PrintChatMsg("Server stop. Disconnected by server.");
								break;
							}
						case tNet.constants.DisconnectionCode.NO_RESPONSE:{
								PrintChatMsg("Server stop. No server response.");
								break;
							}
					}
				}
			}
		);
		manager.setOnReceiveDataInServerListener(
			new OnReceiveDataListener(){
				public void run(NetworkMessage msg){
					//PrintChatMsg("(Server) Client "+msg.getSenderID().id+" -> "+msg.getData().toString());
				}
			}
		);
	}
}
