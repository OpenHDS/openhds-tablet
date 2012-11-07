package org.openhds.mobile.utilities;

/**
 * @author Ime
 * 
 * Logger class
 * Example usage: L.w()
 *  
 */
public class L extends Logg {

public static final boolean debug = true;
   
           public static String DEFAULT_TAG = "OpenHDS";
     
           public static void i() {
                if(debug) callLogger("i", DEFAULT_TAG, "called");
           }
     
           public static void i(String message) {
                  if(debug) callLogger("i", DEFAULT_TAG, message);
           }

           public static void i(String message, String tag) {
                  if(debug) callLogger("i", tag, message);
           }

           public static void d() {
               if(debug) callLogger("d", DEFAULT_TAG, "called");
          }
           
           public static void d(String message) {
        	   if(debug) callLogger("d", DEFAULT_TAG, message);
	        }
	   
	        public static void d(String message, String tag) {
	        	if(debug) callLogger("d", tag, message);
	        }

	           public static void e() {
	                if(debug) callLogger("e", DEFAULT_TAG, "called");
	           }	        
	        
	        public static void e(String message) {
	        	if(debug)   callLogger("e", DEFAULT_TAG, message);
	        }
	   
	        public static void e(String message, String tag) {
	        	if(debug)  callLogger("e", tag, message);
	        }

	           public static void w() {
	                if(debug) callLogger("w", DEFAULT_TAG, "called");
	           }	
	           
	        public static void w(String message) {
	        	if(debug)   callLogger("w", DEFAULT_TAG, message);
	        }
	   
	        public static void w(String message, String tag) {
	        	if(debug)  callLogger("w", tag, message);
	        }
	   
	        public static void v(String message) {
	        	if(debug)  callLogger("v", DEFAULT_TAG, message);
	        }
	   
	        public static void v(String message, String tag) {
	        	if(debug)   callLogger("v", tag, message);
	        }           
 }