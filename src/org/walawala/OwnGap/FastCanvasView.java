/**
 * Copyright 2012 Adobe Systems Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.walawala.OwnGap;

import android.opengl.GLSurfaceView;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class FastCanvasView extends GLSurfaceView {
	public class InputConnection extends BaseInputConnection
	{
		private final KeyEvent delKeyDownEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
		private final KeyEvent delKeyUpEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL);

		public InputConnection(View view)
		{
			super(view, false);
			this.setSelection(0, 0);
		}

		@Override
		public boolean deleteSurroundingText(int leftLength, int rightLength)
		{
			// Android SDK 16+ doesn't send key events for backspace but calls this method
			//NativeInterface.Activity.onKeyDown(KeyEvent.KEYCODE_DEL, this.delKeyDownEvent);
			//NativeInterface.Activity.onKeyUp(KeyEvent.KEYCODE_DEL, this.delKeyUpEvent);
			synchronized (OController.keysDown) {
				OController.keysDown.add(8);
			}
			return super.deleteSurroundingText(leftLength, rightLength);
		}
	}

	public FastCanvasView(Context context) {
		super(context);
		this.setEGLConfigChooser( false );// turn off the depth buffer
		mRenderer = new FastCanvasRenderer(this);

		this.setRenderer(mRenderer);
		this.setRenderMode(RENDERMODE_CONTINUOUSLY);

		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		this.requestFocus();
	}

	@Override
	public boolean onCheckIsTextEditor() // required for creation of soft keyboard
	{
		return false;
	}

	public void setQueue(String queue) {
		mRenderer.mRenderCommands = queue;
	}

	@Override
	public boolean onTouchEvent (MotionEvent event) {
		// todo!!!
		return true;
	}
	
	public void surfaceCreated(SurfaceHolder holder)
	{
        Log.i("CANVAS", "CanvasView surfaceCreated");
	    super.surfaceCreated(holder);
	    if (isPaused) {
	    	isPaused = false;
	    	mRenderer.reloadTextures();
	    	//super.onResume();
	    }
	}
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttributes)  // required for creation of soft keyboard
	{
		outAttributes.actionId = EditorInfo.IME_ACTION_DONE;
		outAttributes.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI;
		outAttributes.inputType = InputType.TYPE_CLASS_TEXT;
		return new InputConnection(this);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
        Log.i("CANVAS", "CanvasView surfaceChanged");
		OwnGapActivity.surfaceChanged(w, h);
	    super.surfaceChanged(holder, format, w, h);
	}
	
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// Context is gone, no GL calls after this
        Log.i("CANVAS", "CanvasView surfaceDestroyed");
		isPaused = true;
	    super.surfaceDestroyed(holder);
		super.onPause();
		OwnGapActivity.contextLost();
		mRenderer.onSurfaceDestroyed();
	}

	public final FastCanvasRenderer mRenderer;
	public boolean isPaused = false;
}
