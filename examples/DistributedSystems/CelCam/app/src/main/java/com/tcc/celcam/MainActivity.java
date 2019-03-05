package com.tcc.celcam;

import android.app.*;
import android.os.*;
import android.hardware.*;
import android.renderscript.*;
import android.content.*;
import android.util.*;
import android.widget.*;
import android.view.*;
import java.io.*;
import tNet.*;
import java.util.*;
import android.view.View.*;
import tNet.events.*;

public class MainActivity extends Activity 
{
	NetworkManager manager;
	Camera camera;
	CameraPreview cameraPreview;
	Camera.Size size;
	Context context;
	
	LinearLayout imgView;
	
	TextView textViewServerIP;
	//TextView textViewConsole;
	EditText editTextAddressToConnect;
	Button btnHost;
	Button btnClient;
	//Button btnSendMsg;
	//EditText editTextMsgToSend;
	ProgressDialog pd;
	
	private List<ConnectionID> observers = new ArrayList<ConnectionID>();
	public ConnectionID[] getObservers () {
		synchronized(observers){
			return observers.toArray( new ConnectionID[observers.size()]);
			
		}
	}
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		context = this;
		
		imgView = findViewById(R.id.imageView);
		
		
		btnHost = findViewById(R.id.btn_CreateHost);
		btnClient = findViewById(R.id.btn_JoinClient);

		textViewServerIP = findViewById(R.id.textView_HostAddress);
		//textViewConsole = findViewById(R.id.textView_console);
		editTextAddressToConnect = findViewById(R.id.editText_ClientAddress);

		//btnSendMsg = findViewById(R.id.btn_SendMsg);
		//editTextMsgToSend = findViewById(R.id.editText_MsgToSend);

