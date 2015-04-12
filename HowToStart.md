# How to start working on the application (by Guy) #

## Java SE 6 ##

Download from http://www.oracle.com/technetwork/java/javase/downloads/jdk-6u29-download-513648.html

## Eclipse EE ##

Download version 3.7.1 from http://www.eclipse.org/downloads/

## ADT Plugin ##

In Eclipse go to Help->Install New Software, insert the url https://dl-ssl.google.com/android/eclipse/ , Select all and finish.

Go to Window->Android SDK Manager, pick all the tools of Android 2.3.3 and press on Install Packages.

## Google Plugin ##

In Eclipse go to Help->Install New Software, insert the url http://dl.google.com/eclipse/plugin/3.7 , Select all and finish.

## SVN Plugin ##

In Eclipse go to Help->Install New Software, insert the url http://subclipse.tigris.org/update_1.8.x , Select all and finish.

### SVN Guide ###
http://www.ankara-gtug.org/2011/06/04/how-to-create-google-code-project-and-synchronize-files-with-svn-repository-using-subclipse/

## Import Projects ##

In Eclipse go to File->Import->SVN->Checkout Project from SVN->Create a new repository location. Then insert the url: https://friendizer.googlecode.com/svn/trunk
Choose Friendizer-Android (a new project named Friendizer-Android should appear in the package explorer).
Do the same for Friendizer-AppEngine
