package tNet.connection;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
//import android.os.*;
import java.io.*;
import java.net.*;
import java.util.*;
import tNet.*;
import tNet.core.*;
import tNet.connection.*;



public class ServerTCP implements Server {

	private Thread thread;

    private int port = 8080;
	public int getPort(){return port;}

    private HashMap<ConnectionID, Socket> socketMap = new HashMap<ConnectionID, Socket>();
	private HashMap<ConnectionID,Sender> senderMap = new HashMap<ConnectionID,Sender>();
	//private HashMap<ConnectionID,Thread> senderThreadMap = new HashMap<ConnectionID,Thread>();
	private HashMap<ConnectionID,Listenner> listennerThreadMap = new HashMap<ConnectionID,Listenner>();

	private int nextClientID = 1;

    private boolean connectionStarted = false;
	public boolean isConnectionStarted(){return connectionStarted;}

    private ServerSocket serverSocket;

	private boolean running = true;

    public int  maxClientCount = 99;
	public void setMaxClientCount(int maxClientCount)
	{this.maxClientCount = maxClientCount;}
	public int getMaxClientCount()
	{return maxClientCount;}

    public boolean acceptingClient = true;
	public void setAcceptingClient(boolean acceptingClient)
	{this.acceptingClient = acceptingClient;}
	public boolean isAcceptingPlayers()
	{return acceptingClient;}

	//private Thread listenerThread;
	private Action clientConnectionAction;
	private Action listenerAction;
	private Action serverStartedAction;

	private Action errorToStartAction;


    public ServerTCP(int port, Action clientConnectionAction, Action listenerAction, Action ServerStartedAction, Action errorToStartServerAction) {
		this.port = port;
		this.clientConnectionAction=clientConnectionAction;
		this.listenerAction=listenerAction;
		this.serverStartedAction = ServerStartedAction;
		this.errorToStartAction = errorToStartServerAction;
		CreateWorker();
    }

	public void sendData(byte[] data, ConnectionID conn){
		synchronized(senderMap){
			if(senderMap.containsKey(conn)){
				Sender sender = senderMap.get(conn);
				sender.sendMessage(data);
			}
		}
	}
	public void sendDataToAll(byte[] data){
		synchronized(senderMap){
			for(Sender sender : senderMap.values()){
				sender.sendMessage(data);
			}
		}

	}

	public void removeClient(ConnectionID conn){

		synchronized(senderMap){
			senderMap.get(conn).close();
			senderMap.remove(conn);
		}
		/*
		 synchronized(senderThreadMap){
		 senderThreadMap.get(conn).stop();
		 senderThreadMap.remove(conn);
		 }
		 */
		synchronized(listennerThreadMap){
			listennerThreadMap.get(conn).close();
			listennerThreadMap.remove(conn);
		}
		synchronized(socketMap){
			/*
			 try
			 {
			 socketMap.get(conn).close();
			 }
			 catch (IOException e)
			 {e.printStackTrace();}
			 */
			socketMap.remove(conn);
		}
	}

	public void close(){
		running = false;
		setAcceptingClient(false);
		ConnectionID[] ids = socketMap.keySet().toArray(new ConnectionID[socketMap.size()]);
		for(ConnectionID id:ids){
			removeClient(id);
		}
		
		try
		{
			serverSocket.close();
			//System.out.println("Server closed: "+serverSocket.isClosed());
		}
		catch (IOException e)
		{e.printStackTrace();}
		
	}

	void CreateWorker(){
		thread = new Thread(
			new Runnable(){
				public void run(){

					if (serverSocket == null) {
						try {
							serverSocket = new ServerSocket(port);
							connectionStarted = true;
							serverStartedAction.Execute(null);
							while (running) {
								if (isAcceptingPlayers() && getMaxClientCount()>socketMap.size() && !serverSocket.isClosed()) {
									try{
									final Socket socket = serverSocket.accept();
									//CreateListenerThread(socket);


									final ConnectionID conn = new ConnectionID(nextClientID++);
									synchronized(socketMap){
										socketMap.put(conn, socket);
									}
									Sender sender = new SenderTCP(socket);
									synchronized(senderMap){
										senderMap.put(conn,sender);
									}
									/*
									 Thread senderThread = new Thread(sender);
									 senderThread.setName("Sender-ID"+conn.id);
									 senderThread.start();
									 synchronized(senderMap){
									 senderThreadMap.put(conn,senderThread);
									 }
									 */
									Listenner listennerThread = new ListennerTCP(socket, listenerAction); //CreateListenerThread(socket);
									//listennerThread.setName("Listenner-ID"+conn.id);
									//listennerThread.start();
									synchronized(senderMap){
										listennerThreadMap.put(conn,listennerThread);
									}

									clientConnectionAction.Execute(conn);
									}catch(SocketException e){
										
									}
								}

								try { Thread.sleep(500); } catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							/*
							try
							{
								serverSocket.close();
								//System.out.println("Server closed: "+serverSocket.isClosed());
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
							*/
							
						} catch (IOException e) {
							//e.printStackTrace();
							e.printStackTrace();
							errorToStartAction.Execute(0);
						} 
					}
				}
			}
		);
		thread.setName("Server");
		thread.start();
	}


	/*

	 Thread CreateListenerThread(final Socket socket){
	 //final Socket socket = connSocket;
	 Thread listenerThread = new Thread(
	 new Runnable(){
	 @Override
	 public void run() {

	 try {


	 final InputStream inputStream = socket.getInputStream();
	 final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

	 while (true) {
	 final Object object;

	 object = objectInputStream.readObject();
	 if (object != null) {
	 System.out.println("Message received in Server");

	 listenerAction.Execute(object);


	 }

	 try { Thread.sleep(50); } catch (InterruptedException e) {
	 e.printStackTrace();
	 }


	 }

	 } catch (IOException e) {
	 e.printStackTrace();
	 }catch(ClassNotFoundException e ){e.printStackTrace();}
	 }
	 });
	 listenerThread.start();
	 return listenerThread;
	 }
	 */

}


