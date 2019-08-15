package ee.bilal.dev.speechrecorder.activity;

import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import ee.bilal.dev.speechrecorder.R;
import ee.bilal.dev.speechrecorder.model.SpeechModel;
import ee.bilal.dev.speechrecorder.model.SphinxSpeechModel;
import ee.bilal.dev.speechrecorder.service.GoogleSpeechRecognitionService;

public class GoogleRecorderActivity extends BaseRecognitionActivity {

    private static final String TAG = "GoogleRecorderActivity";

    private int currentModel = 0;

    private String[] models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_recorder);

        models = SphinxSpeechModel.getNames().toArray(new String[0]);

        if(models == null || models.length == 0) {
            Log.e("OnCreate", "Error: could not get model names!!!");

            return;
        }

        try {
            initRecognitionService();
        } catch (Exception e) {
            Log.e("OnCreate", "Error: could not get assert dir: " + e.getMessage());

            return;
        }

        init(R.string.google_recognition_title);

        updateModelName();
    }

    private void initRecognitionService() throws IOException {
        Assets assets = new Assets(getApplicationContext());
        File assetDir = assets.syncAssets();

        recognitionService = new GoogleSpeechRecognitionService(this);
    }

    private void updateModelName(){
        modelNameTextView.setText(models[currentModel]);
    }

    @Override
    public void updateCaption(){
        String name = models[currentModel];
        SpeechModel model = SphinxSpeechModel.getByName(name);
        if(model != null){
            captionText.setText(model.getCaption());
        }
    }

}
