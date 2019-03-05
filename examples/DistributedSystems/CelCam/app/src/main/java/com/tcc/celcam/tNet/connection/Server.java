//package com.bc_aplicativos.wifichatapp;
package tNet.connection;


import tNet.*;
import tNet.core.*;



public interface Server {

	public int getPort();
	
	public boolean isConnectionStarted();
	
	public void setMaxClientCount(int maxClientCount);
	public int getMaxClientCount();
	
    public void setAcceptingClient(boolean acceptingClient);
	public boolean isAcceptingPlayers();
	
	public void sendData(byte[] data, ConnectionID conn);
	public void sendDataToAll(byte[] data);
	
	public void removeClient(ConnectionID conn);
	
	public void close();
	
}


