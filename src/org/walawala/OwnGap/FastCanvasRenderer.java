/**
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

//import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.util.Log;


public class FastCanvasRenderer implements GLSurfaceView.Renderer {
	// ==========================================================================
	public FastCanvasRenderer(FastCanvasView view) {
		super();
		mView = view;
	}

	// ==========================================================================
	public String mRenderCommands;
	private FastCanvasView mView;
	public int width;
	public int height;
	public boolean alreadyOrthoSet = true;

	public LinkedList<String> imagesToLoad = new LinkedList<String>();
	public List<FastCanvasTexture> mTextures = new ArrayList<FastCanvasTexture>();

	// ==========================================================================
	private void flushQueue() {
		synchronized (this) {
			while (imagesToLoad.size() > 0) {
				String currentImage = imagesToLoad.remove();
				int imageId = OwnGapActivity.loadImage(currentImage);
				try {
					InputStream instream = mView.getContext().getAssets().open(currentImage);
					Bitmap bmp = BitmapFactory.decodeStream(instream);
					mTextures.add(imageId, new FastCanvasTexture(currentImage, imageId, new FastCanvasTextureDimension(bmp.getWidth(), bmp.getHeight())));
					bmp.recycle();
					bmp = null;
					instream.close();
				} catch (IOException e) {}
			}
			if (!alreadyOrthoSet) {
				OwnGapActivity.setOrtho(width, height);
				alreadyOrthoSet = true;
			}
		}
	}

	// ==========================================================================
	private void checkError() {
		int error = GLES10.glGetError();
		if (error != GLES10.GL_NO_ERROR) {
			Log.i("CANVAS", "CanvasRenderer glError=" + error);
		}
		assert error == GLES10.GL_NO_ERROR;
	}
	
	// ==========================================================================
	public void onDrawFrame(GL10 gl) {
		if (!mView.isPaused) {
			flushQueue();
			OwnGapActivity.render();
			checkError();
		}
	}

	// ==========================================================================
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.i("CANVAS", "CanvasRenderer onSurfaceCreated. config:" + config.toString()	+ " gl:" + gl.toString());

	    IntBuffer ib = IntBuffer.allocate(100);
	    ib.position(0);
	    GLES10.glGetIntegerv( GLES10.GL_RED_BITS, ib );
	    int red = ib.get(0);
	    GLES10.glGetIntegerv( GLES10.GL_GREEN_BITS, ib );
	    int green = ib.get(0);
	    GLES10.glGetIntegerv( GLES10.GL_BLUE_BITS, ib );
	    int blue = ib.get(0);
	    GLES10.glGetIntegerv( GLES10.GL_STENCIL_BITS, ib );
	    int stencil = ib.get(0);
	    GLES10.glGetIntegerv( GLES10.GL_DEPTH_BITS, ib );
	    int depth = ib.get(0);

		OwnGapActivity.initCanvas();
	    Log.i( "CANVAS", "CanvasRenderer R=" + red + " G=" + green + " B=" + blue + " DEPETH=" + depth + " STENCIL=" + stencil );
	}

	// ==========================================================================
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.i("CANVAS", "CanvasRenderer onSurfaceChanged. width:" + width + " height:" + height + " gl:" + gl.toString());

		OwnGapActivity.surfaceChanged(width, height);
	}

	// ==========================================================================
	// Not an override - this is a way for the view to tell the renderer when the context has been destroyed
	public void onSurfaceDestroyed() {
		Log.i("CANVAS", "CanvasRenderer onSurfaceDestroyed");
	}

	public void unloadTexture( int id) {
		OwnGapActivity.removeTexture(id);
		Log.i("CANVAS", "CanvasRenderer unloadtexture");
		checkError();
	}

	public void reloadTextures() {
		Log.i("CANVAS", "CanvasRenderer reloadtextures");
		// maybe later
	}
}
