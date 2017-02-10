package com.mihir.react.tts;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;


import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.GuardedAsyncTask;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;


import java.util.Iterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


/**
 * Created by mihir on 11/4/15.
 */
public class RCTTextToSpeech extends ReactContextBaseJavaModule{

    private static TextToSpeech tts;

    public RCTTextToSpeech(ReactApplicationContext reactContext) {
        super(reactContext);
        init();
    }
    /***
     * This method will expose all the available languages in TTS engine
     * @param callback
     */
    @ReactMethod
    public void getLocale(final Callback callback) {
        new GuardedAsyncTask<Void, Void>(getReactApplicationContext()) {
            @Override
            protected void doInBackgroundGuarded(Void... params) {
                try{
                    if(tts == null){
                        init();
                    }
                    Locale[] locales = Locale.getAvailableLocales();
                    WritableArray data = Arguments.createArray();
                    for (Locale locale : locales) {
                        int res = tts.isLanguageAvailable(locale);
                        if(res == TextToSpeech.LANG_COUNTRY_AVAILABLE){
                            data.pushString(locale.getLanguage());
                        }
                    }
                    callback.invoke(null,data);
                } catch (Exception e) {
                    callback.invoke(ErrorUtils.getError(null,e.getMessage()),null);
                }
            }
        }.execute();
    }

