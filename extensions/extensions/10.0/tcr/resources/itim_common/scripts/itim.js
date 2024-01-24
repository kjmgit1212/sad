importPackage(java.lang);
importPackage(java.text);
importPackage(java.util);

function getCustomLabelsBundleName() {
	var name = "CustomLabels";
	var locale = reportContext.getLocale();
	if (locale != null) {
		if (locale.getLanguage() != null) {
			name += "_" + locale.getLanguage();
		}
		if (locale.getCountry() != null) {
			name += "_" + locale.getCountry();
		}
		if (locale.getVariant() != null) {
			name += "_" + locale.getVariant();
		}
	}
	return name;
}

// Global variables for reports:
// Date format from the ITIM database. 
df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS zzz");
df.setTimeZone(TimeZone.getTimeZone("GMT"));
// Current time of the has been selected to run.
currentTime = (new Date()).getTime();

function getDateFromString(date) {
	if ((date != null) && (date.length === 27)) {
		return df.parse(date);
	}
	return new Date(0);
}

function getDaysFromDate(d) {
	if (d != null) {
		var t = d.getTime();
		var ct = (currentTime - t);
		return ct / 1000 / 60 / 60 / 24;
	}
	return 0;
}

function getDaysFromDateString(lastAccessDateString) {
	var d = getDateFromString(lastAccessDateString);
	var t = d.getTime();
	var ct = (currentTime - t);
	return ct / 1000 / 60 / 60 / 24;
}

function getDormantPeriodString(dormantPeriod) {
	if (dormantPeriod == 1) {
		return dormantPeriod + " " + getNLS("days");
	} else if (dormantPeriod > 1) {
		return dormantPeriod + " " + getNLS("days");
	}
}

function getDormantPeriodDate(dormantPeriod) {
	var numDays = parseInt(dormantPeriod);
	var cal = new GregorianCalendar();
	cal.add(Calendar.DAY_OF_YEAR, -1 * numDays );
	return cal.getTime();
}

function getDormantPeriodDateString(dormantPeriod) {
	return df.format(getDormantPeriodDate(dormantPeriod));
}

function getDateGMTString(date) {
	return df.format(date);
}

function isNotAny(param) {
	return !"Any".equalsIgnoreCase(param);
}

function isAccountActive(status) {
	//debugLogger("isAccountActive: " + status);
	if ("0".equals(status)) {
		return true;
	}
	return false;
}

function isExclusiveAccess(access)
{
	if( access == null ||"0".equals(access))
	{
		return true;
	}
	return false;
}


var checkinNLS = getNLS("checkin");
var checkoutNLS = getNLS("checkout");
var getpasswordNLS = getNLS("view_password");
var expirationNLS = getNLS("expiration_notification");
var NotifyCheckinExpiredLeaseNLS = getNLS("notify_checkin_expiration_notification");

function convertSAActionsToNLS(action)
{
 	if ("Checkin".equals(action)) {
		return checkinNLS;
	} else if ("Checkout".equals(action)) {
		return checkoutNLS;
	}else if ("GetPassword".equals(action)) {
		return getpasswordNLS;
	}else if ("NotifyExpiredLease".equals(action)) {
		return expirationNLS;
	}
	else if ("NotifyCheckinExpiredLease".equals(action)){
		return NotifyCheckinExpiredLeaseNLS;
	}
}

var roleNLS = getNLS("heading_role");
var personNLS = getNLS("person");

function convertOwnerTypeToNLS(type)
{
 	if ("Person".equals(type)) {
		return personNLS;
	} else if ("Role".equals(type)) {
		return roleNLS;
	}
}

function isRequestStatusApproved(status) {
	//debugLogger("isRequestStatusApproved: " + status);
	if ("AA".equals(status)) {
		return true;
	}
	return false;
}

function convertAnyToSql(parameter) {
	if ("Any".equals(parameter.value)) {
		return '%';
	}
	return parameter;
}

function convertStaticAnyToSql(parameter) {
	if ("-1".equals(parameter.value)) {
		return '%';
	}
	return parameter;
}

var anyNLS = getNLS("any");

function convertAnyToNLS(parameter, name) {
	if ("Any".equals(parameter.value)) {
		return getNLS("any");
	}
	return name;
}

function convertSplatToNLS(parameter) {
	if ("%".equals(parameter.value) || "'%'".equals(parameter.value)) {
		return getNLS("any");
	}
	return parameter;
}

var activeNLS = getNLS("active");
var inactiveNLS = getNLS("inactive");
function convertAccountStatusToNLS(status) {
	if ("%".equals(status)) {
		return anyNLS;
	} else if ("0".equals(status)) {
		return activeNLS;
	} else if ("1".equals(status)) {
		return inactiveNLS;
	}
}

var credentialNLS = getNLS("Credential");
var credentialpoolNLS = getNLS("Credential_Pool");

