package org.walawala.OwnGap;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import tv.ouya.console.api.OuyaController;
import tv.ouya.console.api.OuyaFacade;

import java.util.ArrayList;
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
	public static final ArrayList<Integer> keysDown = new ArrayList<Integer>();
	public boolean isShiftPressed = false;
	private final boolean isRunninOnOuya = OuyaFacade.getInstance().isRunningOnOUYAHardware();

	public OController(View surfaceView, Context context) {
		OuyaController.init(context);
		for (int i = 0; i < 5; ++i) {
			controllers.put(i, new ControllerInfo());
		}

		surfaceView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
			@Override
			public boolean onGenericMotion(View view, MotionEvent motionEvent) {
				if (isRunninOnOuya) {
					int playerId = OuyaController.getPlayerNumByDeviceId(motionEvent.getDeviceId());
					ControllerInfo controller = controllers.get(playerId);

					controller.axis_ls_x = motionEvent.getAxisValue(OuyaController.AXIS_LS_X);
					controller.axis_ls_y = motionEvent.getAxisValue(OuyaController.AXIS_LS_Y);
					controller.axis_rs_x = motionEvent.getAxisValue(OuyaController.AXIS_RS_X);
					controller.axis_rs_y = motionEvent.getAxisValue(OuyaController.AXIS_RS_Y);
					controller.axis_l2 = motionEvent.getAxisValue(OuyaController.AXIS_L2);
					controller.axis_r2 = motionEvent.getAxisValue(OuyaController.AXIS_R2);
				}
				return true;
			}
		});

		surfaceView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
				if (keyEvent.getDeviceId() != -1) {
					int playerId = OuyaController.getPlayerNumByDeviceId(keyEvent.getDeviceId());
					controllers.get(playerId).buttons.put(keyCode, keyEvent.getAction() == KeyEvent.ACTION_DOWN);
				} else {
					isShiftPressed = keyEvent.isShiftPressed();
					if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
						synchronized (keysDown) {
							// todo: a map would be much better than this
							if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
								keysDown.add(keyCode + 36);
							} else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
								keysDown.add(keyCode + 41);
							} else if (keyCode == KeyEvent.KEYCODE_SPACE) {
								keysDown.add(32);
							} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
								keysDown.add(13);
							} else if (keyCode == KeyEvent.KEYCODE_TAB) {
								keysDown.add(9);
							} else if (keyCode == KeyEvent.KEYCODE_COMMA) {
								keysDown.add(188);
							} else if (keyCode == KeyEvent.KEYCODE_PERIOD) {
								keysDown.add(190);
							} else if (keyCode == KeyEvent.KEYCODE_DEL) {
								keysDown.add(8);
							} else if (keyCode == KeyEvent.KEYCODE_PLUS) {
								keysDown.add(171);
							} else if (keyCode == KeyEvent.KEYCODE_POUND) {
								keysDown.add(163);
							} else if (keyCode == KeyEvent.KEYCODE_RIGHT_BRACKET) {
								keysDown.add(221);
							} else if (keyCode == KeyEvent.KEYCODE_LEFT_BRACKET) {
								keysDown.add(219);
							} else if (keyCode == KeyEvent.KEYCODE_SLASH) {
								keysDown.add(191);
							} else if (keyCode == KeyEvent.KEYCODE_BACKSLASH) {
								keysDown.add(220);
							} else if (keyCode == KeyEvent.KEYCODE_APOSTROPHE) {
								keysDown.add(222);
							}
						}
					}
				}
				return true;
			}
		});
		/*surfaceView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// todo: touch events

				return true;
			}
		});*/
	}

	public void ShowCursor(boolean showCursor) {
		if (isRunninOnOuya) {
			OuyaController.showCursor(showCursor);
		}
	}

	public boolean GetButtonState(int playerId, int button) {
		if (isRunninOnOuya) {
			ControllerInfo controller = controllers.get(playerId);
			if (controller.buttons.containsKey(button)) {
				return controller.buttons.get(button);
			}
		}
		return false;
	}

	public float GetAxisState(int playerId, int axis) {
		if (isRunninOnOuya) {
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
		}
		return 0.0f;
	}
}
