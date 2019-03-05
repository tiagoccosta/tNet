package tNet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import org.apache.commons.codec.binary.*;
import tNet.tools.*;

public class NetworkMessage implements Serializable{
	private int type;
	public int getType(){return type;}
	
	private String key = "";
	public String getKey(){return key;}
	public void setKey(String val){this.key=val;}
	
	private String data;
	public Object getData(){
		return deserialize(data);
	}
	public void setData(Object data){
		this.data = serializeToBase64(data);
	}
	public void setRawData(String data){
		this.data = data;
	}
	
	private int sender;
	private void setSender(ConnectionID senderID){this.sender=senderID.id;}
	public ConnectionID getSenderID(){return ConnectionID.fromInt(sender);}
	
	private int[] receivers;
	public List<ConnectionID> getReceivers(){
		List<ConnectionID> ret = new ArrayList<ConnectionID>();
		for(int connID : receivers){
			ret.add(ConnectionID.fromInt(connID));
		}
		return ret;
	}
	
	
	NetworkMessage(int type, ConnectionID sender){
		setSender( sender);
		this.type=type;
	}
	NetworkMessage(int type, Object data, ConnectionID sender){
		setSender( sender);
		this.type=type;
		setData(data);
		//this.data= data; //serialize(data);
	}
	NetworkMessage(int type, String key, Object data, ConnectionID sender){
		setSender(sender);
		this.type=type;
		setData(data);
		//this.data=data; //serialize(data);
		this.key = key;
	}
	public void setReceivers(ConnectionID[] receivers){
		//this.receivers = new ArrayList<ConnectionID>(Arrays.asList(receivers));
		this.receivers = new int[receivers.length];
		int i = 0;
		for(ConnectionID conn : receivers){
			this.receivers[i]=conn.id;
			i++;
		}

	}
	public void setReceivers(List<ConnectionID> receivers){
		//this.receivers = new ArrayList<ConnectionID>(receivers);
		synchronized(receivers){
			this.receivers = new int[receivers.size()];
			int i = 0;
			for(ConnectionID conn : receivers){
				this.receivers[i]=conn.id;
				i++;
			}
		}
	}
	
	public static NetworkMessage fromString(String json){
		NetworkMessage msg = null;
		json = json.substring(1, json.length()-1);  
		//json.copyValueOf(json.toCharArray(),1,json.length()-2);
		
		String[] vars = json.split(",");
		String[] values = new String[vars.length];
		for(int i = 0; i < vars.length; i++){
			//System.out.println("Var-> "+vars[i]);
			String[] splitedVar = vars[i].split(":");
			if(splitedVar[1].charAt(0)=='"'){
				values[i] = splitedVar[1].substring(1, splitedVar[1].length()-1);
			} else{

				if(splitedVar[1].charAt(0)=='['){
					values[i] = splitedVar[1].substring(1, splitedVar[1].length()-1);
				} else{
					values[i] = splitedVar[1];
				}
			}
			
		}
		msg = new NetworkMessage(
			Integer.parseInt(values[0]),
			ConnectionID.fromString(values[2]));
		msg.setRawData(values[4]);
		msg.setKey(values[1]);
		String[] receiversSplited = values[3].split("-");
		List<ConnectionID> receivers = new ArrayList<ConnectionID>();
		for(String s : receiversSplited){
			if(!s.isEmpty() && !s.equals("")){
				try
				{
					int id = Integer.parseInt(s);
					receivers.add(new ConnectionID(id));
				}
				catch (NumberFormatException e)
				{e.printStackTrace();}
			}
		}
		msg.setReceivers(receivers);
		/*
		int state = 1;
		boolean isKey = false;
		boolean isValue = false;
		boolean writing = false;
		for(char c : json.toCharArray()){
			if(state==1){
				
				if(c==':'){
					
				}
			}
			if(state==2){
				
			}
		}
		*/
		
		return msg;
	}

	@Override
	public String toString()
	{
		// TODO: Implement this method
		String netMsg = "";
		netMsg += "{";
		
		//type
		netMsg += "\"type\":";
		netMsg += type;
		netMsg += ",";
		
		//key
		netMsg += "\"key\":";
		netMsg += "\""+key+"\"";
		netMsg += ",";
		
		//sender
		netMsg += "\"sender\":";
		netMsg += sender;
		netMsg += ",";
		
		netMsg += "\"receivers\":\"";
		int i=0;
		for(int id: receivers){
			netMsg += id;
			i++;
			if(i<receivers.length){
				netMsg += "-";
			}
			
		}
		netMsg += "\",";


		//data
		netMsg += "\"data\":";
		netMsg += "\""+data+"\"";
		netMsg += "";
		
		netMsg += "}";
		return netMsg;
	}

	
	

	private Object deserialize(String base64){
		Base64 codec = new Base64();
		byte[] data = codec.decodeBase64( base64.getBytes());
		return Serializer.deserialize(data);
	}
	
	private String serializeToBase64(Object data){
		Base64 codec = new Base64();
		return new String(codec.encodeBase64(Serializer.serialize(data)));
	}

	private byte[] serialize(Object data){
		byte[] serializedData = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(data);
			out.flush();
			serializedData = bos.toByteArray();
		}catch (IOException ex) {
			ex.printStackTrace();
			// ignore close exception
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
		return serializedData;
	}
	
}