function convertEntitlementTypeToNLS(entitlement_type) {
	if ("0".equals(entitlement_type)) {
		return credentialNLS;
	} else if ("1".equals(entitlement_type)) {
		return credentialpoolNLS;
	}
}

function convertEntitlementTypeDataToNLS(entitlement_type) {
	if ("Credential".equals(entitlement_type)) {
		return credentialNLS;
	} else if ("CredentialPool".equals(entitlement_type)) {
		return credentialpoolNLS;
	}
}

var compliantNLS = getNLS("compliant");
var noncompliantNLS = getNLS("noncompliant");
var disallowedNLS = getNLS("disallowed");
function convertAccountComplianceToNLS(status) {
	if ("%".equals(status)) {
		return anyNLS;
	} else if ("1".equals(status)) {
		return compliantNLS;
	} else if ("3".equals(status)) {
		return noncompliantNLS;
	} else if ("2".equals(status)) {
		return disallowedNLS;
	}
}

function convertAllRoleNameToNLS(roleName) {
	if ("provisioningMemberTypeAllPerson".equals(roleName)) {
		return getNLS("provisioningMemberTypeAllPerson");
	} else if ("provisioningMemberTypeOthers".equals(roleName)) {
		return getNLS("provisioningMemberTypeOthers");
	}
	return roleName;
}

var rejectedNLS = getNLS("rejected");
var approvedNLS = getNLS("approved");
function convertApprovalRequestStatusToNLS(status) {
	if ("%".equals(status)) {
		return anyNLS;
	} else if ("AR".equals(status)) {
		return rejectedNLS;
	} else if ("AA".equals(status)) {
		return approvedNLS;
	}
}

function convertActivityNameToNLS(activityName) {
	if (activityName == null) {
		return "";
	}
	if (activityName.indexOf("$") == 0) {
		// Remove the $ in the label.
		var key = activityName.substring(1,activityName.length);
		var newActivityName = reportContext.getMessage(key,reportContext.getLocale());
		if (newActivityName != null) {
			return newActivityName;
		}
	}
	return activityName;
}

function convertRoleTargetToNLS(targetName, wildcard) {
	if ("R;2;*".equals(targetName) && "1".equals(wildcard)) {
		return getNLS("ALL");
	} else {
		return targetName;
	}
}

function convertServiceTargetToNLS(targetName, wildcard) {
	if ("2;*".equals(targetName) && "1".equals(wildcard)) {
		return getNLS("ALL");
	} else {
		return targetName;
	}
}

function convertGroupTargetToNLS(targetName, wildcard) {
	if ("G;2;*;*".equals(targetName) && "1".equals(wildcard)) {
		return getNLS("ALL");
	}
	else if ("G;3;*".equals(targetName) && "1".equals(wildcard)) {
		return getNLS("all_groups_on_specified_accts");		
	}
	else {
		return targetName;
	}
}

function convertEmptyName(name) {
	return name==null?getNLS("empty_name"):name;
}

function wrapString(string) {
	if (!string) return "";
	if (string.length == 0) return "";
	var sb = new StringBuffer();
	var wrap = 40;
	var boundary = BreakIterator.getWordInstance(reportContext.getLocale());
	var separator = "\n";
	boundary.setText(string);
	var start = boundary.first();
	var wordStart = start;
	for (var end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
		if (end - wordStart < wrap) {
			sb.append(string.substring(start, end));
        } else {
        	sb.append(separator);
        	sb.append(string.substring(start, end));
        	wordStart = end;
        }
	}
	return sb.toString();
}

///////////////////////////////////////////////////////////////////////////////
//// RecurringTimeSchedule reconstruction methods
//// - Logic for this helpfully came from: com.ibm.itim.ui.view.ScheduleFrequencyViewUtils.java
////
//// The first 4 methods below rely on the JDK to return the right strings.  This avoids Pii on our end.
///////////////////////////////////////////////////////////////////////////////
function getMonthString(month) {
month=month-1;//com.ibm.itim.scheduling.RecurringTimeSchedule.java explains why month-1
var l = reportContext.getLocale();
var dateFormat = new SimpleDateFormat("MMMMM", l);
var cal = Calendar.getInstance(l);
cal.set(Calendar.MONTH, month);
return dateFormat.format(cal.getTime());
}
function getDayOfWeekString(day) {
var l = reportContext.getLocale();
var dateFormat = new SimpleDateFormat("EEEEE", l);
var cal = Calendar.getInstance(l);
cal.set(Calendar.DAY_OF_WEEK, day);
return dateFormat.format(cal.getTime());
}
function getDayOfMonthString(day) {
var l = reportContext.getLocale();
var dateFormat = new SimpleDateFormat("EEEEE", l);
var cal = Calendar.getInstance(l);
cal.set(Calendar.DAY_OF_MONTH, day);
return dateFormat.format(cal.getTime());
}
function getTimeString(hour,minute) {
var l = reportContext.getLocale();
var dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, l); //new SimpleDateFormat();
var cal = Calendar.getInstance(l);
cal.set(Calendar.HOUR_OF_DAY, hour);
cal.set(Calendar.MINUTE, minute);
return dateFormat.format(cal.getTime());
}

