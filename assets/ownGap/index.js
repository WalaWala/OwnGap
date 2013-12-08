var _timer = [];
var window = this;
var isDebug = false;
var printStats = true;
var timers = 2; // don't start at 0. two is good.
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
	if (isDebug)
		console.log("timer id: " + obj.id);
	return obj.id;
}

function setInterval(fn, timeout) {
	if (isDebug)
		console.log("new interval!!");
	if (isDebug && arguments.callee.caller)
		log("Setting interval from: " + arguments.callee.caller.toString());
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
	if (isDebug)
		console.log("timer id: " + obj.id);
	return obj.id;
}

function clearTimeout(id) {
	// todo: BUGBUG wargh, whatafix!!!
	//if (id === 0) {
	//	return;
	//}
	if (isDebug && arguments.callee.caller) {
		log("Clearing timeout from: " + arguments.callee.caller.toString() + " for id: " + id);
	} else if (isDebug && arguments.callee) {
		log("2) Clearing timeout for id: " + id);		
	}
	for (var i in _timer) {
		if (_timer.hasOwnProperty(i)) {
			if (_timer[i].id === id) {
				var index = _timer.indexOf(_timer[i]);
				var item = _timer.splice(index, 1)[0];
				if (isDebug) {
					log("found item index: " + index + "/" + JSON.stringify(item));
					log(item.fn.toString());
				}
				_oldTimers.push(item);
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
	return false;
}

function loadScript(src) {
	ig = window.ig;
	// normalize path (assets don't like .. and .)
	// taken from node.js
	function normalizeArray_node(parts, keepBlanks) {
		var directories = [],
		prev;
		for (var i = 0, l = parts.length - 1; i <= l; i++) {
			var directory = parts[i];

			// if it's blank, but it's not the first thing, and not the last thing, skip it.
			if (directory === "" && i !== 0 && i !== l && !keepBlanks) continue;

			// if it's a dot, and there was some previous dir already, then skip it.
			if (directory === "." && prev !== undefined) continue;

			// if it starts with "", and is a . or .., then skip it.
			if (directories.length === 1 && directories[0] === "" && (
				directory === "." || directory === "..")) continue;

			if (
				directory === ".." && directories.length && prev !== ".." && prev !== "." && prev !== undefined && (prev !== "" || keepBlanks)) {
				directories.pop();
				prev = directories.slice(-1)[0]
			} else {
			if (prev === ".") directories.pop();
				directories.push(directory);
				prev = directory;
			}
		}
		return directories;
	}
	src = normalizeArray_node(src.split("/"), false).join("/");
	log("Getting script: " + src);
	load(src);
}

loadScript("ownGap/ownGapCanvas.js");

var HTMLElement = function () {
	var scriptLoaderElement = {};
	var dataObj = {};
	scriptLoaderElement.setAttribute = function (dataStr, data) {
		dataObj[dataStr] = data;
	};
	scriptLoaderElement.getAttribute = function (dataStr) {
		return dataObj[dataStr];
	};

	scriptLoaderElement.onload = function() {};
	Object.defineProperty(scriptLoaderElement, "src",
		{
			set: function (value) {
				loadScript(value);
				if (scriptLoaderElement.onload) {
					scriptLoaderElement.onload.apply(window, [{currentTarget: scriptLoaderElement, type: "load"}]);
				}
			}
		}
	);
	scriptLoaderElement.removeEventListener = function () {
		
	};
	scriptLoaderElement.addEventListener = function (what, fun, capture) {
		if (what === "load") {
			scriptLoaderElement.onload = fun;
		}
	};
	scriptLoaderElement.appendChild = function () {};
	scriptLoaderElement.insertBefore = function () {};
	scriptLoaderElement.parentNode = scriptLoaderElement;
	scriptLoaderElement.readyState = "complete";
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
document.body = new HTMLElement();
window.document = document;
var width = 1280;
var height = 720;
var readyEvents = [];

function alert(str) {
	console.log(str);
}

function confirm() {}
function prompt() {}

var keyDownEvents = [];
var keyPressEvents = [];
var keyUpEvents = [];
window.addEventListener = function (evt, fn, capture) {
	if (evt === "load") {
		readyEvents.push(fn);
	} else if (evt === "keydown") {
		keyDownEvents.push(fn);
	} else if (evt === "keypress") {
		keyPressEvents.push(fn);
	} else if (evt === "keyup") {
		keyUpEvents.push(fn);
	}
};
document.addEventListener = function (evt, fn, capture) {
	if (evt === "DOMContentLoaded") {
		readyEvents.push(fn);
	} else if (evt === "keydown") {
		keyDownEvents.push(fn);
	} else if (evt === "keypress") {
		keyPressEvents.push(fn);
	} else if (evt === "keyup") {
		keyUpEvents.push(fn);
	}
};
document.removeEventListener = function (evt, fn, capture) {
	if (evt === "keydown") {
		var index = keyDownEvents.indexOf(fn);
		if (index !== -1) {
			keyDownEvents.splice(index, 1);
		}
	}
	if (evt === "keypress") {
		var index = keyPressEvents.indexOf(fn);
		if (index !== -1) {
			keyPressEvents.splice(index, 1);
		}
	}
	if (evt === "keyup") {
		var index = keyUpEvents.indexOf(fn);
		if (index !== -1) {
			keyUpEvents.splice(index, 1);
		}
	}
	if (evt === "load") {
		var index = readyEvents.indexOf(fn);
		if (index !== -1) {
			readyEvents.splice(index, 1);
		}
	}
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
loadScript("ownGap/xmlHttpRequest.js");
loadScript("ownGap/controller.js");

loadScript("main.js");

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


function tick() {
	while (!shouldExit()) {
		if (!isActivityPaused) {
			currentTime = getTimestamp();
			for (l = 0; l < _timer.length; ++l) {
				if (_timer[l] && _timer[l].targetTime <= currentTime) {
					var fun = _timer[l].fn;
					if (_timer[l].once) {
						clearTimeout(_timer[l].id);
					} else if (!_timer[l].once) {
						_timer[l].targetTime += _timer[l].timeout;
					}
					fun();
				}
			}
			++eventloopLoops;
			if (currentTime-lastLoop >= 2000) {
				if (printStats) {
					log("eventloopLoops: " + parseInt(eventloopLoops/2) + ", timerLength: " + _timer.length);
				}
				eventloopLoops = 0;
				if (secondsSinceReset >= 10) {
					// whatever, better check timer script, but for now...
					console.log("GC!");
					//callIdle(); // gc
					secondsSinceReset = 0;
					var tmptimer = [];
					for (l = 0; l < _timer.length; ++l) {
						if (_timer.hasOwnProperty(l) && _timer[l] && _timer[l].targetTime > currentTime) {
							tmptimer.push(_timer[l]);
						}
					}
					_timer = tmptimer;
				}
				++secondsSinceReset;
				lastLoop = getTimestamp();
			}
			window.ownGapCanvas.render();
		} else {
			if (!isPaused()) {
				isActivityPaused = false;
				currentTime = getTimestamp();
				for (v = 0; v < _timer.length; ++v) {
					_timer[v].targetTime = currentTime + _timer[v].timeout;
				}
			}
		}
	}
}
registerTick();

if (isDebug)
	log("JavaScript exited");
