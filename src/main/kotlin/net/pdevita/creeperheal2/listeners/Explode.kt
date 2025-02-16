package net.pdevita.creeperheal2.listeners

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Wither
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent

class Explode(var plugin: CreeperHeal2) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityExplodeEvent(event: EntityExplodeEvent) {
        if (plugin.settings.types.allowExplosionEntity(event.entityType)) {
            event.location.world?.name?.let { worldName ->
                if (plugin.settings.worldList.allowWorld(worldName)) {
                    plugin.createNewExplosion(event.blockList())

                    // Remove container blocks from the event's list to avoid duplicate drops.
                    event.blockList().removeIf { block ->
                        block.type == Material.CHEST ||
                                block.type == Material.TRAPPED_CHEST ||
                                block.type == Material.FURNACE ||
                                block.type == Material.BARREL
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockExplodeEvent(event: BlockExplodeEvent) {
        event.block.location.world?.name?.let { worldName ->
            if (plugin.settings.worldList.allowWorld(worldName) &&
                plugin.settings.types.allowExplosionBlock(/*event.block.blockData.material*/)
            ) {
                plugin.createNewExplosion(event.blockList())

                // Remove container blocks from the event's list to avoid duplicate drops.
                event.blockList().removeIf { block ->
                    block.type == Material.CHEST ||
                            block.type == Material.TRAPPED_CHEST ||
                            block.type == Material.FURNACE ||
                            block.type == Material.BARREL
                }
            }
        }
    }

    @EventHandler
    fun onHangingBreakEvent(event: HangingBreakEvent) {
        if (plugin.settings.general.entityType && event.entity.location.world?.let { plugin.settings.worldList.allowWorld(it.name) } == true) {
            if (event.cause == HangingBreakEvent.RemoveCause.EXPLOSION) {
                if (event.entity.location.world?.let { plugin.settings.worldList.allowWorld(it.name) } == true) {
                    if (event is HangingBreakByEntityEvent) {
                        plugin.debugLogger("${event.entity} ${event.entity.type} at ${event.entity.location} exploded by ${event.remover}")
                    } else {
                        plugin.debugLogger("${event.entity} ${event.entity.type} at ${event.entity.location} exploded")
                    }
                }
                plugin.createNewExplosion(event.entity)
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        if (plugin.settings.general.entityType && event.entity.location.world?.let { plugin.settings.worldList.allowWorld(it.name) } == true) {
            if (event.entityType == EntityType.ARMOR_STAND) {
                if (event.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                    event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                ) {
                    val armorStand = event.entity as ArmorStand
                    // You should be able to just check if it was dealt a killing blow but ehhhhh idk don't work
                    // println("${armorStand.health} - ${event.finalDamage}")
                    // if ((armorStand.health - event.finalDamage).roundToInt() < 0) {
                    plugin.createNewExplosion(armorStand)
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun witherBreak(event: EntityChangeBlockEvent) {
        if (event.entity is Wither) {
            event.block.location.world?.name?.let { worldName ->
                if (plugin.settings.worldList.allowWorld(worldName)) {
                    plugin.createNewExplosion(listOf(event.block))
                }
            }
        }
    }
}

