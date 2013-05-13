package org.dobots.ctrl.robots.rover2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.SocketFactory;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.dobots.ctrl.robots.ac13.AC13RoverTypes;
import org.dobots.ctrl.robots.ac13.AC13RoverTypes.AC13RoverParameters;
import org.dobots.utilities.log.Loggable;

import robots.IVideoListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Rover2Controller extends Loggable {

	private static final String TAG = "Rover2Controller";
	Socket socket;
	Socket receiverMediaSocket;
	Socket senderMediaSocket;

	DataInputStream dataIn;
	DataOutputStream dataOut;
	DataInputStream mediaReceiverInputStream;
	DataOutputStream mediaReceiverOutputStream;
	DataInputStream mediaSenderInputStream;
	DataOutputStream mediaSenderOutputStream;

	static Rover2Controller instance;

	boolean bConnected;
	boolean bRun = true;
	boolean bSocketState;

	long lLastCmdTimeStamp;
	private int moveFlag;
	private boolean changeFlag;
	private boolean move;

	private String targetHost;
	private int targetPort;
	String targetId;
	String targetPassword;

	private AC13RoverParameters parameters;
	private String cameraId;

	private int[] challenge = {0, 0, 0, 0};
	private int[] reverse_challenge = {0, 0, 0, 0};
	private String deviceId;
	private IVideoListener oVideoListener;
	private Timer keepAliveTimer;
	private TimerTask keepAliveTask;
	private Timer batteryTimer;
	private TimerTask batteryTask;
	private int m_nBatteryPower;

	public Rover2Controller() {

		instance = this;
		
		targetHost = Rover2Types.ADDRESS;
		targetPort = Rover2Types.PORT;
		targetId = Rover2Types.ID;
		targetPassword = Rover2Types.PWD;

		parameters = new AC13RoverTypes().new AC13RoverParameters();

		keepAliveTimer = new Timer("keep alive");
		keepAliveTask = new TimerTask()
		{
			public void run()
			{
				try
				{
					Log.d(TAG, "startKeepAliveTask:" + System.currentTimeMillis());
					byte[] arrayOfByte = CommandEncoder.cmdKeepAlive();
					send(arrayOfByte);
					return;
				}
				catch (IOException localIOException)
				{
					localIOException.printStackTrace();
				}
			}
		};

		batteryTimer = new Timer("battery timer");
		batteryTask = new TimerTask()
		{
			public void run()
			{
				try
				{
					byte[] arrayOfByte = CommandEncoder.cmdFetchBatteryPowerReq();
					send(arrayOfByte);
					return;
				}
				catch (IOException localIOException)
				{
					localIOException.printStackTrace();
				}
			}
		};
	}

	public void setVideoListener(IVideoListener listener) {
		oVideoListener = listener;
	}

	public void removeVideoListener(IVideoListener listener) {
		if (oVideoListener == listener) {
			oVideoListener = null;
		}
	}

	private void send(byte[] buffer) throws IOException {
		dataOut.write(buffer);
		dataOut.flush();
	}

	public void startKeepAliveTask()
	{
		Log.d(TAG, "startKeepAliveTask");
		keepAliveTimer.schedule(keepAliveTask, 1000, 30000);
	}

	public void startBatteryTask()
	{
		Log.d(TAG, "startBatteryTask");
		batteryTimer.schedule(batteryTask, 1000, 5000);
	}

	private Socket createCommandSocket(String host, int port) throws IOException {
		socket = SocketFactory.getDefault().createSocket();
		InetSocketAddress localInetSocketAddress = new InetSocketAddress(host, port);
		socket.connect(localInetSocketAddress, 5000);
		bSocketState = true;
		return socket;
	}

	private Socket createMediaReceiverSocket(String host, int port) throws IOException {
		receiverMediaSocket = SocketFactory.getDefault().createSocket();
		InetSocketAddress localInetSocketAddress = new InetSocketAddress(host, port);
		receiverMediaSocket.connect(localInetSocketAddress, 5000);
		bSocketState = true;
		return receiverMediaSocket;
	}

	private Socket createMediaSenderSocket(String host, int port) throws IOException {
		senderMediaSocket = SocketFactory.getDefault().createSocket();
		InetSocketAddress localInetSocketAddress = new InetSocketAddress(host, port);
		senderMediaSocket.connect(localInetSocketAddress, 5000);
		bSocketState = true;
		return senderMediaSocket;
	}

	public boolean cameradown()	throws IOException {
		if (!(bConnected))
			return false;
		moveFlag = 1;
		byte[] arrayOfByte = CommandEncoder.cmdDecoderControlReq(2);
		send(arrayOfByte);
		return true;
	}

	public boolean camerastop()	throws IOException {
		if (!(bConnected))
			return false;
		moveFlag = 0;
		byte[] arrayOfByte = CommandEncoder.cmdDecoderControlReq(1);
		send(arrayOfByte);
		return true;
	}

	public boolean cameraup() throws IOException {
		if (!(bConnected))
			return false;
		moveFlag = 1;
		byte[] arrayOfByte = CommandEncoder.cmdDecoderControlReq(0);
		send(arrayOfByte);
		return true;
	}

	public void change()
	{
		changeFlag = true;
	}

	public void checkConnection()
	{
		if (bConnected);
		try
		{
			connectCommand();
			return;
		}
		catch (IOException localIOException)
		{
			localIOException.printStackTrace();
		}
	}

	private void connectCommand() throws IOException {
		//	    monitorenter;
		Socket localSocket;
		try
		{
			localSocket = createCommandSocket(targetHost, targetPort);
			//	      throw new IOException();
		}
		finally
		{
			//	      monitorexit;
		}
		dataOut = new DataOutputStream(localSocket.getOutputStream());
		dataIn = new DataInputStream(localSocket.getInputStream());
		//		challenge[0] = 1; challenge[1] = 1; challenge[2] = 1; challenge[3] = 1;
		challenge[0] = -402456576; challenge[1] = -804847616; challenge[2] = 3000; challenge[3] = 4000;
		//		byte[] arrayOfByte = CommandEncoder.cmdLoginReq(-1, -1, -1, -1);
		//		byte[] arrayOfByte = CommandEncoder.cmdLoginReq(-402456576, -804847616, 3000, 4000);
		byte[] arrayOfByte = CommandEncoder.cmdLoginReq(challenge);
		send(arrayOfByte);
		Thread localThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				Object byteBuffer = new ByteArrayBuffer(1048576);
				while (bRun) {
					try
					{
						int nCount;
						if (!bConnected) {
							return;
						} else {
							nCount = dataIn.available();
						}
						
						if (nCount > 0) {
							byte[] buffer = new byte[nCount];
							((ByteArrayBuffer)byteBuffer).append(buffer, 0, dataIn.read(buffer, 0, nCount));
							ByteArrayBuffer newByteBuffer = CommandEncoder.parseCommand(instance, (ByteArrayBuffer)byteBuffer);
							byteBuffer = newByteBuffer;
						}
						
						do {
							if (!(bSocketState))
								return;
							nCount = dataIn.available();
						} while (nCount <= 0);
					}
					catch (IOException localIOException1)
					{
						localIOException1.printStackTrace();
						if (!(bConnected))
							return;
						try
						{
							connectCommand();
							return;
						}
						catch (IOException localIOException2)
						{
							localIOException2.printStackTrace();
						}
					}
				}
			}
		});
		localThread.setName("Command Thread");
		localThread.start();
		requestAllParameters();
		//	    monitorexit;
	}

	public void connectMediaReceiver(int paramInt) throws IOException {
		try
		{
			Socket localSocket = createMediaReceiverSocket(targetHost, targetPort);
			mediaReceiverOutputStream = new DataOutputStream(localSocket.getOutputStream());
			mediaReceiverInputStream = new DataInputStream(localSocket.getInputStream());
			byte[] arrayOfByte = CommandEncoder.cmdMediaLoginReq(paramInt);
			mediaReceiverOutputStream.write(arrayOfByte);
			Thread localThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					Object localObject = new ByteArrayBuffer(1048576);
					((ByteArrayBuffer)localObject).clear();
					while (true)
						try
					{
							while (true)
							{
								if (!(bSocketState))
									return;

								byte[] arrayOfByte = new byte[8192];
								int i = mediaReceiverInputStream.read(arrayOfByte, 0, 8192);
								if (i > 0) {
									Log.d("limit", "read:" + i + "\tavailable:" + mediaReceiverInputStream.available());
									((ByteArrayBuffer)localObject).append(arrayOfByte, 0, i);
									ByteArrayBuffer localByteArrayBuffer = CommandEncoder.parseMediaCommand(instance, (ByteArrayBuffer)localObject, mediaReceiverInputStream.available());
									localObject = localByteArrayBuffer;
								}
								try
								{
									Thread.sleep(5L);
								}
								catch (InterruptedException localInterruptedException)
								{
									localInterruptedException.printStackTrace();
								}
							}
					}
					catch (IOException localIOException)
					{
						Log.i("zhang", "media Thread is stopping");
						//	              WificarActivity.getInstance().reStartConnect();
						localIOException.printStackTrace();
					}
				}
			});
			localThread.setName("Media Thread");
			localThread.start();

			//	      monitorexit;
			return;
		}
		finally
		{
			//	      localObject = finally;
			//	      monitorexit;
			//	      throw localObject;
		}
	}

	public void connectMediaSender(int paramInt) throws IOException {
		try
		{
			Socket localSocket = createMediaSenderSocket(targetHost, targetPort);
			mediaSenderOutputStream = new DataOutputStream(localSocket.getOutputStream());
			mediaSenderInputStream = new DataInputStream(localSocket.getInputStream());
			byte[] arrayOfByte = CommandEncoder.cmdMediaLoginReq(paramInt);
			mediaSenderOutputStream.write(arrayOfByte);
			Thread localThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					Object localObject = new ByteArrayBuffer(1048576);
					((ByteArrayBuffer)localObject).clear();
					while (true)
						try
					{
							while (true)
							{
								if (!(bSocketState))
									return;
								if (8192 > 0)
								{
									byte[] arrayOfByte = new byte[8192];
									int i = mediaSenderInputStream.read(arrayOfByte, 0, 8192);
									Log.d("limit", "limit:" + mediaSenderInputStream.available());
									((ByteArrayBuffer)localObject).append(arrayOfByte, 0, i);
									ByteArrayBuffer localByteArrayBuffer = CommandEncoder.parseMediaCommand(instance, (ByteArrayBuffer)localObject, mediaSenderInputStream.available());
									localObject = localByteArrayBuffer;
								}
								try
								{
									Thread.sleep(5L);
								}
								catch (InterruptedException localInterruptedException)
								{
									localInterruptedException.printStackTrace();
								}
							}
					}
					catch (IOException localIOException)
					{
						localIOException.printStackTrace();
					}
				}
			});
			localThread.setName("Media Send Thread");
			localThread.start();
			return;
		}
		finally
		{
			//	      localObject = finally;
			//	      monitorexit;
			//	      throw localObject;
		}
	}

	public void receiveVideo(byte[] data) {
		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

		if (oVideoListener != null) {
			oVideoListener.frameReceived(bmp);
		}
	}

	public boolean disableIR() throws IOException {
		if (!(bConnected))
			return false;
		byte[] arrayOfByte = CommandEncoder.cmdDecoderControlReq(95);
		send(arrayOfByte);
		return true;
	}

	public void disableMoveFlag()
	{
		move = false;
		//	    batteryStop = 0.0F;
		//	    battery_valueStop = 0;
		//	    batteryCountStop = 0;
		moveFlag = 0;
	}

	public boolean disableVideo() throws IOException {
		if (!(bConnected))
			return false;
		byte[] arrayOfByte = CommandEncoder.cmdVideoEnd();
		send(arrayOfByte);
		return true;
	}

	public boolean enableIR() throws IOException {
		if (!(bConnected))
			return false;
		byte[] arrayOfByte = CommandEncoder.cmdDecoderControlReq(94);
		send(arrayOfByte);
		return true;
	}

	public void enableMoveFlag()
	{
		move = true;
		//	    moveFlagCount = moveFlagMaxCount;
		moveFlag = 1;
		//	    disableAudioFlag();
	}

	public boolean enableVideo() throws IOException {
		Log.d(TAG, "enableVideo");
		byte[] arrayOfByte = CommandEncoder.cmdVideoStartReq();
		send(arrayOfByte);
		return true;
	}

	public boolean g_move(int paramInt1, int paramInt2) throws IOException {
		if (!(bConnected))
			return false;
		byte[] arrayOfByte = null;
		if (paramInt2 > 0)
		{
			moveFlag = 1;
			arrayOfByte = CommandEncoder.cmdDeviceControlReq(11, 255);
		}
		if (paramInt2 < 0)
		{
			moveFlag = 1;
			arrayOfByte = CommandEncoder.cmdDeviceControlReq(12, 255);
		}
		send(arrayOfByte);
		return true;
	}

	public String getURLContent(String paramString)
	{
		StringBuffer localStringBuffer = new StringBuffer();
		try
		{
			//	      int i = WificarActivity.getInstance().getWifiCar().isConnected();
			int i = isConnected();
			if (i == 0)
				return "";
			Log.d("network", paramString);
			BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(new URL(paramString).openConnection().getInputStream(), "UTF8"));
			String str = localBufferedReader.readLine();
			if (str == null)
				return localStringBuffer.toString();
			localStringBuffer.append(str);
		}
		catch (Exception localException)
		{
			Log.d("network", "error");
			localException.printStackTrace();
		}
		return localStringBuffer.toString();
	}

	public String getSSID()	throws Exception
	{
		String str1 = "http://" + targetHost + ":" + targetPort + "/get_params.cgi?user=" + targetId + "&pwd=" + targetPassword;
		Log.d("wificar", "get ssid:" + str1);
		String str2 = getURLContent(str1);
		int i = str2.indexOf(39, str2.indexOf("adhoc_ssid"));
		int j = str2.indexOf(39, i + 1);
		String str3 = str2.substring(i + 1, j);
		Log.d("wificar", "ssid:" + str3);
		return str3;
	}

	public int isConnected()
	{
		try
		{
			if (socket == null)
				return 0;
			return socket.isConnected() ? 1 : 0;
		}
		catch (Exception localException)
		{
			localException.printStackTrace();
		}
		return 0;
	}

	public boolean move(int par1, int par2) {
		enableMoveFlag();
		moveFlag = 1;
		byte[] arrayOfByte = null;
		try {
			arrayOfByte = CommandEncoder.cmdDeviceControlReq(par1, par2);
			send(arrayOfByte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void ledON() throws IOException {
		byte[] arrayOfByte;
		arrayOfByte = CommandEncoder.cmdDeviceControlReq(8, 0);
		send(arrayOfByte);
	}

	public void ledOFF() throws IOException {	
		byte[] arrayOfByte = CommandEncoder.cmdDeviceControlReq(9, 0);
		send(arrayOfByte);
	}

	public boolean connect()
	{
		try
		{
			connectCommand();
			bConnected = true;
			return true;
		}
		catch (IOException localIOException)
		{
			localIOException.printStackTrace();
		}
		return false;
	}

	public boolean disconnect()
	{
		if (!(bConnected))
			return false;
		try
		{
			bConnected = false;
			Log.d(TAG, "disconnect");
			bSocketState = false;
			socket.close();
			receiverMediaSocket.close();

			dataOut = null;
			dataIn = null;
			mediaReceiverInputStream = null;
			mediaReceiverOutputStream = null;
			mediaSenderInputStream = null;
			mediaSenderOutputStream = null;

			return true;
		}
		catch (IOException localIOException)
		{
			localIOException.printStackTrace();
		}
		return false;
	}

	public void requestAllParameters()
	{
		//Getting Parameters
		new Thread(new GetParametersRunnable()).start();
	}

	private class GetParametersRunnable implements Runnable {

		@Override
		public void run() {
			try {	

				ArrayList<String> params = new ArrayList<String>();

				HttpClient mClient= new DefaultHttpClient();
				HttpGet get = new HttpGet(String.format("http://%s:%d/get_params.cgi", Rover2Types.ADDRESS, Rover2Types.PORT));
				get.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("AC13", "AC13"),"UTF-8", false));

				mClient.execute(get);
				HttpResponse response = mClient.execute(get);

				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";

				while ((line = rd.readLine()) != null) 
					params.add((line.substring(line.indexOf("=")+1,line.indexOf(";"))).replace("'", ""));

				parameters.fillParameters(params);

			} catch (Exception e) {
				e.printStackTrace();
				error(TAG, "GET PARAMETERS ERROR: " +  e.toString());
			}
		}
	}

	public void setDeviceId(String id) {
		deviceId = id;
	}

	public void setCameraId(String id) {
		cameraId = id;
	}

	public String getKey() {
		return targetId + ":" + cameraId + "-save-private:" + targetPassword;
		//		  return "AC13:" + cameraId + "-save-private:AC13";
	}

	public int[] getChallenge() {
		return challenge;
	}

	public void setReverseChallenge(int[] challenge) {
		reverse_challenge = challenge;
	}

	public void verifyCommand()	throws IOException {
		try
		{
			byte[] arrayOfByte = CommandEncoder.cmdVerifyReq(getKey(), reverse_challenge);
			send(arrayOfByte);
			startBatteryTask();
			startKeepAliveTask();
			return;
		}
		finally
		{
		}
	}

	public void run(int par3, int par4) throws IOException {
		byte[] arrayOfByte = CommandEncoder.cmdDecoderControlReq(par3);
		send(arrayOfByte);
	}

	// FORWARD

	public void moveForward(int i_nLeftVelocity, int i_nRightVelocity) {
		moveLeftForward(i_nLeftVelocity);
		moverRightForward(i_nRightVelocity);
	}

	private void moverRightForward(int i_nVelocity) {
		move(1, i_nVelocity);
	}

	private void moveLeftForward(int i_nVelocity) {
		move(4, i_nVelocity);
	}

	// BACKWARD

	public void moveBackward(int i_nLeftVelocity, int i_nRightVelocity) {
		moveLeftBackward(i_nLeftVelocity);
		moveRightBackward(i_nRightVelocity);
	}

	private void moveRightBackward(int i_nVelocity) {
		move(2, i_nVelocity);
	}

	private void moveLeftBackward(int i_nVelocity) {
		move(5, i_nVelocity);
	}

	// ROTATE

	public void rotateLeft(int i_nVelocity) {
		moveLeftBackward(i_nVelocity);
		moverRightForward(i_nVelocity);
	}

	public void rotateRight(int i_nVelocity) {
		moveRightBackward(i_nVelocity);
		moveLeftForward(i_nVelocity);
	}

	// STOP

	public void moveStop() {
		moveLeftStop();
		moveRightStop();
	}

	private void moveRightStop() {
		move(0, 0);
	}

	private void moveLeftStop() {
		move(3, 0);
	}

	// call is nonblocking. returns directly and doesn't wait for an answer
	public void switchTo640X480Resolution(){
		new Thread(new ResolutionCommandRunnable(32)).start();	
	}

	// call is made blocking until answer received
	public boolean setResolution640x480() {
		ResolutionCommandRunnable oRunner = new ResolutionCommandRunnable(32);
		oRunner.run();
		return oRunner.success;
	}

	// call is made blocking until answer received
	public void switchTo320X240Resolution(){
		new Thread(new ResolutionCommandRunnable(8)).start();
	}

	public boolean setResolution320x240() {
		ResolutionCommandRunnable oRunner = new ResolutionCommandRunnable(8);
		oRunner.run();
		return oRunner.success;
	}

	private class ResolutionCommandRunnable implements Runnable {

		int command;
		public boolean success = false;

		public ResolutionCommandRunnable(int command) {
			this.command = command;
		}

		public void run() {
			try {
				HttpClient mClient= new DefaultHttpClient();
				String strCommand = String.format("http://%s:%d/set_params.cgi?resolution=%s", Rover2Types.ADDRESS, Rover2Types.PORT, command);
				HttpGet get = new HttpGet(strCommand);
				get.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("AC13", "AC13"), "UTF-8", false));

				mClient.execute(get);
				HttpResponse response = mClient.execute(get);

				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					info(TAG, "RESOLUTION COMMAND: " + line + " " + command);

					if (line.startsWith("ok")) {
						success = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				error(TAG, "Resolution Command Error "+  e.toString());
			}
		}
	}

	public AC13RoverParameters getParameters() {
		return parameters;
	}

	public void setBatteryPower(int i_nValue) {
		m_nBatteryPower = i_nValue;
	}

}