		btnHost.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					startHost();
				}
			});
		btnClient.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					startClient();
				}
			});
		
		
		manager = new NetworkManager();
		/*
			@Override
			public void onStartServer(){
				super.onStartServer();
				pd.dismiss();
				runOnUiThread(new Runnable(){
						public void run(){
							btnHost.setText("Stop Server");
							btnHost.setOnClickListener(new OnClickListener(){
									@Override
									public void onClick(View v){
										StopServer();
									}	
								});
						}
					});
			}
			@Override
			public void onStartClient(ConnectionID conn){
				super.onStartClient(conn);
				pd.dismiss();
				runOnUiThread(new Runnable(){
						public void run(){
							btnClient.setText("Stop Client");
							btnClient.setOnClickListener(new OnClickListener(){
									@Override
									public void onClick(View v){
										StopClient();
									}	
								});
						}
					});
			}
			@Override
			public void errorOnStartServer(int code){
				super.errorOnStartServer(code);
				pd.dismiss();
			}
			@Override
			public void errorOnStartClient(int code){
				super.errorOnStartClient(code) ; 
				pd.dismiss();
			}
			
			@Override
			public void onUpdateClientList(List<ConnectionID> clients){
				ConnectionID[] observersCopy = observers.toArray(new ConnectionID[observers.size()]);
				for(ConnectionID observer:observersCopy){
					if(clients.contains(observer)){
						observers.remove(observer);
					}
				}
			}
			
			@Override
			public void OnReceiveCustomizedData(NetworkMessage msg,boolean inServer){
				if(msg.getKey().equals("request_cam_info")){
					observers.add(msg.getSenderID());
					manager.sendToClients("","cam_info", new ConnectionID[]{msg.getSenderID()});
				}
				//if(msg.getKey().equals("cam_info")){}
			}
			
			@Override
			public void onServerStop(int code){
				runOnUiThread(new Runnable(){
						public void run(){
							btnHost.setText("Start Host");
							btnHost.setOnClickListener(new OnClickListener(){
									@Override
									public void onClick(View v){
										pd = new ProgressDialog(MainActivity.this);
										pd.setMessage("Initializing server...");
										pd.setCancelable(false);
										pd.show();
										StartHost(8080);
									}	
								});
						}
					});
			}

			@Override
			public void onClientStop(int  disconnectionCode){
				super.onClientStop(disconnectionCode);
				runOnUiThread(new Runnable(){
						public void run(){
							btnClient.setText("Start Client");
							btnClient.setOnClickListener(new OnClickListener(){
									@Override
									public void onClick(View v){
										pd = new ProgressDialog(MainActivity.this);
										pd.setMessage("Initializing client...");
										pd.setCancelable(false);
										pd.show();

										String address = editTextAddressToConnect.getText().toString();
										System.out.println("Starting client.... "+address);
										StartClient(address,8080);
									}	
								});
						}
					});
			}
		};
		*/
		manager.setOnStartServerListener(
			new OnConnectionStartListener(){
				public void run(){
					pd.dismiss();
					runOnUiThread(new Runnable(){
							public void run(){
								btnHost.setText("Stop Server");
								btnHost.setOnClickListener(new OnClickListener(){
										@Override
										public void onClick(View v){
											manager.StopServer();
										}	
									});
							}
						});
				}
			}
		);
		manager.setOnStartClientListener(
			new OnConnectionStartListener(){
				public void run(){
					pd.dismiss();
					runOnUiThread(new Runnable(){
							public void run(){
								btnClient.setText("Stop Client");
								btnClient.setOnClickListener(new OnClickListener(){
										@Override
										public void onClick(View v){
											manager.StopClient();
										}	
									});
							}
						});
				}
			}
		);
		manager.setOnStopServerListener(
			new ConnectionInterruptListener(){
				public void run(int code){
					runOnUiThread(new Runnable(){
							public void run(){
								btnHost.setText("Start Host");
								btnHost.setOnClickListener(new OnClickListener(){
										@Override
										public void onClick(View v){
											pd = new ProgressDialog(MainActivity.this);
											pd.setMessage("Initializing server...");
											pd.setCancelable(false);
											pd.show();
											manager.StartHost(8080);
										}	
									});
							}
						});
				}
			}
		);
		manager.setOnStopClientListener(
			new ConnectionInterruptListener(){
				public void run(int code){
					runOnUiThread(new Runnable(){
							public void run(){
								btnClient.setText("Start Client");
								btnClient.setOnClickListener(new OnClickListener(){
										@Override
										public void onClick(View v){
											pd = new ProgressDialog(MainActivity.this);
											pd.setMessage("Initializing client...");
											pd.setCancelable(false);
											pd.show();

											String address = editTextAddressToConnect.getText().toString();
											System.out.println("Starting client.... "+address);
											manager.StartClient(address,8080);
										}	
									});
							}
						});
				}
			}
		);
		manager.setErrorListennerOnStartClient(
			new ConnectionInterruptListener(){
				public void run(int code){
					pd.dismiss();
				}
			}
		);
		manager.setErrorListennerOnStartServer(
			new ConnectionInterruptListener(){
				public void run(int code){
					pd.dismiss();
				}
			}
		);
		manager.setOnReceiveDataInClientListener(
			new OnReceiveDataListener(){
				public void run(NetworkMessage msg){
					if(msg.getKey().equals("request_cam_info")){
						observers.add(msg.getSenderID());
						manager.sendToClients("","cam_info", new ConnectionID[]{msg.getSenderID()});
					}
				}
			});
		manager.setOnUpdateClientListListener( 
			new OnUpdateClientListListener(){
				public void run(List<ConnectionID> data){
					List<ConnectionID> clients = data;
					ConnectionID[] observersCopy = observers.toArray(new ConnectionID[observers.size()]);
					for(ConnectionID observer:observersCopy){
						if(clients.contains(observer)){
							observers.remove(observer);
						}
					}
				}
			}
		);
		textViewServerIP.setText(NetworkManager.getIpAddress());
		
        // Create an instance of Camera
        camera = getCameraInstance();
		cameraPreview = new CameraPreview(this, camera, manager);
        LinearLayout preview = (LinearLayout) findViewById(R.id.imageView);
        preview.addView(cameraPreview);
		
    }

	
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		}
		catch (Exception e){
			e.printStackTrace();
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}
	
	public void startHost(){
		//if(chatInitialized)
		//	return;
		pd = new ProgressDialog(MainActivity.this);
		pd.setMessage("Initializing server...");
		pd.setCancelable(false);
		pd.show();

		manager.StartHost (8080);
	}

	public void startClient(){
		//if(clientInitialized)
		//	return;
		pd = new ProgressDialog(MainActivity.this);
		pd.setMessage("Initializing client...");
		pd.setCancelable(false);
		pd.show();

		String address = editTextAddressToConnect.getText().toString();
		System.out.println("Starting client.... "+address);
		manager.StartClient(address,8080);
	}
	
	/*
	public void SetImageInView(final android.graphics.Bitmap bmp){
		runOnUiThread(new Runnable(){
			public void run(){
				System.out.println("Put image to imageView");
				//imgView.setImageBitmap(null);
				imgView.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), bmp));
			}
		});
	}
	
	public void receivePreview(byte[]data, Camera cam){
		Camera.Size size = cam.getParameters().getPreviewSize();
		android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(size.width, size.height, android.graphics.Bitmap.Config.ARGB_8888);
		Allocation bmData = renderScriptNV21ToRGBA888(
			context,
			size.width,
			size.height,
			data);
		bmData.copyTo(bitmap);
		SetImageInView(bitmap);
		//.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), bitmap));
	}
	public Allocation renderScriptNV21ToRGBA888(Context context, int width, int height, byte[] nv21) {
		RenderScript rs = RenderScript.create(context);
		ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

		Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
		Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

		Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
		Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

		in.copyFrom(nv21);

		yuvToRgbIntrinsic.setInput(in);
		yuvToRgbIntrinsic.forEach(out);
		return out;
	}
	*/
}



