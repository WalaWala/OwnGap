loadScript("test2.js");

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