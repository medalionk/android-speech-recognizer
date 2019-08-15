package ee.bilal.dev.speechrecorder.model;

public final class SpeechModel {
    private final String name;
    private final String grammarFile;
    private final String caption;

    public SpeechModel(String name, String grammarFile, String caption) {
        this.name = name;
        this.grammarFile = grammarFile;
        this.caption = caption;
    }

    public static SpeechModel of(String name, String grammarFile, String caption) {
        return new SpeechModel(name, grammarFile, caption);
    }

    public String getName() {
        return name;
    }

    public String getGrammarFile() {
        return grammarFile;
    }

    public String getCaption() {
        return caption;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this){
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!SpeechModel.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final SpeechModel other = (SpeechModel) obj;
        return this.name.equalsIgnoreCase(other.name);
    }
}
