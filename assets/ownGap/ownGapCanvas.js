console.log("Entering ownGapCanvas.js");

window.ownGapImage = function () {
	var img = {};
	var onload = null;

	Object.defineProperty(img, "onload", {
		get: function () {
			return onload;
		},
		set: function (value) {
			onload = value;
		}
	});

	img.width = 0;
	img.height = 0;
	img._int_id = 0;//image.getId();
	var src = "";

	Object.defineProperty(img, "src", {
		get: function () {
			return src;
		},
		set: function (value) {
			console.log("Loading image: " + value);
			img._int_id = addPngTexture(value);
			if (img._int_id === undefined) {
				return;
			}
			if (isDebug)
				console.log("img._int_id: " + img._int_id);
			src = value;

			function imgLoadTest() {
				if (isImageLoaded(img._int_id) || img._int_id === 0) {
					img.width = getTextureWidth(img._int_id);
					img.height = getTextureHeight(img._int_id);
					if (img.onload) {
						img.onload();
					}
				} else {
					setTimeout(imgLoadTest, 100);
				}
			}
			setTimeout(imgLoadTest, 100);
		}
	});

	Object.defineProperty(img, "int_id", {
		get: function () {
			return img._int_id;
		}
	});

	return img;
};
if (isOwnGap) {
	window.Image = window.ownGapImage;

	var Audio = function (path){
		this._volume = 1;
		this.stream_id = -1;
		this.sound_id = -1;
		this.isLoaded = false;
		this.callbackFunc = null;
		this.path = path;
		if (path)
			this.sound_id = loadSound(path);
		this._loop = false;
	};
	Audio.prototype.load = function () {
		var that = this;
		function loaded() {
			if (that.sound_id === 0) {
				return;
			}
			if (getSoundLoaded(that.sound_id)) {
				that.isLoaded = true;
				if (that.callbackFunc) {
					that.callbackFunc(that.path, true);
				}
			} else {
				setTimeout(loaded, 100);
			}
		}
		setTimeout(loaded, 100);
	};
	Audio.prototype.__defineGetter__("loop", function () {
		return this._loop;
	});
	Audio.prototype.__defineSetter__("loop", function (value) {
		this._loop = value;
	});
	Audio.prototype.play = function () {
		if (this.sound_id !== -1 && this.isLoaded) {
			this.stream_id = playSound(this.sound_id, this.loop);
		} else if (this.sound_id !== -1) {
			this.startSound = true;
		}
	};
	Audio.prototype.canPlayType = function () {
		return true;
	};
	Audio.prototype.pause = function () {};
	Audio.prototype.stop = function () {
		if (this.stream_id !== -1) {
			stopSound(this.stream_id);
		}
	};

	Audio.prototype.__defineGetter__("volume", function () {
		return this._volume;
	});
	Audio.prototype.__defineSetter__("volume", function (value) {
		this._volume = value;
		if (this.stream_id !== -1)
			setVolume(this.stream_id);
	});
	Audio.prototype.addEventListener = function (name, fun) {
		if (name === "canplaythrough") {
			this.callbackFunc = fun;
		}
	};
	Audio.prototype.removeEventListener = function (name, fun) {
		if (name === "canplaythrough") {
			this.callbackFunc = null;
		}
	};
}

var renderQueue = "";
window.ownGapCanvas = {
	images: [],

	ownGapContext: {
		drawImage: function (image, sx, sy, sw, sh, dx, dy, dw, dh) {
			return window.ownGapCanvas.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
		},
		save: function () {
			renderQueue = renderQueue.concat("v;");
		},
		restore: function () {
			renderQueue = renderQueue.concat("e;");
		},
		scale: function (a, d) {
			renderQueue = renderQueue.concat("k" + a.toFixed(6) + "," + d.toFixed(6) + ";");
		},
		fillStyle: "",
		fillRect: function () {},
		getImageData: function () {
			return [];
		}
	},

	style: {},

	getContext: function () {
		if (isDebug)
			console.log("getting context");
		return window.ownGapCanvas.ownGapContext;
	},
	createImage: function () {
		var img;
		if (isOwnGap) {
			img = window.ownGapImage();
			window.ownGapCanvas.images.push(img);
		} else {
			img = new Image();
		}
		return img;
	},

	createCanvas: function () {
		if (isOwnGap) {
			return window.ownGapCanvas;
		} else {
			return document.getElementById("canvas");
		}
	},

	render: function () {
		if (isOwnGap && renderQueue !== "") {
			render(renderQueue);
			renderQueue = "";
		}
	},
	drawImage: function (image, sx, sy, sw, sh, dx, dy, dw, dh) {
		if (sw === undefined && sh === undefined && dx === undefined && dy === undefined && dw === undefined && dh === undefined) {
			sw = image.width;
			sh = image.height;
		}
		if (dx === undefined && dy === undefined && dw === undefined && dh === undefined) {
			dw = sw;
			dh = sh;
			dx = sx;
			dy = sy;
			sx = 0;
			sy = 0;
		}
		if (sw === 0 && sh === 0 && dw === 0 && dh === 0) {
			sw = image.width;
			sh = image.height;
			dw = sw;
			dh = sh;
		}
		renderQueue = renderQueue.concat("d"+image.int_id+","+sx+","+sy+","+sw+","+sh+","+dx+","+dy+","+dw+","+dh+";");
	},
	_width: -1,
	_height: -1
};

Object.defineProperty(window.ownGapCanvas, "width", {
	get: function () {
		return this._width;
	},
	set: function (value) {
		this._width = value;
		if (this.width !== -1 && this.height !== -1) {
			setOrtho(this.width, this.height);
		}
	}
});

Object.defineProperty(window.ownGapCanvas, "height", {
	get: function () {
		return this._height;
	},
	set: function (value) {
		this._height = value;
		if (this.width !== -1 && this.height !== -1) {
			setOrtho(this.width, this.height);
		}
	}
});