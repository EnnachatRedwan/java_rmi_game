package client;

import server.GameInterface;
import server.GuessResult;

import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.UUID;
import javax.swing.*;
import javax.swing.border.*;

public class GameClient extends UnicastRemoteObject implements ClientCallbackInterface {
    private static final long serialVersionUID = 1L;

    
    private String clientId;
    private GameInterface server;
    private int score;
    private int trials;

    
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JLabel trialsLabel;
    private JTextArea logArea;
    private JTextField guessField;
    private JSlider guessSlider;
    private JButton guessButton;
    private JButton buyTrialsButton;
    private JSpinner trialsSpinner;
    private JPanel gamePanel;
    private JPanel historyPanel;
    private JPanel connectPanel;
    private JTextField serverField;
    private JButton connectButton;

    public GameClient() throws RemoteException {
        this.clientId = UUID.randomUUID().toString().substring(0, 8);
        initializeGUI();
    }

    @Override
    public String getClientId() throws RemoteException {
        return clientId;
    }

    @Override
    public void notifyNumberReset(String winnerMessage) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            logArea.append("\n[SERVER NOTIFICATION] " + winnerMessage + "\n");
            JOptionPane.showMessageDialog(mainFrame, winnerMessage, "Game Reset", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void initializeGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        mainFrame = new JFrame("Number Guessing Game - Client ID: " + clientId);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);

        
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        
        JPanel statusPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statusPanel.setBorder(new CompoundBorder(
                new TitledBorder("Game Status"),
                new EmptyBorder(5, 5, 5, 5)
        ));

        statusLabel = new JLabel("Not connected", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.RED);

        scoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));

        trialsLabel = new JLabel("Trials: 0", JLabel.CENTER);
        trialsLabel.setFont(new Font("Arial", Font.BOLD, 16));

        statusPanel.add(statusLabel);
        statusPanel.add(scoreLabel);
        statusPanel.add(trialsLabel);

        
        JTabbedPane tabbedPane = new JTabbedPane();

        
        gamePanel = new JPanel(new BorderLayout(10, 10));
        gamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        
        JPanel guessPanel = new JPanel(new BorderLayout(10, 10));
        guessPanel.setBorder(new CompoundBorder(
                new TitledBorder("Make a Guess"),
                new EmptyBorder(10, 10, 10, 10)
        ));

        guessSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 50);
        guessSlider.setMajorTickSpacing(10);
        guessSlider.setMinorTickSpacing(1);
        guessSlider.setPaintTicks(true);
        guessSlider.setPaintLabels(true);

        guessField = new JTextField("50", 5);
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setFont(new Font("Arial", Font.BOLD, 18));

        
        guessSlider.addChangeListener(e -> {
            guessField.setText(String.valueOf(guessSlider.getValue()));
        });

        guessField.addActionListener(e -> {
            try {
                int value = Integer.parseInt(guessField.getText());
                if (value >= 1 && value <= 100) {
                    guessSlider.setValue(value);
                }
            } catch (NumberFormatException ex) {
                
            }
        });

        guessButton = new JButton("Make Guess");
        guessButton.setFont(new Font("Arial", Font.BOLD, 14));
        guessButton.setEnabled(false);
        guessButton.addActionListener(e -> makeGuess());

        JPanel guessControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        guessControlPanel.add(new JLabel("Your guess: "));
        guessControlPanel.add(guessField);
        guessControlPanel.add(guessButton);

        guessPanel.add(guessSlider, BorderLayout.CENTER);
        guessPanel.add(guessControlPanel, BorderLayout.SOUTH);

        
        JPanel buyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buyPanel.setBorder(new CompoundBorder(
                new TitledBorder("Buy Trials"),
                new EmptyBorder(5, 5, 5, 5)
        ));

        trialsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JLabel costLabel = new JLabel("Cost: 10 points");

        trialsSpinner.addChangeListener(e -> {
            int numTrials = (Integer) trialsSpinner.getValue();
            costLabel.setText("Cost: " + (numTrials * 10) + " points");
        });

        buyTrialsButton = new JButton("Buy Trials");
        buyTrialsButton.setEnabled(false);
        buyTrialsButton.addActionListener(e -> buyTrials());

        buyPanel.add(new JLabel("Number of trials: "));
        buyPanel.add(trialsSpinner);
        buyPanel.add(costLabel);
        buyPanel.add(buyTrialsButton);

        gamePanel.add(guessPanel, BorderLayout.CENTER);
        gamePanel.add(buyPanel, BorderLayout.SOUTH);

        
        historyPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        
        connectPanel = new JPanel(new BorderLayout(10, 10));
        connectPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel serverInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        serverField = new JTextField("localhost", 15);
        connectButton = new JButton("Connect to Server");

        connectButton.addActionListener(e -> connectToServer());

        serverInputPanel.add(new JLabel("Server address: "));
        serverInputPanel.add(serverField);
        serverInputPanel.add(connectButton);

        JTextArea instructionsArea = new JTextArea();
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setText("Game Instructions:\n\n" +
                "1. Connect to the server\n" +
                "2. You start with 100 points\n" +
                "3. Buy trials (10 points each)\n" +
                "4. Make guesses to find the number between 1-100\n" +
                "5. Each guess costs 1 trial\n\n" +
                "Scoring:\n" +
                "- If your guess is within 5 of the number: +20 points\n" +
                "- If your guess is within 20 of the number: +10 points\n" +
                "- If your guess is more than 20 away: -10 points\n\n" +
                "When you win, you get a refund for unused trials and a new number is generated!");

        JScrollPane instructionsScrollPane = new JScrollPane(instructionsArea);

        connectPanel.add(serverInputPanel, BorderLayout.NORTH);
        connectPanel.add(instructionsScrollPane, BorderLayout.CENTER);

        
        tabbedPane.addTab("Connect", connectPanel);
        tabbedPane.addTab("Game", gamePanel);
        tabbedPane.addTab("History", historyPanel);

        
        mainPanel.add(statusPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        
        mainFrame.setContentPane(mainPanel);
        mainFrame.setVisible(true);
    }

    private void connectToServer() {
        String host = serverField.getText().trim();

        try {
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            server = (GameInterface) registry.lookup("GuessingGame");

            
            score = server.registerClient(this);
            trials = 0;

            
            statusLabel.setText("Connected");
            statusLabel.setForeground(new Color(0, 128, 0));
            scoreLabel.setText("Score: " + score);
            trialsLabel.setText("Trials: " + trials);

            
            buyTrialsButton.setEnabled(true);
            guessButton.setEnabled(trials > 0);

            
            logArea.append("Connected to server at " + host + "\n");
            logArea.append("Initial score: " + score + " points\n");

            
            ((JTabbedPane) gamePanel.getParent()).setSelectedComponent(gamePanel);

        } catch (Exception e) {
            logArea.append("Failed to connect: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(mainFrame,
                    "Failed to connect to server: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buyTrials() {
        int numberOfTrials = (Integer) trialsSpinner.getValue();

        try {
            int newScore = server.buyTrials(this, numberOfTrials);
            trials += numberOfTrials;
            score = newScore;

            
            scoreLabel.setText("Score: " + score);
            trialsLabel.setText("Trials: " + trials);
            guessButton.setEnabled(true);

            
            logArea.append("Bought " + numberOfTrials + " trials for " + (numberOfTrials * 10) + " points\n");
            logArea.append("Current score: " + score + ", Available trials: " + trials + "\n");

        } catch (RemoteException e) {
            logArea.append("Error buying trials: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(mainFrame,
                    "Error buying trials: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeGuess() {
        if (trials <= 0) {
            JOptionPane.showMessageDialog(mainFrame,
                    "No trials left. Please buy more trials.",
                    "No Trials",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int guess = Integer.parseInt(guessField.getText());

            if (guess < 1 || guess > 100) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Please enter a number between 1 and 100.",
                        "Invalid Guess",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            GuessResult result = server.makeGuess(this, guess);

            
            score = result.getCurrentScore();
            trials = result.getRemainingTrials();
            scoreLabel.setText("Score: " + score);
            trialsLabel.setText("Trials: " + trials);
            guessButton.setEnabled(trials > 0);

            
            logArea.append("\n----- Guess: " + guess + " -----\n");
            logArea.append(result.getMessage() + "\n");

            String resultMessage = result.getMessage();
            Icon icon = null;
            String title = "";

            if (result.getStatus() == GuessResult.Status.CORRECT) {
                
                title = "You Won!";
                icon = UIManager.getIcon("OptionPane.informationIcon");
                logArea.append("You won! Refund: " + result.getScoreChange() + " points\n");

                
                showWinAnimation();
            } else if (result.getStatus() == GuessResult.Status.ERROR) {
                
                title = "Error";
                icon = UIManager.getIcon("OptionPane.errorIcon");
            } else {
                
                String direction = (result.getStatus() == GuessResult.Status.TOO_SMALL) ?
                        "Your guess is too small" : "Your guess is too big";
                logArea.append(direction + "\n");

                String scoreChangeText = (result.getScoreChange() >= 0 ? "+" : "") +
                        result.getScoreChange() + " points";
                logArea.append("Score change: " + scoreChangeText + "\n");

                title = "Guess Result";
                if (result.getScoreChange() > 0) {
                    icon = UIManager.getIcon("OptionPane.informationIcon");
                } else {
                    icon = UIManager.getIcon("OptionPane.warningIcon");
                }
            }

            logArea.append("Current score: " + score + ", Remaining trials: " + trials + "\n");

            
            JOptionPane.showMessageDialog(mainFrame, resultMessage, title, JOptionPane.PLAIN_MESSAGE, icon);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Please enter a valid number.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (RemoteException e) {
            logArea.append("Error making guess: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(mainFrame,
                    "Error making guess: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showWinAnimation() {
        JDialog winDialog = new JDialog(mainFrame, "Bravoooooo hahaha!", true);
        winDialog.setSize(400, 300);
        winDialog.setLocationRelativeTo(mainFrame);

        JPanel animationPanel = new JPanel() {
            private final int MAX_PARTICLES = 100;
            private final int[] x = new int[MAX_PARTICLES];
            private final int[] y = new int[MAX_PARTICLES];
            private final int[] speed = new int[MAX_PARTICLES];
            private final Color[] colors = new Color[MAX_PARTICLES];
            private Timer timer;
            private int count = 0;

            {
                Random random = new Random();
                for (int i = 0; i < MAX_PARTICLES; i++) {
                    x[i] = random.nextInt(400);
                    y[i] = random.nextInt(300);
                    speed[i] = 2 + random.nextInt(5);
                    colors[i] = new Color(
                            random.nextInt(256),
                            random.nextInt(256),
                            random.nextInt(256)
                    );
                }

                timer = new Timer(50, e -> {
                    count++;
                    for (int i = 0; i < MAX_PARTICLES; i++) {
                        y[i] += speed[i];
                        if (y[i] > 300) {
                            y[i] = 0;
                            x[i] = random.nextInt(400);
                        }
                    }
                    repaint();
                    if (count > 60) {
                        timer.stop();
                        winDialog.dispose();
                    }
                });
                timer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                
                for (int i = 0; i < MAX_PARTICLES; i++) {
                    g.setColor(colors[i]);
                    g.fillRect(x[i], y[i], 5, 5);
                }

                
                g.setColor(Color.BLACK);
                Font font = new Font("Arial", Font.BOLD, 24);
                g.setFont(font);
                FontMetrics metrics = g.getFontMetrics(font);
                String text = "Bravoooooo hahaha!";
                int x = (getWidth() - metrics.stringWidth(text)) / 2;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g.drawString(text, x, y);

                
                font = new Font("Arial", Font.PLAIN, 16);
                g.setFont(font);
                metrics = g.getFontMetrics(font);
                text = "You've won the game!";
                x = (getWidth() - metrics.stringWidth(text)) / 2;
                y += 30;
                g.drawString(text, x, y);
            }
        };

        winDialog.add(animationPanel);
        winDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new GameClient();
            } catch (Exception e) {
                System.err.println("Client exception: " + e.toString());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error creating client: " + e.getMessage(),
                        "Client Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}