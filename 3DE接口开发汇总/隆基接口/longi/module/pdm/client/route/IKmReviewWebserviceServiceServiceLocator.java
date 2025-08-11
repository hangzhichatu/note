/**
 * IKmReviewWebserviceServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package longi.module.pdm.client.route;

import longi.common.util.LONGiPropertiesUtil;

public class IKmReviewWebserviceServiceServiceLocator extends org.apache.axis.client.Service implements longi.module.pdm.client.route.IKmReviewWebserviceServiceService {

    public IKmReviewWebserviceServiceServiceLocator() {
    }


    public IKmReviewWebserviceServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public IKmReviewWebserviceServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for IKmReviewWebserviceServicePort
//    private java.lang.String IKmReviewWebserviceServicePort_address = "http://portaltest.longi-silicon.com:8885/sys/webservice/kmReviewWebserviceService";
    private java.lang.String IKmReviewWebserviceServicePort_address = LONGiPropertiesUtil.readInterfaceProperties("IKmReviewWebserviceServicePort_address");
    
    public java.lang.String getIKmReviewWebserviceServicePortAddress() {
        return IKmReviewWebserviceServicePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String IKmReviewWebserviceServicePortWSDDServiceName = "IKmReviewWebserviceServicePort";

    public java.lang.String getIKmReviewWebserviceServicePortWSDDServiceName() {
        return IKmReviewWebserviceServicePortWSDDServiceName;
    }

    public void setIKmReviewWebserviceServicePortWSDDServiceName(java.lang.String name) {
        IKmReviewWebserviceServicePortWSDDServiceName = name;
    }

    public longi.module.pdm.client.route.IKmReviewWebserviceService getIKmReviewWebserviceServicePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
           endpoint = new java.net.URL(IKmReviewWebserviceServicePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getIKmReviewWebserviceServicePort(endpoint);
    }

    public longi.module.pdm.client.route.IKmReviewWebserviceService getIKmReviewWebserviceServicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            longi.module.pdm.client.route.IKmReviewWebserviceServiceServiceSoapBindingStub _stub = new longi.module.pdm.client.route.IKmReviewWebserviceServiceServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getIKmReviewWebserviceServicePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setIKmReviewWebserviceServicePortEndpointAddress(java.lang.String address) {
    	IKmReviewWebserviceServicePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (longi.module.pdm.client.route.IKmReviewWebserviceService.class.isAssignableFrom(serviceEndpointInterface)) {
                longi.module.pdm.client.route.IKmReviewWebserviceServiceServiceSoapBindingStub _stub = new longi.module.pdm.client.route.IKmReviewWebserviceServiceServiceSoapBindingStub(new java.net.URL(IKmReviewWebserviceServicePort_address), this);
                _stub.setPortName(getIKmReviewWebserviceServicePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("IKmReviewWebserviceServicePort".equals(inputPortName)) {
            return getIKmReviewWebserviceServicePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://webservice.review.km.kmss.landray.com/", "IKmReviewWebserviceServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://webservice.review.km.kmss.landray.com/", "IKmReviewWebserviceServicePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("IKmReviewWebserviceServicePort".equals(portName)) {
            setIKmReviewWebserviceServicePortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
