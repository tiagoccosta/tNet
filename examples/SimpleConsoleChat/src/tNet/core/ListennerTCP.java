//package com.bc_aplicativos.wifichatapp;
package tNet.core;

import java.io.*;
import java.net.*;

import tNet.*;



public class ListennerTCP implements Listenner 
{
	Thread thread;
	private Action listenerAction;
	private Socket socket;
	private boolean running = true;

	public ListennerTCP(Socket socket, Action listenerAction){
		this.listenerAction = listenerAction;
		this.socket = socket;
		initializeThread();
	}

	private void initializeThread(){
		thread = new Thread(new Runnable(){
				public void run(){Run();}
			});
		thread.setName("Listenner "+this.socket.getInetAddress().toString());

		thread.start();
	}

	public void close(){
		running = false;
		/*
		 try
		 {
		 this.thread.join(0);
		 }
		 catch (InterruptedException e)
		 {e.printStackTrace();}
		 */
		//socket.close();
	}

	private void Run() {

		try {


			final InputStream inputStream = socket.getInputStream();
			final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);


			while (running && !socket.isClosed()) {
				//Object object;
				byte[] object = new byte[16386];
				/*
				 if(inputStream.read()==-1)
				 {
				 System.out.println("Socket fechado! Thread: "+Thread.currentThread().getName());
				 }
				 */
				try
				{

					/*
					 int count;
					 byte[] buffer = new byte[8192]; // or 4096, or more
					 while ((count = inputStream.read(buffer)) > 0)
					 {
					 listenerAction.Execute(buffer);
					 }
					 */


					object = (byte[])objectInputStream.readObject();
					//int code = inputStream.read(object);
					//System.out.println("Code "+code);
					if (object != null)
					//if(code>0)
					{
						//System.out.println("Message received in Server");

						listenerAction.Execute(object);


					}else{
						//if(code==-1){
						//	running=false;
						//}
					}


				}
				catch (EOFException e)
				{
					//e.printStackTrace(); 
					running = false;
				}catch(SocketException e){}
				try { Thread.sleep(50); } catch (InterruptedException e) {
					e.printStackTrace();
				}


			}

		}//catch (EOFException e)
		//{e.printStackTrace(); running = false;}
		catch (IOException e) {
			e.printStackTrace();
		}catch(ClassNotFoundException e ){e.printStackTrace();}

	}

}
