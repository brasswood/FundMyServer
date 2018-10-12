package com.gmail.andrewriachi.FundMyServer;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

public class Term {

    private final LocalDate start, end;
    private final double goal;
    private ArrayList<Donation> donations = new ArrayList<>();
    private static Term currentTerm;

    private static final File currentTermFile = new File(FundMyServer.pluginFolder, "currentterm.yml");

    public Term(LocalDate start, LocalDate end, double goal) {
        this.start = start;
        this.end = end;
        this.goal = goal;
    }

    public static Term getCurrentTerm() {
        if (!currentTermFile.exists()) {
            FundMyServer.log.info("currentterm.yml does not exist, creating...");
            currentTerm = new Term(LocalDate.now(), LocalDate.now().plusMonths(1), 20);
            currentTerm.write(currentTermFile);
        } else if (currentTerm == null) {
            FundMyServer.log.info("Allocating the current term");
            currentTerm = getTerm(currentTermFile);
        }
        LocalDate today = LocalDate.now();
        if (today.isAfter(currentTerm.end) || currentTerm.getTotalRaised() >= currentTerm.goal) {
            Period delta = Period.ofMonths(1);
            LocalDate newStart = currentTerm.start.plus(delta);
            LocalDate newEnd = currentTerm.end.plus(delta);
            double goal = currentTerm.goal;
            try {
                File history = new File(FundMyServer.pluginFolder, "history");
                history.mkdir();
                currentTerm.write(new File(history, "term-" + currentTerm.end.toString()));
            } catch (Exception ex) {
                ex.printStackTrace();
                FundMyServer.log.warning("Could not create a history folder.");
            }
            currentTerm = new Term(newStart, newEnd, goal);
            currentTerm.write();
        }
        return currentTerm;
    }


    public static Term getTerm(File termFile) {
        YamlConfiguration conf = new YamlConfiguration();
        LocalDate start = null , end = null;
        double goal = 0;
        try {
            conf.load(termFile);
            FundMyServer.log.info(conf.getString("start-date"));
        } catch (Exception ex) {
            ex.printStackTrace();
            // FundMyServer.crash("Could not load the term.yml specified.");
        }

        try {
            start = LocalDate.parse(conf.getString("start-date"));
            end = LocalDate.parse(conf.getString("end-date"));
            goal = conf.getDouble("goal");
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            // FundMyServer.crash("Incorrect date format. Use yyyy-mm-dd");
        }
        Term newTerm = new Term(start, end, goal);
        MemorySection c = (MemorySection) conf.getConfigurationSection("donations");
        if (c == null) {c = (MemorySection) conf.createSection("donations");}
        for (int id = 0; id < c.getKeys(false).size(); id++) {
            c = (MemorySection) c.getConfigurationSection(Integer.toString(id));
            newTerm.donations.add(new Donation(UUID.fromString(c.getString("player")), LocalDate.parse(c.getString("date")), c.getDouble("amount")));
            c = (MemorySection) c.getParent();
        }
        return newTerm;
    }

    public void write(File file) {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("start-date", start.toString());
        conf.set("end-date", end.toString());
        conf.set("goal", goal);
        MemorySection c = (MemorySection) conf.createSection("donations");
        for (int id = 0; id < donations.size(); id++) {
            Donation d = donations.get(id);
            c = (MemorySection) c.createSection(Integer.toString(id));
            c.set("player", d.player.toString());
            c.set("date", d.date.toString());
            c.set("amount", d.amount);
            c = (MemorySection) c.getParent();
        }
        try {
            conf.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            FundMyServer.log.warning("Could not write the term that ended on " + end.toString());
        }
    }

    public void write() {
        if (this.equals(currentTerm)) {
            write(currentTermFile);
        }
    }

    public void addDonation(UUID player, LocalDate date, double amount) {
        addDonation(new Donation(player, date, amount));
    }

    public void addDonation(Donation donation) {
        if (donation.amount + getTotalRaised() > goal) {
            Donation d1, d2;
            d1 = new Donation(donation.player, donation.date, goal - getTotalRaised());
            d2 = new Donation(donation.player, donation.date, donation.amount - (goal - getTotalRaised()));
            donations.add(d1);
            getCurrentTerm().addDonation(d2);
        } else {
            donations.add(donation);
            getCurrentTerm();
        }
    }

    public double getTotalRaised() {
        double total = 0;
        for (int id = 0; id < donations.size(); id++) {
            total += donations.get(id).amount;
        }
        return total;
    }

    public double getGoal() {
        return goal;
    }

    public LocalDate getStartDate() {
        return start;
    }

    public LocalDate getEndDate() {
        return end;
    }

    public String[] print() {
        String one = "Raising money for "+ start.toString() + " to " + end.toString();
        String two = String.format("We have raised $%1$.2f/%2$.2f, or %3$.0f%% of our goal.", getTotalRaised(), goal, (getTotalRaised()/goal) * 100);
        String three = "We have " + DAYS.between(LocalDate.now(), end) + " days left to reach our goal.";
        String four = "Donate now at §n" + FundMyServer.url;
        return new String[] {one, two, three, four};
    }

}
