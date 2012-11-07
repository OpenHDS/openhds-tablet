package org.openhds.mobile.utilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.util.Log;

/**
 * @author Ime
 * 
 * Useful logger that displays: class name, method name and line number
 * Based off http://www.hautelooktech.com/2011/08/15/android-logging/
 * See L.java
 * 
 */

@SuppressWarnings("unused")
public class Logg {
  
       public static String DEFAULT_TAG ="HauteLookLib";
  
       final static int depth = 4;
  
       public static void i(String message) {
              callLogger("i", DEFAULT_TAG, message);
       }
  
       public static void i(String message, String tag) {
              callLogger("i", tag, message);
       }
  
       public static void d(String message) {
              callLogger("d", DEFAULT_TAG, message);
       }
  
       public static void d(String message, String tag) {
              callLogger("d", tag, message);
       }
  
       public static void e(String message) {
              callLogger("e", DEFAULT_TAG, message);
       }
  
       public static void e(String message, String tag) {
              callLogger("e", tag, message);
       }
  
       public static void w(String message) {
              callLogger("w", DEFAULT_TAG, message);
       }
  
       public static void w(String message, String tag) {
              callLogger("w", tag, message);
       }
  
       public static void v(String message) {
              callLogger("v", DEFAULT_TAG, message);
       }
  
       public static void v(String message, String tag) {
              callLogger("v", tag, message);
       }
  
       @SuppressWarnings("rawtypes")
       public static void callLogger(String methodName, String tag, String message) {
              final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
              try {
                     Class cls = Class.forName("android.util.Log");
                     Method method = cls.getMethod(methodName, String.class, String.class);
                     method.invoke(null, tag, getTrace(ste) + message);
              } catch (ClassNotFoundException e) {
                     e.printStackTrace();
              } catch (IllegalArgumentException e) {
                     e.printStackTrace();
              } catch (SecurityException e) {
                     e.printStackTrace();
              } catch (IllegalAccessException e) {
                     e.printStackTrace();
              } catch (InvocationTargetException e) {
                     e.printStackTrace();
              } catch (NoSuchMethodException e) {
                     e.printStackTrace();
              }
       }
  
       public static String getTrace(StackTraceElement[] ste) {
              return"[" + getClassName(ste) + "][" + getMethodName(ste) + "][" + getLineNumber(ste) + "] ";
       }
  
       public static String getClassPackage(StackTraceElement[] ste) {
              return ste[depth].getClassName();
       }
  
       public static String getClassName(StackTraceElement[] ste) {
              String[] temp = ste[depth].getClassName().split("\\.");
              return temp[temp.length - 1];
       }
  
       public static String getMethodName(StackTraceElement[] ste) {
              return ste[depth].getMethodName();
       }
  
       public static int getLineNumber(StackTraceElement[] ste) {
              return ste[depth].getLineNumber();
       }
  
}
