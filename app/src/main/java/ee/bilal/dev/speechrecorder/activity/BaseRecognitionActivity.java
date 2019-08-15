package ee.bilal.dev.speechrecorder.activity;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import ee.bilal.dev.speechrecorder.R;
import ee.bilal.dev.speechrecorder.service.FileWriterSpeechListener;
import ee.bilal.dev.speechrecorder.service.SpeechListener;
import ee.bilal.dev.speechrecorder.service.SpeechRecognitionService;
import ee.bilal.dev.speechrecorder.service.ViewSpeechListener;

import static android.widget.Toast.makeText;

public abstract class BaseRecognitionActivity extends AppCompatActivity implements SpeechRecognitionActivity {

    private static final String TAG = "BaseRecognitionActivity";

    protected SpeechRecognitionService recognitionService;

    protected TextView modelNameTextView;
    protected TextView captionText;
    protected TextView resultText;

    protected Switch writeFileSwitch;
    protected Switch runBackgroundSwitch;
    protected Switch startRecordingSwitch;

    protected Handler handler;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
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

    protected void init(int title) {
        handler = new Handler();

        initViews();

        setupListeners();

        setupActionBar(title);
    }

    protected void initViews(){
        modelNameTextView = findViewById(R.id.model_name);
        writeFileSwitch = findViewById(R.id.writeFileSwitch);
        runBackgroundSwitch = findViewById(R.id.backgroundRunSwitch);
        startRecordingSwitch = findViewById(R.id.startSwitch);

        captionText = ((TextView) findViewById(R.id.caption_text));
        resultText = ((TextView) findViewById(R.id.result_text));

        captionText.setText(getResources().getString(R.string.idle_msg));
    }

    protected void setupListeners(){
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

    protected void setupActionBar(int title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }
    }

    protected void startStopRecording(boolean start){
        if(start) {
            start();
        } else {
            stop();
        }
    }

    protected void startStopFileWrite(boolean start){
        if(start) {
            SpeechListener fileListener = new FileWriterSpeechListener(this, TAG);
            recognitionService.addListener(SpeechListener.Type.FILE, fileListener);
        } else {
            recognitionService.removeListener(SpeechListener.Type.FILE);
        }
    }

    protected void start() {
        captionText.setText(R.string.preparing_recognizer);

        SpeechListener viewListener = new ViewSpeechListener(this, recognitionService);
        recognitionService.addListener(SpeechListener.Type.VIEW, viewListener);

        handler.post(new Runnable() {
            @Override
            public void run() {
                recognitionService.start();
            }
        });

    }

    protected void stop(){
        if(recognitionService != null){
            recognitionService.stop();
        }
    }
}
