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
//        <property>tcr_common/scripts/ModifyQuery.js</property>
//    </list-property>
//You can insert it after the <method> ... </method> near the beginning
//of the file.  You CAN refer to the methods in this file from scripts
//with the report.


//----------------------------------------------------------------------
// Updates the Candle date/time strings in the current query to correspond
// to beginning and ending dates of the Report Period parameter string.
// You can call this in the beforeOpen() method of the data set.
// Assumptions: 
// 1. A default "WHERE" clause already exists using dummy begin and end
//    Candle time strings of "0000000000000000" and "9999999999999999",
//    respectively.
// Arguments:
//   dataset - dataset object of the calling beforeOpen() method
//   daterange - one of pre-defined date range values defined by 'Report Period'
//               parameter in Tivoli Common Reporting library, e.g. "T","Y","LM", etc.
//----------------------------------------------------------------------       
function ModifyQueryCandleDateRange (dataset,ReportPeriod)
{
    dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
    
    dsDebugLogger("ModifyQueryCandleDateRange: ReportPeriod=" + ReportPeriod);
    try {
      
      var dt = getStartDate(ReportPeriod);
	  ModifyQueryCandleBeginDate(dataset,dt);
	        
      dt = getEndDate(ReportPeriod);
      ModifyQueryCandleEndDate(dataset,dt);
    
      dsDebugLogger("queryText=" + dataset.queryText);
    } catch (ex) {
      dsDebugLogger("ModifyQueryCandleDateRange: "+ex);
    }
}

//----------------------------------------------------------------------
// Updates the Candle date/time strings in the current query to correspond
// to beginning and ending dates of the Report Period parameter string or 
// the specifically entered dates & times. 
// Useful in setting date from the date range parameter group.
// You can call this in the beforeOpen() method of the data set.
// Assumptions: 
// 1. A default "WHERE" clause already exists using dummy begin and end
//    Candle time strings of "0000000000000000" and "9999999999999999",
//    respectively.
// Arguments:
//   dataset - dataset object of the calling beforeOpen() method
//   reportperiod - one of pre-defined date range values defined by 'Report Period'
//               parameter in Tivoli Common Reporting library, e.g. "T","Y","LM", etc.
//   startdate - a date object, string representation of a date object or a null if not specified.
//   enddate   - a date object, string representation of a date object or a null if not specified
// Note: 
// The values of the entered parameters should be passed to the routine, rather than the parameter
// names or actual parameters. 
//----------------------------------------------------------------------
function ModifyQueryCandleDateRangeFromGroupValues(dataset,reportPeriod, startDate, endDate)
{
    dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
    
    dsDebugLogger("ModifyQueryCandleDateRangeFromGroupValues: ReportPeriod=" + reportPeriod + " start " + startDate + " end " + endDate);
    try {
      
      
      if ( (startDate != null && startDate != "") 
       	&& 
        	(endDate != null && endDate != "") )
      {
        	dt = jsToJavaDateConverterUtil(startDate);
           	dsDebugLogger("start date... " + dt);  	
      }
      else 
      {
      		dt = getStartDate(reportPeriod);
      		dsDebugLogger("start date from report period ..." + dt);
      }
      
      ModifyQueryCandleBeginDate(dataset,dt);
      
      if ( (startDate != null && startDate != "")
         && 
           (endDate != null && endDate != "") )
      {
            dt =jsToJavaDateConverterUtil(endDate);
            dsDebugLogger("end date... " + dt);  	
       }
      else 
      {
      	dt = getEndDate(reportPeriod);
      }
    
      ModifyQueryCandleEndDate(dataset,dt);
    
      dsDebugLogger("queryText=" + dataset.queryText);
    } catch (ex) {
      dsDebugLogger("ModifyQueryCandleDateRange: "+ex);
    }
}

//----------------------------------------------------------------------
// Updates the Candle begin date/time strings in the current query to correspond
// to beginning date passed to the routine
// You can call this in the beforeOpen() method of the data set.
// Assumptions: 
// 1. A default "WHERE" clause already exists using dummy begin date
//    Candle time strings of "0000000000000000"  for beginning date
// Arguments:
//   dataset - dataset object of the calling beforeOpen() method
//   date - date object to change the begin date
//----------------------------------------------------------------------
function ModifyQueryCandleBeginDate (dataset,date)
{
    dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
    
    dsDebugLogger("ModifyQueryCandleBeginDate: date=" + date);
    
    if (date != null) {
        
        var str = dateToCandleTime(date);
        var pattern = "0000000000000000";
        var qt = dataset.queryText;
        var pos = qt.indexOf(pattern);
        while(pos >=0)
        {    
            
        	dsDebugLogger("startdate=" + date + " str=" + str + " pos=" + pos);
        	dataset.queryText = qt.substr(0, pos) + str + qt.substr(pos + pattern.length);
        	qt = dataset.queryText;
        	pos = qt.indexOf(pattern);
        }
    }
}

//----------------------------------------------------------------------
// Updates the Candle end date/time strings in the current query to correspond
// to ending date passed to the routine
// You can call this in the beforeOpen() method of the data set.
// Assumptions: 
// 1. A default "WHERE" clause already exists using dummy end date
//    Candle time strings of "9999999999999999"  for ending date
// Arguments:
//   dataset - dataset object of the calling beforeOpen() method
//   date - date object to change the begin date
//----------------------------------------------------------------------
function ModifyQueryCandleEndDate (dataset,date)
{
    dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
    
    dsDebugLogger("ModifyQueryCandleEndDate: date=" + date);
    
    if (date != null) {
    
        var str = dateToCandleTime(date);
        var pattern = "9999999999999999";
        var qt = dataset.queryText;
        var pos = qt.indexOf(pattern);
        while(pos >=0)
        {
        	dsDebugLogger("enddate=" + date + " str=" + str + " pos=" + pos);
            dataset.queryText = qt.substr(0, pos) + str + qt.substr(pos + pattern.length);
            qt = dataset.queryText;
            pos = qt.indexOf(pattern);
         }
      }
 }   


