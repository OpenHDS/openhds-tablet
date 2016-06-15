openhds-tablet
==============

Android app for Health and Demographic Data collection and management

Tablet Developer Instructions
==============

To get started, you must download Eclipse Mars (4.5.2). https://eclipse.org/mars/

Prerequisite Software

    Install Java SE JDK 7
        Make sure you install the JDKand not the JRE.
        Run the installer
        Set the JAVA_HOME environment variable to point to the root of the JDK installation
            Navigate to Start -> Control Panel -> System -> Advanced, then click Environment Variables.
            Under User Variables, click New
            Use JAVA_HOME for Variable Name
            Set Variable Value to the path of the JDK, by default it should be: C:\Program Files\Java\jdk1.7.0_01 (depending on which version of the JDK you install, the numbers may differ slightly)
        Verify the environment variable was set by navigating to Start -> Run, then type: cmd
        In the command prompt, type: echo %JAVA_HOME%
            It should output the path that was placed in the Variable Value field


C:\Documents and Settings\Dave>echo %JAVA_HOME% C:\Program Files\Java\jdk1.7.0_01 ```

    Install Maven (Last tested version was Maven 3.3.9)
        Follow the installation instructions 
http://maven.apache.org/download.cgi#Installation
        Requires JAVA_HOME environment variable to be set


Install Android Eclipse Plugin

Install Eclipse Android Plugin. For instructions, see here: http://developer.android.com/sdk/installing/installing-adt.html
Install (m2e-android) Android Maven Eclipse Plugin

You must install an Eclipse plugin to properly interact with the Android maven project. Instructions are here: http://rgladwell.github.com/m2e-android/
Clone Tablet Git Repo

Next, clone the tablet application git repo: git clone https://github.com/SwissTPH/openhds-tablet.git

Import into Eclipse

Now import the tablet application into Eclipse:

File -> Import... -> Maven -> Exisiting Maven Project
