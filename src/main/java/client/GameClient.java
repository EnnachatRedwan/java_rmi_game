package client;

import server.GameInterface;
import server.GuessResult;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.UUID;

public class GameClient extends UnicastRemoteObject implements ClientCallbackInterface {
    private static final long serialVersionUID = 1L;

    private String clientId;
    private GameInterface server;
    private int score;
    private int trials;

    public GameClient() throws RemoteException {
        this.clientId = UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public String getClientId() throws RemoteException {
        return clientId;
    }

    @Override
    public void notifyNumberReset(String winnerMessage) throws RemoteException {
        System.out.println("\n[SERVER NOTIFICATION] " + winnerMessage);
    }

    public void connectToServer(String host) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            server = (GameInterface) registry.lookup("GuessingGame");


            score = server.registerClient(this);
            trials = 0;

            System.out.println("Connected to the game server!");
            System.out.println("Your client ID: " + clientId);
            System.out.println("Initial score: " + score);

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void buyTrials(int numberOfTrials) {
        try {
            score = server.buyTrials(this, numberOfTrials);
            trials += numberOfTrials;
            System.out.println("Bought " + numberOfTrials + " trials for " + (numberOfTrials * 10) + " points.");
            System.out.println("Current score: " + score);
            System.out.println("Available trials: " + trials);
        } catch (RemoteException e) {
            System.err.println("Error buying trials: " + e.getMessage());
        }
    }

    public void makeGuess(int guess) {
        try {
            GuessResult result = server.makeGuess(this, guess);

            System.out.println("------------------------------");
            System.out.println("Guess: " + guess);
            System.out.println(result.getMessage());

            if (result.getStatus() == GuessResult.Status.CORRECT) {
                System.out.println("You won! The correct number was: " + guess);
                System.out.println("Refund: " + result.getScoreChange() + " points");
            } else if (result.getStatus() == GuessResult.Status.ERROR) {
                System.out.println("Error: " + result.getMessage());
            } else {
                String direction = (result.getStatus() == GuessResult.Status.TOO_SMALL) ?
                        "Your guess is too small" : "Your guess is too big";
                System.out.println(direction);
                System.out.println("Score change: " + (result.getScoreChange() >= 0 ? "+" : "") +
                        result.getScoreChange() + " points");
            }


            score = result.getCurrentScore();
            trials = result.getRemainingTrials();

            System.out.println("Current score: " + score);
            System.out.println("Remaining trials: " + trials);
            System.out.println("------------------------------");

        } catch (RemoteException e) {
            System.err.println("Error making guess: " + e.getMessage());
        }
    }

    public void play() {
        Scanner scanner = new Scanner(System.in);
        boolean playing = true;

        while (playing) {
            System.out.println("\n=== GAME MENU ===");
            System.out.println("1. Check score");
            System.out.println("2. Buy trials");
            System.out.println("3. Make a guess");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    try {
                        score = server.getScore(this);
                        System.out.println("Current score: " + score);
                        System.out.println("Available trials: " + trials);
                    } catch (RemoteException e) {
                        System.err.println("Error getting score: " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.print("How many trials do you want to buy? ");
                    int numberOfTrials = scanner.nextInt();
                    buyTrials(numberOfTrials);
                    break;

                case 3:
                    if (trials <= 0) {
                        System.out.println("You have no trials left. Please buy more trials.");
                    } else {
                        System.out.print("Enter your guess (1-100): ");
                        int guess = scanner.nextInt();
                        makeGuess(guess);
                    }
                    break;

                case 4:
                    System.out.println("Thank you for playing!");
                    playing = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        String host = "localhost";


        if (args.length > 0) {
            host = args[0];
        }

        try {
            GameClient client = new GameClient();
            client.connectToServer(host);
            client.play();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}