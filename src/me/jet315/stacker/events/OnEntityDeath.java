package me.jet315.stacker.events;

import me.jet315.stacker.MobStacker;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by Jet on 24/01/2018.
 */
public class OnEntityDeath implements Listener{

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity entity =  e.getEntity();

        if(entity.getType() == EntityType.ARMOR_STAND || entity.getType() == EntityType.SLIME){
            return;
        }


        if (entity.getType() != EntityType.PLAYER) {
            if(MobStacker.getInstance().getEntityStacker().getEntitiesToMultiplyOnDeath().contains(entity) || (entity.getKiller() != null && entity.getKiller().hasPermission("mobstacker.killstack"))){
                MobStacker.getInstance().getEntityStacker().getEntitiesToMultiplyOnDeath().remove(entity);
                e.setDroppedExp(e.getDroppedExp() * multiplyDropsReturnExp(entity,e.getDrops()));
                return;
            }
            MobStacker.getInstance().getStackEntity().attemptUnstackOne(entity);
        }

        if(MobStacker.getInstance().getMobStackerConfig().stackOnlySpawnerMobs){
            MobStacker.getInstance().getEntityStacker().getValidEntity().remove(entity);
        }
    }


    public int multiplyDropsReturnExp(LivingEntity dead, List<ItemStack> drops){
        int amountToMultiply = MobStacker.getInstance().getStackEntity().parseAmount(dead.getCustomName());
        if(amountToMultiply <=1) return 1;
        for(ItemStack i : drops){
            if(!isMobDrop(i)) continue;
            ItemStack item = new ItemStack(i);
            item.setAmount(amountToMultiply);
            dead.getWorld().dropItem(dead.getLocation(),item);
        }
        return amountToMultiply;

    }

    private boolean isMobDrop(ItemStack i){
        Material m = i.getType();
        return m == Material.ROTTEN_FLESH ||
                m == Material.BONE ||
                m == Material.STRING ||
                m == Material.ARROW ||
                m == Material.GUNPOWDER ||
                m == Material.LEATHER ||
                m == Material.BEEF ||
                m == Material.COOKED_BEEF ||
                m == Material.PORKCHOP ||
                m == Material.COOKED_PORKCHOP ||
                m == Material.FEATHER ||
                m == Material.CHICKEN ||
                m == Material.COOKED_CHICKEN ||
                m == Material.COD ||
                m == Material.COOKED_COD ||
                m == Material.BAMBOO ||
                m == Material.GOLD_NUGGET ||
                m == Material.COAL ||
                m == Material.GLASS_BOTTLE ||
                m == Material.GLOWSTONE_DUST ||
                m == Material.SUGAR ||
                m == Material.STICK ||
                m == Material.REDSTONE ||
                m == Material.SPIDER_EYE ||
                m == Material.EMERALD ||
                isMusicDisc(m);
    }

    private boolean isMusicDisc(Material m){
        return m == Material.MUSIC_DISC_11 ||
                m == Material.MUSIC_DISC_13 ||
                m == Material.MUSIC_DISC_BLOCKS ||
                m == Material.MUSIC_DISC_CAT ||
                m == Material.MUSIC_DISC_CHIRP ||
                m == Material.MUSIC_DISC_FAR ||
                m == Material.MUSIC_DISC_MALL ||
                m == Material.MUSIC_DISC_MELLOHI ||
                m == Material.MUSIC_DISC_STAL ||
                m == Material.MUSIC_DISC_STRAD ||
                m == Material.MUSIC_DISC_WAIT ||
                m == Material.MUSIC_DISC_WARD;
    }

}
