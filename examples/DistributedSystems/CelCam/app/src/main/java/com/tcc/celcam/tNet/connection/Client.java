//package com.bc_aplicativos.wifichatapp;
package tNet.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
//import android.os.*;
import java.io.*;
import tNet.core.*;
import tNet.*;


public interface Client {
	
	
	public Socket getSocket();
	
	public String getAddress();
	
    public int getPort();
	
    public boolean isConnectionStarted();
	
	
	
	public void close();

	public void sendData(byte[] data);
	
	
}
