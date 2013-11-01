package org.walawala.OwnGap;

import android.app.Activity;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.HashMap;

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
				Init(OwnGapActivity.this.getAssets(), "index.js");

				enterEventLoop();
			}
		});

		scriptThread.start();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.paused = false;
		startScriptAndStuff();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.paused = true;
		// todo
		contextLost();
		scriptThread = null;

		this.finish();
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
