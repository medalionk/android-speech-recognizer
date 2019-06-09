package ee.bilal.dev.speechrecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity implements
        RecognitionListener {

    private static final String ACOUSTIC_MODEL = "en-us-ptm";
    private static final String DICTIONARY = "cmudict-en-us.dict";

    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String PHONE_SEARCH = "phones";
    private static final String CUSTOM_SEARCH = "custom";
    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "people";

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int LISTENING_TIMEOUT = 10000;

    private static String currentSearch;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    private TextView captionText;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCaptions();

        initViews();

        currentSearch = DIGITS_SEARCH;

        if(isRequestPermissionNeeded()){
            return;
        }

        new SetupTask(this).execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new SetupTask(this).execute();
            } else {
                finish();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();

        resultText.setText(text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        resultText.setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        switchSearch(currentSearch);
    }

    public void onDigitsSearch(View view){
        updateSearch(DIGITS_SEARCH);
    }

    public void onForecastSearch(View view){
        updateSearch(FORECAST_SEARCH);
    }

    public void onPhonesSearch(View view){
        updateSearch(PHONE_SEARCH);
    }

    public void onCustomSearch(View view){
        updateSearch(CUSTOM_SEARCH);
    }

    private void updateSearch(String searchName){
        currentSearch = searchName;
        switchSearch(searchName);
    }

    private boolean isRequestPermissionNeeded(){
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return true;
        }

        return false;
    }

    private void initCaptions(){
        captions = new HashMap<>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(CUSTOM_SEARCH, R.string.custom_caption);
        captions.put(DIGITS_SEARCH, R.string.digits_caption);
        captions.put(PHONE_SEARCH, R.string.phone_caption);
        captions.put(FORECAST_SEARCH, R.string.forecast_caption);
    }

    private void initViews(){
        captionText = ((TextView) findViewById(R.id.caption_text));
        resultText = ((TextView) findViewById(R.id.result_text));

        captionText.setText(R.string.preparing_recognizer);
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<MainActivity> activityReference;
        SetupTask(MainActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                ((TextView) activityReference.get().findViewById(R.id.caption_text))
                        .setText(String.format("Failed to init recognizer %s", result));
            } else {
                //activityReference.get().switchSearch(currentSearch);
            }
        }
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        recognizer.startListening(searchName, LISTENING_TIMEOUT);

        String caption = getResources().getString(captions.get(searchName));
        captionText.setText(caption);
    }

    private void setupRecognizer(File assetsDir) throws IOException {

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, ACOUSTIC_MODEL))
                .setDictionary(new File(assetsDir, DICTIONARY))

                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();

        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(currentSearch, KEYPHRASE);

        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(CUSTOM_SEARCH, menuGrammar);

        // Create grammar-based search for digit recognition
        File digitsGrammar = new File(assetsDir, "digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

        // Create language model search
        File languageModel = new File(assetsDir, "weather.dmp");
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);

        // Phonetic search
        File phoneticModel = new File(assetsDir, "en-phone.dmp");
        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(currentSearch);
    }
}
