//package com.bc_aplicativos.wifichatapp;

package tNet;
import tNet.Action;

import java.net.*;
import java.io.*;
import java.util.*;
import tNet.core.*;
import tNet.connection.*;
import tNet.tools.*;
import tNet.events.*;
import tNet.constants.*;

public class NetworkManager
{
	
	private Network network;
	
	
	private boolean initialized = false;
	public boolean isInitialized(){return initialized;}
	
	private boolean isServer = false;
	public boolean isServer(){return this.isServer;}
	
	private boolean isClient = false;
	public boolean isClient(){return this.isClient;}
	
	private static boolean isConnected = false;
	public static boolean isConnected(){return isConnected;}

	private int port = 8080;
	public int getPort(){return port;}
	
	
	
	//Server variables	
	private Server server;
	private List<ConnectionID> clientsID = new ArrayList<ConnectionID>();
	public ConnectionID[] getClients(){
		ConnectionID[] ids= new ConnectionID[clientsID.size()];
		ids= clientsID.toArray(ids);
		return ids;
	}
	private void addClientToList(ConnectionID connID){
		synchronized(clientsID){

			clientsID.add(connID);
		}
		if(isServer()){
			synchronized(clientsPing){
				clientsPing.put(connID,new PingConnection(connID));
			}
		}
	}
	private void removeClientFromList(ConnectionID connID){
		synchronized(clientsID){
			clientsID.remove(connID);
		}
		if(isServer()){
			synchronized(clientsPing){
				clientsPing.remove(connID);
			}
		}
	}
	private void setClientList(List<ConnectionID> conns){
		synchronized(clientsID){
			clientsID.clear();
			clientsID.addAll(conns);
			
		}
		if(isServer()){
			synchronized(clientsPing){
				clientsPing.clear();
				clientsPing = PingConnection.CreateList(conns);
			}
		}
	}
	private Hashtable<ConnectionID,PingConnection> clientsPing = new Hashtable<ConnectionID,PingConnection>();
	
	private int maxClients = 99;
	public int getMaxClientsCount(){return maxClients;}
	
	private boolean startingHost = false;
	private boolean isHost = false;
	public boolean isHost(){return isHost;}
	
	private Client client;
	private ConnectionID clientID = null;
	private PingConnection clientPing;
	
	
	public NetworkManager(){
		//if(singleton==null){
		//	singleton=this;
			initializeNetworkManager();
		//}else{
		//	System.out.println("Just one NetworkManager can be initialized");
		//}
	}
	
	void ResetManager(){
		isServer = false;
		isClient = false;
		isHost = false;
		isConnected = false;
		clientsPing.clear();
		clientsID.clear();
		clientsID.add(ConnectionID.fromInt(0));
	}
	
	private void initializeNetworkManager (){
		network = new Network(
			new Runnable(){
				public void run(){
					NetworkLoop();
				}
		});
		ResetManager();
		//networkThread = new Thread(network);
		//networkThread.start();
		initialized = true;
		
	}
	
	public void startHost(final int port){
		if(isServer()){return;}
		if(isClient()){return;}
		startingHost = true;
		this.port = port;
		network.RunInNetworkThread(
			new Runnable(){
				@Override
				public void run(){
					initializeServer();
					//InitializeClient("127.0.0.1",port);
				}
			}
		);
		
		
	}
	
