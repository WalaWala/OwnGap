package org.walawala.OwnGap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import tv.ouya.console.api.OuyaIntent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class OwnGapActivity extends Activity {
	static {
		System.loadLibrary("owngap");
	}
	private FastCanvasView fastCanvasView;
	public OController controller;
	private HashMap<Integer, Boolean> soundLoaded = new HashMap<Integer, Boolean>();
	private HashMap<Integer, MediaPlayer> mediaPlayers = new HashMap<Integer, MediaPlayer>();
	private SoundPool soundPool;
	private boolean paused;
	private int imagesNumbers = 0;

	private Thread scriptThread;

	private void startScriptAndStuff() {
		fastCanvasView = new FastCanvasView(this);
		controller = new OController(fastCanvasView, this);
		soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
		SetJavaFunctions(this);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final RelativeLayout top = new RelativeLayout(OwnGapActivity.this);
				top.setLayoutParams(new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.MATCH_PARENT));
				top.addView(fastCanvasView);
				OwnGapActivity.this.setContentView(top);

			} // end run
		}); // end runnable

		scriptThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Init(OwnGapActivity.this.getAssets(), "ownGap/index.js");

				enterEventLoop();
			}
		});

		scriptThread.start();

	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			for(Integer se: soundLoaded.keySet()) {
				soundPool.stop(se);
			}
		}
	};
	PowerManager.WakeLock wl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "OwnGap Wakelock");

		this.registerReceiver(broadcastReceiver, new IntentFilter(OuyaIntent.ACTION_MENUAPPEARING));
		wl.acquire();
		startScriptAndStuff();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.paused = false;
		//startScriptAndStuff();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.paused = true;
		// todo
		//contextLost();
		//scriptThread = null;
		//this.unregisterReceiver(broadcastReceiver);
		//wl.release();
		//this.finish();
	}

	public boolean IsPaused() {
		return this.paused;
	}

	public int LoadSound(String path) {
		try {
			soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					soundLoaded.put(sampleId, true);
				}
			});
			int soundId = soundPool.load(getAssets().openFd(path), 1);

			return soundId;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public boolean GetSoundLoaded(int soundId) {
		if (!soundLoaded.containsKey(soundId)) {
			return false;
		}

		return soundLoaded.get(soundId);
	}

	public void StopSound(int streamId) {
		soundPool.stop(streamId);
	}

	public void SetVolume(int streamId, float volume) {
		if (streamId == -1) {
			return;
		}
		if (volume == -1) {
			volume = 1;
		}
		soundPool.setVolume(streamId, volume, volume);
	}

	public int PlaySound(final int id, final boolean loop) {
		soundPool.play(id, 0.5f, 0.5f, 1, loop ? -1 : 0, 1.0f);
		return id;
	}

	public void SetRenderQueueCommands(String queue) {
		synchronized (fastCanvasView) {
			fastCanvasView.setQueue(queue);
		}
	}

	public int LoadImage(String imagePath) {
		if (imagePath.equals("")) {
			return -1;
		}
		synchronized (fastCanvasView.mRenderer) {
			fastCanvasView.mRenderer.imagesToLoad.add(imagePath);
		}
		return imagesNumbers++;
	}

	public int GetImageWidth(int id) {
		if (fastCanvasView.mRenderer.mTextures.size() < id)
			return -1;
		synchronized (fastCanvasView.mRenderer) {
			FastCanvasTexture tex = fastCanvasView.mRenderer.mTextures.get(id);
			if (tex != null) {
				return tex.dimension.width;
			}
		}
		return -1;
	}

	public int GetImageHeight(int id) {
		if (fastCanvasView.mRenderer.mTextures.size() < id)
			return -1;
		synchronized (fastCanvasView.mRenderer) {
			FastCanvasTexture tex = fastCanvasView.mRenderer.mTextures.get(id);
			if (tex != null) {
				return tex.dimension.height;
			}
		}
		return -1;
	}

	public void ShowCursor(boolean showCursor) {
		controller.ShowCursor(showCursor);
	}

	private HashMap<Integer, String> httpResponses = new HashMap<Integer, String>();
	int currentRequestId = 0;
	public int MakeHttpRequest(final String targetUrl, final String method, final String parameters) {
		final int thatId = currentRequestId + 1;

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				URL url;
				HttpURLConnection connection = null;
				try {
					//Create connection
					url = new URL(targetUrl);
					connection = (HttpURLConnection)url.openConnection();
					connection.setRequestMethod(method);
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

					connection.setRequestProperty("Content-Length", Integer.toString(parameters.getBytes().length));
					//connection.setRequestProperty("Content-Language", "en-US");

					connection.setUseCaches(false);
					connection.setDoInput(true);
					connection.setDoOutput(true);

					//Send request
					DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
					wr.writeBytes(parameters);
					wr.flush();
					wr.close();

					//Get Response
					InputStream is = connection.getInputStream();
					BufferedReader rd = new BufferedReader(new InputStreamReader(is));
					String line;
					StringBuilder response = new StringBuilder();
					while((line = rd.readLine()) != null) {
						response.append(line);
						response.append('\r');
					}
					rd.close();
					httpResponses.put(thatId, response.toString());
				} catch (Exception e) {
					e.printStackTrace();
					httpResponses.put(thatId, "<Error>");
				} finally {
					if(connection != null) {
						connection.disconnect();
					}
				}
			}
		});
		t.start();

		return thatId;
	}

	public void ShowKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null) {
			inputMethodManager.showSoftInput(fastCanvasView, InputMethodManager.SHOW_FORCED);
		}
	}

	public void HideKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null) {
			inputMethodManager.hideSoftInputFromWindow(fastCanvasView.getWindowToken(), 0);
		}
	}

	public String GetHttpResponse(int id) {
		if (httpResponses.containsKey(id)) {
			return httpResponses.get(id);
		}
		return "<No Response>";
	}

	public boolean GetButtonState(int playerId, int button) {
		return controller.GetButtonState(playerId, button);
	}

	public float GetAxisState(int playerId, int axis) {
		return controller.GetAxisState(playerId, axis);
	}

	public void SetOrtho(final int width, final int height) {
		fastCanvasView.mRenderer.width = width;
		fastCanvasView.mRenderer.height = height;
		fastCanvasView.mRenderer.alreadyOrthoSet = false;
	}

	public int[] GetKeys() {
		int[] ret = new int[controller.keysDown.size()];
		Iterator<Integer> iterator = controller.keysDown.iterator();
		if (controller.keysDown.size() == 0) {
			return ret;
		}

		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = iterator.next();
		}

		synchronized (controller.keysDown) {
			controller.keysDown.clear();
		}

		return ret;
	}

	public boolean ShiftPressed() {
		boolean pressed = controller.isShiftPressed;
		controller.isShiftPressed = false;
		return pressed;
	}

	public native boolean Init(AssetManager manager, String fileName);
	public native void SetJavaFunctions(OwnGapActivity activity);
	public static native void setBackgroundColor(int red, int green, int blue);
	public static native void setOrtho(int width, int height);
	public static native void addTexture(int id, int glID, int width, int height); // id's must be from 0 to numTextures-1
	public static native void removeTexture(int id); // id must have been passed to addTexture in the past
	public static native int loadImage(String imagePath);
	public static native void render();
	public static native void enterEventLoop();
	public static native void initCanvas();
	public static native void surfaceChanged( int width, int height );
	public static native void contextLost(); // Deletes native memory associated with lost GL context
	public static native void release(); // Deletes native canvas
}
