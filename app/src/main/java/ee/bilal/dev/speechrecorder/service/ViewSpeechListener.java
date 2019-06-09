package ee.bilal.dev.speechrecorder.service;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import ee.bilal.dev.speechrecorder.R;
import ee.bilal.dev.speechrecorder.activity.SpeechRecognitionActivity;

public class ViewSpeechListener implements SpeechListener {
    private SpeechRecognitionActivity activity;
    private SpeechRecognitionService service;

    public ViewSpeechListener(SpeechRecognitionActivity activity, SpeechRecognitionService service) {
        this.activity = activity;
        this.service = service;
    }

    @Override
    public void onStarted() {
        activity.updateCaption();
        activity.setStartSwitchText(activity.getResourceString(R.string.stop_switch));
    }

    @Override
    public void onStopped() {
        activity.setCaptionText(activity.getResourceString(R.string.stopped_msg));
        activity.setStartSwitchText(activity.getResourceString(R.string.start_switch));
        activity.setResultText(StringUtils.EMPTY);
    }

    @Override
    public void onPartialResult(String text) {
        activity.setResultText(text);
    }

    @Override
    public void onResult(String text) {
        activity.setResultText(StringUtils.EMPTY);
        activity.makeToast(text);
    }

    @Override
    public void onSpeechBegin() {

    }

    @Override
    public void onSpeechEnd() {
        try {
            service.restartListener();
        } catch (IOException ex) {
            Log.e("OnSpeechEnd", "Error restarting recognition service: " + ex.getMessage());
        }

    }

    @Override
    public void onError(String message) {
        activity.setCaptionText(message);

        service.stop();
    }

    @Override
    public void onTimeout() {
        try {
            service.restartListener();
        } catch (IOException ex) {
            Log.e("OnTimeout", "Error restarting recognition service: " + ex.getMessage());
        }
    }
}
