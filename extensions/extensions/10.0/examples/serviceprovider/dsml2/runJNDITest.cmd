@rem ****************************************************************
@rem *  Licensed Materials - Property of IBM
@rem *  
@rem *  (c) Copyright IBM Corp.  2007 All Rights Reserved
@rem *  
@rem *  US Government Users Restricted Rights - Use, duplication or
@rem *  disclosure restricted by GSA ADP Schedule Contract with
@rem *  IBM Corp.
@rem ****************************************************************
@rem Runs DSMLv2 JNDI provider test program
setlocal

@rem modify these script variables for your environment
set LIB=c:\itim45\lib
set EXAMPLES=..\..\..\lib\examples.jar
set JAVA="C:\Program Files\WebSphere\AppServer\java\bin\java"
set CERT_HOME=C:\Program Files\WebSphere\AppServer\etc

@rem Library files from ITIM lib directory
set DSML2=%LIB%\antlr-2.7.2.jar;%LIB%\castor-0.9.4.3-xml.jar;%LIB%\dsml2.jar;%LIB%\regexp.jar
set AGENTLIB=%LIB%\enroleagent.jar
set SSLLIBS=%LIB%\sslj.jar;%LIB%\jsafe.jar
set JNDILIBS=%LIB%\ldapjdk.jar
set XMLPARSER=%LIB%\xml-apis.jar;%LIB%\xercesImpl.jar
set LABELS=c:\itim45\data
set CLASSPATH=.;%DSML2%;%AGENTLIB%;%SSLLIBS%;%JNDILIBS%;%XMLPARSER%;%LABELS%;%EXAMPLES%

@rem system properties for https protocol implementation
set TRUSTSTORE="%CERT_HOME%\DummyServerTrustFile.jks"
set PROPS=-Djava.protocol.handler.pkgs=com.ibm.net.ssl.internal.www.protocol -Djavax.net.ssl.trustStore=%TRUSTSTORE% -Djavax.net.ssl.trustStorePassword=WebAS

%JAVA% -classpath %CLASSPATH% %PROPS% examples.serviceprovider.dsml2.DSML2DirContextTest %1 %2

