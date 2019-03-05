//package com.bc_aplicativos.wifichatapp;
package tNet.core;

import java.util.*;
import java.net.*;

public class Network
{
	private Thread thread;
	private List<Runnable> callersInThread = new ArrayList<Runnable>();
	private Runnable loopWork;
	private boolean running = false;
	public Network(Runnable loopWork){
		this.loopWork = loopWork;
		CreateWorker();
	}
	
	public void RunInNetworkThread(Runnable runnable){
		synchronized(this.callersInThread){
			this.callersInThread.add(runnable);
		}
	}
	
	public void stop(){
		running = false;
		try
		{
			thread.join(0);
		}
		catch (InterruptedException e)
		{e.printStackTrace();}
	}

	void CreateWorker(){
		running = true;
		thread = new Thread(
			new Runnable(){
				public void run(){

					while(running){
						loopWork.run();
						if(callersInThread.size()>0){
							Runnable runner = null;
							synchronized(callersInThread){
								runner = callersInThread.get(0);
								callersInThread.remove(0);
							}
							if(runner!=null){
								runner.run();
							}
						}
						
					}
					
				}	
			}
		);
		thread.setName("Network");
		thread.start();
	}
	
	
}
