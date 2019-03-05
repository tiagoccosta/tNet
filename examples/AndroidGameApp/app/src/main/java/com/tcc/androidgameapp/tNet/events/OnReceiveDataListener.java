package tNet.events;
import tNet.*;

public interface OnReceiveDataListener
{
	public void run(NetworkMessage msg);
}
