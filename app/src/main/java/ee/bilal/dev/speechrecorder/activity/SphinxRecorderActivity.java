package ee.bilal.dev.speechrecorder.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import ee.bilal.dev.speechrecorder.R;
import ee.bilal.dev.speechrecorder.model.SpeechModel;
import ee.bilal.dev.speechrecorder.model.SphinxSpeechModel;
import ee.bilal.dev.speechrecorder.service.SphinxSpeechRecognitionService;

public class SphinxRecorderActivity extends BaseRecognitionActivity {

    private static final String TAG = "SphinxRecorderActivity";

    private int currentModel = 0;

    private String[] models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphinx_recorder);

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

        init(R.string.sphinx_recognition_title);

        updateModelName();
    }

    private void initRecognitionService() throws IOException {
        Assets assets = new Assets(getApplicationContext());
        File assetDir = assets.syncAssets();

        recognitionService = new SphinxSpeechRecognitionService(assetDir);
    }

    public void onSearchType(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(SphinxRecorderActivity.this);
        builder.setTitle(getResources().getString(R.string.language_model_dialog_title));

        builder.setSingleChoiceItems(models, currentModel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateModel(which);
            }
        });

        builder.setPositiveButton(getResources().getString(R.string.close_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateModel(int model) {
        currentModel = model;

        updateModelName();
    }

    private void updateModelName(){
        ((SphinxSpeechRecognitionService)recognitionService).setSpeechModel(models[currentModel]);

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
