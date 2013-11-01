#include "owngap.h"

// Extracts a C string from a V8 Utf8Value.
const char* ToCString(const String::Utf8Value& value) {
  return *value ? *value : "<string conversion failed>";
}

// abu: copied shamelessly from shell.cc
void ReportException(Isolate* isolate, TryCatch* try_catch) {
	HandleScope handle_scope(isolate);
	Unlocker unlocker(isolate);
	String::Utf8Value exception(try_catch->Exception());
	const char* exception_string = ToCString(exception);
	Handle<Message> message = try_catch->Message();
	if (message.IsEmpty()) {
		// V8 didn't provide any extra information about this error; just
		// print the exception.
		// TODO: this never worked... whatever...
		//LOGD("%s\n", exception_string);
	} else {
		// Print (filename):(line number): (message).
		String::Utf8Value filename(message->GetScriptResourceName());
		const char* filename_string = ToCString(filename);
		int linenum = message->GetLineNumber();
		LOGD("%s:%i: %s\n", filename_string, linenum, exception_string);
		// Print line of source code.
		String::Utf8Value sourceline(message->GetSourceLine());
		const char* sourceline_string = ToCString(sourceline);
		LOGD("%s\n", sourceline_string);
		// Print wavy underline (GetUnderline is deprecated).
		int start = message->GetStartColumn();
		for (int i = 0; i < start; i++) {
			LOGD(" ");
		}
		int end = message->GetEndColumn();
		for (int i = start; i < end; i++) {
			LOGD("^");
		}
		String::Utf8Value stack_trace(try_catch->StackTrace());
		if (stack_trace.length() > 0) {
			const char* stack_trace_string = ToCString(stack_trace);
			LOGD("%s\n", stack_trace_string);
		}
	}
}

void LoadFile(const char* fileName) {
	AAsset* asset = AAssetManager_open(assetmanager, fileName, AASSET_MODE_UNKNOWN);
	if (NULL == asset) {
		LOGD("asset : is NULL");
		return;
	}
	off_t size = AAsset_getLength(asset);

	char* buf = (char*) malloc(size+1);
	if (NULL == buf) {
		LOGD("asset : is NULL");
		AAsset_close(asset);
		return;
	}

	int bytesread = AAsset_read(asset, (void*)buf, size);
	buf[size] = '\0';

	AAsset_close(asset);

	HandleScope handle_scope(isolate);
	Handle<String> source = String::New(buf);

	TryCatch try_catch;

	// Compile the source code.
	Handle<Script> script = Script::Compile(source);

	free(buf);

	// Run the script to get the result.
	Handle<Value> result = script->Run();

	ReportException(isolate, &try_catch);

	// Convert the result to an ASCII string and print it.
	String::AsciiValue ascii(result);
}

char *getChar(Local<Value> value, const char *fallback = "") {
    if (value->IsString()) {
        String::AsciiValue string(value);
        char *str = (char *) malloc(string.length() + 1);
        strcpy(str, *string);
        return str;
    }
    char *str = (char *) malloc(strlen(fallback) + 1);
    strcpy(str, fallback);
    return str;
}

static void SetOrtho(const v8::FunctionCallbackInfo<v8::Value>& args) {
	//Locker locker(isolate);
	HandleScope handle_scope(isolate);
	Local<Int32> width = args[0]->ToInt32();
	Local<Int32> height= args[1]->ToInt32();
	JNIEnv* env;
	vm->AttachCurrentThread(&env, NULL);
	env->CallVoidMethod(jniObj, setOrthoJava, width->Value(), height->Value());
}

int LoadImage(const char* path)
{
	LOGD("Loading image from native: %s", path);
	bool success = false;
    Canvas *theCanvas = Canvas::GetCanvas();
    if (theCanvas) {
		AAsset* asset = AAssetManager_open(assetmanager, path, AASSET_MODE_UNKNOWN);
		if (asset == NULL) return -1;

		long size = AAsset_getLength(asset);
		unsigned char* buffer = (unsigned char*) malloc (size);
		if (buffer == NULL) return -1;

		AAsset_read (asset, buffer, size);
		AAsset_close(asset);

		unsigned int width;
		unsigned int height;
        success = theCanvas->AddPngTexture(buffer, size, currentTextureId, &width, &height);
		free (buffer);
    }
	LOGD("ImageId: %d", currentTextureId);
    //free((char*)path);
    // todo
    currentTextureId++;
	return currentTextureId-1;
}

