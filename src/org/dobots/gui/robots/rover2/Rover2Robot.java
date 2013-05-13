package org.dobots.gui.robots.rover2;

import org.dobots.ctrl.robots.rover2.Rover2;
import org.dobots.ctrl.robots.rover2.Rover2Types.VideoResolution;
import org.dobots.rover2.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import robots.RobotInventory;
import robots.RobotRemoteListener;
import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.ctrl.RemoteControlHelper;
import robots.gui.SensorGatherer;
import robots.gui.WifiRobot;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class Rover2Robot extends WifiRobot {

	private static final int DIALOG_SETTINGS_ID = 1;

	private static final int ACCEL_ID = CONNECT_ID + 1;
	private static final int ADVANCED_CONTROL_ID = ACCEL_ID + 1;
	private static final int VIDEO_ID = ADVANCED_CONTROL_ID + 1;
	private static final int VIDEO_SETTINGS_ID = VIDEO_ID + 1;

	private static final int REMOTE_CTRL_GRP = GENERAL_GRP + 1;
	private static final int SENSOR_GRP = REMOTE_CTRL_GRP + 1;
	private static final int VIDEO_GRP = SENSOR_GRP + 1	;
	
    private Rover2 m_oRover;
	private RemoteControlHelper m_oRemoteCtrl;
	private boolean connected;
	private String m_strRobotID;
	private Boolean m_bOwnsRobot;
	private boolean bIRon;
	private boolean bLighton;
	private Rover2SensorGatherer m_oSensorGatherer;

	private AlertDialog m_dlgSettingsDialog;

	public Rover2Robot(BaseActivity i_oOwner) {
		super(i_oOwner);
	}
	
	public Rover2Robot() {
		super();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        m_strRobotID = (String) getIntent().getExtras().get("RobotID");
        m_bOwnsRobot = (Boolean) getIntent().getExtras().get("OwnsRobot");
        
        m_oRover = (Rover2) RobotInventory.getInstance().getRobot(m_strRobotID);
        m_oRover.setHandler(m_oUiHandler);
        connectToRobot();
		
        m_oSensorGatherer = new Rover2SensorGatherer(this, m_oRover);
        
//		m_dblSpeed = m_oRover.getBaseSped();

		RobotRemoteListener oListener = new RobotRemoteListener(m_oRover);
		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, oListener);
        m_oRemoteCtrl.setProperties();

        updateButtons(false);

        if (m_oRover.isConnected()) {
			updateButtons(true);
		}
        
    }

	@Override
	protected void setProperties(RobotType i_eRobot) {
    	m_oActivity.setContentView(R.layout.rover2_main);

    	final EditText edtPar1 = (EditText) findViewById(R.id.edtPar1);
    	final EditText edtPar2 = (EditText) findViewById(R.id.edtPar2);
    	
    	Button btnMove = (Button) findViewById(R.id.btnMove);
    	btnMove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int par1 = Integer.parseInt(edtPar1.getText().toString());
				int par2 = Integer.parseInt(edtPar2.getText().toString());
				m_oRover.move(par1, par2);
			}
		});

    	final EditText edtPar3 = (EditText) findViewById(R.id.edtPar3);
    	final EditText edtPar4 = (EditText) findViewById(R.id.edtPar4);
    	
    	Button btnRun = (Button) findViewById(R.id.btnRun);
    	btnRun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int par3 = Integer.parseInt(edtPar3.getText().toString());
				int par4 = Integer.parseInt(edtPar4.getText().toString());
				m_oRover.run(par3, par4);
			}
		});
    	
    	bIRon = false;
    	ToggleButton btnIR = (ToggleButton) findViewById(R.id.btnIR);
    	btnIR.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				bIRon = ! bIRon;
				if (bIRon) {
					m_oRover.enableIR();
				} else {
					m_oRover.disableIR();
				}
			}
		});
    	
    	bLighton = false;
    	ToggleButton btnLight = (ToggleButton) findViewById(R.id.btnLight);
    	btnLight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				bIRon = ! bIRon;
				if (bIRon) {
					m_oRover.enableLight();
				} else {
					m_oRover.disableLight();
				}
			}
		});
    	
    	
    	Button btnCameraUp = (Button) findViewById(R.id.btnCameraUp);
    	btnCameraUp.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oRover.cameraStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oRover.cameraUp();
					break;
				}
				return true;
			}
		});

    	Button btnCameraDown = (Button) findViewById(R.id.btnCameraDown);
    	btnCameraDown.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					m_oRover.cameraStop();
					break;
				case MotionEvent.ACTION_DOWN:
					m_oRover.cameraDown();
					break;
				}
				return true;
			}
		});
    	    	
	}

	@Override
	protected void onConnect() {
		connected = true;
		m_oSensorGatherer.onConnect();
		updateButtons(true);
	}

	@Override
	protected void onDisconnect() {
		connected = false;
		updateButtons(false);
	}

	@Override
	protected void onConnectError() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void disconnect() {
		m_oRover.disconnect();
	}

	@Override
	protected void resetLayout() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		m_oRemoteCtrl.updateButtons(i_bEnabled);
		Utils.setEnabledRecursive((ViewGroup)m_oActivity.findViewById(android.R.id.content), i_bEnabled);
	}

	@Override
	protected IRobotDevice getRobot() {
		// TODO Auto-generated method stub
		return m_oRover;
	}

	@Override
	protected SensorGatherer getSensorGatherer() {
		// TODO Auto-generated method stub
		return m_oSensorGatherer;
	}

	@Override
	protected void connect() {
		m_oRover.connect();
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(VIDEO_GRP, VIDEO_SETTINGS_ID, VIDEO_SETTINGS_ID, "Video Settings");

		return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupVisible(VIDEO_GRP, m_oRover.isConnected());
    	
		return true;
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case VIDEO_SETTINGS_ID:
    		showDialog(DIALOG_SETTINGS_ID);
    		break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

    /**
     * This is called when a dialog is created for the first time.  The given
     * "id" is the same value that is passed to showDialog().
     */
    @Override
    protected Dialog onCreateDialog(int id) {
    	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    	View layout;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	switch (id) {
    	case DIALOG_SETTINGS_ID:
        	layout = inflater.inflate(R.layout.ac13rover_videosettings, null);
        	builder.setTitle("Video Resolution");
        	builder.setView(layout);
        	builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface arg0, int arg1) {
    				adjustVideoResolution();
    			}
    		});
        	m_dlgSettingsDialog = builder.create();
        	return m_dlgSettingsDialog;
    	}
    	return null;
    }

    /**
     * This is called each time a dialog is shown.
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	if (id == DIALOG_SETTINGS_ID) {
    		switch (m_oRover.getResolution()) {
    		case res_320x240:
    			((RadioButton) dialog.findViewById(R.id.rb320x240)).setChecked(true);
    			break;
    		case res_640x480:
    			((RadioButton) dialog.findViewById(R.id.rb640x480)).setChecked(true);
    			break;
    		default:
    			((RadioGroup) dialog.findViewById(R.id.rgVideoResolution)).clearCheck();
    			break;
    		}
    	}
    }
    
    private void adjustVideoResolution() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// in order for the resolution change to take effect we need to disconnect
				// and reconnect to the robot.
		    	disconnect();
		    	// wait for a moment to give the threads time to shut down
		    	Utils.waitSomeTime(500);
		    	// because the settings are changed over http requests we don't need to have
		    	// the tcp sockets connected in order to change the settings!
		    	switch (((RadioGroup) m_dlgSettingsDialog.findViewById(R.id.rgVideoResolution)).getCheckedRadioButtonId()) {
		    	case R.id.rb320x240:
		    		m_oSensorGatherer.setResolution(VideoResolution.res_320x240);
		    		break;
		    	case R.id.rb640x480:
		    		m_oSensorGatherer.setResolution(VideoResolution.res_640x480);
		    		break;
		    	}
		    	// connect again to receive the new video stream
		    	connect();
			}
		}).start();
    }
    
}
