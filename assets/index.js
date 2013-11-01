var _timer = [];
var window = this;
var isDebug = false;
var printStats = true;
var timers = 0;
var _oldTimers = [];
var isOwnGap = true;
function setTimeout(fn, timeout) {
	if (isDebug && arguments.callee.caller)
		log("Setting timeout from: " + arguments.callee.caller.toString());
	var obj;
	if (_oldTimers.length > 0) {
		obj = _oldTimers.pop();
		obj.once = true;
		obj.targetTime = getTimestamp() + timeout;
		obj.timeout = timeout;
		obj.fn = fn;
	} else {
		obj = { id: timers++, once: true, targetTime: getTimestamp() + timeout, timeout: timeout, fn: fn };
	}
	_timer.push(obj);
	return obj.id;
}

function setInterval(fn, timeout) {
	var obj;
	if (_oldTimers.length > 0) {
		obj = _oldTimers.pop();
		obj.once = false;
		obj.targetTime = getTimestamp() + timeout;
		obj.timeout = timeout;
		obj.fn = fn;
	} else {
		obj = { id: timers++, once: false, targetTime: getTimestamp() + timeout, timeout: timeout, fn: fn };
	}
	_timer.push(obj);
	return obj.id;
}

function clearTimeout(id) {
	if (isDebug && arguments.callee.caller)
		log("Clearing timeout from: " + arguments.callee.caller.toString());
	for (var i in _timer) {
		if (_timer.hasOwnProperty(i)) {
			if (_timer[i].id === id) {
				var index = _timer.indexOf(_timer[i]);
				//delete _timer[i];
				_oldTimers.push(_timer.splice(index, 1)[0]);
			}
		}
	}
}
var clearInterval = clearTimeout;

var console = {
	log: function (msg) { log(msg); },
	err: function (msg) { log(msg); },
	warn: function (msg) { log(msg); }
};

var isActivityPaused = false;
function shouldExit() {
	return isActivityPaused;
}

function loadScript(src) {
	ig = window.ig;
	log("Getting script: " + src);
	load(src);
}

loadScript("ownGapCanvas.js");

var HTMLElement = function () {
	var scriptLoaderElement = {};
	scriptLoaderElement.onload = function() {};
	Object.defineProperty(scriptLoaderElement, "src",
		{
			set: function (value) {
				loadScript(value);
				if (scriptLoaderElement.onload) {
					scriptLoaderElement.onload.apply(window);
				}
			}
		}
	);
	scriptLoaderElement.appendChild = function () {};

	return scriptLoaderElement;
};
var allScripts = [];
var dummyElement = new HTMLElement();

var document = {};
document.createElement = function (tagName) {
	switch (tagName.toLowerCase()) {
		case "canvas":
			return window.ownGapCanvas;
		case "script":
			var sc = new HTMLElement();
			allScripts.push(sc);
			return sc;
	}
	console.log("Returning dummy element");
	return dummyElement;
};

document.getElementById = function (id) {
	for (var i in allScripts) {
		if (allScripts.hasOwnProperty(i)) {
			if (allScripts[i].id === id) {
				return allScripts[i];
			}
		}
	}
	return window.ownGapCanvas;
};
document.getElementsByTagName = function (tagName) {
	switch (tagName.toLowerCase()) {
		case "canvas":
			return [window.ownGapCanvas];
		case "script":
			return allScripts;

	}
	if (isDebug)
		console.log("getElementsByTagName: " + tagName);
	return [dummyElement];
};

document.location = {};
document.location.href = "index.js";
document.readyState = "complete";
document.body = {};

var width = 1280;
var height = 720;
var readyEvents = [];

function alert(str) {
	console.log(str);
}

function confirm() {}
function prompt() {}

window.addEventListener = function (evt, fn, capture) {
	if (evt === "load") {
		readyEvents.push(fn);
	}
};
document.addEventListener = function (evt, fn, capture) {
	if (evt === "DOMContentLoaded") {
		readyEvents.push(fn);
	}
};
document.removeEventListener = function (evt, fn, capture) {
};
window.removeEventListener = function (evt, fn, capture) {
};
window.innerWidth = width;
window.innerHeight = height;
window.screen = {};
window.screen.width = width;
window.screen.height = height;
window.screen.availWidth = width;
window.screen.availHeight = height;
navigator = {};
navigator.userAgent = "";
loadScript("lib/impact/impact.js");
loadScript("lib/game/main.js");

if (document.onready)
	document.onready.apply(window);
if (window.onready)
	window.onready.apply(window);

for (var i in readyEvents) {
	if (readyEvents.hasOwnProperty(i)) {
		readyEvents[i].apply(window);
	}
}

setBackgroundColor(0.0, 0.0, 0.0);
showCursor(false);

// own eventloop
log("Entering event loop (readyEvents: " + readyEvents.length +")...");
var v = 0;
var eventloopLoops = 0;
var lastLoop = getTimestamp();
var secondsSinceReset = 0;
var currentTime = 0;

setInterval(function () {
	isActivityPaused = isPaused();
}, 500);
loadScript("controller.js");


function tick() {
	while (!shouldExit()) {
		//if (!isPaused()) {
			currentTime = getTimestamp();
			for (v = 0; v < _timer.length; ++v) {
				if (_timer[v] && _timer[v].targetTime <= currentTime) {
					var fun = _timer[v].fn;
					if (_timer[v].once) {
						var index = _timer.indexOf(_timer[v]);
						_oldTimers.push(_timer.splice(index, 1)[0]);
					} else if (!_timer[v].once) {
						_timer[v].targetTime += _timer[v].timeout;
					}
					fun();
				}
			}
			++eventloopLoops;
			if (currentTime-lastLoop >= 2000) {
				if (printStats) {
					log("eventloopLoops: " + eventloopLoops + ", timerLength: " + _timer.length);
				}
				eventloopLoops = 0;
				//if (secondsSinceReset >= 10) {
				//	console.log("GC!");
				//	callIdle(); // gc
				//	secondsSinceReset = 0;
				//}
				++secondsSinceReset;
				lastLoop = getTimestamp();
			}
			window.ownGapCanvas.render();
		//}
	}
}
registerTick();

if (isDebug)
	log("JavaScript exited");