static void IsImageLoaded(const v8::FunctionCallbackInfo<v8::Value>& args)
{
	//Locker locker(isolate);
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	Unlocker unlocker(isolate);
	vm->AttachCurrentThread(&env, NULL);
	int width = env->CallIntMethod(jniObj, getImageWidthJava, args[0]->ToInt32()->Value());
	//LOGD("IsImageLoaded: %d image: %d", width, args[0]->ToInt32()->Value());
	args.GetReturnValue().Set(Boolean::New(width != -1));
}

static void AddPngTexture(const v8::FunctionCallbackInfo<v8::Value>& args)
{
	//Locker locker(isolate);
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	Locker lock(isolate);
	char* path = getChar(args[0]);
	vm->AttachCurrentThread(&env, NULL);
	jstring stringPath = env->NewStringUTF(path);
	int imgId = env->CallIntMethod(jniObj, loadImageJava, stringPath);
	//LoadImage(path);
	//LOGD("Answer: %i", otherTextureCount);
	//free(path);

	args.GetReturnValue().Set(Integer::New(imgId));
}

static void GetTextureWidth(const v8::FunctionCallbackInfo<v8::Value>& args) {
	//Locker locker(isolate);
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	Locker lock(isolate);

	Local<Integer> number = args[0]->ToInteger();
	vm->AttachCurrentThread(&env, NULL);
	int width = env->CallIntMethod(jniObj, getImageWidthJava, number->ToInt32()->Value());

	args.GetReturnValue().Set(Integer::New(width));
}

static void GetTextureHeight(const v8::FunctionCallbackInfo<v8::Value>& args) {
	//Locker locker(isolate);
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	Locker lock(isolate);

	Local<Integer> number = args[0]->ToInteger();
	vm->AttachCurrentThread(&env, NULL);
	int height = env->CallIntMethod(jniObj, getImageHeightJava, number->ToInt32()->Value());

	args.GetReturnValue().Set(Integer::New(height));
}

static void Render(const v8::FunctionCallbackInfo<v8::Value>& args) {
	//Locker locker(isolate);
	HandleScope handle_scope(isolate);
	if (args.Length() == 0) {
		return;
	}

    Canvas *theCanvas = Canvas::GetCanvas();
    if (theCanvas) {
    	String::Utf8Value arg0(args[0]);
    	if (arg0.length() > 0) {
    		theCanvas->Render(*arg0, arg0.length());
    	}

	}
}

static void Load(const v8::FunctionCallbackInfo<v8::Value>& args) {
	for (int i = 0; i < args.Length(); i++) {
		//Locker locker(isolate);
		HandleScope handle_scope(isolate);
		LoadFile(getChar(args[i]));
	}
}

uint currentTimeInMilliseconds()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return ((tv.tv_sec * 1000) + (tv.tv_usec / 1000));
}

static void GetTimestamp(const v8::FunctionCallbackInfo<v8::Value>& args) {
	//Locker locker(isolate);
	HandleScope handle_scope(isolate);
	Local<Integer> lval = Integer::NewFromUnsigned(currentTimeInMilliseconds());
	args.GetReturnValue().Set(lval);
}

static void Print(const v8::FunctionCallbackInfo<v8::Value>& args) {
	//Locker locker(isolate);
	HandleScope handle_scope(isolate);
	String::Utf8Value arg0(args[0]);
	LOGD("%s", *arg0);
}

static void SetBackgroundColor(const v8::FunctionCallbackInfo<v8::Value>& args) {
	HandleScope handle_scope(isolate);
	Canvas *theCanvas = Canvas::GetCanvas();
    if (theCanvas) {
    	theCanvas->SetBackgroundColor(args[0]->ToNumber()->Value(), args[1]->ToNumber()->Value(), args[2]->ToNumber()->Value());
    }
}

static void CallIdle(const FunctionCallbackInfo<Value>& args) {
	HandleScope handle_scope(isolate);
    while (!V8::IdleNotification(1000)) {}
}

