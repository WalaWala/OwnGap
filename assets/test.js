loadScript("test2.js");
function runAjax(url, JSONstring, callback) {
	if (XMLHttpRequest) {
		req = new XMLHttpRequest();
		var params = "data=" + encodeURIComponent(JSONstring)

		req.open("POST", "http://www.walawala.org:8087/" + url, true);
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
var context = canvas.getContext("2d");
var image = new Image();
image.onload = function () {
	setInterval(function () {
		context.drawImage(image, 100, 100);
	}, 1000/60);
};
image.src = "heart-full.png";