    /***
     * This method will expose all the available voices in TTS engine
     * @param callback
     */
    @ReactMethod
    public void speechVoices(final Callback callback) {
        new GuardedAsyncTask<Void, Void>(getReactApplicationContext()) {
            @Override
            protected void doInBackgroundGuarded(Void... params) {
                try{
                    if(tts == null){
                        init();
                    }
                    WritableMap speechVoices = Arguments.createMap();
                    Set<Voice> voices = tts.getVoices();
                    if (voices != null) {
                      Iterator<Voice> iterator = voices.iterator();
                      while(iterator.hasNext()) {
                        Voice voice = iterator.next();
                        WritableMap voiceInfo = Arguments.createMap();;
                        voiceInfo.putString("name", voice.getLocale().getDisplayName());
                        voiceInfo.putString("language", voice.getName());
                        if (voice.getQuality() == Voice.QUALITY_NORMAL) {
                          voiceInfo.putString("quality", "default");
                        } else if (voice.getQuality() == Voice.QUALITY_HIGH || voice.getQuality() == Voice.QUALITY_VERY_HIGH) {
                          voiceInfo.putString("quality", "enhanced");
                        } else {
                          voiceInfo.putString("quality", "low");
                        }
                        speechVoices.putMap(voice.getName(), voiceInfo);
                      }
                    }
                    callback.invoke(null,speechVoices);
                } catch (Exception e) {
                    callback.invoke(ErrorUtils.getError(null,e.getMessage()),null);
                }
            }
        }.execute();
    }
    @ReactMethod
    public void isSpeaking(final Callback callback){
        new GuardedAsyncTask<Void,Void>(getReactApplicationContext()){
            @Override
            protected  void doInBackgroundGuarded(Void... params){
                try {
                    if (tts.isSpeaking()) {
                        callback.invoke(null,true);
                    } else {
                        callback.invoke(null,false);
                    }
                } catch (Exception e){
                    callback.invoke(ErrorUtils.getError(null,e.getMessage()),null);
                }
            }
        }.execute();
    }
    public void init(){
        tts = new TextToSpeech(getReactApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.ERROR){
                    FLog.e(ReactConstants.TAG,"Not able to initialized the TTS object");
                }
            }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                WritableMap map = Arguments.createMap();
                map.putString("id", utteranceId);
                getReactApplicationContext().getJSModule(RCTDeviceEventEmitter.class)
                    .emit("FinishSpeechUtterance", map);
            }

            @Override
            public void onError(String utteranceId) {
                WritableMap map = Arguments.createMap();
                map.putString("id", utteranceId);
                getReactApplicationContext().getJSModule(RCTDeviceEventEmitter.class)
                    .emit("ErrorSpeechUtterance", map);
            }

            @Override
            public void onStart(String utteranceId) {
                WritableMap map = Arguments.createMap();
                map.putString("id", utteranceId);
                getReactApplicationContext().getJSModule(RCTDeviceEventEmitter.class)
                    .emit("StartSpeechUtterance", map);
            }
        });
    }

    @ReactMethod
    public void stop(final Callback callback){
        new GuardedAsyncTask<Void,Void>(getReactApplicationContext()){
            @Override
            protected  void doInBackgroundGuarded(Void... params){
                try{
                    tts.stop();
                    callback.invoke(null,true);

                } catch (Exception e){
                    callback.invoke(ErrorUtils.getError(null,e.getMessage()),null);
                }
            }
        }.execute();
    }

    @ReactMethod
    public void shutDown(final Callback callBack){
        new GuardedAsyncTask<Void,Void>(getReactApplicationContext()){
            @Override
            protected  void doInBackgroundGuarded(Void... params){
                if(tts == null) {
                    callBack.invoke(true);
                }
                try{
                    tts.shutdown();
                    callBack.invoke(null,true);
                } catch (Exception e){
                    callBack.invoke(ErrorUtils.getError(null,e.getMessage()),null);
                }
            }
        }.execute();
    }

    @Override
    public String getName() {
        return "AndroidTTS";
    }

    @ReactMethod
    public void speak(final ReadableMap args,final  Callback callback) {
        new GuardedAsyncTask<Void, Void>(getReactApplicationContext()) {
            @Override
            protected void doInBackgroundGuarded(Void... params) {
                if(tts == null){
                    init();
                }
                String voiceIdentifier = args.hasKey("voiceIdentifier") ? args.getString("voiceIdentifier") : null;
                String text = args.hasKey("text") ? args.getString("text") : null;
                double rate = args.hasKey("rate") ? args.getDouble("rate") : 1.0;
                String language = args.hasKey("language") ? args.getString("language") : null;
                Boolean forceStop = args.hasKey("forceStop") ?  args.getBoolean("forceStop") : null;
                Float pitch = args.hasKey("pitch") ? (float)  args.getDouble("pitch") : null;
                if(tts.isSpeaking()){
                    //Force to stop and start new speech
                    if(forceStop != null && forceStop){
                        tts.stop();
                    } else {
                        callback.invoke(ErrorUtils.getError(null,"TTS is already speaking something , Please shutdown or stop  TTS and try again"),null);
                        return;
                    }
                }
                if(args.getString("text") == null || text == ""){
                    callback.invoke(ErrorUtils.getError(null,"t can not be blank"),null);
                    return;
                }
                try {
                  tts.setSpeechRate((float)rate);
                  if (voiceIdentifier != null && voiceIdentifier != "") {
                    Set<Voice> voices = tts.getVoices();
                    if (voices != null) {
                      Iterator<Voice> iterator = tts.getVoices().iterator();
                      while(iterator.hasNext()) {
                        Voice voice = iterator.next();
                        if (voice.getName().equals(voiceIdentifier)) {
                          tts.setVoice(voice);
                        }
                      }
                    }
                  } else if (language != null && language != "") {
                        tts.setLanguage(new Locale(language));
                    } else {
                        //Setting up default language
                        tts.setLanguage(new Locale("en"));
                    }
                    //Set the pitch if provided by the user
                    if(pitch != null){
                        tts.setPitch(pitch);
                    }
                    int speakResult = 0;
                    String utteranceId = UUID.randomUUID().toString();
                    if (Build.VERSION.SDK_INT >= 21) {
                        Bundle bundle = new Bundle();
                        bundle.putCharSequence(Engine.KEY_PARAM_UTTERANCE_ID, "");
                        speakResult = tts.speak(text, TextToSpeech.QUEUE_FLUSH, bundle, utteranceId);
                    } else {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
                        speakResult = tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
                    }

                    if (speakResult < 0) {
                      throw new Exception("Speak failed, make sure that TTS service is installed on you device");
                    }

                    WritableMap resultMap = Arguments.createMap();
                    resultMap.putString("id", utteranceId);
                    callback.invoke(null, resultMap);
                } catch (Exception e) {
                    callback.invoke(ErrorUtils.getError(null,e.getMessage()), null);
                }
            }
        }.execute();
    }
}
