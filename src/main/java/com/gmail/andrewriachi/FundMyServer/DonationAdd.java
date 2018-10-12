package com.gmail.andrewriachi.FundMyServer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.LocalDate;
import java.util.UUID;

public class DonationAdd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        LocalDate date = null;
        double amount;
        if (args.length == 2) {
            date = LocalDate.now();
            amount = Double.parseDouble(args[1]);
        } else if (args.length == 3) {
            date = LocalDate.parse(args[1]);
            amount = Double.parseDouble(args[2]);
        } else {
            commandSender.sendMessage("Invalid number of arguments!");
            return false;
        }
        UUID player = UUID.fromString(args[0]);
        Donation d = new Donation(player, date, amount);
        d.register();
        Term.getCurrentTerm().write();
        Donor.getDonor(UUID.fromString(args[0])).write();
        commandSender.sendMessage("Added a donation from player " + args[0] + " on " + date + " with an amount of " + amount);
        Bukkit.broadcastMessage(Bukkit.getOfflinePlayer(player).getName() + " just made a donation! Here is an update on our goal:");
        for (String s : Term.getCurrentTerm().print()) {
            Bukkit.broadcastMessage(s);
        }
        return true;
    }
}
