package me.jet315.stacker.util;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import me.jet315.stacker.MobStacker;
import me.jet315.stacker.events.OnEntityDespawn;
import me.jet315.stacker.events.OnEntitySpawn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Jet on 24/01/2018.
 */
public class Config {



    /** The mob stack radius. Mobs of the same type within this radius will be stacked. */
    public int stackRadius = 1;
    /** The mob types that we want to stack. */
    public HashMap<EntityType, Integer> mobsToStack = new HashMap<>();
    /** The minimum amount to check from. */
    public HashMap<EntityType, Integer> mobsMinimum = new HashMap<>();
    /** The update delay for mob stacking, in ticks. */
    public int updateTickDelay = 20;
    /**Stores the worlds where mobs should not stack */
    public Set<World> disabledWorlds = new HashSet<>();
    /**Stores the regions where mobs should not be stacked */
    public Set<String> disabledRegions = new HashSet<>();
    public boolean worldguardEnabled = false;
    /**Stores boolean value of whether ONLY mob spawners should be stacked */
    public boolean stackOnlySpawnerMobs = false;
    /**Stores whether stacking of Tamed Mobs is enabled*/
    public boolean stackTamedMobs = false;
    /**Stores whether stacking of Leashed Mobs is enabled*/
    public boolean stackLeachedMobs = false;
    /**Stores whether to kill the whole mob stack on fall*/
    public boolean killMobStackOnFall = true;

    /**
     * Stores what should be displayed over the mbos head
     * Will store %number% where the number of mobs stacked should be shown
     */
    public String stackMobsDispalyName = "%number% Mobs";
    /**
     * Stores the index at which the %number% is at - need so I am able to parse in the nametag
     */
    public int indexLocation = 0;


    private FileConfiguration configFile;


    public Config(FileConfiguration configFile) {
        this.configFile = configFile;
        this.reloadConfig();
    }



    public void reloadConfig() {
        boolean stackOnlySpawnerMobsBefore = stackOnlySpawnerMobs;
        stackRadius = configFile.getInt("StackRadius");
        compileEntityTypesList(configFile.getConfigurationSection("MobTypes")); // Load EntityTypes list (mobTypes)
        updateTickDelay = configFile.getInt("UpdateTickDelay");
        String stackFormat = configFile.getString("StackFormat");
        stackFormat = (stackFormat == null) ? "&a&l%number% &6&l%type%" : stackFormat;
        stackMobsDispalyName = ChatColor.translateAlternateColorCodes('&',stackFormat);
        for(String s : stackFormat.split(" ")){
            if(s.contains("%number%")){
                break;
            }else{
                indexLocation++;
            }
        }

        compileWorldList(configFile.getStringList("DisabledWorlds")); // Load Worlds
        stackOnlySpawnerMobs = configFile.getBoolean("MergeOnlySpawnerMobs");
        stackTamedMobs = configFile.getBoolean("MergeTamedMobs");
        stackLeachedMobs = configFile.getBoolean("MergeLeashedMobs");
        killMobStackOnFall = configFile.getBoolean("killMobStackOnFall");
        compileRegionList(configFile.getStringList("WorldGuardRegions")); // Load EntityTypes list (mobTypes)

        //Load Event if needed - If the event was not loaded before && needs to be loaded - load event
        if(!stackOnlySpawnerMobsBefore && stackOnlySpawnerMobs){
                Bukkit.getPluginManager().registerEvents(new OnEntitySpawn(), MobStacker.getInstance());
                Bukkit.getPluginManager().registerEvents(new OnEntityDespawn(), MobStacker.getInstance());
        }

    }

    /*
     * Helping Methods
     */
    private void compileEntityTypesList(ConfigurationSection list) {
        if (list == null || list.getKeys(false).size() == 0) return; // List may be nothing

        mobsToStack.clear();
        mobsMinimum.clear();

        for (String entityName : list.getKeys(false)) {
            try {
                EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                if (!list.isConfigurationSection(entityName)) {
                    this.mobsToStack.put(entityType, list.getInt(entityName));
                    this.mobsMinimum.put(entityType, 1);
                } else {
                    this.mobsToStack.put(entityType, list.getInt(entityName + ".amount"));
                    this.mobsMinimum.put(entityType, list.getInt(entityName + ".min"));
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("======= MOB STACKER =======");
                System.out.println("INVALID MOB TYPE DETECTED: "+ entityName);
            }
        }
    }

    private void compileWorldList(List<String> list) {
        if (list == null || list.size() == 0) return; // List may be nothing

        for (String worldName : list) {
            World world = Bukkit.getWorld(worldName);
            if(world == null){
                System.out.println("======= MOB STACKER =======");
                System.out.println("INVALID WORLD NAME DETECTED: "+ worldName);
            }else{
                this.disabledWorlds.add(world);
            }
        }
    }

    private void compileRegionList(List<String> list) {
        if (list == null || list.size() == 0){    // List may be nothing
            worldguardEnabled = false;
            return;
        }
        worldguardEnabled = true;

        disabledRegions.addAll(list);
    }
}