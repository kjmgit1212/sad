function preparePage()
{
	var status=document.getElementsByTagName("status")[0];
	processStatus(status);
	var contactInfo=document.getElementsByTagName("emergency-contact")[0];
	processContactInfo(contactInfo);
}
function processStatus(statusElement)
{
	isSaving=statusElement.getAttribute("isSaving");
	if(isSaving=="true") self.close();	
	isReadOnly=statusElement.getAttribute("isReadOnly");
	if(isReadOnly=="true")
	{
		var form=document.forms[0];
		form.elements['name'].readOnly=isReadOnly;		
		form.elements['telephone'].readOnly=isReadOnly;		
		form.elements['relationship'].readOnly=isReadOnly;
		document.getElementById('readwrite_buttons').className='hide';
		document.getElementById('readonly_buttons').className='';
	}
	else
	{
		document.getElementById('readwrite_buttons').className='';
		document.getElementById('readonly_buttons').className='hide';
	}
}
function processContactInfo(contactInfoElement)
{
	var form=document.forms[0];
	form.elements['name'].value=contactInfoElement.getAttribute('name');		
	form.elements['telephone'].value=contactInfoElement.getAttribute('telephone');		
	form.elements['relationship'].value=contactInfoElement.getAttribute('relationship');		
}
