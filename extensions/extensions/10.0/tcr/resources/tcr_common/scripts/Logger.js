//
// Licensed Materials - Property of IBM
// 5724-T69 
// IBM Tivoli Reporting
//
// (C) Copyright IBM Corp. 2007 All Rights Reserved.
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.

//Javascript example of simple logging for testing and report
//design purposes.  This sample is not intended for production 
//use. 

//Javascript functions needed in report wide namespace.  There is
//currently no support in the BIRT Designer Eclipse GUI for specifying
//that a report should use this file.  To use in a report, you
//have to edit the .rptdesign file by hand, and insert the following:
//    <list-property name="includeScripts">
//        <property>tcr_common/scripts/Logger.js</property>
//    </list-property>
//You can insert it after the <method> ... </method> near the beginning
//of the file.  You CAN refer to the methods in this file from scripts
//within the report.

importPackage( Packages.java.io );


//--------------------------------------------------------------------
// Global variables for logger objects
var fgLogger;
var tcrLogger;
var tcrReportDesign;
var reportName;
//--------------------------------------------------------------------
// Setup logging.
// This method sets up a configured logger object in the global
// variable fgLogger or tcrLogger if it determines that logging is configured.
// Logging is configured in the following way:
//       Check if we are running under the TCR server, if so, then set up logging to 
//       send the trace messages to the TCR server log.
//       If not under TCR, use the persistent global variable logfileName to define the name
//       of the logfile on the file system, if not set, then logging is off
//       The value of logfileName is used as the logfile and
//       it is written to the first directory of the following found to
//       exist: /tmp, c:/temp, .
//
function setupLogging() {
   if (((""+typeof(tcrLogger)) == "undefined" ) || (tcrLogger == null)) {

   // see if we are running within the TCR server environment
    if (!reportContext.getAppContext().isEmpty() &&
        reportContext.getAppContext().containsKey("com.ibm.tivoli.reporting.api.reportEngine.IReportLogger") )
     {
         importPackage( Packages.com.ibm.tivoli.reporting.api.reportEngine);
         importPackage( Packages.com.ibm.tivoli.reporting.api.dataAdministration);
         tcrLogger = reportContext.getAppContext().get("com.ibm.tivoli.reporting.api.reportEngine.IReportLogger");
         tcrReportDesign = reportContext.getAppContext().get("com.ibm.tivoli.reporting.api.dataAdministration.Design");
         reportName = tcrReportDesign.getName();
         //   reportContext.getReportRunnable().getReportName();
      }
  }


  if ( (((""+typeof(tcrLogger)) == "undefined" ) || (tcrLogger == null)) 
	  && 
     (((""+typeof(fgLogger)) == "undefined" ) || (fgLogger == null))  ) {
	     var dir = null;
        var logFile = null;
   	  //Check if logFile is defined for this report
        logFile = reportContext.getPersistentGlobalVariable("logfileName");
        //logFile is  defined 
       if ((logFile != null) && (logFile.length() > 0)) 
       {
      	//create a directory to store the logfile 
   		 dir = new java.io.File("/tmp");
  	      
    	    if (!dir.isDirectory()) 
     	    {     
       		  dir = new java.io.File("c:/temp");
   	    }
     
  		    if (!dir.isDirectory()) 
    		 {
       	     dir = new java.io.File(".");
  		 	 }
     
    
  			 // Open the logfile if directory is OK
  	 		 if ((dir != null) && dir.isDirectory()) {
  	    	   logf = new java.io.File(dir, logFile);
     			fos = new java.io.FileOutputStream(logf, true);
   	 		fgLogger = new java.io.PrintWriter(fos);
   	 	}
    	}
  	}
}


//--------------------------------------------------------------------
// Close the logger file.
function closeLogger() {
  if (((""+typeof(fgLogger)) != "undefined" ) && (fgLogger != null)) {
    fgLogger.close();
    fgLogger = null;
  }
}

//--------------------------------------------------------------------
// Writes to log if logging enabled.  This function assumes
// that setupLogging has been called and its result placed in
// a variable called fgLogger or tcrLogger.
function debugLogger(logtext) {
 if(((""+typeof(tcrLogger)) != "undefined" ) && (tcrLogger != null)) {	
      tcrLogger.trace( reportName, logtext);
 }
 else if (((""+typeof(fgLogger)) != "undefined" ) && (fgLogger != null)) {
    fgLogger.println(logtext);
  }
}

//--------------------------------------------------------------------
// Log entry for initialize
function logInitialize() {
  debugLogger("===================================================================");
  debugLogger("initialize ----- " + new Date());
  if(((""+typeof(tcrLogger)) != "undefined" ) && (tcrLogger != null)) {	
      debugLogger(tcrReportDesign);
   }
  debugLogger("--------------------------------------------------------------------");
}

 