package com.gmail.andrewriachi.FundMyServer;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Donor {
    private ArrayList<Donation> donations = new ArrayList<>();
    private UUID uuid;
    private static final File donorFolder = new File(FundMyServer.pluginFolder, "donors");
    private static HashMap<UUID, Donor> donors = new HashMap<>();

    private Donor(UUID player) {
        uuid = player;
    }

    public static Donor getDonor(UUID player) {
        if (donors.get(player) == null) {
            File f = new File(donorFolder, player.toString() + ".yml");
            Donor d = new Donor(player);
            if (f.exists()) {
                YamlConfiguration conf = new YamlConfiguration();
                try {
                    conf.load(f);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // FundMyServer.crash("Could not read the donor information");
                }
                for (int id = 0; id < conf.getKeys(false).size(); id++) {
                    MemorySection c = (MemorySection) conf.getConfigurationSection(Integer.toString(id));
                    d.donations.add(new Donation(player, LocalDate.parse(c.getString("date")), c.getDouble("amount")));
                }
            }
            donors.put(player, d);
        }
        return donors.get(player);
    }

    public void write() {
        YamlConfiguration conf = new YamlConfiguration();
        for (int id = 0; id < donations.size(); id++) {
            MemorySection c = (MemorySection) conf.createSection(Integer.toString(id));
            Donation don = donations.get(id);
            c.set("player", uuid.toString());
            c.set("date", don.date.toString());
            c.set("amount", don.amount);
        }
        try {
            donorFolder.mkdir();
            File f = new File(donorFolder, uuid.toString() + ".yml");
            conf.save(f);
            // Free the resource when writing
            donors.remove(uuid);
        } catch (Exception ex) {
            ex.printStackTrace();
            // FundMyServer.crash("Could not save the donor information");
        }
    }

    public void addDonation(Donation d) {
        if (d.player == uuid) {
            donations.add(d);
        }
    }

    public void addDonation(LocalDate date, double amount) {
        donations.add(new Donation(uuid, date, amount));
    }
}
