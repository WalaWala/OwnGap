var BUTTON_DPAD_UP = 19;
var BUTTON_DPAD_RIGHT = 22;
var BUTTON_DPAD_DOWN = 20;
var BUTTON_DPAD_LEFT = 21;
var BUTTON_O = 96;
var BUTTON_U = 99;
var BUTTON_Y = 100;
var BUTTON_A = 97;
var BUTTON_R2 = 105;
var BUTTON_MENU = 82;
var BUTTON_R3 = 107;
var BUTTON_L3 = 106;
var BUTTON_L2 = 104;
var BUTTON_R1 = 103;
var BUTTON_L1 = 102;

var AXIS_LS_X = 0;
var AXIS_LS_Y = 1;
var AXIS_RS_X = 2;
var AXIS_RS_Y = 3;
var AXIS_L2 = 4;
var AXIS_R2 = 5;

var ALL_BUTTONS = -1;

var askedForButton = [];
var joyButtonsState_P1 = [];
var joyEventListener = [];
var joyDelay = 5;

function fire(buttonId) {
	if (buttonId !== ALL_BUTTONS) {
		fire(ALL_BUTTONS);
	}
	if (joyEventListener[buttonId]) {
		for (var i = 0; i < joyEventListener[buttonId].length; ++i) {
			joyEventListener[buttonId][i]();
		}
	}
}

function getJoyAxis(playerId, axis) {
	return getAxis(playerId, axis);
}

function getButtons() {
	// sometimes it's faster to repeat oneself. for...in is really really slow in v8
	var player_id = 0;
	joyButtonsState_P1[BUTTON_DPAD_UP] = getButton(player_id, BUTTON_DPAD_UP) === true;
	joyButtonsState_P1[BUTTON_DPAD_DOWN] = getButton(player_id, BUTTON_DPAD_DOWN) === true;
	joyButtonsState_P1[BUTTON_DPAD_LEFT] = getButton(player_id, BUTTON_DPAD_LEFT) === true;
	joyButtonsState_P1[BUTTON_DPAD_RIGHT] = getButton(player_id, BUTTON_DPAD_RIGHT) === true;
	joyButtonsState_P1[BUTTON_O] = getButton(player_id, BUTTON_O) === true;
	joyButtonsState_P1[BUTTON_U] = getButton(player_id, BUTTON_U) === true;
	joyButtonsState_P1[BUTTON_Y] = getButton(player_id, BUTTON_Y) === true;
	joyButtonsState_P1[BUTTON_A] = getButton(player_id, BUTTON_A) === true;
	joyButtonsState_P1[BUTTON_MENU] = getButton(player_id, BUTTON_MENU) === true;
	if (joyButtonsState_P1[BUTTON_DPAD_UP]) {
		fire(BUTTON_DPAD_UP);
	}
	if (joyButtonsState_P1[BUTTON_DPAD_DOWN]) {
		fire(BUTTON_DPAD_DOWN);
	}
	if (joyButtonsState_P1[BUTTON_DPAD_LEFT]) {
		fire(BUTTON_DPAD_LEFT);
	}
	if (joyButtonsState_P1[BUTTON_DPAD_RIGHT]) {
		fire(BUTTON_DPAD_RIGHT);
	}
	if (joyButtonsState_P1[BUTTON_O]) {
		fire(BUTTON_O);
	}
	if (joyButtonsState_P1[BUTTON_U]) {
		fire(BUTTON_U);
	}
	if (joyButtonsState_P1[BUTTON_Y]) {
		fire(BUTTON_Y);
	}
	if (joyButtonsState_P1[BUTTON_A]) {
		fire(BUTTON_A);
	}
	if (joyButtonsState_P1[BUTTON_MENU]) {
		fire(BUTTON_MENU);
	}
}
function getButtonStateOnce(buttonId) {
	if (!joyButtonsState_P1[buttonId]) {
		if (askedForButton.indexOf(buttonId) !== -1) {
			askedForButton.erase(buttonId);
		}
		return false;
	}

	if (askedForButton.indexOf(buttonId) !== -1) {
		return false;
	}

	askedForButton.push(buttonId);
	return true;
}

function addJoystickEventListener(buttonId, fun) {
	if (!joyEventListener[buttonId]) {
		joyEventListener[buttonId] = [];
	}

	joyEventListener[buttonId].push(fun);
}

function removeJoystickEventListener(buttonId, fun) {
	var j = joyEventListener[buttonId].indexOf(fun);
	if (j !== -1) {
		joyEventListener[buttonId].splice(j, 1);
	}
}

if (isOwnGap) {
	setInterval(getButtons, joyDelay);
}
