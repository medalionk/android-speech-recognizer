package ee.bilal.dev.speechrecorder.service;

public interface SpeechListener {

    void onStarted();

    void onStopped();

    void onPartialResult(String text);

    void onResult(String text);

    void onSpeechBegin();

    void onSpeechEnd();

    void onError(String message);

    void onTimeout();

    enum Type {
        FILE,
        VIEW
    }
}
