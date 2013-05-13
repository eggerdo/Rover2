package org.dobots.ctrl.robots.rover2;

import java.io.IOException;

import org.dobots.ctrl.robots.ac13.AC13RoverTypes.AC13RoverParameters;
import org.dobots.ctrl.robots.rover2.Rover2Types.VideoResolution;
import org.dobots.utilities.Utils;

import robots.IVideoListener;
import robots.RobotType;
import robots.ctrl.DifferentialRobot;
import robots.ctrl.IMoveRepeaterListener;
import robots.ctrl.MoveRepeater;
import robots.ctrl.MoveRepeater.MoveCommand;
import robots.gui.MessageTypes;
import android.os.Handler;

public class Rover2 extends DifferentialRobot implements IMoveRepeaterListener {
	
	private static final String TAG = "Rover2";
	
	private Rover2Controller m_oController;

	private Handler m_oUiHandler;

	private MoveRepeater m_oRepeater;

	private double m_dblBaseSpeed = 50.0;

	private VideoResolution m_eResolution = VideoResolution.res_unknown;

	public Rover2() {
		super(Rover2Types.AXLE_WIDTH, Rover2Types.MIN_SPEED, Rover2Types.MAX_SPEED, Rover2Types.MIN_RADIUS, Rover2Types.MAX_RADIUS);
		
		m_oController = new Rover2Controller();

		m_oRepeater = new MoveRepeater(this, 100);
	}

	public void setVideoListener(IVideoListener i_oListener) {
		m_oController.setVideoListener(i_oListener);
	}
	
	public void removeVideoListener(IVideoListener i_oListener) {
		m_oController.removeVideoListener(i_oListener);
	}
	
	public void setHandler(Handler i_oUiHandler) {
		m_oUiHandler = i_oUiHandler;
	}

