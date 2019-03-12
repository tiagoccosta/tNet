package tNet.tools;
import java.net.*;
import java.io.*;

public class NetworkDiscovery
{
	public class MulticastPublisher {
		private DatagramSocket socket;
		private InetAddress group;
		private byte[] buf;

		public void multicast(String multicastMessage)  {
			try
			{
				socket = new DatagramSocket();
				group = InetAddress.getByName("230.0.0.0");
				buf = multicastMessage.getBytes();

				DatagramPacket packet 
					= new DatagramPacket(buf, buf.length, group, 4446);
				socket.send(packet);
				socket.close();
			}
			catch (IOException e)
			{e.printStackTrace();}
		}
	}
	
	
	public class MulticastReceiver implements Runnable {
		protected MulticastSocket socket = null;
		protected byte[] buf = new byte[256];

		public void run() {
			try
			{
				socket = new MulticastSocket(4446);
				InetAddress group = InetAddress.getByName("230.0.0.0");
				socket.joinGroup(group);
				while (true)
				{
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					String received = new String(
						packet.getData(), 0, packet.getLength());
					if ("end".equals(received))
					{
						break;
					}
				}
				socket.leaveGroup(group);
				socket.close();
			}
			catch (IOException e)
			{e.printStackTrace();}
		}
	}
	
}
