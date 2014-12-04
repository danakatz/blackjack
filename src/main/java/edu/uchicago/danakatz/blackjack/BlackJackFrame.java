package edu.uchicago.danakatz.blackjack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

/*
 * Card images : https://code.google.com/p/vector-playing-cards/
 * http://www.artofplay.com/products/bicycle-rider-back-playing-cards
 */

public class BlackJackFrame extends JFrame {

    private static final int FRAME_HEIGHT = 300;
    private static final int FRAME_WIDTH = 1400;
    private static final Color BACKGROUND_COLOR = new Color(30, 80, 50);

    private static final int CARD_HEIGHT = 170;
    private static final int NUM_CARDS = 52;
    private static final int DECKS_IN_SHOE = 6;

    private final int PLAYER = 1;
    private final int DEALER = -1;

    private ArrayList<BlackJackCard> deck;

    private double playerFunds;
    private JLabel playerFundsLabel;
    private JTextField bettingBox;
    private BlackJackHand playerHand;
    private JLabel playerTotalLabel;

    private BlackJackHand dealerHand;
    private JLabel dealerTotalLabel;

    private boolean revealHole;
    private boolean dealerTurn;
    private boolean firstDealer;

    private JPanel actionPanel;
    private JPanel dealerPanel;
    private JPanel playerPanel;
    private Container infoPanel;
    private JButton playAgainButton;
    private JButton hitButton;
    private JButton standButton;
    private JButton doubleDownButton;
    private JButton surrenderButton;

    private Random generate;
    private int cardsPlayed;
    private Timer t;

    public BlackJackFrame() {
        deck = new ArrayList<BlackJackCard>();
        initializeDeck();
        cardsPlayed = 0;
        playerFunds = 1000;
        playerHand = new BlackJackHand();
        dealerHand = new BlackJackHand();
        generate = new Random();
        t = new Timer(1000, new DealerTurn());
        t.start();

        actionPanel = new JPanel();
        actionPanel.setBackground(BACKGROUND_COLOR);
        dealerPanel = new JPanel();
        dealerPanel.setBackground(BACKGROUND_COLOR);
        infoPanel = Box.createVerticalBox();
        infoPanel.setBackground(BACKGROUND_COLOR);
        playerPanel = new JPanel();
        playerPanel.setBackground(BACKGROUND_COLOR);

        playerTotalLabel = new JLabel("", JLabel.CENTER);
        playerTotalLabel.setForeground(Color.WHITE);
        dealerTotalLabel = new JLabel("", JLabel.CENTER);
        dealerTotalLabel.setForeground(Color.WHITE);

        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        Container framePanel = Box.createVerticalBox();
        framePanel.setBackground(BACKGROUND_COLOR);
        framePanel.add(actionPanel);
        add(framePanel);

        startNewGame();
    }

    private void startNewGame() {
        playerHand.clear();
        dealerHand.clear();
        revealHole = false;
        dealerTurn = false;
        firstDealer = true;

        setPlayerPanel();
        setDealerPanel();
        setInfoPanel();
        resetActionPanel();

        playAgainButton.setEnabled(false);
        disableButtons();
    }

    private void dealNextCard(int who) {
        if(cardsPlayed < NUM_CARDS * DECKS_IN_SHOE) { // if cards left in shoe
            int next = generate.nextInt(NUM_CARDS);
            while(!deck.get(next).leftInShoe()) {
                next = generate.nextInt(NUM_CARDS);
            }
            BlackJackCard temp = deck.get(next);
            temp.setPlayed();
            cardsPlayed++;

            if(who == DEALER) {
                dealerHand.addCard(temp);
            } else if(who == PLAYER) {
                playerHand.addCard(temp);
            }
        } else { // reset shoe
            cardsPlayed = 0;
            for(BlackJackCard card : deck) {
                card.resetPlayed();
            }
            dealNextCard(who);
        }
    }

    private void resetActionPanel() {
        Component[] comps = actionPanel.getComponents();
        for(Component c : comps) {
            actionPanel.remove(c);
        }

        actionPanel.repaint();

        actionPanel.add(dealerPanel);
        actionPanel.add(infoPanel);
        actionPanel.add(playerPanel);

        updateLabels();
    }



