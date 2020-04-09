package me.jet315.stacker.manager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.jet315.stacker.MobStacker;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jet on 24/01/2018.
 */
public class EntityStackerManager {

    private int mobStackRadius;
    private HashMap<EntityType,Integer> entitiesToStack;
    private ArrayList<LivingEntity> validEnity = new ArrayList<>();
    private ArrayList<LivingEntity> entitiesToMultiplyOnDeath = new ArrayList<>();

    private ArrayList<String> instantKillPlayers = new ArrayList<>();

    public EntityStackerManager(int mobStackRadius, HashMap<EntityType,Integer> entitiesToStack) {
        this.mobStackRadius = mobStackRadius;
        this.entitiesToStack = entitiesToStack;
        startEntityClock();

    }

    private void startEntityClock(){
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MobStacker.getInstance(), () -> {
            // Iterate through all worlds
            for (World world : Bukkit.getServer().getWorlds()) {
                // Iterate through all entities in this world (if not disabled)
                if(MobStacker.getInstance().getMobStackerConfig().disabledWorlds.contains(world)) continue;
                for (LivingEntity entity : world.getLivingEntities()) {
                    if(!checkEntity(entity)) continue;
                    // Iterate through all entities in range
                    int num = 1;
                    for (Entity nearby : entity.getNearbyEntities(mobStackRadius, mobStackRadius, mobStackRadius)) {
                        if(checkEntity(nearby)) {
                            int min = MobStacker.getInstance().getMobStackerConfig().mobsMinimum.getOrDefault(nearby, 1);
                            if(num < min){
                                num++;
                                continue;
                            }
                            MobStacker.getInstance().getStackEntity().stack(entity, (LivingEntity) nearby);
                        }
                    }
                }
            }

        }, 20L, MobStacker.getInstance().getMobStackerConfig().updateTickDelay);

    }


    public ArrayList<LivingEntity> getEntitiesToMultiplyOnDeath() {
        return entitiesToMultiplyOnDeath;
    }

    private boolean checkEntity(Entity entity){
        if(!(entity instanceof LivingEntity)){
            return false;
        }
        if(!entity.isValid()){
            return false;
        }
        if (entity.getType() == EntityType.PLAYER) {
            return false;
        }
        if(!entitiesToStack.containsKey(entity.getType())){
            return false;
        }
        if(MobStacker.getInstance().getMobStackerConfig().worldguardEnabled) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(entity.getWorld()));
            if(regions == null) return false;
            for(ProtectedRegion r : regions.getRegions().values()){
                for(String s : MobStacker.getInstance().getMobStackerConfig().disabledRegions){
                    if(r.getId().equalsIgnoreCase(s)){
                        return false;
                    }
                }
            }
        }
        if(((LivingEntity) entity).isLeashed() && !MobStacker.getInstance().getMobStackerConfig().stackLeachedMobs){
            return false;
        }
        if(entity instanceof Tameable){
            if(!MobStacker.getInstance().getMobStackerConfig().stackTamedMobs){
                return false;
            }
        }
        if(MobStacker.getInstance().getMobStackerConfig().stackOnlySpawnerMobs){
            if (!validEnity.contains(entity)){
                return false;
            }
        }
        return entity.getType() != EntityType.SLIME;
    }

    public ArrayList<LivingEntity> getValidEntity() {
        return this.validEnity;
    }

    public ArrayList<String> getInstantKillPlayers() {
        return instantKillPlayers;
    }
}
