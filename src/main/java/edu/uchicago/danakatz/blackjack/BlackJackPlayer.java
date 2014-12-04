package edu.uchicago.danakatz.blackjack;

import javax.swing.*;

/**
 * must be compiled from IntelliJ.
 * to compile from command line,
 * uncomment the alternate srcPath
 * in the initializeDeck() method
 * of the BlackJackFrame class
 * (near the bottom of the .java file).
 */

public class BlackJackPlayer {
    public static void main(String[] args) {
        JFrame frame = new BlackJackFrame();
        frame.setTitle("BLACKJACK");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
