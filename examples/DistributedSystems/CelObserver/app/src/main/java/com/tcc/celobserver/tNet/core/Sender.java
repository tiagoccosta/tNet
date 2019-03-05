//package com.bc_aplicativos.wifichatapp;
package tNet.core;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.io.*;

public interface Sender {
	public void sendMessage(byte[] message);
	public void close();
}


