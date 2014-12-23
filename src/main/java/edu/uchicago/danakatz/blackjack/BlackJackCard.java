package edu.uchicago.danakatz.blackjack;

import javax.swing.*;
import java.awt.*;

/**
 * this game uses a six-deck shoe, 
 * so each card may be played up to
 * six times.
 */
public class BlackJackCard extends ImageIcon {
    private int timesPlayed;
    private int value;

    public BlackJackCard(Image img, int n) {
        super(img);
        timesPlayed = 0;
        value = n;
    }

    public int getValue() {
        return value;
    }

    public boolean leftInShoe() {
        return timesPlayed < 6;
    }

    public void setPlayed() {
        timesPlayed++;
    }

    public void resetPlayed() {
        timesPlayed = 0;
    }
}
