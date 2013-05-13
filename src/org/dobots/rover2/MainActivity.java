package org.dobots.rover2;

import org.dobots.ctrl.robots.rover2.Rover2;
import org.dobots.gui.robots.rover2.Rover2Robot;

import robots.RobotInventory;
import robots.RobotType;
import robots.ctrl.IRobotDevice;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {
	
	public static Activity INSTANCE;
	
	public static Activity getInstance() {
		return INSTANCE;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		INSTANCE = this;
		
		showRobot(RobotType.RBT_ROVER2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void showRobot(RobotType i_eType) {
		try {
			String i_strRobotID = createRobot(i_eType);
			createRobotView(i_eType, i_strRobotID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createRobotView(RobotType i_eType, String i_strRobotID) {
		Intent intent = new Intent(MainActivity.this, Rover2Robot.class);
		intent.putExtra("RobotType", i_eType);
		intent.putExtra("RobotID", i_strRobotID);
		intent.putExtra("OwnsRobot", true);
		startActivity(intent);
	}
	
	public String createRobot(RobotType i_eType) throws Exception {
		IRobotDevice oRobot = new Rover2();
		String i_strRobotID = RobotInventory.getInstance().addRobot(oRobot);
		return i_strRobotID;
	}

}
