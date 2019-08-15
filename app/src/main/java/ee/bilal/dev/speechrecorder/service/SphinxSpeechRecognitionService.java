package ee.bilal.dev.speechrecorder.service;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import ee.bilal.dev.speechrecorder.model.SpeechModel;
import ee.bilal.dev.speechrecorder.model.SphinxSpeechModel;

public class SphinxSpeechRecognitionService implements SpeechRecognitionService,
        RecognitionListener {

    private static final String TAG = "SphinxSpeechRecognition";

    private static final String ACOUSTIC_MODEL = "en-us-ptm";
    private static final String DICTIONARY = "cmudict-en-us.dict";

    private static final int LISTENING_TIMEOUT = 10000;

    private final Map<SpeechListener.Type, SpeechListener> listeners;
    private final File assetDir;

    private State state = State.IDLE;

    private SpeechRecognizer recognizer;
    private SpeechModel speechModel;

    public SphinxSpeechRecognitionService(File assetDir) {
        this.listeners = new HashMap<>();
        this.assetDir = assetDir;
    }

    @Override
    public void onBeginningOfSpeech() {
        for (SpeechListener listener : listeners.values()) {
            listener.onSpeechBegin();
        }

        Log.d("OnBeginningOfSpeech", "Speech Began");
    }

    @Override
    public void onEndOfSpeech() {
        for (SpeechListener listener : listeners.values()) {
            listener.onSpeechEnd();
        }

        Log.d("OnEndOfSpeech", "Speech Ended");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null){
            return;
        }

        String text = hypothesis.getHypstr();

        for (SpeechListener listener : listeners.values()) {
            listener.onPartialResult(text);
        }

        Log.d("OnPartialResult", text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null){
            return;
        }

        String text = hypothesis.getHypstr();

        for (SpeechListener listener : listeners.values()) {
            listener.onResult(text);
        }

        Log.d("OnResult", "Recognition result: " + text);
    }

    @Override
    public void onError(Exception e) {
        for (SpeechListener listener : listeners.values()) {
            listener.onError(e.getMessage());
        }

        Log.e("OnError", e.getMessage());
    }

    @Override
    public void onTimeout() {
        for (SpeechListener listener : listeners.values()) {
            listener.onTimeout();
        }

        Log.d("OnTimeout", "Recognition timeout");
    }

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
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }

        for (SpeechListener listener : listeners.values()) {
            listener.onStopped();
        }

        listeners.clear();

        state = State.IDLE;

        Log.d("Stop", "Sphinx service canceled and shutdown");
    }

    public void setSpeechModel(String name) {
        Log.d("UpdateSpeechModel", "Set speech model to: " + name);

        speechModel = SphinxSpeechModel.getByName(name);
        if(speechModel == null){
            Log.e("UpdateSpeechModel", "Invalid Speech model name: " + name);
        }
    }

    //@Override
   /* public void updateSpeechModel(String name) {
        speechModel = SphinxSpeechModel.getByName(name);
        if(speechModel == null){
            Log.e("UpdateSpeechModel", "Invalid Speech model name: " + name);

            return;
        }

        startListener();

        Log.d("UpdateSpeechModel", "Speech model updated to name: " + name);
    }*/

    @Override
    public void start() {
        Log.d("Start", "Start recognition with speech model: " + speechModel);

        startListener();;
    }

    @Override
    public void restartListener() {
        startListener();
    }

    private void startListener() {
        Log.d("StartListener", "Start listening");

        try {
            if (isStopped()) {
                setupRecognizer(assetDir);
            }

            recognizer.stop();
            recognizer.startListening(speechModel.getName(), LISTENING_TIMEOUT);

            state = State.RUNNING;

            for (SpeechListener listener : listeners.values()) {
                listener.onStarted();;
            }
        } catch (IOException ex) {
            Log.d("StartListener", "Error starting speech recognition");
        }

    }

    private boolean isStopped() {
        return state.equals(State.IDLE) || state.equals(State.CANCELED) || state.equals(State.STOPPED);
    }

    private void setupRecognizer(File assetsDir) throws IOException {

        Log.d("SetupRecognizer", "Setup recognition with assert dir: " + assetsDir);

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, ACOUSTIC_MODEL))
                .setDictionary(new File(assetsDir, DICTIONARY))
                .getRecognizer();

        recognizer.addListener(this);

        for (SpeechModel model : SphinxSpeechModel.MODELS) {
            File menuGrammar = new File(assetsDir, model.getGrammarFile());
            recognizer.addGrammarSearch(model.getName(), menuGrammar);
        }
    }

}
