package tNet.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
//import android.os.*;
import java.io.*;
import tNet.core.*;
import tNet.*;
import java.net.*;


public class ClientTCP implements Client{

	private Thread thread;

    private Socket socket;
	public Socket getSocket(){return socket;}

	//private Thread senderThread;

	private Sender sender;
	//public Sender getSender(){return sender;}

    private String address;
	public String getAddress(){return address;}

    private int port = 8080;
	//public void setPort(int port){this.port=port;}
	public int getPort(){return this.port;}

    private boolean connectionStarted = false;
	public boolean isConnectionStarted(){return connectionStarted;}

	private Action errorAction;
	private Action listenerAction;
	private Listenner listenerThread;
	//final Handler receiver;

    public ClientTCP(String address, Action listenerAction, Action errorAction) {
        //this.userName = userName;
		this.listenerAction = listenerAction;
		this.address = address;
		this.errorAction = errorAction;
		CreateWorker();
    }
	public ClientTCP(String address, int port, Action listenerAction, Action errorAction) {
        //this.userName = userName;
		this.port = port;
		this.listenerAction = listenerAction;
		this.address = address;
		this.errorAction = errorAction;
		CreateWorker();
    }

	public void close(){
		listenerThread.close();
		sender.close();
		/*
		 try
		 {
		 thread.join(0);
		 }
		 catch (InterruptedException e)
		 {e.printStackTrace();}
		 */
	}

	public void sendData(byte[] data){
		//Sender sender = senderMap.get(conn);
		sender.sendMessage(data);
	}

	void CreateWorker(){
		thread = new Thread(
			new Runnable(){
				public void run(){

					if (socket == null) {
						try {
							if (address != null) {
								socket = new Socket(address, port);
								if (socket.isConnected()) {
									sender = new SenderTCP(socket);
									//senderThread = new Thread(sender);
									//senderThread.start();
									connectionStarted = true;
									//CreateListenerThread();
									listenerThread = new ListennerTCP(socket,listenerAction);
									//listenerThread.setName("ClientListenner");
									//listenerThread.start();
								}
							}

						} catch (UnknownHostException e) {
							//e.printStackTrace();
							errorAction.Execute(2);
						} catch(ConnectException e){
							errorAction.Execute(1);
						} catch (IOException e) {
							errorAction.Execute(0);
							e.printStackTrace();
						}
					}
				}
			}
		);
		thread.setName("Client");
		thread.start();
	}


	/*
	 void CreateListenerThread(){
	 listenerThread = new Thread(
	 new Runnable(){

	 @Override
	 public void run() {
	 try {

	 final InputStream inputStream = socket.getInputStream();
	 final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);



	 while (true) {
	 final Object object =(Object) objectInputStream.readObject();// objectInputStream.readObject();

	 if (object != null) {
	 System.out.println("Message received in client");

	 listenerAction.Execute(object);

	 }


	 try { Thread.sleep(50); } catch (InterruptedException e) {
	 e.printStackTrace();
	 }
	 }
	 } catch (IOException e) {
	 e.printStackTrace();
	 } catch (ClassNotFoundException e) {
	 e.printStackTrace();
	 } 

	 }
	 }
	 );
	 listenerThread.setName("ClientListener");
	 listenerThread.start();
	 }
	 */
}
