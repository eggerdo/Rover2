package org.dobots.ctrl.robots.rover2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.http.util.ByteArrayBuffer;
import org.dobots.rover2.MainActivity;
import org.dobots.rover2.R;
import org.dobots.utilities.Utils;
import org.dobots.utilities.cipher.Blowfish;

import android.util.Log;

import com.wificar.util.BlowFish;

public class CommandEncoder {

	public static final String TAG = "CommandEncoder";

	public static byte[] cmdKeepAlive() throws IOException 
	{
		return new Protocol("MO_O".getBytes(), 255, 0, new byte[0]).output();
	}

	public static ByteArrayBuffer parseCommand(Rover2Controller instance, ByteArrayBuffer paramByteArrayBuffer) throws IOException 
	{
		byte[] arrayOfByte = paramByteArrayBuffer.toByteArray();
		int i = 0;
		if (arrayOfByte.length > 23)
		{
			//	      CommandEncoder.byteArrayToInt(arrayOfByte, 4, 2);
			i = CommandEncoder.byteArrayToInt(arrayOfByte, 15, 4);
			if (arrayOfByte.length < i + 23)
				return paramByteArrayBuffer;
		} else
			return paramByteArrayBuffer;
		int j = i + 23;
		Protocol localProtocol = new Protocol(arrayOfByte, 0);
		paramByteArrayBuffer.clear();
		paramByteArrayBuffer.append(arrayOfByte, j, arrayOfByte.length - j);
		Log.d(TAG, "op:" + localProtocol.getOp());
		switch (localProtocol.getOp())
		{
		default:
			return paramByteArrayBuffer;
		case 1:
			parseLoginResp(instance, localProtocol.getContent(), 1);
			return paramByteArrayBuffer;
		case 3:
			parseVerifyResp(instance, localProtocol.getContent(), 1);
			return paramByteArrayBuffer;
		case 5:
			parseVideoStartResp(instance, localProtocol.getContent(), 1);
			return paramByteArrayBuffer;
			//	    case 5:
			//	      parseVideoStartResp(paramWifiCar, localProtocol.getContent(), 1);
			//	      return paramByteArrayBuffer;
			//	    case 9:
			//	      parseAudioStartResp(paramWifiCar, localProtocol.getContent(), 1);
			//	      return paramByteArrayBuffer;
			//	    case 12:
			//	      parseTalkStartResp(paramWifiCar, localProtocol.getContent(), 1);
			//	      return paramByteArrayBuffer;
	    case 252:
	      parseFetchBatteryPowerResp(instance, localProtocol.getContent(), 1);
	      return paramByteArrayBuffer;
		}
	}

	public static byte[] parseFetchBatteryPowerResp(Rover2Controller instance, byte[] paramArrayOfByte, int paramInt) throws IOException
	{
		Protocol localProtocol = new Protocol("MO_O".getBytes(), 252, paramArrayOfByte.length, paramArrayOfByte);
		int battery = byteArrayToInt(localProtocol.getContent(), 0, 1);
		instance.setBatteryPower(battery);
		Log.e("wild1", "battery value:" + battery);
		return localProtocol.output();
	}

