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
//        <property>tcr_common/scripts/DateTime.js</property>
//    </list-property>
//You can insert it after the <method> ... </method> near the beginning
//of the file.  You CAN refer to the methods in this file from scripts
//with the report.

// importPackage (Packages.java.text);

//--------------------------------------------------------------------
// Returns Date formed from given Candle Time string.  Candle Time
// is a string representation of time formatted as
// CCCMMddHHmmssSSS, where CCC is years since 1900 and other fields
// as defined in the Java SimpleDateFormat class.  For example,
// 1061206092057001 = 2006/12/06 09:20:57.001
function candleTimeToDate(timestamp) {

  return new Date( ( candleTime=timestamp,
  (parseInt(1900) + parseInt(candleTime.substr(0,3)))  + "/" +
  candleTime.substr(3,2) + "/" + candleTime.substr(5,2) + " " +
  candleTime.substr(7,2) + ":" + candleTime.substr(9,2) + ":" +
  candleTime.substr(11,2) ))

}

//--------------------------------------------------------------------
// Converts Date object to Candle Time string.  Candle Time
// is a string representation of time formatted as
// CCCMMddHHmmssSSS, where CCC is years since 1900 and other fields
// as defined in the Java SimpleDateFormat class.  For example,
// 1061206092057001 = 2006/12/06 09:20:57.001
function dateToCandleTime(date) {
  if (date == null) {
    date = new Date();
    date.setTime(0);    // 1/1/1970 00:00 GMT
  }
  s = "";
  v = date.getYear();
  if (v < 10) {
    s += "0";
  }
  if (v < 100) {
    s += "0";
  }
  // This will be a Y10K issue, but this should be out of service by then.
  s += (v > 999) ? "999" : v;
  v = date.getMonth() + 1;
  if (v < 10) {
    s += "0";
  }
  s += v;
  v = date.getDate();
  if (v < 10) {
    s += "0";
  }
  s += v;
  v = date.getHours();
  if (v < 10) {
    s += "0";
  }
  s += v;
  v = date.getMinutes();
  if (v < 10) {
    s += "0";
  }
  s += v;
  v = date.getSeconds();
  if (v < 10) {
    s += "0";
  }
  s += v;
  v = date.getTime()%1000;
  if (v < 10) {
    s += "0";
  }
  if (v < 100) {
    s += "0";
  }
  s += v;
  return s;
}



//--------------------------------------------------------------------
// Returns a string of the form M/D/YYYY for the given Javascript date-time.
function dateToString(date) {
// Alternative method: use java.text.DateFormat
//    var df = DateFormat.getDateInstance(DateFormat.SHORT);
//    var result = "" + df.format(date);
    var monthNum = date.getMonth() + 1;
    var result = "" + monthNum;
    result += "/" + date.getDate();
    result += "/" + date.getFullYear();
    return result;
}

//--------------------------------------------------------------------
// Returns a string of the form HH:MM:SS for the given
// number of seconds.
function formatElapsedTime(seconds) {
  var h = Math.floor(seconds/3600);
  var v = seconds % 3600;
  var m = Math.floor(v/60);
  var s = v % 60;
  var result = "";
  if (h < 10) {
    result += "0";
  }
  result += h + ":";
  if (m < 10) {
    result += "0";
  }
  result += m + ":";
  if (s < 10) {
    result += "0";
  }
  result += s;
  return result;
}

//----------------------------------------------------------------------
// Returns Date normalized to beginning of day (00:00:00)
function beginningOfDay(d)
{
	d.setHours(0);
	d.setMinutes(0);
	d.setSeconds(0);
   d.setMilliseconds(0);
	return d;
}

//----------------------------------------------------------------------
// Returns Date normalized to beginning of day (00:00:00)
function endOfDay(d)
{
	d.setHours(23);
	d.setMinutes(59);
	d.setSeconds(59);
   d.setMilliseconds(999);
	return d;
}