static void GetButton(const FunctionCallbackInfo<Value>& args) {
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	vm->AttachCurrentThread(&env, NULL);
	bool btnState = env->CallBooleanMethod(jniObj, getButtonStateJava, args[0]->ToInt32()->Value(), args[1]->ToInt32()->Value());

	args.GetReturnValue().Set(Boolean::New(btnState));
}

static void GetAxis(const FunctionCallbackInfo<Value>& args) {
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	vm->AttachCurrentThread(&env, NULL);
	float axisState = env->CallFloatMethod(jniObj, getAxisStateJava, args[0]->ToInt32()->Value(), args[1]->ToInt32()->Value());

	args.GetReturnValue().Set(Number::New(axisState));
}

static void ShowCursor(const FunctionCallbackInfo<Value>& args) {
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	vm->AttachCurrentThread(&env, NULL);
	env->CallVoidMethod(jniObj, showCursorJava, args[0]->ToBoolean()->Value());
}

void DispatchDebugMessages() {
	Debug::ProcessDebugMessages();
}

void EventLoop() {
	//Locker locker(isolate);
	JNIEnv* env;
	vm->AttachCurrentThread(&env, NULL);
    //locker.StartPreemption(isolate, 10);
	//LOGD("Started preemption!");
	while (true) {
		HandleScope handle_scope(isolate);
		Local<Context> context_local = Local<Context>::New(isolate, context);
		Local<Function> tick = Local<Function>::New(isolate, callbackFunction);
		Local<Integer> lval = Integer::NewFromUnsigned(currentTimeInMilliseconds());
		Local<Value> args[] = { lval };
		TryCatch try_catch;
		Local<Value> result = tick->Call(context_local->Global(), 1, args);
		ReportException(isolate, &try_catch);
    }
}

static void PlaySound(const FunctionCallbackInfo<Value>& args) {
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	vm->AttachCurrentThread(&env, NULL);
	int streamId = env->CallIntMethod(jniObj, playSoundJava, args[0]->ToInt32()->Value(), args[1]->ToBoolean()->Value());

	args.GetReturnValue().Set(Number::New(streamId));
}

static void GetSoundLoaded(const FunctionCallbackInfo<Value>& args) {
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	vm->AttachCurrentThread(&env, NULL);
	LOGD("Getting sound loaded! (%d)", args[0]->ToInt32()->Value());
	bool loaded = env->CallBooleanMethod(jniObj, getSoundLoadedJava, args[0]->ToInt32()->Value());

	args.GetReturnValue().Set(Boolean::New(loaded));
}

static void SetVolume(const FunctionCallbackInfo<Value>& args) {
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	vm->AttachCurrentThread(&env, NULL);
	env->CallVoidMethod(jniObj, setVolumeJava, args[0]->ToInt32()->Value(), (float)args[1]->ToNumber()->Value());
}

static void StopSound(const FunctionCallbackInfo<Value>& args) {
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	vm->AttachCurrentThread(&env, NULL);
	env->CallVoidMethod(jniObj, stopSoundJava, args[0]->ToInt32()->Value());
}

static void LoadSound(const FunctionCallbackInfo<Value>& args) {
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	vm->AttachCurrentThread(&env, NULL);
	String::Utf8Value arg0(args[0]);
	jstring jstrBuf = env->NewStringUTF(*arg0);
	int id = env->CallIntMethod(jniObj, loadSoundJava, jstrBuf);

	args.GetReturnValue().Set(Number::New(id));
}

static void IsPaused(const FunctionCallbackInfo<Value>& args) {
	JNIEnv* env;
	HandleScope handle_scope(isolate);
	vm->AttachCurrentThread(&env, NULL);
	bool paused = env->CallBooleanMethod(jniObj, isPausedJava);

	args.GetReturnValue().Set(Boolean::New(paused));
}