	public static boolean parseLoginResp(Rover2Controller instance, byte[] response, int par)
	{
		try
		{
			Log.d(TAG, "cmdLoginResp");

			Protocol localProtocol;
			int[] device = new int[4];

			if (par == 0)
			{
				localProtocol = new Protocol(response, 0);
			} else {
				byte[] arrayOfByte2 = "MO_O".getBytes();
				int len = response.length;
				localProtocol = new Protocol(arrayOfByte2, 1, len, response);
			}

			byte[] content = localProtocol.getContent();
			if (byteArrayToInt(content, 0, 2) != 0)
				return false;
			String cameraId = Utils.byteArrayToString(content, 2, 13);

			for (int i = 0; i < 4; ++i) {
				device[i] = byteArrayToInt(content, i + 23, 1);
			}
			String deviceId = device[0] + "." + device[1] + "." + device[2] + "." + device[3];
			instance.setDeviceId(deviceId);
			instance.setCameraId(cameraId);
			Log.d(TAG, "loginResp(deviceId):" + deviceId);
			Log.d(TAG, "loginResp(cameraId):" + cameraId);

			int[] challenge_verify = new int[4];
			challenge_verify[0] = byteArrayToInt(content, 27);
			challenge_verify[1] = byteArrayToInt(content, 31);
			challenge_verify[2] = byteArrayToInt(content, 35);
			challenge_verify[3] = byteArrayToInt(content, 39);

			int[] reverse_challenge = new int[4];
			reverse_challenge[0] = byteArrayToInt(content, 43, 4);
			reverse_challenge[1] = byteArrayToInt(content, 47, 4);
			reverse_challenge[2] = byteArrayToInt(content, 51, 4);
			reverse_challenge[3] = byteArrayToInt(content, 55, 4);
			instance.setReverseChallenge(reverse_challenge);

			int[] challenge = instance.getChallenge();
			int[] arr1 = { challenge[0] };
			int[] arr2 = { challenge[1] };
			int[] arr3 = { challenge[2] };
			int[] arr4 = { challenge[3] };

			InputStream is_blowfish = MainActivity.getInstance().getResources().openRawResource(R.raw.rover2_blowfish);
			Blowfish localBlowFish = new Blowfish();
			localBlowFish.InitializeBlowfish(is_blowfish, instance.getKey().getBytes(), instance.getKey().length());
			localBlowFish.Blowfish_encipher(arr1, arr2);
			localBlowFish.Blowfish_encipher(arr3, arr4);

			String challenge_1 = int32ToByteHexR(arr1[0]);
			String challenge_2 = int32ToByteHexR(arr2[0]);
			String challenge_3 = int32ToByteHexR(arr3[0]);
			String challenge_4 = int32ToByteHexR(arr4[0]);

			String verify_1 = int32ToByteHex(challenge_verify[0]);
			String verify_2 = int32ToByteHex(challenge_verify[1]);
			String verify_3 = int32ToByteHex(challenge_verify[2]);
			String verify_4 = int32ToByteHex(challenge_verify[3]);

			Log.d(TAG, "loginResp(val1):" + challenge_1 + "," + challenge_2 + "," + challenge_3 + "," + challenge_4);
			Log.d(TAG, "loginResp(valreturn ):" + verify_1 + "," + verify_2 + "," + verify_3 + "," + verify_4);
			if ((challenge_1.equals(verify_1)) && (challenge_2.equals(verify_2)) && (challenge_3.equals(verify_3)) && (challenge_4.equals(verify_4)))
			{
				Log.d(TAG, "===============================");
				instance.verifyCommand();
				return true;
			} else {
				return false;
			}
		}
		catch (Exception localException)
		{
			localException.printStackTrace();
			return false;
		}
	}

	public static int parseVerifyResp(Rover2Controller instance, byte[] paramArrayOfByte, int paramInt)
	{
		int i;
		Protocol localProtocol;

		Log.d(TAG, "cmdVerifyResp");

		if (paramInt == 0)
			localProtocol = new Protocol(paramArrayOfByte, 0);
		else {
			localProtocol = new Protocol("MO_O".getBytes(), 3, paramArrayOfByte.length, paramArrayOfByte);
		}

		i = byteArrayToInt(localProtocol.getContent(), 0, 2);
		try
		{
			instance.enableVideo();
			return i;
		}
		catch (IOException localIOException)
		{
			localIOException.printStackTrace();
			return i;
		}
	}

	public static void parseVideoStartResp(Rover2Controller instance, byte[] paramArrayOfByte, int paramInt) throws IOException
	{
		byteArrayToInt(paramArrayOfByte, 0, 2);
		int i = byteArrayToInt(paramArrayOfByte, 2, 4);
		Log.d("wild0", "cmdVideoStartResp:" + i);
		instance.connectMediaReceiver(i);
		//		instance.enableAudio();
	}