	public void startServer(int port){
		if(isServer()){return;}
		this.port = port;
		network.RunInNetworkThread(
			new Runnable(){
				@Override
				public void run(){
					initializeServer();
				}
			}
		);
	}
	private void initializeServer(){
		//System.out.println("Initializing server");
		server = new ServerTCP(
			this.port,
			new Action(){
				@Override
				public void Execute(final Object data){
					network.RunInNetworkThread(
						new Runnable(){
							@Override
							public void run(){
								onClientConnected((ConnectionID)data);
							}
						}
					);
				}
			},
			new Action(){
				@Override
				public void Execute(final Object data){
					network.RunInNetworkThread(
						new Runnable(){
							@Override
							public void run(){
								//System.out.println("Server received data: "+data);
								OnReceiveNetworkMessage(NetworkMessage.fromString((String)Serializer.deserialize((byte[])data)),true);
							}
						}
					);}
			},
			new Action(){
				@Override
				public void Execute(final Object data){
					network.RunInNetworkThread(
						new Runnable(){
							@Override
							public void run(){
								onStartServer();
							}
						}
					);
					
				}
			},
			new Action(){
				@Override
				public void Execute(final Object data){
					network.RunInNetworkThread(
						new Runnable(){
							@Override
							public void run(){
								errorOnStartServer((int)data);
							}
						}
					);

				}
			}
		);
		server.setMaxClientCount(getMaxClientsCount());
		//serverThread = new Thread(serverRunner);
		//serverThread.start();
		
	}
	
	public void startClient(final String ipAddress, final int port){
		if(isClient()){return;}
		this.port = port;
		network.RunInNetworkThread(
			new Runnable(){
				@Override
				public void run(){
					initializeClient(ipAddress, port);
				}
			}
		);
	}
	
	private void initializeClient(String ipAddress, int port){
		//System.out.println("Initializing client in address: "+ipAddress);
		client = new ClientTCP(
			ipAddress,
			port,
			new Action(){
				@Override
				public void Execute(final Object data){
					
					network.RunInNetworkThread(
						new Runnable(){
							@Override
							public void run(){
								//System.out.println("Client received data: "+data);
								OnReceiveNetworkMessage(NetworkMessage.fromString((String)(Serializer.deserialize((byte[])data))),false);
							}
						}
					);
				}
			},
			new Action(){
				@Override
				public void Execute(final Object data){

					network.RunInNetworkThread(
						new Runnable(){
							@Override
							public void run(){
								//System.out.println("Client received data: "+data);
								errorOnStartClient(data);
							}
						}
					);
				}
			}
		);
		//clientRunner.setPort(port);
		//clientThread = new Thread(clientRunner);
		//clientThread.start();
	}
	
	public void StopClient(){
		network.RunInNetworkThread(new Runnable(){
			public void run(){
				NetworkMessage msg = new NetworkMessage(MessageType.UPDATE_CLIENT_STATE,"StopRequest",DisconnectionCode.DISCONNECT_BY_REQUEST,clientID);
				msg.setReceivers(new ConnectionID[]{ConnectionID.fromInt(0)});
				sendToServer(msg);
			}
		});
	}
	
	public void StopServer(){
		if(isServer()){
			/*
			if(isClient()){
				onClientStop(DisconnectionCode.DISCONNECT_BY_SERVER);
			}
			*/
			NetworkMessage msg = new NetworkMessage(MessageType.UPDATE_CLIENT_STATE,"Stop",DisconnectionCode.DISCONNECT_BY_SERVER,ConnectionID.fromInt(0));
			msg.setReceivers(clientsID);
			server.sendDataToAll(Serializer.serialize(msg.toString()));
			network.RunInNetworkThread(new Runnable(){
					public void run(){
						server.close();
						ResetManager();
						onServerStop(DisconnectionCode.DISCONNECT_BY_REQUEST);
					}
				}
			);
		}
	}
	
	private void onStartServer(){
		isServer = true;
		isConnected = true;
		if(startingHost){initializeClient("127.0.0.1",port);}
		//System.out.println("Server started");
		//System.out.println("onStartServer called in "+Thread.currentThread().getName()+" Thread");
		if(onStartServerListener!=null){
			onStartServerListener.run();
		}
	}
	private OnConnectionStartListener onStartServerListener = null;
	public void setOnStartServerListener(OnConnectionStartListener listener){
		this.onStartServerListener = listener;
	} 
	
