// (C) Copyright 2003 by Enrico Liboni (enrico(at)computer.org)
// This piece of code is distributed under the GNU Public Licence
// http://www.gnu.org/licenses/gpl.txt
//

// To compile:
//  javac -classpath jcifs-0.7.3.jar ValidateNTLogin.java
// To execute:
//  java -classpath jcifs-0.7.3.jar:. ValidateNTLogin Domain Username Password DomainController
//
// Usage in other classes:
//     ValidateNTLogin vnl = new ValidateNTLogin();
//     vnl.setDomainController(domainCtrlAddr);
//     if (vnl.isValidNTLogin(domain,user,pass)) {
//	System.err.println("Authentication Succesful");
//     } else {
//	System.err.println("Authentication Error");
//     }
//

package art.utils;

import jcifs.UniAddress;
import jcifs.smb.SmbSession;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;


/**
 * Class to enable NTLM authentication
 * 
 * @author Enrico Liboni
 */
public class ValidateNTLogin {

	UniAddress domainController;

    /**
     * 
     */
    public ValidateNTLogin() {

	}

    /**
     * 
     * @param ntdomain
     * @param user
     * @param pass
     * @return <code>true</code> if successfully authenticated
     */
    public synchronized boolean isValidNTLogin(String  ntdomain
	,String  user
	,String  pass) {
	
		NtlmPasswordAuthentication mycreds = new NtlmPasswordAuthentication( ntdomain, user, pass );
		try {
			//jcifs.smb.Session.logon()
			SmbSession.logon( domainController, mycreds );
			// SUCCESS
			return true;
		} catch( SmbAuthException sae ) {
			// AUTHENTICATION FAILURE
			return false;
		} catch( SmbException se ) {
			// NETWORK PROBLEMS?
			se.printStackTrace();
			return false;
		} catch(Exception s) {
			s.printStackTrace();
			return false;
		}
	}

    /**
     * 
     * @param domainCtrl
     * @return <code>true</code> if successful
     */
    public boolean setDomainController(String  domainCtrl) {
		try {
			domainController = UniAddress.getByName( domainCtrl );
			return true;
		} catch(Exception s) {
			s.printStackTrace();
			return false;
		}

	}

    /**
     * 
     * @param args
     */
    public static void main(String args[]) {
		ValidateNTLogin vnl = new ValidateNTLogin();
		if (args.length != 4) {
			System.err.println("Usage: java -classpath jcifs.jar:. ValidateNTLogin Domain Username Password DomainController");
			return;
		}

		if (vnl.setDomainController(args[3])
				&&   vnl.isValidNTLogin(args[0],args[1],args[2])) {
			System.err.println(args[0]+"\\"+args[1]+" authenticated succesfully");
		} else {
			System.err.println("Invalid credentials or Domain Controller");
		}
	}
}
