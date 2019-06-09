package ee.bilal.dev.speechrecorder.activity;

import android.support.v7.app.AppCompatActivity;

public interface SpeechRecognitionActivity {

    void updateCaption();

    void setCaptionText(String text);

    void setResultText(String text);

    void setStartSwitchText(String text);

    void makeToast(String msg);

    String getResourceString(int id);

}