	private void errorOnStartServer(int code){
		if(errorOnStartServerListener!=null){
			errorOnStartServerListener.run(code);
		}
	}
	private ConnectionInterruptListener errorOnStartServerListener = null;
	public void setErrorListennerOnStartServer(ConnectionInterruptListener listener){
		this.errorOnStartServerListener = listener;
	} 
	
	private void onStartClient(ConnectionID conn){
		isClient = true;
		isConnected = true;
		//System.out.println("Client started");
		if(onStartClientListener!=null){
			onStartClientListener.run();
		}
		//System.out.println("onStartClient called in "+Thread.currentThread().getName()+" Thread");
	}
	private OnConnectionStartListener onStartClientListener = null;
	public void setOnStartClientListener(OnConnectionStartListener listener){
		this.onStartClientListener = listener;
	} 
	
	private void errorOnStartClient(int code){
		if(errorOnStartClientListener!=null){
			errorOnStartClientListener.run(code);
		}
	}
	private ConnectionInterruptListener errorOnStartClientListener = null;
	public void setErrorListennerOnStartClient(ConnectionInterruptListener listener){
		this.errorOnStartClientListener = listener;
	} 
	
	//Called in server
	private void onClientConnected(ConnectionID conn){
		//System.out.println("Client connected. Id: "+conn.connectionID);
		addClientToList(conn);
		
		NetworkMessage msgAll = new NetworkMessage(
			MessageType.UPDATE_CONNECTION_LIST,
			ConnectionID.ConvertArrayConnectionID(clientsID), 
			ConnectionID.fromInt(0));
		msgAll.setReceivers(clientsID);

		sendToAll(msgAll);
		
		NetworkMessage msg = new NetworkMessage(MessageType.UPDATE_CLIENT_STATE, "Start", conn.id, ConnectionID.fromInt(0));
		msg.setReceivers(new ConnectionID[]{conn});
		server.sendData(Serializer.serialize(msg.toString()),conn);
		
		
		
		//System.out.println("onClientConnected called in "+Thread.currentThread().getName()+" Thread");
	}
	

	//Called on server
	private void onClientDisconnected(ConnectionID conn){
		removeClientFromList(conn);
		
		server.removeClient(conn);
		NetworkMessage msgAll = new NetworkMessage(MessageType.UPDATE_CONNECTION_LIST, ConnectionID.ConvertArrayConnectionID(clientsID), ConnectionID.fromInt(0));
		msgAll.setReceivers(clientsID);
		sendToAll(msgAll);

		//System.out.println("onClientDisconnected called in "+Thread.currentThread().getName()+" Thread");
	}

	public void stop(){
		if(isClient()) {
			StopClient();
		}
		if(isServer()){
			StopServer();
		}
		network.stop();
	}
	
	private void onServerStop(int disconnectionCode){
		if(onServerStopListener!=null){
			onServerStopListener.run(disconnectionCode);
		}
		//ResetManager();

		//System.out.println("onServerStop called in "+Thread.currentThread().getName()+" Thread");
	}
	private ConnectionInterruptListener onServerStopListener = null;
	public void setOnStopServerListener(ConnectionInterruptListener listener){
		this.onServerStopListener = listener;
	} 
	
	private void onClientStop(int disconnectionCode){
		client.close();
		isClient = false;
		if(onClientStopListener!=null){
			onClientStopListener.run(disconnectionCode);
		}
		//if(!isServer){ResetManager();}

		//System.out.println("onClientStop called in "+Thread.currentThread().getName()+" Thread");
	}
	private ConnectionInterruptListener onClientStopListener = null;
	public void setOnStopClientListener(ConnectionInterruptListener listener){
		this.onClientStopListener = listener;
	} 
	
	public void DisconnectClient(final ConnectionID id){
		if(isServer()){
			network.RunInNetworkThread(
				new Runnable(){
					public void run(){
						NetworkMessage msg = new NetworkMessage(MessageType.UPDATE_CLIENT_STATE,"Stop",DisconnectionCode.DISCONNECT_BY_SERVER,ConnectionID.fromInt(0));
						msg.setReceivers(new ConnectionID[]{id});
						server.sendData(
							Serializer.serialize(msg.toString()),
							id);
						onClientDisconnected(id);
					}
				}
			);
		}
	}
	
