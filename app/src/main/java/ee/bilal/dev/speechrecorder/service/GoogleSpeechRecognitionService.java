package ee.bilal.dev.speechrecorder.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

//import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import ee.bilal.dev.speechrecorder.model.SpeechModel;
import ee.bilal.dev.speechrecorder.model.SphinxSpeechModel;

public class GoogleSpeechRecognitionService implements SpeechRecognitionService,
        RecognitionListener {

    private static final String TAG = "SphinxSpeechRecognition";

    private static final String ACOUSTIC_MODEL = "en-us-ptm";
    private static final String DICTIONARY = "cmudict-en-us.dict";

    private static final int LISTENING_TIMEOUT = 10000;

    private final Map<SpeechListener.Type, SpeechListener> listeners;
    private final AppCompatActivity context;
    //private final File assetDir;

    private State state = State.IDLE;

    private SpeechRecognizer recognizer;
    private SpeechModel speechModel;
    private Intent intent;

    /*public GoogleSpeechRecognitionService(File assetDir) {
        this.listeners = new HashMap<>();
        this.assetDir = assetDir;
    }*/

    public GoogleSpeechRecognitionService(AppCompatActivity context) {
        this.listeners = new HashMap<>();
        this.context = context;
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {
        for (SpeechListener listener : listeners.values()) {
            listener.onSpeechBegin();
        }

        Log.d("OnBeginningOfSpeech", "Speech Began");
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d("OnEndOfSpeech", "Speech Ended");

        for (SpeechListener listener : listeners.values()) {
            listener.onSpeechEnd();
        }
    }

    @Override
    public void onError(int i) {
        Log.e("OnError", "A network or recognition error occurred: " + i);

        handleErrorMessage(i);
//        if(i = )
//        String error = getErrorMessage(i);
//
//        for (SpeechListener listener : listeners.values()) {
//            listener.onError(error + ": " + i);
//        }
    }

    private void notifyOnError(String error) {
        for (SpeechListener listener : listeners.values()) {
            listener.onError(error);
        }
    }

    private void handleErrorMessage(int error) {
        String message;
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio error";
                //notifyOnError(message);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client error";
                //notifyOnError(message);
                return;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                //notifyOnError(message);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                //notifyOnError(message);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Recognizer busy";
                return;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
            case SpeechRecognizer.ERROR_NO_MATCH:
            case SpeechRecognizer.ERROR_SERVER:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                stop();
                start();
                return;
            default:
                message = "Speech Recognizer cannot understand you";
                break;
        }

        notifyOnError(message);
        //return message;

        //recognizer.stopListening();
        //recognizer.startListening(intent);
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> words = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        StringBuilder text = new StringBuilder();

        for (String word : words) {
            text.append(word).append(" ");
        }

        Log.d("OnResult", "Recognition result: " + text);

        for (SpeechListener listener : listeners.values()) {
            listener.onResult(text.toString());
        }

    }

    @Override
    public void onPartialResults(Bundle bundle) {
        ArrayList<String> words = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        StringBuilder text = new StringBuilder();

        for (String word : words) {
            text.append(word).append(" ");
        }

        Log.d("OnPartialResult", "Partial recognition result: " + text);

        for (SpeechListener listener : listeners.values()) {
            listener.onPartialResult(text.toString());
        }
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    /*@Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null){
            return;
        }

        String text = hypothesis.getHypstr();

        for (SpeechListener listener : listeners.values()) {
            listener.onPartialResult(text);
        }

        Log.d("OnPartialResult", text);
    }*/

    /*@Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null){
            return;
        }

        String text = hypothesis.getHypstr();

        for (SpeechListener listener : listeners.values()) {
            listener.onResult(text);
        }

        Log.d("OnResult", "Recognition result: " + text);
    }*/

    /*@Override
    public void onError(Exception e) {
        for (SpeechListener listener : listeners.values()) {
            listener.onError(e.getMessage());
        }

        Log.e("OnError", e.getMessage());
    }*/

    /*@Override
    public void onTimeout() {
        for (SpeechListener listener : listeners.values()) {
            listener.onTimeout();
        }

        Log.d("OnTimeout", "Recognition timeout");
    }*/

    @Override
    public void addListener(SpeechListener.Type type, SpeechListener listener) {
        listeners.put(type, listener);
    }

    @Override
    public void removeListener(SpeechListener.Type type) {
        listeners.remove(type);
    }

    @Override
    public void stop() {
        Log.d("Stop", "Stop Google speech recognition");

        if (recognizer != null) {
            recognizer.stopListening();
            recognizer.destroy();
        }

        for (SpeechListener listener : listeners.values()) {
            listener.onStopped();
        }

        //listeners.clear();

        state = State.IDLE;


    }

    /*@Override
    public void updateSpeechModel(String name) throws IOException {
        throw new NotImplementedException("UpdateSpeechModel not implemented here");
        *//*speechModel = SphinxSpeechModel.getByName(name);
        if(speechModel == null){
            Log.e("UpdateSpeechModel", "Invalid Speech model name: " + name);

            return;
        }

        startListener();

        Log.d("UpdateSpeechModel", "Speech model updated to name: " + name);*//*
    }*/

    @Override
    public void start()  {
        Log.d("Start", "Start recognition");

        startListener();
    }

    @Override
    public void restartListener() {
        recognizer.stopListening();
        stop();
        //recognizer.

        startListener();
    }

    private void startListener() {
        Log.d("StartListener", "Start listening ");

        if (isStopped()) {
           setupRecognizer();
        }

        //setupRecognizer();

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
//        intent.putExtra(RecognizerIntent.EXTRA_WEB_SEARCH_ONLY, "false");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, "10000");

        //recognizer.stopListening();
        recognizer.startListening(intent);

        state = State.RUNNING;

        for (SpeechListener listener : listeners.values()) {
            listener.onStarted();;
        }
    }

    private boolean isStopped() {
        return state.equals(State.IDLE) || state.equals(State.CANCELED) || state.equals(State.STOPPED);
    }

    private void setupRecognizer() {

        Log.d("SetupRecognizer", "Setup speech recognizer.");

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(this);
    }

}
