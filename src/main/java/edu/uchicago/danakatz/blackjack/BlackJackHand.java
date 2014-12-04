package edu.uchicago.danakatz.blackjack;

import java.util.ArrayList;

/**
 * Created by danakatz on 11/13/14.
 */
public class BlackJackHand {

    private ArrayList<BlackJackCard> hand;
    private boolean isSoft;

    public static int ACE_HIGH = 11;
    public static int ACE_LOW = 1;

    public BlackJackHand() {
        hand = new ArrayList<BlackJackCard>();
        isSoft = false;
    }

    public void addCard(BlackJackCard card) {
        hand.add(card);
        if(card.getValue() == ACE_LOW) {
            isSoft = true;
        }
    }

    public boolean isSoft() {
        return isSoft;
    }

    public int getTotal(int aceValue) {
        int total = 0;
        if(aceValue == ACE_HIGH) {
            for(BlackJackCard card : hand) {
                int v = card.getValue();
                if(v == ACE_LOW) {
                    total += ACE_HIGH;
                } else {
                    total += v;
                }
            }
        } else {
            total = getTotal();
        }

        return total;
    }

    public int getTotal() {
        int total = 0;

        for(BlackJackCard card : hand) {
            total += card.getValue();
        }

        return total;
    }

    public String showTotal() {
        int low = 0;
        int high = 0;

        for(BlackJackCard card : hand) {
            if(card.getValue() == ACE_LOW) {
                low += ACE_LOW;
                high += ACE_HIGH;
            } else {
                low += card.getValue();
                high += card.getValue();
            }
        }
        if (high > 21) {
            return Integer.toString(low);
        }

        return low + "/" + high;
    }

    public BlackJackCard[] getCards() {
        BlackJackCard[] handArray = new BlackJackCard[hand.size()];
        for(int i = 0; i < hand.size(); i++) {
            handArray[i] = hand.get(i);
        }
        return handArray;
    }

    public void clear() {
        hand.clear();
        isSoft = false;
    }
}