	public void sendToClients(final Object data, final ConnectionID[] targets){
		network.RunInNetworkThread(
			new Runnable(){
				public void run(){
					NetworkMessage msg = new NetworkMessage(MessageType.CUSTOMIZED_DATA, data, clientID);
					msg.setReceivers(targets);
					sendToServer(msg);
				}
			}
		);
		
	}
	public void sendToClients(final Object data, final String key, final ConnectionID[] targets){
		network.RunInNetworkThread(
			new Runnable(){
				public void run(){
					NetworkMessage msg = new NetworkMessage(MessageType.CUSTOMIZED_DATA, key, data, clientID);
					msg.setReceivers(targets);
					sendToServer(msg);
				}
			}
		);

	}
	
	public void sendToAll(final Object data){
		network.RunInNetworkThread(
			new Runnable(){
				public void run(){
					ConnectionID conn = clientID;
					if(conn == null){conn = new ConnectionID(0);}
					NetworkMessage msg = new NetworkMessage(MessageType.CUSTOMIZED_DATA, data, conn);
					sendToAll(msg);
				}
			}
		);
		
	}
	public void sendToAll(final Object data, final String key){
		network.RunInNetworkThread(
			new Runnable(){
				public void run(){
					ConnectionID conn = clientID;
					if(conn == null){conn = new ConnectionID(0);}
					NetworkMessage msg = new NetworkMessage(MessageType.CUSTOMIZED_DATA, key, data, conn);
					sendToAll(msg);
				}
			}
		);

	}
	private void sendToAll(NetworkMessage message) {
		message.setReceivers(
			clientsID
		);
		if(!isClient){
			if(!isServer){return;}
			this.server.sendDataToAll(Serializer.serialize(message.toString()));
		}else{
			
			sendToServer(message);
		}
    }
	
	public void sendToServer(final Object data){
		network.RunInNetworkThread(
			new Runnable(){
				public void run(){
					NetworkMessage msg = new NetworkMessage(MessageType.CUSTOMIZED_DATA, data, clientID);
					sendToServer(msg);
				}
			}
		);
		
	}
	public void sendToServer(final Object data, final String key){
		network.RunInNetworkThread(
			new Runnable(){
				public void run(){
					NetworkMessage msg = new NetworkMessage(MessageType.CUSTOMIZED_DATA, key, data, clientID);
					sendToServer(msg);
				}
			}
		);

	}
    private void sendToServer(NetworkMessage msg) {
        //SenderThread sendGameChange = new SenderThread(ClientConnectionThread.socket, msg);
		//sendGameChange.start();
		
		this.client.sendData(Serializer.serialize(msg.toString()));
        
    }
	
	
		
