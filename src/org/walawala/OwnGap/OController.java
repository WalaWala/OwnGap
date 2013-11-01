package org.walawala.OwnGap;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import tv.ouya.console.api.OuyaController;

import java.util.HashMap;

public class OController {
	public class ControllerInfo {
		public HashMap<Integer, Boolean> buttons = new HashMap<Integer, Boolean>();
		public float axis_ls_x = 0;
		public float axis_ls_y = 0;
		public float axis_rs_x = 0;
		public float axis_rs_y = 0;
		public float axis_l2 = 0;
		public float axis_r2 = 0;
	}

	public HashMap<Integer, ControllerInfo> controllers = new HashMap<Integer, ControllerInfo>();

	public OController(View surfaceView, Context context) {
		OuyaController.init(context);
		for (int i = 0; i < 5; ++i) {
			controllers.put(i, new ControllerInfo());
		}

		surfaceView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
			@Override
			public boolean onGenericMotion(View view, MotionEvent motionEvent) {
				int playerId = OuyaController.getPlayerNumByDeviceId(motionEvent.getDeviceId());
				ControllerInfo controller = controllers.get(playerId);

				controller.axis_ls_x = motionEvent.getAxisValue(OuyaController.AXIS_LS_X);
				controller.axis_ls_y = motionEvent.getAxisValue(OuyaController.AXIS_LS_Y);
				controller.axis_rs_x = motionEvent.getAxisValue(OuyaController.AXIS_RS_X);
				controller.axis_rs_y = motionEvent.getAxisValue(OuyaController.AXIS_RS_Y);
				controller.axis_l2 = motionEvent.getAxisValue(OuyaController.AXIS_L2);
				controller.axis_r2 = motionEvent.getAxisValue(OuyaController.AXIS_R2);
				return true;
			}
		});
		surfaceView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				int playerId = OuyaController.getPlayerNumByDeviceId(keyEvent.getDeviceId());
				controllers.get(playerId).buttons.put(keyCode, keyEvent.getAction() == KeyEvent.ACTION_DOWN);
				return true;
			}
		});
	}

	public void ShowCursor(boolean showCursor) {
		OuyaController.showCursor(showCursor);
	}

	public boolean GetButtonState(int playerId, int button) {
		ControllerInfo controller = controllers.get(playerId);
		if (controller.buttons.containsKey(button)) {
			return controller.buttons.get(button);
		}
		return false;
	}

	public float GetAxisState(int playerId, int axis) {
		ControllerInfo controller = controllers.get(playerId);
		switch (axis) {
			case 0: // LX
				return controller.axis_ls_x;
			case 1: // LY
				return controller.axis_ls_y;
			case 2: // RX
				return controller.axis_rs_x;
			case 3: // RY
				return controller.axis_rs_y;
			case 4: // L2
				return controller.axis_l2;
			case 5: // R2
				return controller.axis_r2;
		}
		return 0.0f;
	}
}
