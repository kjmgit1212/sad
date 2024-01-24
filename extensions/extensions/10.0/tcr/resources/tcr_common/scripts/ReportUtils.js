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

//Javascript functions needed in report wide namespace.  There is
//currently no support in the BIRT Designer Eclipse GUI for specifying
//that a report should use this file.  To use in a report, you
//have to edit the .rptdesign file by hand, and insert the following:
//    <list-property name="includeScripts">
//        <property>tcr_common/scripts/ReportUtils.js</property>
//    </list-property>
//You can insert it after the <method> ... </method> near the beginning
//of the file.  You CAN refer to the methods in this file from scripts
//with the report.


//--------------------------------------------------------------------
// NLS support within BIRT scripts.
// Assume you have a properties file for your report with the localized
// label mytext:
//    mytext=Some Localized Text
// To use the localized text for current locale in a script, use
//    reportContext.getMessage("mytext", reportContext.getLocale());
// The value of this expression will be "Some Localized Text".

//--------------------------------------------------------------------
// Get message from resource bundle for key, or return error message (in English)
function getNLS(key)
{
  var nlsText = reportContext.getMessage(key,reportContext.getLocale());
  if (nlsText != null)
	{
	  return nlsText;
	}
  return "Key " + key + " missing";
}


//--------------------------------------------------------------------
// Trim leading and trailing blanks from the given string
function trimString(s) {
  //  debugLogger("trimString s=" + s);
  if (s == null) {
    return null;
  } else {
    return s.replace(/^\s*|\s*$/g,"");
  }
}

//-----------------------------------------------------------------------
// Sets the title string in the report from the given resource key. This title
//is the same as HTML title. It shows up in the browser title bar. 
function setReportTitle(key) {
  var dh = reportContext.getReportRunnable().designHandle.getDesignHandle();
  dh.setProperty( dh.TITLE_PROP, getNLS(key) );
  
}

//------------------------------------------------------------------------
//Set global variable parameter validity. In the initialize method of the report, setParameterValidity("")
function setParameterValidity(str)
{
	reportContext.setPersistentGlobalVariable("param_validity", str);
}

//------------------------------------------------------------------------
//Check for parameter validity. Parameter is valid if global variable "param_validity" is blank
function isParamsValid(str)
{
	return reportContext.getPersistentGlobalVariable("param_validity") == "";
}

//--------------------------------------------------------------------
// Global variable for rows returned count
var rowCount = 0;

//Counter that could be use to compute the total number of record that each 
//table in a report returns , this could be use to display or hide the 
//no data set information grid. 
var totalCount = 0;

reportContext.setPersistentGlobalVariable("totalCount", "0");
reportContext.setPersistentGlobalVariable("rowCount", "0");
//--------------------------------------------------------------------
// Sets row count 
//
function setRowCount(rows) {
  rowCount = rows;
  reportContext.setPersistentGlobalVariable("rowCount", rows+"");
}

function setTotalCount(rows) {
  totalCount = Packages.java.lang.Integer.parseInt(totalCount) + Packages.java.lang.Integer.parseInt(rows);
  //why affect the individual tables rowCount... using totalCount instead.
  reportContext.setPersistentGlobalVariable("totalCount", totalCount+""); 
}
//--------------------------------------------------------------------
// returns true if row count == 0
//
function isRowCountZero() {
   return (rowCount == 0);
}
//-----------------------------------------------------------------------
//Return row count
function getRowCount()
{
 return rowCount;
}

function isTotalCountZero()
{
 return (totalCount == 0);
}
//--------------------------------------------------------------------
//Return total count
//This is used where there are two tables contributing to rowCount.
//--------------------------------------------------------------------
function getTotalCount()
{
 return totalCount;
}