	private void OnReceiveNetworkMessage(NetworkMessage message, boolean isServerReceiver){
		//System.out.println("NetworkMessage received. Is server ="+isServerReceiver+" | Type = "+message.getType());
		//System.out.println("OnReceiveNetworkMessage called in "+Thread.currentThread().getName()+" Thread");
		if(isServerReceiver){
			if(message.getType() == MessageType.CUSTOMIZED_DATA){
				OnReceiveCustomizedData(message, isServerReceiver);
			}
			if(message.getType() == MessageType.CHECK_ACTIVITY){
				if(clientsPing.containsKey(message.getSenderID())){
					PingConnection ping = clientsPing.get(message.getSenderID());
					if(ping.checkResponse()){
						ping.pingReceived();
						//System.out.println("Server ping Client"+message.getSenderID().toString());
					}
				}
			}
			if(message.getType() == MessageType.UPDATE_CLIENT_STATE){
				if(message.getKey().equals("StopRequest")){
					NetworkMessage msg = new NetworkMessage(MessageType.UPDATE_CLIENT_STATE,"Stop",DisconnectionCode.DISCONNECT_BY_REQUEST,ConnectionID.fromInt(0));
					msg.setReceivers(new ConnectionID[]{message.getSenderID()});
					server.sendData(
						Serializer.serialize(msg.toString()),
						message.getSenderID());
					onClientDisconnected(message.getSenderID());
				}
			}
			List<ConnectionID> receivers = message.getReceivers();
			for(ConnectionID conn : receivers){
				server.sendData(
					Serializer.serialize(message.toString()),
					conn);
			}
			
		}else{
			switch(message.getType()){

				case MessageType.CHECK_ACTIVITY:  
					if(clientPing !=null){
						clientPing.pingReceived();
						message.setReceivers(new ConnectionID[]{new ConnectionID(0)});
						sendToServer(message);
						clientPing.pingSended();
						//System.out.println("Client"+clientID.toString()+" PING");
					}
					break;
					
				case MessageType.UPDATE_CLIENT_STATE:  
					if(message.getKey().equals("Start")){
						clientID = ConnectionID.fromInt(message.getData());
						clientPing = new PingConnection(clientID);
						onStartClient(clientID);
					}
					if(message.getKey().equals("Stop")){
						onClientStop(message.getData());
						//System.out.println("Client: stop request response received.");
					}
					break;
					
				case MessageType.CUSTOMIZED_DATA:  
					OnReceiveCustomizedData(message, isServerReceiver);
					break;
					
					
				case MessageType.UPDATE_CONNECTION_LIST: 
					if(message.getData() instanceof int[]){
						int[] ids = (int[])message.getData();
						setClientList(Arrays.asList( ConnectionID.ConvertArrayConnectionID(ids)));
						onUpdateClientList(Arrays.asList( ConnectionID.ConvertArrayConnectionID(ids)));
						//System.out.println("Client list updated. Count: "+clientsID.size());
					}else{System.out.println("List error format");}
					break;
			}
		}
	}
	
	private void OnReceiveCustomizedData(NetworkMessage msg, boolean receivedInServer){
		if(receivedInServer){
			if(onReceiveDataInServerListener!=null){
				onReceiveDataInServerListener.run(msg);
			}
		}else{
			if(onReceiveDataInClientListener!=null){
				onReceiveDataInClientListener.run(msg);
			}
		}
	}
	private OnReceiveDataListener onReceiveDataInServerListener = null;
	public void setOnReceiveDataInServerListener(OnReceiveDataListener listener){
		this.onReceiveDataInServerListener = listener;
	} 
	private OnReceiveDataListener onReceiveDataInClientListener = null;
	public void setOnReceiveDataInClientListener(OnReceiveDataListener listener){
		this.onReceiveDataInClientListener = listener;
	} 
	
	public void setMaxClientCount(int max){
		if(isServer()){
			server.setMaxClientCount(max);
		}
	}
	public void setAcceptingClients(boolean accepting){
		if(isServer()){
			server.setAcceptingClient(accepting);
		}
	}
	
	
	private OnUpdateClientListListener onUpdateClientListListener = null;
	public void setOnUpdateClientListListener(OnUpdateClientListListener listener){
		this.onUpdateClientListListener = listener;
	} 
	private void onUpdateClientList(List<ConnectionID> list){
		if(onUpdateClientListListener!=null){
			onUpdateClientListListener.run(list);
		}
	}
	
	private void NetworkLoop(){
		if(isServer()){
			synchronized(clientsPing){
				for(PingConnection ping : clientsPing.values()){
					if(ping.checkResponse()){
						if(ping.isWaitingToSend()){
							NetworkMessage msg = new NetworkMessage(MessageType.CHECK_ACTIVITY,"","0",ping.id);
							msg.setReceivers(new ConnectionID[]{ping.id});
							server.sendData(Serializer.serialize(msg.toString()),ping.id);
							ping.pingSended();
							
						}
					}else{
						//client nao responde e deve ser desconectado
						//System.out.println("Client nao responde... Client"+ping.id);
						onClientDisconnected(ping.id);
					}
				}
			}
		}
		if(isClient()){
			if(!clientPing.checkResponse()){
				//Nao ha resposta do servidor
				//System.out.println("Servidor nao responde...");
				onClientStop(DisconnectionCode.NO_RESPONSE);
			}
		}
	}
	

