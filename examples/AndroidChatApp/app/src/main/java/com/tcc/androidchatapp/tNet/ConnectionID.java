package tNet;
import java.io.*;
import java.util.*;

public class ConnectionID{
	public Integer id = -1;
	public ConnectionID (int id){
		this.id = id;
	}

	@Override
	public boolean equals(Object obj)
	{
		// TODO: Implement this method
		//return (((ConnectionID)obj).connectionID==this.connectionID);
		return this.hashCode()==obj.hashCode();
	}

	@Override
	public int hashCode()
	{
		// TODO: Implement this method
		return id.hashCode();
	}

	public int toInt(){return id;}
	public static ConnectionID fromInt(int val)
	{return new ConnectionID(val);}

	@Override
	public String toString()
	{
		// TODO: Implement this method
		return id.toString();
	}
	
	public static ConnectionID fromString(String id){
		int idVal = Integer.parseInt(id);
		return ConnectionID.fromInt(idVal);
	}

	
	
	public static int[] ConvertArrayConnectionID(ConnectionID[] array){
		int[] ret = new int[array.length];
		synchronized(array){
			for(int i = 0; i < array.length; i++){
				ret[i] = array[i].id;
			}
		}
		return ret;
	}
	public static int[] ConvertArrayConnectionID(List<ConnectionID> list){
		int[] ret = new int[list.size()];
		synchronized(list){
			for(int i = 0; i < list.size(); i++){
				ret[i] = list.get(i).id;
			}
		}
		return ret;
	}
	
	public static ConnectionID[] ConvertArrayConnectionID(int[] array){
		ConnectionID[] ret = new ConnectionID[array.length];
		for(int i = 0; i < array.length; i++){
			ret[i] = ConnectionID.fromInt(array[i]);
		}
		return ret;
	}
	public static ConnectionID[] ConvertArrayConnectionID(List<Integer> list){
		ConnectionID[] ret = new ConnectionID[list.size()];
		for(int i = 0; i < list.size(); i++){
			ret[i] = ConnectionID.fromInt(list.get(i));
		}
		return ret;
	}
	
}

