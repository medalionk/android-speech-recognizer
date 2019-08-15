package ee.bilal.dev.speechrecorder.model;

import com.google.common.collect.ImmutableList;

import  org.apache.commons.lang3.text.WordUtils;

import java.util.LinkedList;
import java.util.List;

public class SphinxSpeechModel {

    public static final List<SpeechModel> MODELS = ImmutableList.of(
            SpeechModel.of("digits", "digits.gram", "Digits recognition. e.g: \"one two three four five\"."),
            //SpeechModel.of("forecast", "weather.dmp", "Weather recognition. e.g: \"sunny spells on Wednesday\"."),
            //SpeechModel.of("phones", "en-phone.dmp", "Say anything."),
            SpeechModel.of("custom", "menu.gram", "Menu recognition. e.g: \"digits\", \"forecast\" or \"phones\".")
    );

    public static SpeechModel getByName(String name){
        for (SpeechModel model : MODELS) {
            if(model.getName().equalsIgnoreCase(name)){
                return model;
            }
        }

        return null;
    }

    public static List<String> getNames(){
        List<String> names = new LinkedList<>();

        for (SpeechModel model : MODELS) {
            names.add(WordUtils.capitalize(model.getName()));
        }

        return names;
    }
}
