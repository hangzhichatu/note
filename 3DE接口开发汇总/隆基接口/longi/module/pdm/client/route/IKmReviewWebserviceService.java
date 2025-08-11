/**
 * IKmReviewWebserviceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package longi.module.pdm.client.route;

public interface IKmReviewWebserviceService extends java.rmi.Remote {
    public java.lang.String approveProcess(longi.module.pdm.client.route.KmReviewParamterForm arg0) throws java.rmi.RemoteException, longi.module.pdm.client.route.Exception;
    public java.lang.String updateReviewInfo(longi.module.pdm.client.route.KmReviewParamterForm arg0) throws java.rmi.RemoteException, longi.module.pdm.client.route.Exception;
    public java.lang.String addReview(longi.module.pdm.client.route.KmReviewParamterForm arg0) throws java.rmi.RemoteException, longi.module.pdm.client.route.Exception;
    public java.lang.String discardReviewInfo(longi.module.pdm.client.route.KmReviewParamterForm arg0) throws java.rmi.RemoteException, longi.module.pdm.client.route.Exception;
}
