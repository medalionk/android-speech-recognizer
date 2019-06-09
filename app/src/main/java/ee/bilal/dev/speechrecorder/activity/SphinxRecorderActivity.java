package ee.bilal.dev.speechrecorder.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import ee.bilal.dev.speechrecorder.R;
import ee.bilal.dev.speechrecorder.model.SpeechModel;
import ee.bilal.dev.speechrecorder.model.SphinxSpeechModel;
import ee.bilal.dev.speechrecorder.service.FileWriterSpeechListener;
import ee.bilal.dev.speechrecorder.service.SpeechListener;
import ee.bilal.dev.speechrecorder.service.SpeechRecognitionService;
import ee.bilal.dev.speechrecorder.service.SphinxSpeechRecognitionService;
import ee.bilal.dev.speechrecorder.service.ViewSpeechListener;

import static android.widget.Toast.makeText;

public class SphinxRecorderActivity extends AppCompatActivity implements SpeechRecognitionActivity {

    private static final String TAG = "SphinxActivity";

    private int currentModel = 0;

    private String[] models;

    private SpeechRecognitionService recognitionService;

    private TextView modelNameTextView;
    private TextView captionText;
    private TextView resultText;

    private Switch writeFileSwitch;
    private Switch runBackgroundSwitch;
    private Switch startRecordingSwitch;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphinx_recorder);

        handler = new Handler();

        initViews();

        setupListeners();

        models = SphinxSpeechModel.getNames().toArray(new String[0]);

        if(models == null || models.length == 0) {
            Log.e("OnCreate", "Error: could not get model names!!!");

            return;
        }

        try {
            launchRecognitionService();
        } catch (IOException e) {
            Log.e("OnCreate", "Error: could not get assert dir: " + e.getMessage());

            return;
        }

        updateModelName();

        setupActionBar();
    }

    private void initViews(){
        modelNameTextView = findViewById(R.id.model_name);
        writeFileSwitch = findViewById(R.id.writeFileSwitch);
        runBackgroundSwitch = findViewById(R.id.backgroundRunSwitch);
        startRecordingSwitch = findViewById(R.id.startSwitch);

        captionText = ((TextView) findViewById(R.id.caption_text));
        resultText = ((TextView) findViewById(R.id.result_text));

        captionText.setText(getResources().getString(R.string.idle_msg));
    }

    private void launchRecognitionService() throws IOException {
        Assets assets = new Assets(getApplicationContext());
        File assetDir = assets.syncAssets();
        recognitionService = new SphinxSpeechRecognitionService(assetDir);
    }

    private void setupListeners(){
        startRecordingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startStopRecording(isChecked);
            }
        });

        writeFileSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startStopFileWrite(isChecked);
            }
        });
    }

    private void startStopRecording(boolean start){
        if(start) {
            start();
        } else {
            stop();
        }
    }

    private void startStopFileWrite(boolean start){
        if(start) {
            SpeechListener fileListener = new FileWriterSpeechListener(this, TAG);
            recognitionService.addListener(SpeechListener.Type.FILE, fileListener);
        } else {
            recognitionService.removeListener(SpeechListener.Type.FILE);
        }
    }

    private void start() {
        captionText.setText(R.string.preparing_recognizer);

        SpeechListener viewListener = new ViewSpeechListener(this, recognitionService);
        recognitionService.addListener(SpeechListener.Type.VIEW, viewListener);

        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    recognitionService.start(models[currentModel]);
                } catch (IOException e) {
                    Log.e("Start", e.getMessage());
                }
            }
        });

    }

    private void stop(){
        if(recognitionService != null){
            recognitionService.stop();
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.sphinx_recorder_title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
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

        try {
            recognitionService.updateSpeechModel(models[currentModel]);

            updateModelName();

        } catch (IOException ex) {
            Log.e("UpdateModel", "Error updating speech model: " + ex.getMessage());
        }
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

    @Override
    public void setCaptionText(String text) {
        captionText.setText(text);
    }

    @Override
    public void setResultText(String text) {
        resultText.setText(text);
    }

    @Override
    public void setStartSwitchText(String text) {
        startRecordingSwitch.setText(text);
    }

    @Override
    public void makeToast(String msg) {
        makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getResourceString(int id) {
        return getResources().getString(id);
    }
}
