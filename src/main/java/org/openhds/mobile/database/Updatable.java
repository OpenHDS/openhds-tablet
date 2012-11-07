package org.openhds.mobile.database;

import android.content.ContentResolver;

public interface Updatable {

    void updateDatabase(ContentResolver resolver, String filepath);
}
