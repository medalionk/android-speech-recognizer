package ee.bilal.dev.speechrecorder.service;

public interface SpeechRecognitionService {

    void addListener(SpeechListener.Type type, SpeechListener listener);

    void removeListener(SpeechListener.Type type);

    void stop();

    //void updateSpeechModel(String model);

    void start();

    void restartListener();

    enum State {
        IDLE,
        RUNNING,
        STOPPED,
        CANCELED
    }
}
