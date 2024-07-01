package dev.sterner

import moriyashiine.bewitchment.common.block.entity.GlyphBlockEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


class RitualManager {
    /**
     * Runs a command depending on which key phrase is used, "start", "tick", "end". Runs the [RitualManager.runCommand]
     *
     * @param world       world
     * @param extendedRitualRecipe recipe
     * @param blockPos    pos of the ritual origin
     * @param phase       keyword for which phase the command should run in
     */
    fun runCommand(world: World, extendedRitualRecipe: ExtendedRitualRecipe, blockPos: BlockPos, phase: String) {
        val minecraftServer = world.server
        for ((command, type) in extendedRitualRecipe.command) {
            if (type == phase) {
                runCommand(minecraftServer, blockPos, command)
            }
        }
    }

    /**
     * Runs the command with the command manager
     *
     * @param minecraftServer server
     * @param blockPos        ritual center
     * @param command         command to execute
     */
    private fun runCommand(minecraftServer: MinecraftServer?, blockPos: BlockPos, command: String) {
        var command = command
        if (minecraftServer != null && !command.isEmpty()) {
            command = "execute positioned {pos} run $command"
            val posString = blockPos.x.toString() + " " + blockPos.y + " " + blockPos.z
            val parsedCommand = command.replace("\\{pos}".toRegex(), posString)
            val commandSource = minecraftServer.commandSource
            val commandManager = minecraftServer.commandManager
            val parseResults = commandManager.dispatcher.parse(parsedCommand, commandSource)
            commandManager.execute(parseResults, parsedCommand)
        }
    }
}