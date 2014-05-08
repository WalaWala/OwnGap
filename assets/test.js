loadScript("test2.js");
function runAjax(url, JSONstring, callback) {
	if (XMLHttpRequest) {
		req = new XMLHttpRequest();
		var params = "data=" + encodeURIComponent(JSONstring)

		req.open("POST", "http://www.walawala.org/" + url, true);
		//req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		//req.setRequestHeader("Content-length", params.length);
		//req.setRequestHeader("Connection", "close");
		req.onreadystatechange = callback;
		req.send(params);
	}
}

runAjax("getScores", "", function () {
	console.log("XMLHTTPREQUEST: " + req.readyState);
	if (req.readyState === 4) {
		try {
			console.log("XMLHTTPREQUEST ready: " + req.responseText);
		} catch (e) {}
	}
});

setBackgroundColor(1.0, 1.0, 0.0);
var canvas = document.createElement("canvas");
window.ownGapCanvas.width = 1280;
window.ownGapCanvas.height = 720;

var context = canvas.getContext("2d");
var image = new Image();
var rot = 0;
var posX = 400;
var posY = 300;
image.onload = function () {
	requestAnimationFrame(function (lastTimer) {
		context.drawImage(image, posX, posY);
		context.globalAlpha = 0.5;
		context.drawImage(image, 430, 430);
		context.save();
		context.globalAlpha = 0.3;
		context.translate(400,300);
		context.rotate(rot);
		rot += 0.01;
		if (rot >= Math.PI*2) {
			rot = 0;
		}
		context.drawImage(image, -64, -128);
		//context.translate(-64,-128);
		//context.translate(464,428);
		context.globalAlpha = 1.0;
		context.restore();
	});
};
image.src = "media/starship.png";
window.addEventListener("touchmove", function (evt) {
	posX = evt.pageX - 64;
	posY = evt.pageY - 128;
});

function sk() {
	showKeyboard();	
}
hideKeyboard();
setTimeout(sk, 2000);
window.addEventListener("keydown", function (evt) {
	console.log("got keydown: keyCode: " + evt.keyCode + ", key: " + evt.key);
});
