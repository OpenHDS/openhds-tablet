package org.openhds.mobile.listener;

/**
 * Generic interface that can be used for simple async task
 */
public interface TaskCompleteListener<T> {
    
    void onComplete(T value);

}
