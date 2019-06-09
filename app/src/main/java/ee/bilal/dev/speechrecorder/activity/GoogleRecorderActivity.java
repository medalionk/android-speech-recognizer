package ee.bilal.dev.speechrecorder.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import ee.bilal.dev.speechrecorder.R;

public class GoogleRecorderActivity extends AppCompatActivity {

    private String[] modelNames;

    private TextView modelNameTextView;

    private Switch writeFileSwitch;
    private Switch runBackgroundSwitch;
    private Switch startRecordingSwitch;

    private String selectedModel;

    private boolean writeFile;
    private boolean backgroundRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_recorder);

        initViews();

        setupListeners();

        modelNames = getResources().getStringArray(R.array.google_language_models);
        selectedModel = modelNames[0];

        updateModelName();

        setupActionBar();
    }

    private void initViews(){
        modelNameTextView = findViewById(R.id.model_name);
        writeFileSwitch = findViewById(R.id.writeFileSwitch);
        runBackgroundSwitch = findViewById(R.id.backgroundRunSwitch);
        startRecordingSwitch = findViewById(R.id.startSwitch);
    }

    private void setupListeners(){
        startRecordingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startStopRecording(isChecked);
            }
        });
    }

    private void startStopRecording(boolean isChecked){

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.google_recorder_title);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(GoogleRecorderActivity.this);
        builder.setTitle(getResources().getString(R.string.language_model_dialog_title));

        builder.setSingleChoiceItems(modelNames, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedModel = modelNames[which];
                updateModelName();
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

    private void updateModelName(){
        modelNameTextView.setText(selectedModel);
    }

}