	public static byte[] cmdDecoderControlReq(int paramInt) throws IOException 
	{
		ByteBuffer localByteBuffer = ByteBuffer.allocate(1);
		localByteBuffer.put(int8ToByteArray(paramInt));
		return new Protocol("MO_O".getBytes(), 14, 1, localByteBuffer.array()).output();
	}

	public static byte[] cmdDeviceControlReq(int paramInt1, int paramInt2) throws IOException 
	{
		ByteBuffer localByteBuffer = ByteBuffer.allocate(2);
		localByteBuffer.put(int8ToByteArray(paramInt1));
		localByteBuffer.put(int8ToByteArray(paramInt2));
		return new Protocol("MO_O".getBytes(), 250, 2, localByteBuffer.array()).output();
	}

	public static byte[] cmdLoginReq(int[] challenge) throws IOException 
	{
		byte[] arrayOfByte1 = int32ToByteArray(challenge[0]);
		byte[] arrayOfByte2 = int32ToByteArray(challenge[1]);
		byte[] arrayOfByte3 = int32ToByteArray(challenge[2]);
		byte[] arrayOfByte4 = int32ToByteArray(challenge[3]);
		Log.d(TAG, "cmdLoginReq");
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		localByteArrayOutputStream.write(arrayOfByte1);
		localByteArrayOutputStream.write(arrayOfByte2);
		localByteArrayOutputStream.write(arrayOfByte3);
		localByteArrayOutputStream.write(arrayOfByte4);
		Log.d(TAG, "input:(" + localByteArrayOutputStream.size() + ")" + CommandEncoder.bytesToHex(localByteArrayOutputStream.toByteArray()));
		return new Protocol("MO_O".getBytes(), 0, 16, localByteArrayOutputStream.toByteArray()).output();
	}

	public static byte[] cmdMediaLoginReq(int paramInt) throws IOException 
	{
		Log.d(TAG, "mediaLoginReq");
		byte[] arrayOfByte = int32ToByteArray(paramInt);
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		localByteArrayOutputStream.write(arrayOfByte);
		return new Protocol("MO_V".getBytes(), 0, localByteArrayOutputStream.size(), localByteArrayOutputStream.toByteArray()).output();
	}

	public static ByteArrayBuffer parseMediaCommand(Rover2Controller instance, ByteArrayBuffer paramByteArrayBuffer, int paramInt) throws IOException
	{
		byte[] arrayOfByte = paramByteArrayBuffer.toByteArray();
		int i = 0;
		int k = 0;
		int l;

		if (arrayOfByte.length > 23)
		{
			//	      CommandEncoder.byteArrayToInt(arrayOfByte, 4, 2);
			i = CommandEncoder.byteArrayToInt(arrayOfByte, 15, 4);
			if (arrayOfByte.length < i + 23)
				return paramByteArrayBuffer;
		} else
			return paramByteArrayBuffer;

		int j = i + 23;
		paramByteArrayBuffer.clear();
		paramByteArrayBuffer.append(arrayOfByte, j, arrayOfByte.length - j);

		Protocol localProtocol = new Protocol(arrayOfByte, 0);
		if (localProtocol != null) {
			switch(localProtocol.getOp()) {
			case 1:
				parseVideoData(instance, localProtocol.output());
				break;
			case 2:
				//	      parseAudioData(paramWifiCar, localProtocol.output());
				break;
			default:
				Log.d(TAG, "default");
			}
		}

		return paramByteArrayBuffer;
	}

	public static byte[] cmdVideoEnd() throws IOException
	{
		return new Protocol("MO_O".getBytes(), 6, 0, new byte[0]).output();
	}

	public static byte[] cmdVideoStartReq() throws IOException
	{
		Log.d(TAG, "cmdVideoStartReq");
		ByteBuffer localByteBuffer = ByteBuffer.allocate(4);
		localByteBuffer.put(int8ToByteArray(1));
		return new Protocol("MO_O".getBytes(), 4, 1, localByteBuffer.array()).output();
	}

