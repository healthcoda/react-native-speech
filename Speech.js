/**
 * @providesModule TextToSpeech
 * @flow
 */
'use strict';

var React = require('react-native');
var { NativeModules } = React;
var IOSNativeSpeechSynthesizer = NativeModules.SpeechSynthesizer;
var AndroidTTS = require('react-native').NativeModules.AndroidTTS;
/**
 * High-level docs for the SpeechSynthesizer iOS API can be written here.
 */

var TextToSpeech = {
  speak(options) {

    return new Promise(function(resolve, reject) {
      if (AndroidTTS) {
        AndroidTTS.speak(options, (error,result) => {
            if (error) {
              reject(error);
            } else {
              resolve(result);
            }
          });
      } else {
        IOSNativeSpeechSynthesizer.speakUtterance(options, function(error, success) {
          if (error) {
            return reject(error);
          }

          resolve(true);
        });
      }
    });
  },

  stop() {
    if (AndroidTTS) {
      return new Promise((resolve,reject) => {
        AndroidTTS.stop((error,result)=>{
          if (error) {
            reject(error);
          } else {
            resolve(result);
          }
        });
      });
    } else {
      IOSNativeSpeechSynthesizer.stopSpeakingAtBoundary;
    }
  },

  pause() {
    if (AndroidTTS) {
      // not implemented
    } else {
      IOSNativeSpeechSynthesizer.pauseSpeakingAtBoundary;
    }
  },

  resume() {
    if (AndroidTTS) {
      // not implemented
    } else {
      IOSNativeSpeechSynthesizer.continueSpeakingAtBoundary;
    }
  },

  isPaused() {
    return new Promise(function(resolve, reject) {
      IOSNativeSpeechSynthesizer.paused(function(error, paused) {
        if (error) {
          return reject(error);
        }

        if (paused === 1) {
          resolve(true);
        } else {
          resolve(false);
        }
      });
    });
  },

  isSpeaking() {
    return new Promise(function(resolve, reject) {
      if (AndroidTTS) {
        AndroidTTS.isSpeaking((error,result) => {
          if (error) {
            reject(error);
          } else {
            resolve(result);
          }
        });
      } else {
        IOSNativeSpeechSynthesizer.speaking(function(error, speaking) {
          if (error) {
            return reject(error);
          }

          if (speaking === 1) {
            resolve(true);
          } else {
            resolve(false);
          }
        });
      }
    });
  },

  supportedVoices() {
    return new Promise(function(resolve, reject) {
      if (AndroidTTS) {
        AndroidTTS.speechVoices((error, results) =>{
          if (error) {
             reject(error);
          } else {
            resolve(results);
          }
        });
      } else {
        IOSNativeSpeechSynthesizer.speechVoices(function(error, speechVoices) {
          if (error) {
            return reject(error);
          }

          resolve(speechVoices);
        });
      }
    });
  }
};

module.exports = TextToSpeech;