	@Override
	public RobotType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void connect() {
		Utils.runAsyncTask(new Runnable() {
			
			public void run() {
				if (m_oController.connect()) {
					Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTED, null);
				} else {
					Utils.sendMessage(m_oUiHandler, MessageTypes.STATE_CONNECTERROR, null);
				}
			}
		});
	}

	@Override
	public void disconnect() {
		m_oController.disconnect();
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return m_oController.bConnected;
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeCircle(double i_dblTime, double i_dblSpeed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBaseSpeed(double i_dblSpeed) {
		m_dblBaseSpeed = i_dblSpeed;
	}

	@Override
	public double getBaseSped() {
		return m_dblBaseSpeed;
	}

	@Override
	public void moveForward() {
		moveForward(m_dblBaseSpeed );
	}

	@Override
	public void moveBackward() {
		moveBackward(m_dblBaseSpeed);
	}

	@Override
	public void rotateCounterClockwise() {
		rotateCounterClockwise(m_dblBaseSpeed);
	}

	@Override
	public void rotateClockwise() {
		rotateClockwise(m_dblBaseSpeed);
	}

	@Override
	public void moveLeft() {
		// not available
	}

	@Override
	public void moveRight() {
		// not available
	}

	public void move(int par1, int par2) {
		m_oController.move(par1, par2);
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void toggleInvertDrive() {
		// TODO Auto-generated method stub
		
	}

	public void run(int par3, int par4) {
		try {
			m_oController.run(par3, par4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void enableIR() {
		try {
			m_oController.enableIR();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void disableIR() {
		try {
			m_oController.disableIR();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void enableLight() {
		try {
		m_oController.ledON();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}

	public void disableLight() {
		try {
		m_oController.ledOFF();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}

	public void cameraUp() {
		try {
			m_oController.cameraup();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void cameraStop() {
		try {
			m_oController.camerastop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void cameraDown() {
		try {
			m_oController.cameradown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setResolution(VideoResolution i_eResolution) {
		if (m_eResolution != i_eResolution) {
			switch(i_eResolution) {
			case res_320x240:
				m_oController.setResolution320x240();
				break;
			case res_640x480:
				m_oController.setResolution640x480();
				break;
			}
			m_eResolution = i_eResolution;
		}
	}
	
	public VideoResolution getResolution() {
		if (m_eResolution == VideoResolution.res_unknown) {
			// we don't know what the resolution is so we obtain it from the robot
			AC13RoverParameters param = m_oController.getParameters();
			switch (Integer.valueOf(param.resolution)) {
			case 8:
				m_eResolution = VideoResolution.res_320x240;
				break;
			case 32:
				m_eResolution = VideoResolution.res_640x480;
				break;
			}
		}
		return m_eResolution;
	}
	
	@Override
	public void moveForward(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_FWD, i_dblSpeed, true);
	}

	@Override
	public void moveForward(double i_dblSpeed, int i_nRadius) {
		m_oRepeater.startMove(MoveCommand.MOVE_FWD, i_dblSpeed, i_nRadius, true);
	}

	@Override
	public void moveForward(double i_dblSpeed, double i_dblAngle) {

		debug(TAG, String.format("moveForward(%f, %f)", i_dblSpeed, i_dblAngle));
		
		if (Math.abs(i_dblAngle) < 10) {
			moveForward(i_dblSpeed);
		} else {
			int nRadius = angleToRadius(i_dblAngle);
			moveForward(i_dblSpeed, nRadius);
		}
		
	}

	@Override
	public void moveBackward(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.MOVE_BWD, i_dblSpeed, true);
	}

	@Override
	public void moveBackward(double i_dblSpeed, int i_nRadius) {
		m_oRepeater.startMove(MoveCommand.MOVE_BWD, i_dblSpeed, i_nRadius, true);
	}

	@Override
	public void moveBackward(double i_dblSpeed, double i_dblAngle) {

		debug(TAG, String.format("moveBackward(%f, %f)", i_dblSpeed, i_dblAngle));
		
		if (Math.abs(i_dblAngle) < 10) {
			moveBackward(i_dblSpeed);
		} else {
			int nRadius = angleToRadius(i_dblAngle);
			moveBackward(i_dblSpeed, nRadius);
		}
		
	}

	@Override
	public void rotateClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.ROTATE_RIGHT, i_dblSpeed, true);
	}

	@Override
	public void rotateCounterClockwise(double i_dblSpeed) {
		m_oRepeater.startMove(MoveCommand.ROTATE_LEFT, i_dblSpeed, true);
	}

	@Override
	public void moveStop() {
		m_oRepeater.stopMove();
		m_oController.moveStop();
	}

	
	// ------------------------------------------------------------------------------------------

	@Override
	public void onDoMove(MoveCommand i_eMove, double i_dblSpeed) {
		switch(i_eMove) {
		case MOVE_BWD:
			executeMoveBackward(i_dblSpeed);
			break;
		case MOVE_FWD:
			executeMoveForward(i_dblSpeed);
			break;
		case ROTATE_LEFT:
			executeRotateCounterClockwise(i_dblSpeed);
			break;
		case ROTATE_RIGHT:
			executeRotateClockwise(i_dblSpeed);
			break;
		default:
			error(TAG, "Move not available");
			return;
		}
	}

	private void executeMoveForward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.moveForward(nVelocity, nVelocity);
	}
	
	private void executeMoveBackward(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);

		m_oController.moveBackward(nVelocity, nVelocity);
	}
	
	private void executeRotateCounterClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.rotateLeft(nVelocity);
	}
	
	private void executeRotateClockwise(double i_dblSpeed) {
		int nVelocity = calculateVelocity(i_dblSpeed);
		
		m_oController.rotateRight(nVelocity);
	}

	@Override
	public void onDoMove(MoveCommand i_eMove, double i_dblSpeed, int i_nRadius) {
		switch(i_eMove) {
		case MOVE_BWD:
			executeMoveBackward(i_dblSpeed, i_nRadius);
			break;
		case MOVE_FWD:
			executeMoveForward(i_dblSpeed, i_nRadius);
			break;
		default:
			error(TAG, "Move not available");
			return;
		}
	}

	private void executeMoveForward(double i_dblSpeed, int i_nRadius) {
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);
		
		m_oController.moveForward(oVelocity.left, oVelocity.right);
	}

	private void executeMoveBackward(double i_dblSpeed, int i_nRadius) {
		DriveVelocityLR oVelocity = calculateVelocity(i_dblSpeed, i_nRadius);
		
		m_oController.moveBackward(oVelocity.left, oVelocity.right);
	}

	// ------------------------------------------------------------------------------------------

}