	public static void parseVideoData(Rover2Controller instance, byte[] paramArrayOfByte)
	{
		byte[] arrayOfByte1 = new Protocol(paramArrayOfByte, 0).getContent();
		int i = byteArrayToInt(arrayOfByte1, 0, 4);
		int j = byteArrayToInt(arrayOfByte1, 4, 4);
		byteArrayToInt(arrayOfByte1, 8, 1);
		int k = byteArrayToInt(arrayOfByte1, 9, 4);
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		localByteArrayOutputStream.write(arrayOfByte1, 13, k);
		byte[] arrayOfByte2 = localByteArrayOutputStream.toByteArray();
		try
		{
			instance.receiveVideo(arrayOfByte2);
			return;
		}
		catch (Exception localException)
		{
			localException.printStackTrace();
		}
	}

	static class Protocol
	{
		byte[] content;
		int contentLength;
		byte[] header;
		int op;
		byte preserve1;
		byte[] preserve2;
		long preserve3;

		public Protocol(byte[] paramArrayOfByte)
		{
			this(paramArrayOfByte, 0);
		}

		public Protocol(byte[] paramArrayOfByte, int paramInt)
		{
			this.op = 0;
			this.preserve1 = 0;
			this.preserve2 = new byte[8];
			this.contentLength = 0;
			this.preserve3 = 0L;
			this.content = new byte[0];
			this.header = "MO_V".getBytes();
			this.op = CommandEncoder.byteArrayToInt(paramArrayOfByte, paramInt + 4, 2);
			this.contentLength = CommandEncoder.byteArrayToInt(paramArrayOfByte, paramInt + 15, 4);
			if (this.contentLength <= 0)
				return;
			this.content = new byte[this.contentLength];
			System.arraycopy(paramArrayOfByte, paramInt + 23, this.content, 0, this.contentLength);
		}

		public Protocol(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2)
		{
			this.op = 0;
			this.preserve1 = 0;
			this.preserve2 = new byte[8];
			this.contentLength = 0;
			this.preserve3 = 0L;
			this.content = new byte[0];
			this.header = paramArrayOfByte1;
			this.op = paramInt1;
			this.contentLength = paramInt2;
			this.content = paramArrayOfByte2;
		}

		public byte[] getContent()
		{
			return this.content;
		}

		public int getOp()
		{
			return this.op;
		}

		public byte[] output() throws IOException 
		{
			byte[] arrayOfByte1 = CommandEncoder.int16ToByteArray(this.op);
			byte[] arrayOfByte2 = CommandEncoder.int32ToByteArray(this.content.length);
			ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
			localByteArrayOutputStream.write(this.header);
			localByteArrayOutputStream.write(arrayOfByte1);
			localByteArrayOutputStream.write(new byte[1]);
			localByteArrayOutputStream.write(new byte[8]);
			localByteArrayOutputStream.write(arrayOfByte2);
			localByteArrayOutputStream.write(new byte[4]);
			localByteArrayOutputStream.write(this.content);
			return localByteArrayOutputStream.toByteArray();
		}
	}

	public static byte[] cmdVerifyReq(String paramString, int[] reverse_challenge) throws IOException 
	{
		Log.d(TAG, "cmdVerifyReq");
		BlowFish localBlowFish = new BlowFish();
		int[] arrayOfInt1 = { reverse_challenge[0] };
		int[] arrayOfInt2 = { reverse_challenge[1] };
		int[] arrayOfInt3 = { reverse_challenge[2] };
		int[] arrayOfInt4 = { reverse_challenge[3] };
		localBlowFish.InitBlowfish(paramString.getBytes(), paramString.length());
		localBlowFish.Blowfish_encipher(arrayOfInt1, arrayOfInt2);
		localBlowFish.Blowfish_encipher(arrayOfInt3, arrayOfInt4);
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		localByteArrayOutputStream.write(int32ToByteArray(arrayOfInt1[0]));
		localByteArrayOutputStream.write(int32ToByteArray(arrayOfInt2[0]));
		localByteArrayOutputStream.write(int32ToByteArray(arrayOfInt3[0]));
		localByteArrayOutputStream.write(int32ToByteArray(arrayOfInt4[0]));
		Protocol localProtocol = new Protocol("MO_O".getBytes(), 2, localByteArrayOutputStream.size(), localByteArrayOutputStream.toByteArray());
		Log.d(TAG, "============================ verify");
		return localProtocol.output();
	}