static void RegisterTick(const FunctionCallbackInfo<v8::Value>&args) {
	HandleScope handle_scope(args.GetIsolate());
	//LOGD("Registering tick!");
	Local<String> tick_name = String::New("tick");
  	Local<Context> context_local = Local<Context>::New(isolate, context);
   	Handle<Value> tick_val = context_local->Global()->Get(tick_name);
   	if (!tick_val->IsFunction()) {
   		LOGD("tick is not a function!");
   		return;
   	}
   	Handle<Function> tick_fun = Handle<Function>::Cast(tick_val);
   	callbackFunction.Reset(isolate, tick_fun);
}

Handle<Context> CreateContext(Isolate* isolate) {
	// Create a template for the global object.
	Handle<ObjectTemplate> global = ObjectTemplate::New();
	global->Set(String::New("log"), FunctionTemplate::New(Print));
	global->Set(String::New("registerTick"), FunctionTemplate::New(RegisterTick));
	global->Set(String::New("getTimestamp"), FunctionTemplate::New(GetTimestamp));
	global->Set(String::New("setOrtho"), FunctionTemplate::New(SetOrtho));
	global->Set(String::New("addPngTexture"), FunctionTemplate::New(AddPngTexture));
	global->Set(String::New("getTextureHeight"), FunctionTemplate::New(GetTextureHeight));
	global->Set(String::New("getTextureWidth"), FunctionTemplate::New(GetTextureWidth));
	global->Set(String::New("render"), FunctionTemplate::New(Render));
	global->Set(String::New("setBackgroundColor"), FunctionTemplate::New(SetBackgroundColor));
	global->Set(String::New("load"), FunctionTemplate::New(Load));
	global->Set(String::New("isImageLoaded"), FunctionTemplate::New(IsImageLoaded));
	global->Set(String::New("callIdle"), FunctionTemplate::New(CallIdle));
	global->Set(String::New("getButton"), FunctionTemplate::New(GetButton));
	global->Set(String::New("getAxis"), FunctionTemplate::New(GetAxis));
	global->Set(String::New("showCursor"), FunctionTemplate::New(ShowCursor));
	global->Set(String::New("isPaused"), FunctionTemplate::New(IsPaused));

	global->Set(String::New("playSound"), FunctionTemplate::New(PlaySound));
	global->Set(String::New("loadSound"), FunctionTemplate::New(LoadSound));
	global->Set(String::New("stopSound"), FunctionTemplate::New(StopSound));
	global->Set(String::New("setVolume"), FunctionTemplate::New(SetVolume));
	global->Set(String::New("getSoundLoaded"), FunctionTemplate::New(GetSoundLoaded));

	return Context::New(isolate, NULL, global);
}

extern "C" void Java_org_walawala_OwnGap_OwnGapActivity_render(JNIEnv *env, jobject jobj) {
    Canvas *theCanvas = Canvas::GetCanvas();
    if (theCanvas) {
		theCanvas->RenderInt();
    }
}

extern "C" void Java_org_walawala_OwnGap_OwnGapActivity_enterEventLoop(JNIEnv *env, jobject jobj) {
    EventLoop();
}

extern "C" void Java_org_walawala_OwnGap_OwnGapActivity_setOrtho(JNIEnv *env, jobject jobj, jint width, jint height) {
    Canvas *theCanvas = Canvas::GetCanvas();
    if (theCanvas) {
    	LOGD("Setting ortho: %i/%i", width, height);
	    theCanvas->SetOrtho(width, height);
    }
}

extern "C" void Java_org_walawala_OwnGap_OwnGapActivity_surfaceChanged(JNIEnv *env, jobject jobj, jint width, jint height) {
    Canvas *theCanvas = Canvas::GetCanvas();
    if (theCanvas) {
		theCanvas->OnSurfaceChanged(width, height);
    }
}

extern "C" void Java_org_walawala_OwnGap_OwnGapActivity_initCanvas(JNIEnv *env, jobject jobj) {
    Canvas *theCanvas = Canvas::GetCanvas();
    if (theCanvas) {

    }
}

extern "C" void Java_org_walawala_OwnGap_OwnGapActivity_contextLost(JNIEnv *env, jobject jobj) {
    Canvas *theCanvas = Canvas::GetCanvas();
    if (theCanvas) {
		theCanvas->ContextLost();
    }
}

extern "C" void Java_org_walawala_OwnGap_OwnGapActivity_release(JNIEnv *env, jobject jobj) {
    Canvas *theCanvas = Canvas::GetCanvas();
    if (theCanvas) {
		theCanvas->Release();
    }
}