class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
	Context mContext;
	private long lastSend = 0;
	
	NetworkManager mManager;

    public CameraPreview(Context context, Camera camera, NetworkManager manager) {
        super(context);
        mCamera = camera;
		mContext = context;
		lastSend = System.currentTimeMillis();

		mManager = manager;
		
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
			
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            System.out.println ("Error setting camera preview: " + e.getMessage());
        }
    }
	public void receivePreview(byte[]data, Camera cam){
		if(!mManager.isClient()){return;}
		if(System.currentTimeMillis() < lastSend + 1000){
			return;
		}
		lastSend = System.currentTimeMillis();
		Camera.Size size = cam.getParameters().getPreviewSize();
		android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(size.width, size.height, android.graphics.Bitmap.Config.ARGB_8888);
		Allocation bmData = renderScriptNV21ToRGBA888(
			mContext,
			size.width,
			size.height,
			data);
		bmData.copyTo(bitmap);
		ByteArrayOutputStream blob = new ByteArrayOutputStream();
		bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob);
		byte[] bitmapdata = blob.toByteArray();
		mManager.sendToClients(bitmapdata,((MainActivity)mContext).getObservers());
		//((MainActivity)mContext).SetImageInView(bitmap);
		 //.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), bitmap));
		 
	}
	public Allocation renderScriptNV21ToRGBA888(Context context, int width, int height, byte[] nv21) {
		RenderScript rs = RenderScript.create(context);
		ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

		Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
		Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

		Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
		Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

		in.copyFrom(nv21);

		yuvToRgbIntrinsic.setInput(in);
		yuvToRgbIntrinsic.forEach(out);
		return out;
	}
	

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
			// preview surface does not exist
			return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
			// ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            Camera.Parameters params = mCamera.getParameters();
			int previewHeight = params.getPreviewSize().height;
			int previewWidth = params.getPreviewSize().width;
			int previewFormat = params.getPreviewFormat();
			// Crop the edges of the picture to reduce the image size
			android.graphics.Rect r = new android.graphics.Rect(100, 100, previewWidth - 100, previewHeight - 100);
			byte[] mCallbackBuffer = new byte[460800];
			mCamera.setParameters(params);
			mCamera.setPreviewCallback(new Camera.PreviewCallback(){
					public void onPreviewFrame(byte[] data, Camera cam){
						receivePreview(data,cam);
					}
				});
			mCamera.addCallbackBuffer(mCallbackBuffer);
			mCamera.startPreview();

        } catch (Exception e){
            System.out.println("Error starting camera preview: " + e.getMessage());
        }
    }
}