	public static byte[] cmdFetchBatteryPowerReq() throws IOException {
		Log.d("wild1", "send op:251");
		return new Protocol("MO_O".getBytes(), 251, 0, new byte[0]).output();
	}

	public static int byteArrayToInt(byte[] paramArrayOfByte, int offset, int size)
	{
		int val = 0;
		for (int i = 0; i < size; ++i) {
			//			  int shift = (size - 1 - i) * 8;
			int shift = i * 8;
			val += (paramArrayOfByte[i + offset] & 0xFF) << shift;
		}
		return val;
	}

	public static int byteArrayToInt(byte[] paramArrayOfByte, int offset) throws IOException 
	{
		int val = 0;
		for (int i = 0; i < 4; ++i) {
			int shift = (4 - 1 - i) * 8;
			val += (paramArrayOfByte[i + offset] & 0xFF) << shift;
		}
		return val;
	}

	public static byte[] int16ToByteArray(int paramInt)
	{
		byte[] arrayOfByte = new byte[2];
		for (int i = 0; ; ++i)
		{
			if (i >= 2)
				return arrayOfByte;
			arrayOfByte[i] = (byte)(0xFF & paramInt >>> i * 8);
		}
	}

	public static byte[] int32ToByteArray(int paramInt)
	{
		byte[] arrayOfByte = new byte[4];
		for (int i = 0; ; ++i)
		{
			if (i >= 4)
				return arrayOfByte;
			arrayOfByte[i] = (byte)(0xFF & paramInt >>> i * 8);
		}
	}

	public static byte[] int8ToByteArray(int paramInt)
	{
		byte[] arrayOfByte = new byte[1];
		for (int i = 0; ; ++i)
		{
			if (i >= 1)
				return arrayOfByte;
			arrayOfByte[i] = (byte)(0xFF & paramInt >>> i * 8);
		}
	}

	public static String int32ToByteHex(int paramInt)
	{
		byte[] arrayOfByte = new byte[4];
		for (int i = 0; ; ++i)
		{
			if (i >= 4)
				return bytesToHex(arrayOfByte);
			arrayOfByte[i] = (byte)(0xFF & paramInt >>> i * 8);
		}
	}

	public static String int32ToByteHexR(int paramInt)
	{
		byte[] arrayOfByte = new byte[4];
		arrayOfByte[0] = (byte)(0xFF & paramInt >> 24);
		arrayOfByte[1] = (byte)(0xFF & paramInt >> 16);
		arrayOfByte[2] = (byte)(0xFF & paramInt >> 8);
		arrayOfByte[3] = (byte)(0xFF & paramInt >> 0);
		return bytesToHex(arrayOfByte);
	}

	static char[] hexDigit = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70 };

	public static String bytesToHex(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
	{
		StringBuffer localStringBuffer = new StringBuffer();
		for (int i = 0; ; ++i)
		{
			if (i >= paramInt2)
				return localStringBuffer.toString();
			localStringBuffer.append(byteToHex(paramArrayOfByte[(paramInt1 + i)]));
		}
	}

	public static String byteToHex(byte paramByte)
	{
		char[] arrayOfChar = new char[2];
		arrayOfChar[0] = hexDigit[(0xF & paramByte >> 4)];
		arrayOfChar[1] = hexDigit[(paramByte & 0xF)];
		return new String(arrayOfChar);
	}

	public static String bytesToHex(byte[] paramArrayOfByte)
	{
		return bytesToHex(paramArrayOfByte, 0, paramArrayOfByte.length);
	}

}
