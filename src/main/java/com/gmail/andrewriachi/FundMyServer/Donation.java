package com.gmail.andrewriachi.FundMyServer;

import java.time.LocalDate;
import java.util.UUID;

public class Donation {
    UUID player;
    LocalDate date;
    double amount;

    public Donation(UUID player, LocalDate date, double amount) {
        this.player = player;
        this.date = date;
        this.amount = amount;
    }

    public void register() {
        Term.getCurrentTerm().addDonation(this);
        Donor.getDonor(player).addDonation(this);
    }

}
