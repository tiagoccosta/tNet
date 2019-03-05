package tNet.core;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.io.*;

public class SenderTCP implements Sender{

	private Thread thread;
    private Socket socket;
    //Object message;
	private List<byte[]> messageList = new ArrayList<byte[]>();
	private OutputStream outputStream;
	private ObjectOutputStream objectOutputStream;
	private boolean running = true;

    public SenderTCP(Socket socket) {
        this.socket = socket;
		InitializeStreams();
    }
	/*
	 public Sender(Socket socket, Object message) {
	 this.socket = socket;
	 this.messageList.add(message);
	 InitializeStreams();
	 }
	 */
	void InitializeStreams(){
		try{
			outputStream = this.socket.getOutputStream();
			objectOutputStream = new ObjectOutputStream(outputStream);
		} catch(IOException e){e.printStackTrace();}
		thread = new Thread(new Runnable(){public void run(){Run();}});
		thread.setName("Sender "+this.socket.getInetAddress().toString());
		thread.start();
	}
	public void sendMessage(byte[] message){
		synchronized(this.messageList){
			this.messageList.add(message);
		}
	}

	public void close(){
		//hostThreadSocket.close();
		running = false;
		/*
		 try
		 {
		 thread.join(0);
		 }
		 catch (InterruptedException e)
		 {e.printStackTrace();}
		 */
	}


	private void Run()
	{
		// TODO: Implement this method
		while((running && !socket.isClosed()) || messageList.size()>0){
			if(messageList.size()>0){

				//final Object msg;
				final byte[] msg;
				synchronized(messageList){
					msg = messageList.get(0);
					messageList.remove(0);
				}
				//System.out.println("MessageList contains message in queue");
				if(msg!=null){
					//System.out.println("Sending message!");
					if (this.socket.isConnected()) {
						///*
						try{
							//PrintStream p = new PrintStream(hostThreadSocket.getOutputStream());
							//outputStream.write(msg);
							objectOutputStream.writeObject(msg);
							objectOutputStream.flush();
						}catch (StreamCorruptedException e) {
							//e.printStackTrace();
						}catch(IOException e){e.printStackTrace();}
						//*/

					}
				}

			}else{
				try { Thread.sleep(50); } catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
		
		 try
		 {
		 socket.close();
		 }
		 catch (IOException e)
		 {e.printStackTrace();}
		 
	}

}




						/*
							Network.RunInNetworkThread(
								new Runnable(){
									@Override
									public void run(){
										try{
											objectOutputStream.writeObject(msg);
										}catch (IOException e) {
											e.printStackTrace();
										}
									}
								}

							);
							*/