//----------------------------------------------------------------------
// Inserts a WHERE condition in the data-set SQL query to filter according
// to beginning and ending dates of the Report Period parameter string.
// You can call this in the beforeOpen() method of the data set.
// Function searches for "WHERE" clause in current SQL query and inserts
// an additional condition, similar to the following:
// pay.PAYMENTDATE between '01/01/1970' and '12/31/2099'
//
// Assumptions: 
// 1. A "WHERE" clause already exists in query to be modified.
//
// Arguments:
//   dataset - dataset object of the calling beforeOpen() method
//   column  - column name in dataset SQL query which represents a date for
//             filtering the query, e.g. pay.PAYMENTDATE
//   daterange - one of pre-defined date range values defined by 'Report Period'
//               parameter in Tivoli Common Reporting library, e.g. "T","Y","LM", etc.
//   format_fn - Javascript function to format/convert a Javascript date object
//               to a string suitable for the SQL query.  Allowable choices are
//               dateToString() and dateToCandleTime(), both defined in DateTime.js.
//----------------------------------------------------------------------
function ModifyQueryAddDateRange (dataset,column,daterange,format_fn)
{
    dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
    
    dsDebugLogger("ModifyQueryAddDateRange: column=" + column + ", daterange=" + daterange);
    try {
        
        var dateStart = getStartDate(daterange);
        var dateEnd   = getEndDate(daterange);
        dsDebugLogger("dateStart=" + dateStart + ", dateEnd=" + dateEnd);
        if (dateStart && dateEnd && format_fn) {
            var condition = "" 
                + column + " between '" + format_fn(dateStart) 
                 + "' and '" + format_fn(dateEnd) + "'";
            ModifyQueryAddFilter (dataset, condition);
        }
            
    } catch (ex) {
        dsDebugLogger("ModifyQueryAddDateRange: " + ex);
    }

}
 
 
//----------------------------------------------------------------------
// Modify the current query by adding an additional filter to the WHERE clause.
// You can call this in the beforeOpen() method of the data set.
//
// Assumptions: 
// 1. A "WHERE" clause already exists in query to be modified.
//
// Arguments:
//   dataset - dataset object of the calling beforeOpen() method
//   filter  - SQL relational expression, e.g. customer_name = 'Mall Wort'
//----------------------------------------------------------------------
function ModifyQueryAddFilter (dataset,filter)
{
    dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
    
    try {
      
      var qt = dataset.queryText;
      var pattern = "WHERE ";
      var pos = qt.indexOf(pattern);
      if (pos == -1) { pos = qt.indexOf("where "); }  // try lower case
      
      dsDebugLogger("ModifyQueryAddFilter: filter=" + filter + " pos=" + pos);
      if (pos >= 0) {
        dataset.queryText = qt.substr(0, pos + pattern.length) 
            + filter + " AND " +
            qt.substr(pos + pattern.length);
      }
      
    
      dsDebugLogger("queryText=" + dataset.queryText);
    } catch (ex) {
      dsDebugLogger("ModifyQueryAddFilter: "+ex);
    }

}


//----------------------------------------------------------------------
// Inserts a WHERE condition in the data-set SQL query to filter according
// to beginning and ending dates of the Report Period parameter string.
// You can call this in the beforeOpen() method of the data set.
// Function searches for "WHERE" clause in current SQL query and inserts
// an additional condition, similar to the following:
// pay.PAYMENTDATE between '01/01/1970' and '12/31/2099'
//
// Assumptions: 
// 1. A "WHERE" clause already exists in query to be modified.
//
// Arguments:
//   dataset - dataset object of the calling beforeOpen() method
//   column  - column name in dataset SQL query which represents a date for
//             filtering the query, e.g. pay.PAYMENTDATE
//   daterange - one of pre-defined date range values defined by 'Report Period'
//               parameter in Tivoli Common Reporting library, e.g. "T","Y","LM", etc.
//   format_fn - Javascript function to format/convert a Javascript date object
//               to a string suitable for the SQL query.  Allowable choices are
//               dateToString() and dateToCandleTime(), both defined in DateTime.js.
//   startDate - value of Begin Date parameter
//   endDate - value of End Date parameter
//----------------------------------------------------------------------
function ModifyQueryAddDateRangeFromGroupValues (dataset,column,daterange,format_fn,startDate, endDate)
{
    dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
    
    dsDebugLogger("ModifyQueryAddDateRangeFromGroupValues: column=" + column + ", daterange=" + daterange + ", startDate=" + startDate + ", endDate=" + endDate );
    try {
        if((startDate != null && startDate != "") && (endDate != null && endDate != ""))
        {
      		var dateStart = jsToJavaDateConverterUtil(startDate);
        	var dateEnd   = jsToJavaDateConverterUtil(endDate);
        	
        }
        else
        {
        	var dateStart = getStartDate(daterange);
        	var dateEnd   = getEndDate(daterange);
        }
        
        dsDebugLogger("dateStart=" + dateStart + ", dateEnd=" + dateEnd);
        
        
        if (dateStart && dateEnd && format_fn) {
            var condition = "" 
                + column + " between '" + format_fn(dateStart) 
                 + "' and '" + format_fn(dateEnd) + "'";
            ModifyQueryAddFilter (dataset, condition);
        }
            
    } catch (ex) {
        dsDebugLogger("ModifyQueryAddDateRangeFromGroupValues: " + ex);
    }

}
 
