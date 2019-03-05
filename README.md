# tNet

Simple library to create servers and clients, managing communication via socket.

---------------
## Install

Just copy "tNet" folder in your java project scripts folder.

---------------
## How To Use

Create an instance of a NetworkManager, set the desired events, and you are ready to call functions to start a server or client.

* __Creating an Instance__
```java
manager = new NetworkManager();
```


* __Events__
```java
manager.setOnStartServerListener(
	new OnConnectionStartListener(){
		public void run(){}
	}
);
manager.setOnStartClientListener(
	new OnConnectionStartListener(){
		public void run(){}
	}
);
manager.setErrorListennerOnStartServer(
	new ConnectionInterruptListener(){
		public void run(int code){}
	}
);
manager.setErrorListennerOnStartClient(
	new ConnectionInterruptListener(){
		public void run(int code){}
	}
);
manager.setOnStopServerListener(
	new ConnectionInterruptListener(){
		public void run(int code){}
	}
);
manager.setOnStopClientListener(
	new ConnectionInterruptListener(){
		public void run(int code){}
	}
);
manager.setOnUpdateClientListListener(
	new OnUpdateClientListListener(){
		public void run(List<ConnectionID> clients){}
	}
);
manager.setOnReceiveDataInServerListener(
	new OnReceiveDataListener(){
		public void run(NetworkMessage msg){}
	}
);
manager.setOnReceiveDataInClientListener(
	new OnReceiveDataListener(){
		public void run(NetworkMessage data){}
	}
);
```


* __Starting Connections__
```java
//Create a server
manager.startServer(portNumber);

//Create a server and a client
manager.startHost(portNumber);

//Create a client
manager.startClient(address,portNumber);
```


* __Sending Data__
```java
//Send data to all clients
manager.sendToAll(object);

//Send data to all clients with key for data identify
manager.sendToAll(object,"key");

//Send data to specific clients
manager.sendToClients(object, connectionID_array);

//Send data to specific clients with key
manager.sendToClients(object, "key", connectionID_array);

//Send data to server
manager.sendToServer(object);

//Send data to server with key
manager.sendToServer(object,"key");
```


* __Stop Connections__
```java
//Stop server
manager.stopServer();

//Stop client
manager.stopClient();
```