	public static String getIpAddress() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
				.getNetworkInterfaces();
			while (enumNetworkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = enumNetworkInterfaces
					.nextElement();
				Enumeration<InetAddress> enumInetAddress = networkInterface
					.getInetAddresses();
				while (enumInetAddress.hasMoreElements()) {
					InetAddress inetAddress = enumInetAddress
						.nextElement();

					if (inetAddress.isSiteLocalAddress()) {
						ip += inetAddress.getHostAddress();
					}
				}
			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ip += "Something Wrong! " + e.toString() + "\n";
		}
		return ip;
	}
	
	
	
}


class PingConnection{
	public ConnectionID id;
	public long lastSend = 0;
	public int consecutiveFails = 0;
	private boolean started = false;
	private boolean sended = false;
	public boolean isWaitingToSend(){
		return (!sended && System.currentTimeMillis() - lastSend > 1000);
	}
	public PingConnection(ConnectionID id){
		this.id = id;
		lastSend = System.currentTimeMillis();
	}
	
	public boolean checkResponse(){
		if(!started){return true;}
		if(System.currentTimeMillis()-lastSend>=4000){
			consecutiveFails++;
			lastSend = System.currentTimeMillis();
			sended = false;
			//System.out.println("Timeout ping... ConsecutiveFails: "+consecutiveFails);
		}
		return consecutiveFails<3;
	}
	
	public void pingReceived(){
		sended = false;
		consecutiveFails = 0;
	}
	
	public void pingSended(){
		sended = true;
		started = true;
		lastSend = System.currentTimeMillis();
	}
	

	@Override
	public boolean equals(Object obj)
	{
		// TODO: Implement this method
		//return (((ConnectionID)obj).connectionID==this.connectionID);
		return this.hashCode()==obj.hashCode();
	}

	@Override
	public int hashCode()
	{
		// TODO: Implement this method
		return id.hashCode();
	}
	
	
	public static Hashtable<ConnectionID,PingConnection> CreateList(List<ConnectionID> list){
		Hashtable<ConnectionID,PingConnection> ret= new Hashtable<ConnectionID,PingConnection>();
		for(ConnectionID id : list){
			if(id.id!=0){
			ret.put(id,new PingConnection(id));
			}
		}
		return ret;
	}
}


/*

class NetComponent implements Serializable{
	public int netObjectID = -1;
	public void NetworkThreadUpdate(){}
}

class NetObject extends NetComponent {
	public boolean isClientObject;
	
	public double sendRate = 0.1;
	public boolean syncPosition;
	public boolean syncRotation;
	public boolean syncData;
	private byte[] data;
	public void setSerializableData(Object data){
		this.data = Serializer.serialize(data);
	}
	public Object getSerializableData(){
		return Serializer.deserialize(this.data);
	}
	public void setData(byte[] data){
		this.data = data;
	}
	public byte[] getData(){
		return this.data;
	}
	
	public void Initialize(){
		
	}
	
	@Override
	public void NetworkThreadUpdate(){
		
	}
}

class Serializer{
	
	public static Object deserialize(byte[] data){
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = null;
		Object o = null;
		try {
			in = new ObjectInputStream(bis);
			
			o = in.readObject(); 
			
		}catch (IOException ex) {
			ex.printStackTrace();
			// ignore close exception
		}catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			// ignore close exception
		}  finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
		}
		return o;
	}
	
	public static byte[] serialize(Object data){
		byte[] serializedData = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(data);
			out.flush();
			serializedData = bos.toByteArray();
		}catch (IOException ex) {
			ex.printStackTrace();
			// ignore close exception
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
		return serializedData;
	}
}
*/

