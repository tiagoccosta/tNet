package tNet.tools;
import java.io.*;

public class Serializer
{

	public static byte[] serialize(Object data){
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
	
	public static Object deserialize(byte[] data){
		//Base64 codec = new Base64();
		//byte[] data = codec.decodeBase64( base64.getBytes());
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = null;
		Object o = null;
		try {
			in = new ObjectInputStream(bis);

			o = in.readObject(); 

		}catch (IOException ex) {
			ex.printStackTrace();
			// ignore close exception
		}catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			// ignore close exception
		}  finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
		}
		return o;
	}
}