function getSchedule(scheduleToParse) {
//dataSetRow["ERSCHEDULE"]
var stringTokenizer = new StringTokenizer(scheduleToParse, '\t');

var minute = Packages.java.lang.Integer.parseInt(stringTokenizer.nextToken());
var hour = Packages.java.lang.Integer.parseInt(stringTokenizer.nextToken());
var monthOfYear = Packages.java.lang.Integer.parseInt(stringTokenizer.nextToken());
var dayOfWeek = Packages.java.lang.Integer.parseInt(stringTokenizer.nextToken());
var dayOfMonth = Packages.java.lang.Integer.parseInt(stringTokenizer.nextToken());
var dayOfQuarter = Packages.java.lang.Integer.parseInt(stringTokenizer.nextToken());
var dayOfSemiAnnual = Packages.java.lang.Integer.parseInt(stringTokenizer.nextToken());

var scheduleString = "";
var i18nParams;
var dailyLabel = reportContext.getMessage("DAILY", reportContext.getLocale());
//FREQUENCY_DAILY
i18nParams = [getTimeString(hour,minute)];
scheduleString = reportContext.getMessage("FREQUENCY_DAILY", reportContext.getLocale(), i18nParams);
if (monthOfYear > 0 && dayOfMonth > 0) {
	//FREQUENCY_ON_DATE_OF_YEAR;
	i18nParams = [getMonthString(monthOfYear), dayOfMonth, getTimeString(hour,minute)];//getDayOfMonthString(dayOfMonth)
	scheduleString = reportContext.getMessage("FREQUENCY_ON_DATE_OF_YEAR", reportContext.getLocale(), i18nParams);
} else if (monthOfYear > 0 && (dayOfWeek > 0 || dayOfWeek == -1 || dayOfMonth == -1)) {
	//FREQUENCY_DURING_A_MONTH;
	if (dayOfMonth == -1) {
		dayOfWeek = dailyLabel;//daily
	}else if(dayOfWeek == -1) {
		dayOfWeek = dailyLabel;//daily
	} else {
		dayOfWeek = getDayOfWeekString(dayOfWeek);
	}
	i18nParams = [getMonthString(monthOfYear), getDayOfWeekString(dayOfWeek), getTimeString(hour,minute)];
	scheduleString = reportContext.getMessage("FREQUENCY_DURING_A_MONTH", reportContext.getLocale(), i18nParams);
} else if (dayOfWeek > 0) {
	//FREQUENCY_WEEKLY;
	i18nParams = [dayOfWeek, getTimeString(hour,minute)];
	scheduleString = reportContext.getMessage("FREQUENCY_WEEKLY", reportContext.getLocale(), i18nParams);
} else if (monthOfYear == -1) {
	//FREQUENCY_MONTHLY;
	i18nParams = [getMonthString(monthOfYear), getTimeString(hour,minute)];
	scheduleString = reportContext.getMessage("FREQUENCY_MONTHLY", reportContext.getLocale(), i18nParams);
} else if (hour == -1) {
	//FREQUENCY_HOURLY;
	i18nParams = [minute];
	scheduleString = reportContext.getMessage("FREQUENCY_HOURLY", reportContext.getLocale(), i18nParams);
} else if (dayOfQuarter > 0) {
	//FREQUENCY_DURING_A_QUARTER;
	i18nParams = [dayOfQuarter, getTimeString(hour,minute)];
	scheduleString = reportContext.getMessage("FREQUENCY_DURING_A_QUARTER", reportContext.getLocale(), i18nParams);
} else if (dayOfSemiAnnual > 0) {
	//FREQUENCY_DURING_SEMI_ANNUAL;
	i18nParams = [dayOfSemiAnnual, getTimeString(hour,minute)];
	scheduleString = reportContext.getMessage("FREQUENCY_DURING_SEMI_ANNUAL", reportContext.getLocale(), i18nParams);
}
return scheduleString;
}

//getEmptyOrNLS(dataSetRow["SERVICETYPE"]);
function getEmptyOrNLS(key)
{
	if (key == null || key.equals(""))
	{
		return "";
	}
	else
	{
		return getNLS(key);
	}	
}

function wrap(longStr,width){
	if(longStr == null || longStr.length==0) {
		return 
	}
	length = longStr.length; 
	if(length <= width)
		return longStr;
	return (longStr.substring(0, width) + "..." );
}


