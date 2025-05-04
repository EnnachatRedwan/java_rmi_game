package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallbackInterface extends Remote {
    
    void notifyNumberReset(String winnerMessage) throws RemoteException;

    
    String getClientId() throws RemoteException;
}