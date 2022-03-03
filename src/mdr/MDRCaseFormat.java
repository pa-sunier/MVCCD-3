package mdr;

import messages.MessagesBuilder;
import preferences.Preferences;

public enum MDRCaseFormat {
    NOTHING (Preferences.MDR_NAMING_FORMAT_NOTHING),
    UPPERCASE (Preferences.MDR_NAMING_FORMAT_UPPERCASE),
    LOWERCASE (Preferences.MDR_NAMING_FORMAT_LOWERCASE),
    CAPITALIZE (Preferences.MDR_NAMING_FORMAT_CAPITALIZE),
    LIKEBD (Preferences.MPDR_NAMING_FORMAT_LIKEDB);

    private final String name;

    MDRCaseFormat(String name) {
        this.name = name;
     }

    public String getName() {
        return name;
    }


    public String getText() {
        return MessagesBuilder.getMessagesProperty(name);
    }

    public static MDRCaseFormat findByText(String text){
        for (MDRCaseFormat element: MDRCaseFormat.values()){
            if (element.getText().equals(text)) {
                return element;
            }
        }
        return null;
    }
}