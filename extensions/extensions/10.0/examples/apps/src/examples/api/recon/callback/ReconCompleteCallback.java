/************************************************************************
* IBM Confidential
* OCO Source Materials
* *** IBM Security Identity Manager ***
*
* (C) Copyright IBM Corp. 2015  All Rights Reserved.
*
* The source code for this program is not published or otherwise  
* divested of its trade secrets, irrespective of what has been 
* deposited with the U.S. Copyright Office.
*************************************************************************/

package examples.api.recon.callback;

import com.ibm.itim.remoteservices.ejb.mediation.IReconCompleteCallback;


/**
 * The interface 'IReconCompleteCallback' allows the implementation class 
 * the ability to gain control after a reconciliation process has finished.
 *    The 'onReconComplete' method of this interface is invoked, by the
 * workflow engine, after the completion of reconciliation process.
 *
 * The interface may be implemented and an object of the implementation
 * class can be passed as a parameter to functions of 
 * com.ibm.itim.apps.recon.ReconManager, when adding a new reconciliation 
 * unit for a resource or when running reconciliation immediately, for
 * a given resource. This way the callback object gets registered with the
 * reconciliation process, and gains control, after process completion.
 *     This can be used to notify end users, the completion of
 * reconciliation or perform some clean up activities at the end of 
 * reconciliation process.
 *
**/
public class ReconCompleteCallback implements IReconCompleteCallback {

    public ReconCompleteCallback() {
        super();
    }

    /* This method is executed by ITIM workflow engine after the 
     * reconciliation process ; the callback object implementing this
     * interface was registered with, has completed.
     * 
     * @param processId Id of the reconciliation process the callback 
     *                   was registered with.
     * @param processState  State of the reconciliation process.
     * @param processResult Result of the reconciliation process.
     * @param resultSummary Result summary of the reconciliation process.
     */
    public void onReconComplete(long processId,  
                                String processState, 
                                String processResult, 
                                String resultSummary) {

        // Any user defined or custom defined code can be added here.
        
        System.out.println("process ID = " + processId);
        System.out.println("process state = " + processState);
        System.out.println("process result = " + processResult);
        System.out.println("process result summary = " + resultSummary);
    }
}
