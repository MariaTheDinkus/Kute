package dev.vini2003.kute.command

import com.mojang.brigadier.tree.RootCommandNode
import dev.vini2003.kute.external.brigadier.CommandSource

fun interface Command : (RootCommandNode<CommandSource>) -> Unit