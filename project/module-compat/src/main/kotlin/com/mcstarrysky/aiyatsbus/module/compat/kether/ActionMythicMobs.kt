package com.mcstarrysky.aiyatsbus.module.compat.kether

import ink.ptms.um.Mythic
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.platformLocation
import taboolib.module.kether.KetherLoader
import taboolib.module.kether.combinationParser
import taboolib.module.kether.player
import taboolib.platform.util.toBukkitLocation

/**
 * Aiyatsbus
 * com.mcstarrysky.aiyatsbus.module.compat.kether.ActionMythicMobs
 *
 * @author mical
 * @date 2024/8/29 00:20
 */
@Awake(LifeCycle.ACTIVE)
private fun init() {
    if (Mythic.isLoaded()) {
        KetherLoader.registerParser(combinationParser {
            it.group(
                symbol(), // action
                text(), // name
                command("at", then = any()).option(), // player, location, world-name
                double().option(), // x
                double().option(), // y
                double().option(), // z
                command("and", then = float().and(float())).option().defaultsTo(0f to 0f) // yaw and pitch
            ).apply(it) { action, name, target, x, y, z, (yaw, pitch) ->
                now {
                    when (action) {
                        "castskill", "cast-skill" -> {
                            val player = when (target) {
                                is Player -> target
                                is String -> Bukkit.getPlayerExact(target)
                                else -> null
                            } ?: player().castSafely<Player>() ?: error("No player selected.")
                            val et = Mythic.API.getTargetedEntity(player)
                            Mythic.API.castSkill(
                                caster = player as Entity,
                                skillName = name,
                                trigger = player,
                                origin = player.location,
                                et = if (et != null) listOf(et) else emptyList()
                            )
                        }
                        "spawnmob", "spawn-mob" -> {
                            val location = when (target) {
                                is Location -> target
                                is taboolib.common.util.Location -> target.toBukkitLocation()
                                is String -> platformLocation(taboolib.common.util.Location(target, x!!, y!!, z!!, yaw, pitch))
                                else -> player().castSafely<Player>()?.location ?: error("No location selected.")
                            }
                            Mythic.API.getMobType(name)?.spawn(location, 0.0)
                        }
                        // 其他
                        else -> error("Unknown action $action")
                    }
                }
            }
        }, arrayOf("mm", "mythic", "mythicmobs"), "aiyatsbus", shared = true)
    }
}