//----------------------------------------------------------------------
// Returns Date corresponding to either start date or beginning date of report period parameter string
// Parameters : String values containing the parameter names 
//              normally the names passed will be the names from the DateRange parameter group
function getStartDateFromGroupNames(reportPeriodParamName, startDateParamName, endDateParamName)
{
  dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
  
  var reportPeriod = reportContext.getParameterValue(reportPeriodParamName);
  var startDate = reportContext.getParameterValue(startDateParamName); 
  var endDate = reportContext.getParameterValue(endDateParamName);
  
  dsDebugLogger(" getStartDateFromGroup period " + reportPeriod + " start " + startDate + " end " + endDate); 
  
  if ( (startDate != null   && startDate != "")  
       &&
       (endDate != null  && endDate != "") )
   {
       	 return startDate;
   }
 
   // if both dates are not specified, use the report period to calculate the date
   return  getStartDate(reportPeriod);
 }
 
//----------------------------------------------------------------------
// Returns Date corresponding to either entered end date or ending date of report period parameter string
// Parameters : String values containing the parameter names 
//              normally the names passed will be the names from the DateRange parameter group
function getEndDateFromGroupNames(reportPeriodParamName, startDateParamName, endDateParamName)
{
  dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
  
  var reportPeriod = reportContext.getParameterValue(reportPeriodParamName);
  var startDate = reportContext.getParameterValue(startDateParamName); 
  var endDate = reportContext.getParameterValue(endDateParamName);
  
  dsDebugLogger(" getEndDateFromGroup period " + reportPeriod + " start " + startDate + " end " + endDate); 
  
  if ( (startDate != null   && startDate != "")  
       &&
       (endDate != null  && endDate != "") )
   {
       	 return endDate;
   }
 
   // if both dates are not specified, use the report period to calculate the date
   return  getEndDate(reportPeriod);
 }
 
//----------------------------------------------------------------------
// Returns the NLS string value for the report period depending upon the parameters entered
// Parameters : String values containing the parameter names 
//              normally the names passed will be the names from the DateRange parameter group
function getReportPeriodFromGroupNames(reportPeriodParamName, startDateParamName, endDateParamName)
{
  dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
  
  var reportPeriod = reportContext.getParameterValue(reportPeriodParamName);
  var startDate = reportContext.getParameterValue(startDateParamName); 
  var endDate = reportContext.getParameterValue(endDateParamName);
  
  dsDebugLogger(" getReportPeriodFromGroup period " + reportPeriod + " start " + startDate + " end " + endDate); 
  
  if ( (startDate != null   && startDate != "")  
       &&
       (endDate != null  && endDate != "") )
   {
       	 var stringValue = getNLS("custom_period");   // try to get new NLS value custom_period
         var pos = stringValue.indexOf("missing");     // if getNLS returns key  ... missing 
         if (pos >=0) 
         {
       		 stringValue = getNLS("ALL");     // Use "ALL" as the value
         }
       		
       	 return stringValue;	 
   }
 
   // if both dates are not specified, use the report period to return the string if not null
   if (reportPeriod != null && reportPeriod != "")
   	   return  getNLS(reportPeriod);
   	   
   return getNLS("ALL");          // match the original default if report period is null / blank
 }