extern "C" int Java_org_walawala_OwnGap_OwnGapActivity_loadImage(JNIEnv *env, jobject jobj, jstring fileName) {
	const char* relativepath;
	relativepath = env->GetStringUTFChars(fileName, NULL);
	int imgId = LoadImage(relativepath);
	env->ReleaseStringUTFChars(fileName, relativepath);

	return imgId;
}

extern "C" void Java_org_walawala_OwnGap_OwnGapActivity_DispatchDebugMessages(JNIEnv *env, jobject jobj) {
	DispatchDebugMessages();
}

extern "C" void Java_org_walawala_OwnGap_OwnGapActivity_SetJavaFunctions(JNIEnv *env, jobject jobj, jobject activity) {
	env->GetJavaVM(&vm);
	jniObj = env->NewGlobalRef(activity);

	mainActivityClass = (jclass)env->NewGlobalRef(env->GetObjectClass(activity));

    isPausedJava = env->GetMethodID(mainActivityClass, "IsPaused", "()Z");
	if (isPausedJava == NULL)
    	return;

    loadImageJava = env->GetMethodID(mainActivityClass, "LoadImage", "(Ljava/lang/String;)I");
	if (loadImageJava == NULL)
    	return;
    getImageWidthJava = env->GetMethodID(mainActivityClass, "GetImageWidth", "(I)I");
	if (getImageWidthJava == NULL)
    	return;
    getImageHeightJava = env->GetMethodID(mainActivityClass, "GetImageHeight", "(I)I");
	if (getImageHeightJava == NULL)
    	return;

    getButtonStateJava = env->GetMethodID(mainActivityClass, "GetButtonState", "(II)Z");
	if (getButtonStateJava == NULL)
    	return;
    getAxisStateJava = env->GetMethodID(mainActivityClass, "GetAxisState", "(II)F");
	if (getAxisStateJava == NULL)
    	return;
    showCursorJava = env->GetMethodID(mainActivityClass, "ShowCursor", "(Z)V");
 	if (showCursorJava == NULL)
     	return;

    playSoundJava = env->GetMethodID(mainActivityClass, "PlaySound", "(IZ)I");
	if (playSoundJava == NULL)
    	return;
    stopSoundJava = env->GetMethodID(mainActivityClass, "StopSound", "(I)V");
	if (stopSoundJava == NULL)
    	return;
    loadSoundJava = env->GetMethodID(mainActivityClass, "LoadSound", "(Ljava/lang/String;)I");
	if (loadSoundJava == NULL)
    	return;
    setVolumeJava = env->GetMethodID(mainActivityClass, "SetVolume", "(IF)V");
	if (setVolumeJava == NULL)
    	return;
    getSoundLoadedJava = env->GetMethodID(mainActivityClass, "GetSoundLoaded", "(I)Z");
	if (getSoundLoadedJava == NULL)
    	return;

    setOrthoJava = env->GetMethodID(mainActivityClass, "SetOrtho", "(II)V");
	if (setOrthoJava == NULL)
    	return;

    //LOGD("functions found? probably....");
}

extern "C" bool Java_org_walawala_OwnGap_OwnGapActivity_Init(JNIEnv *env, jobject jobj, jobject java_assetmanager, jstring fileName) {
	jniEnv = env;

	v8::V8::Initialize();
	isolate = v8::Isolate::GetCurrent();
	v8::HandleScope handle_scope(isolate);
	Handle<Context> context_handle = CreateContext(isolate);
	context_handle->Enter();
	context.Reset(isolate, context_handle);

	//Debug::SetDebugMessageDispatchHandler(DispatchDebugMessages, false);
	//Debug::EnableAgent("OwnGap", 5858, false);

	const char* relativepath;
	relativepath = env->GetStringUTFChars(fileName, NULL);
	assetmanager = AAssetManager_fromJava(env, java_assetmanager);
	if (NULL == assetmanager) {
		LOGD("assetmanager : is NULL");
		return false;
	}

 	LoadFile(relativepath);

	env->ReleaseStringUTFChars(fileName, relativepath);

   return true;
}