var XMLHttpRequest = function () {
	var that = {};
	var _method = "GET";
	var _url = "";
	var _async = false;
	var id = -1;
	that.readyState = 0;
	that.responseText = "";
	that.response = "";
	that.status = 0;
	that.responseType = "";

	that.open = function (method, url, async) {
		that.readyState = 1;
		_method = method;
		_url = url;
		_async = async;
	};

	that.setRequestHeader = function () {};

	that.send = function (parameters) {
		var intervalTO = setInterval(function () {
			var answer = getHttpResponse(id);
			if (answer !== "<No Response>") {
				that.readyState = 4;
				if (answer !== "<Error>") {
					that.status = 200;
				} else {
					that.status = 500;
				}
				clearInterval(intervalTO);
				that.responseText = answer;
				that.response = answer;
				that.onreadystatechange();
			}
		}, 10);
		id = makeHttpRequest(_url, _method, parameters);
	};

	that.onreadystatechange = function () {};

	return that;
};