//----------------------------------------------------------------------
// Returns Date corresponding to beginning date of report period parameter string
function getStartDate(ReportPeriod)
{
//	dsDebugLogger = ((""+typeof(debugLogger)) != "undefined" ) ? debugLogger : function(s) {};
  	var now = new Date();
    var ms = Math.floor(now.getTime()/1000); // Seconds since 1970 for current time
  	
//    dsDebugLogger("Report Period = " + ReportPeriod);
	if(ReportPeriod == "T") // Today
	{
      return beginningOfDay(now);
	}
	else if(ReportPeriod == "Y") // Yesterday
	{
	   ms -= 86400;
	}
	else if(ReportPeriod == "L24") // Yesterday
	{
	   ms -= 86400;
       return new Date(ms * 1000);
	}
	else if(ReportPeriod == "L7") // Last 7 days
	{
	   ms -= 604800;
	}
	else if(ReportPeriod == "L30") // Last 30 days
	{
	   ms -= 2592000;
	}
	else if(ReportPeriod == "L90") // Last 90 days
	{
	   ms -= 7776000;
	}
	else if(ReportPeriod == "L365") // Last 365 days
	{
	   ms -= 31536000;
	}
	else if(ReportPeriod == "CW") // Current week
	{
	   dayOfWeek = now.getDay();
	   // Change day to beginning of week by subtracting current day from current time
	   ms -= (dayOfWeek * 86400);
	}
	else if(ReportPeriod == "LW") // Last week
	{
	   dayOfWeek = now.getDay();
	   // Change day to beginning of current week by subtracting current day from current time
	   ms -= (dayOfWeek * 86400);
	   // Change day to beginning of previous week by subtracting 7 more days
	   ms -= 604800;
	}
	else if(ReportPeriod == "CM") // Current month
	{
	   now.setDate(1);  // Set date to beginning of month
	   return beginningOfDay(now);
	}
	else if(ReportPeriod == "LM") // Last month
	{
	   currentMonth = now.getMonth();
	   if (currentMonth == 0)  // If current month is January
	   {
	        // Set date to Dec. 1 of previous year
	   		now.setFullYear(now.getFullYear() - 1, 11, 1);
	   }
	   else
	   {
	   		// Set date to first of previous month
	   		now.setMonth(currentMonth - 1, 1);
	   }		
	   return beginningOfDay(now);
	}
	else if(ReportPeriod == "L3M") // Last 3 months
	{
	   currentMonth = now.getMonth();
	   if (currentMonth < 3)  // If current month is Jan, Feb or March
	   {
	        // Set to previous year
	   		now.setFullYear(now.getFullYear() - 1);
	   }
		// Set date to first of 3 months ago
	   	now.setMonth((currentMonth + 9) % 12, 1);
	    return beginningOfDay(now);
	}
	else if(ReportPeriod == "YTD") // Current year
	{
	   now.setMonth(0,1);  // Set date to beginning of year
	   return beginningOfDay(now);
	}
	else if(ReportPeriod == "ALL" || ReportPeriod == "") // All
	{
	   ms = 0;  // Set date to beginning of time (a.k.a. Jan. 1, 1970)
	}
	else return null;

	return beginningOfDay(new Date(ms * 1000));

}

//----------------------------------------------------------------------
// Returns Date corresponding to ending date of report period parameter string
function getEndDate(ReportPeriod)
{

  	var now = new Date();
    var ms = Math.floor(now.getTime()/1000); // Seconds since 1970 for current time
  	
//    dsDebugLogger("Report Period = " + ReportPeriod);
	if(ReportPeriod=="L24") {
		return now;
	} else
    if (ReportPeriod == "Y") // Yesterday
	{
	   ms -= 86400;
	}
	else if(ReportPeriod == "LW") // Last week
	{
	   dayOfWeek = now.getDay();
	   // Change day to beginning of current week by subtracting current day from current time
	   ms -= (dayOfWeek * 86400);
	   // Change day to last day of previous week by subtracting 1 more day
	   ms -= 86400;
	}
	else if ((ReportPeriod == "LM") || (ReportPeriod == "L3M")) // Last month or last 3 months
	{
	   // Set date to the 1st day of current month
	   now.setDate(1);
	   // Subtract one day to get end of previous month
	   ms = Math.floor(now.getTime()/1000);
	   ms -= 86400;
	}
	else return endOfDay(now);

	return endOfDay(new Date(ms * 1000));
}

//-----------------------------------------------------------------------------------
function daysInMonth(iMonth, iYear)
{
	return 32 - new Date(iYear, iMonth, 32).getDate();
}

//------------------------------------------------------------------------------------
function getNumberOfDays(reportPeriod, startDate, endDate)
{
	if(startDate != null && startDate != "" && endDate != null && endDate != "")
     {     	
         dt1 = startDate;
     		dt2 = endDate;
     }
	else 
	{    
		dt1 = getStartDate(reportPeriod);
    	dt2 = getEndDate(reportPeriod);
    }
    
    if (dt1 != null && dt2 != null) 
      	return Math.floor((dt2.getTime() - dt1.getTime()) / (1000 * 60 * 60 * 24));
    else return null;      
    
}

function jsToJavaDateConverterUtil(date) 
{
  try {
		return new Date(date.getTime());
	  } catch(e) {
		return new Date(""+date);
	  }
}