    private void setPlayerPanel() {
        Component[] comps = playerPanel.getComponents();
        for(Component c : comps) {
            playerPanel.remove(c);
        }

        playerPanel.repaint();

        playerPanel.setLayout(new BorderLayout());
        if(playerHand.getCards().length > 0) {
            JLabel playerLabel = new JLabel("PLAYER", JLabel.CENTER);
            playerLabel.setForeground(Color.WHITE);
            playerPanel.add(playerLabel, BorderLayout.NORTH);
        }
        playerPanel.add(displayCards(PLAYER));
        playerPanel.add(playerTotalLabel, BorderLayout.SOUTH);

        updateLabels();
    }

    private void setInfoPanel() {
        Component[] comps = infoPanel.getComponents();
        for(Component c : comps) {
            infoPanel.remove(c);
        }

        infoPanel.repaint();

        JPanel betPanel = new JPanel();
        betPanel.setBackground(BACKGROUND_COLOR);
        bettingBox = new JTextField(8);
        bettingBox.setEditable(true);
        JLabel betLabel = new JLabel("Bet: $");
        betLabel.setForeground(Color.WHITE);
        betPanel.add(betLabel);
        betPanel.add(bettingBox);
        infoPanel.add(betPanel);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setLayout(new GridLayout(0, 1));
        playerFundsLabel = new JLabel(String.format("Funds: $%.2f", playerFunds), JLabel.CENTER);
        playerFundsLabel.setForeground(Color.WHITE);
        buttonPanel.add(playerFundsLabel);

        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        doubleDownButton = new JButton("Double down");
        surrenderButton = new JButton("Surrender");
        playAgainButton = new JButton("New Hand");

        hitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                surrenderButton.setEnabled(false);
                dealNextCard(PLAYER);
                setPlayerPanel();
                if(playerBust()) {
                    bust();
                }
            }
        });

        standButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableButtons();
                dealerTurn = true;
            }
        });

        doubleDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double betAmt = Double.parseDouble(bettingBox.getText());
                    bettingBox.setText(String.format("%.2f", betAmt * 2));
                } catch(Exception x) {
                    bettingBox.setText("");
                }

                disableButtons();

                dealNextCard(PLAYER);
                setPlayerPanel();
                if(playerBust()) {
                    bust();
                } else {
                    dealerTurn = true;
                }
            }
        });

        surrenderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                revealHole = true;
                try {
                    double betAmt = Double.parseDouble(bettingBox.getText());
                    betAmt = betAmt / 2.0;
                    playerFunds -= betAmt;
                } catch(Exception x) {

                }
                setPlayerPanel();
                setDealerPanel();
                disableButtons();
                playAgainButton.setEnabled(true);
            }
        });

        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });

        final JButton dealButton = new JButton("Deal");
        dealButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dealNextCard(PLAYER);
                dealNextCard(PLAYER);

                dealNextCard(DEALER);
                dealNextCard(DEALER);

                setDealerPanel();
                setPlayerPanel();
                updateLabels();

                bettingBox.setEditable(false);
                enableButtons();
                dealButton.setEnabled(false);
            }
        });

        buttonPanel.add(dealButton);
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);
        buttonPanel.add(doubleDownButton);
        buttonPanel.add(surrenderButton);
        buttonPanel.add(playAgainButton);
        infoPanel.add(buttonPanel);
    }

    private void setDealerPanel() {
        Component[] comps = dealerPanel.getComponents();
        for(Component c : comps) {
            dealerPanel.remove(c);
        }

        dealerPanel.repaint();

        dealerPanel.setLayout(new BorderLayout());
        if(dealerHand.getCards().length > 0) {
            JLabel dealerLabel = new JLabel("DEALER", JLabel.CENTER);
            dealerLabel.setForeground(Color.WHITE);
            dealerPanel.add(dealerLabel, BorderLayout.NORTH);
        }
        displayCards(DEALER);
        dealerPanel.add(displayCards(DEALER));
        dealerPanel.add(dealerTotalLabel, BorderLayout.SOUTH);

        updateLabels();
    }

    private void disableButtons() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleDownButton.setEnabled(false);
        surrenderButton.setEnabled(false);
    }

    private void enableButtons() {
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
        doubleDownButton.setEnabled(true);
        surrenderButton.setEnabled(true);
    }

    private JPanel displayCards(int who) {
        BlackJackCard[] tempHand;
        if(who == PLAYER) {
            tempHand = playerHand.getCards();
        } else {
            tempHand = dealerHand.getCards();
        }

        JPanel tempPanel = new JPanel();
        tempPanel.setBackground(BACKGROUND_COLOR);
        tempPanel.setLayout(new GridLayout(1, 0));

        if(who == DEALER && !revealHole && tempHand.length > 0) { // show back of card until dealer goes
            JLabel cardLabel = new JLabel(tempHand[0]);
            JLabel cardBack = new JLabel(deck.get(deck.size() - 1));
            tempPanel.add(cardLabel);
            tempPanel.add(cardBack);
        } else {
            for(BlackJackCard card : tempHand) {
                JLabel cardLabel = new JLabel(card);
                tempPanel.add(cardLabel);
            }
        }

        return tempPanel;
    }

    private void updateLabels() {
        if(playerHand.getCards().length > 0) {
            if (playerHand.getTotal() == 21 || playerHand.getTotal(BlackJackHand.ACE_HIGH) == 21) {
                playerTotalLabel.setText("Total: BLACKJACK");
            } else if(playerHand.isSoft()) {
                playerTotalLabel.setText("Total: " + playerHand.showTotal());
            } else {
                playerTotalLabel.setText("Total: " + playerHand.getTotal());
            }

            if(!revealHole) {
                dealerTotalLabel.setText("Total: ?");
            } else {
                if(dealerHand.getTotal() == 21 || dealerHand.getTotal(BlackJackHand.ACE_HIGH) == 21) {
                    dealerTotalLabel.setText("Total: BLACKJACK");
                } else if(dealerHand.isSoft()) {
                    dealerTotalLabel.setText("Total: " + dealerHand.showTotal());
                } else {
                    dealerTotalLabel.setText("Total: " + dealerHand.getTotal());
                }
            }
        } else {
            playerTotalLabel.setText("");
            dealerTotalLabel.setText("");
        }
    }

    private boolean playerBlackJack() {
        if(playerHand.getTotal() == 21) {
            return true;
        } else if(playerHand.isSoft() && playerHand.getTotal(BlackJackHand.ACE_HIGH) == 21) {
            return true;
        }

        return false;
    }

    private boolean playerBust() {
        return playerHand.getTotal() > 21;
    }

    private boolean dealerBlackJack() {
        if(dealerHand.getTotal() == 21) {
            return true;
        } else if(dealerHand.isSoft() && dealerHand.getTotal(BlackJackHand.ACE_HIGH) == 21) {
            return true;
        }

        return false;
    }

    private boolean dealerBust() {
        return dealerHand.getTotal() > 21;
    }

    private void bust() {
        revealHole = true;
        disableButtons();
        setDealerPanel();
        playerTotalLabel.setText("Total: " + playerHand.getTotal() + " BUST");
        playAgainButton.setEnabled(true);
        try {
            double betAmt = Double.parseDouble(bettingBox.getText());
            playerFunds -= betAmt;
            playerFundsLabel.setText(String.format("Funds: $%.2f", playerFunds));
        } catch(Exception x) {
            bettingBox.setText("");
        }
    }

    private void displayResult() {
        dealerTurn = false;
        firstDealer = true;
        playAgainButton.setEnabled(true);
        if(dealerBust()) {
            dealerTotalLabel.setText("Total: " + dealerHand.getTotal() + " BUST");
            if(playerBlackJack()) {
                playerTotalLabel.setText("Total: BLACKJACK WIN");
            } else if(playerHand.isSoft()) {
                playerTotalLabel.setText("Total: " + playerHand.showTotal() + " WIN");
            } else {
                playerTotalLabel.setText("Total: " + playerHand.getTotal() + " WIN");
            }
            playerWin();
        } else if(dealerBlackJack()) {
            if(playerBlackJack()) {
                dealerTotalLabel.setText("Total: BLACKJACK PUSH");
                playerTotalLabel.setText("Total: BLACKJACK PUSH");
            } else {
                dealerTotalLabel.setText("Total: BLACKJACK WIN");
                dealerWin();
            }
        } else  {
            if(playerBlackJack()) {
                playerTotalLabel.setText("Total: BLACKJACK WIN");
                playerWin();
            } else if(playerHand.isSoft() && playerHand.getTotal(BlackJackHand.ACE_HIGH) < 21) {
                if(dealerHand.getTotal() < playerHand.getTotal(BlackJackHand.ACE_HIGH)) {
                    playerTotalLabel.setText("Total: " + playerHand.showTotal() + " WIN");
                    playerWin();
                } else if(dealerHand.getTotal() > playerHand.getTotal(BlackJackHand.ACE_HIGH)) {
                    dealerTotalLabel.setText("Total: " + dealerHand.getTotal() + " WIN");
                    dealerWin();
                } else if(dealerHand.getTotal() == playerHand.getTotal(BlackJackHand.ACE_HIGH)) {
                    dealerTotalLabel.setText("Total: " + dealerHand.getTotal() + " PUSH");
                    playerTotalLabel.setText("Total: " + playerHand.showTotal() + " PUSH");
                }
            } else if(dealerHand.getTotal() < playerHand.getTotal()) {
                playerTotalLabel.setText("Total: " + playerHand.getTotal() + " WIN");
                playerWin();
            } else if(dealerHand.getTotal() > playerHand.getTotal()) {
                dealerTotalLabel.setText("Total: " + dealerHand.getTotal() + " WIN");
                dealerWin();
            } else if(dealerHand.getTotal() == playerHand.getTotal()) {
                dealerTotalLabel.setText("Total: " + dealerHand.getTotal() + " PUSH");
                playerTotalLabel.setText("Total: " + playerHand.getTotal() + " PUSH");
            }
        }
    }

    private void playerWin() {
        try {
            double betAmt = Double.parseDouble(bettingBox.getText());
            if(playerBlackJack()) {
                playerFunds += betAmt * 1.5;
            } else {
                playerFunds += betAmt;
            }
            playerFundsLabel.setText(String.format("Funds: $%.2f", playerFunds));
        } catch(Exception x) {
            bettingBox.setText("");
        }
    }

    private void dealerWin() {
        try {
            double betAmt = Double.parseDouble(bettingBox.getText());
            playerFunds -= betAmt;
            playerFundsLabel.setText(String.format("Funds: $%.2f", playerFunds));
        } catch(Exception x) {
            bettingBox.setText("");
        }
    }

    class DealerTurn implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(dealerTurn) {
                if(firstDealer) {
                    revealHole = true;
                    setDealerPanel();
                    firstDealer = false;
                } else if(dealerHand.getTotal() > 17 || dealerBlackJack() || dealerBust()) {
                    displayResult();
                } else if(dealerHand.getTotal() <= 17) {
                    dealNextCard(DEALER);
                    setDealerPanel();
                }
            }
        }
    }

    private void initializeDeck() {
        // String srcPath = System.getProperty("user.dir") + "/src/BlackJack/images/"; // to compile from IDE
        String srcPath = System.getProperty("user.dir") + "/edu/uchicago/danakatz/blackjack/images/"; // to compile from command line
        String[] numCards = {null, "ace", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        String[] faceCards = {"jack", "queen", "king"};
        String[] suits = {"clubs", "diamonds", "hearts", "spades"};

        // initialize ace - 10
        for(int i = 1; i < numCards.length; i++) {
            for(int j = 0; j < suits.length; j++) {
                deck.add(new BlackJackCard(resize(srcPath + numCards[i] + "_of_" + suits[j] + ".png"), i));
            }
        }

        // initialize face cards
        for(int i = 0; i < faceCards.length; i++) {
            for(int j = 0; j < suits.length; j++) {
                deck.add(new BlackJackCard(resize(srcPath + faceCards[i] + "_of_" + suits[j] + ".png"), 10));
            }
        }

        deck.add(new BlackJackCard(resize(srcPath + "blue-back_4_1024x1024.png"), 0));
    }

    private Image resize(String filename) {
        ImageIcon temp = new ImageIcon(filename);
        Image small = temp.getImage().getScaledInstance(-1, CARD_HEIGHT, Image.SCALE_SMOOTH);
        return small;
    }
}
