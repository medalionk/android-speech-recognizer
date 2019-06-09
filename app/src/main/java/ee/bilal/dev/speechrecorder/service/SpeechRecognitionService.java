package ee.bilal.dev.speechrecorder.service;

import java.io.IOException;

public interface SpeechRecognitionService {

    void addListener(SpeechListener.Type type, SpeechListener listener);

    void removeListener(SpeechListener.Type type);

    void stop();

    void updateSpeechModel(String model) throws IOException;

    void start(String initialModel) throws IOException;

    void restartListener() throws IOException;

    enum State {
        IDLE,
        RUNNING,
        STOPPED,
        CANCELED
    }
}
