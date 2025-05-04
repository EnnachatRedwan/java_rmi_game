package server;

import client.ClientCallbackInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameServer implements GameInterface {
    private int randomNumber;
    private Random random;
    private Map<String, ClientInfo> clients;

    
    private static class ClientInfo {
        int score;
        int trials;
        ClientCallbackInterface clientCallback;

        public ClientInfo(ClientCallbackInterface clientCallback) {
            this.score = 100; 
            this.trials = 0;
            this.clientCallback = clientCallback;
        }
    }

    public GameServer() {
        this.random = new Random();
        this.clients = new HashMap<>();
        generateNewRandomNumber();
    }

    private void generateNewRandomNumber() {
        this.randomNumber = random.nextInt(100) + 1; 
        System.out.println("New random number generated: " + randomNumber);
    }

    @Override
    public int registerClient(ClientCallbackInterface client) throws RemoteException {
        String clientId = client.getClientId();
        ClientInfo clientInfo = new ClientInfo(client);
        clients.put(clientId, clientInfo);
        System.out.println("New client registered: " + clientId);
        return clientInfo.score;
    }

    @Override
    public int buyTrials(ClientCallbackInterface client, int numberOfTrials) throws RemoteException {
        String clientId = client.getClientId();
        ClientInfo clientInfo = clients.get(clientId);

        if (clientInfo == null) {
            throw new RemoteException("Client not registered");
        }

        int cost = numberOfTrials * 10;

        if (clientInfo.score < cost) {
            throw new RemoteException("Not enough points to buy trials");
        }

        clientInfo.score -= cost;
        clientInfo.trials += numberOfTrials;

        System.out.println("Client " + clientId + " bought " + numberOfTrials +
                " trials. New score: " + clientInfo.score +
                ", Trials: " + clientInfo.trials);

        return clientInfo.score;
    }

    @Override
    public GuessResult makeGuess(ClientCallbackInterface client, int guess) throws RemoteException {
        String clientId = client.getClientId();
        ClientInfo clientInfo = clients.get(clientId);

        if (clientInfo == null) {
            throw new RemoteException("Client not registered");
        }

        if (clientInfo.trials <= 0) {
            return new GuessResult(GuessResult.Status.ERROR, 0, clientInfo.score, 0,
                    "No trials left. Please buy more trials.");
        }

        
        clientInfo.trials--;

        System.out.println("Client " + clientId + " guessed: " + guess +
                ". Actual number: " + randomNumber);

        
        if (guess == randomNumber) {
            
            int refund = clientInfo.trials * 10;
            clientInfo.score += refund;
            clientInfo.trials = 0;

            
            generateNewRandomNumber();

            
            String winMessage = "Client " + clientId + " won! A new number has been generated.";
            notifyAllClients(winMessage);

            return new GuessResult(GuessResult.Status.CORRECT, refund, clientInfo.score, 0,
                    "Bravoooooo hahaha! You've won! Points refunded: " + refund);
        }

        
        int difference = Math.abs(guess - randomNumber);
        int scoreChange = 0;
        String message;
        GuessResult.Status status;

        if (guess < randomNumber) {
            status = GuessResult.Status.TOO_SMALL;
            message = "Too small! ";
        } else {
            status = GuessResult.Status.TOO_BIG;
            message = "Too big! ";
        }

        
        if (difference <= 5) {
            scoreChange = 20;
            message += "Very close!";
        } else if (difference <= 20) {
            scoreChange = 10;
            message += "Getting closer!";
        } else {
            scoreChange = -10;
            message += "Far off!";
        }

        
        clientInfo.score += scoreChange;

        return new GuessResult(status, scoreChange, clientInfo.score, clientInfo.trials, message);
    }

    @Override
    public int getScore(ClientCallbackInterface client) throws RemoteException {
        String clientId = client.getClientId();
        ClientInfo clientInfo = clients.get(clientId);

        if (clientInfo == null) {
            throw new RemoteException("Client not registered");
        }

        return clientInfo.score;
    }

    private void notifyAllClients(String message) {
        for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
            try {
                entry.getValue().clientCallback.notifyNumberReset(message);
            } catch (RemoteException e) {
                System.err.println("Failed to notify client " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            
            GameServer server = new GameServer();

            
            GameInterface stub = (GameInterface) UnicastRemoteObject.exportObject(server, 0);

            
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("GuessingGame", stub);

            System.out.println("Game server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}