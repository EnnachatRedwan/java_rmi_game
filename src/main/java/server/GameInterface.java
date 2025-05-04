package server;

import client.ClientCallbackInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameInterface extends Remote {
    
    int registerClient(ClientCallbackInterface client) throws RemoteException;

    
    int buyTrials(ClientCallbackInterface client, int numberOfTrials) throws RemoteException;

    
    GuessResult makeGuess(ClientCallbackInterface client, int guess) throws RemoteException;

    
    int getScore(ClientCallbackInterface client) throws RemoteException;
}