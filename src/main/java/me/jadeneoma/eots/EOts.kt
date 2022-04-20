package me.jadeneoma.eots

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.javatuples.Pair
import java.util.*
import java.util.logging.Level
import kotlin.math.pow
import kotlin.math.sqrt


class EOtS : JavaPlugin() {
    var running = false
    override fun onEnable() {
        logger.log(Level.INFO, "EotS Plugin Reporting for duty")
        config.options().copyDefaults()
        saveDefaultConfig()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {

        // /start
        if (command.name.equals("EotS", ignoreCase = true)) {
            if (args[0].equals("start", ignoreCase = true)) {
                if (!config.getBoolean("Started")){
                    val start = updateDest()
                    Bukkit.getWorld("world")!!.setSpawnLocation(start.value0.toInt(), 64, start.value1.toInt())
                    Bukkit.getWorld("world")!!.worldBorder.setCenter(start.value0, start.value1)
                    Bukkit.getWorld("world")!!.worldBorder.size = 160.0
                    logger.log(Level.INFO, "WorldBorder set!!!!")

                    config.set("Started", true)
                    saveConfig()
                } else {
                    logger.log(Level.INFO, "WorldBorder was already set")
                }

                running = true
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
                    {
                        if (running) {
                            val start = Pair(
                                Bukkit.getWorld("world")!!.worldBorder.center.x,
                                Bukkit.getWorld("world")!!.worldBorder.center.z
                            )
                            var dest = Pair(config.getDouble("Destination.X"), config.getDouble("Destination.Z"))
                            val diff = Pair(dest.value0 - start.value0, dest.value1 - start.value1)
                            val dist = sqrt(diff.value0.pow(2) + diff.value1.pow(2))
                            if (dist <= 10) {
                                logger.log(Level.INFO, "Bounce")
                                dest = updateDest()
                                val newMove = movement(start, dest)
                                config.set("Movement.X", newMove.value0)
                                config.set("Movement.Z", newMove.value1)
                                saveConfig()
                            }
                            val move = Pair(config.getDouble("Movement.X"),config.getDouble("Movement.Z"))
                            val center = Pair(start.value0 + move.value0, start.value1 + move.value1)
                            Bukkit.getWorld("world")!!.setSpawnLocation(center.value0.toInt(), 64 , center.value1.toInt())
                            Bukkit.getWorld("world")!!.worldBorder.setCenter(center.value0, center.value1)


                        }

                    },
                    0L,
                    (20  * config.getDouble("SPB")).toLong()
                )

            } else if (args[0].equals("stop", ignoreCase = true)) {
                running = false
            } else if (args[0].equals("reset", ignoreCase = true)){
                running = false
                config.set("Started", false)
                saveConfig()
            }
        }
        return true
    }

    fun genLoc(): Pair<Double, Double> {
        // generate new location and return them as pairs
        val rand = Random()
        val upper = 16000
        val startX = rand.nextInt(upper).toDouble() + 8000.5
        val startZ = rand.nextInt(upper).toDouble() + 8000.5
        return Pair(startX, startZ)
    }

    fun movement(start:Pair<Double,Double>,dest:Pair<Double,Double>): Pair<Double, Double> {
        val diff = Pair(dest.value0 - start.value0, dest.value1 - start.value1)
        val dist = sqrt(diff.value0.pow(2) + diff.value1.pow(2))
        val moveX = diff.value0 / dist
        val moveZ = diff.value1 / dist
        val test = sqrt(moveX.pow(2) + moveZ.pow(2))
        logger.log(Level.INFO, " $moveX $moveZ ")
        return Pair(moveX, moveZ)
    }

    fun updateDest(): Pair<Double, Double> {
        val dest = genLoc()
        config.set("Destination.X", dest.value0)
        config.set("Destination.Z", dest.value1)
        saveConfig()
        return dest
    }


    override fun onDisable() {
        saveConfig()
    }
}