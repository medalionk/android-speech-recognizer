package ee.bilal.dev.speechrecorder.service;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileWriterSpeechListener implements SpeechListener {
    private static final String FILE_NAME = "_speech_result.txt";

    private AppCompatActivity activity;
    private String name;
    private File file;
    private OutputStream outputStream;

    public FileWriterSpeechListener(AppCompatActivity activity, String name) {
        this.activity = activity;
        this.name = name;
    }

    @Override
    public void onStarted() {
        createOutputStream();
    }

    @Override
    public void onStopped() {
        closeOutputStream();
    }

    @Override
    public void onPartialResult(String text) {

    }

    @Override
    public void onResult(String text) {
        writeToFile(text);
    }

    @Override
    public void onSpeechBegin() {

    }

    @Override
    public void onSpeechEnd() {

    }

    @Override
    public void onError(String message) {
        closeOutputStream();
    }

    @Override
    public void onTimeout() {

    }

    private void writeToFile(String text){
        if(isOutputStreamOpen()){
            try {
                outputStream.write(text.getBytes());
                outputStream.write(System.lineSeparator().getBytes());
            } catch (IOException ex) {
                Log.e("WriteToFile", "Error writing to file: " + ex.getMessage());
            }
        }
    }

    private boolean isOutputStreamOpen(){
        return outputStream != null || createOutputStream();
    }

    private boolean createFile(){
        File directory = activity.getApplicationContext().getFilesDir();
        file = new File(directory, name + FILE_NAME);

        try {
            file.createNewFile();
        }catch (IOException ex) {
            Log.e("CreateOutputStream", "Error creating new file: " + ex.getMessage());

            return false;
        }

        return false;
    }

    private boolean fileExist(){
        return file != null && file.exists() || createFile();
    }

    private boolean createOutputStream() {
        if(!fileExist()) {
            Log.d("CreateOutputStream", "File does not exist");

            return false;
        }

        try {
            outputStream = Files.newOutputStream(Paths.get(file.getPath()), StandardOpenOption.APPEND);//Files.newOutputStream(Paths.get(file.getPath()), StandardOpenOption.APPEND);// new FileOutputStream(file, true);

            return true;
        }catch (IOException ex) {
            Log.e("CreateOutputStream", "Error creating OutputStream: " + ex.getMessage());
        }

        return false;
    }

    private boolean closeOutputStream() {
        if(outputStream == null){
            Log.d("CreateOutputStream", "OutputStream is already closed");

            return true;
        }

        try {
            outputStream.close();
            outputStream = null;

            return true;
        }catch (IOException ex) {
            Log.e("CloseOutputStream", "Error closing OutputStream: " + ex.getMessage());
        }

        return false;
    }
}
