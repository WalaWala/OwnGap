package org.walawala.OwnGap;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import tv.ouya.console.api.OuyaController;

import java.util.HashMap;

public class OController {
	public static HashMap<Integer, Boolean> buttons = new HashMap<Integer, Boolean>();
	public float axis_ls_x = 0;
	public float axis_ls_y = 0;
	public float axis_rs_x = 0;
	public float axis_rs_y = 0;
	public float axis_l2 = 0;
	public float axis_r2 = 0;

	public OController(View surfaceView, Context context) {
		OuyaController.init(context);

		surfaceView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
			@Override
			public boolean onGenericMotion(View view, MotionEvent motionEvent) {
				OController.this.axis_ls_x = motionEvent.getAxisValue(OuyaController.AXIS_LS_X);
				OController.this.axis_ls_y = motionEvent.getAxisValue(OuyaController.AXIS_LS_Y);
				OController.this.axis_rs_x = motionEvent.getAxisValue(OuyaController.AXIS_RS_X);
				OController.this.axis_rs_y = motionEvent.getAxisValue(OuyaController.AXIS_RS_Y);
				OController.this.axis_l2 = motionEvent.getAxisValue(OuyaController.AXIS_L2);
				OController.this.axis_r2 = motionEvent.getAxisValue(OuyaController.AXIS_R2);
				return true;
			}
		});
		surfaceView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				buttons.put(keyCode, keyEvent.getAction() == KeyEvent.ACTION_DOWN);
				return true;
			}
		});
	}

	public void ShowCursor(boolean showCursor) {
		OuyaController.showCursor(showCursor);
	}

	public boolean GetButtonState(int playerId, int button) {
		if (buttons.containsKey(button)) {
			return buttons.get(button);
		}
		return false;
	}

	public float GetAxisState(int playerId, int axis) {
		switch (axis) {
			case 0: // LX
				return this.axis_ls_x;
			case 1: // LY
				return this.axis_ls_y;
			case 2: // RX
				return this.axis_rs_x;
			case 3: // RY
				return this.axis_rs_y;
			case 4: // L2
				return this.axis_l2;
			case 5: // R2
				return this.axis_r2;
		}
		return 0.0f;
	}
}
