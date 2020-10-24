package ray.mintcat.blockhead

import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe
import io.izzel.taboolib.TabooLibAPI
import io.izzel.taboolib.util.Commands
import io.izzel.taboolib.util.book.BookFormatter
import io.izzel.taboolib.util.lite.Scripts
import io.izzel.taboolib.util.lite.Signs
import io.izzel.taboolib.util.lite.SoundPack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta
import ray.mintcat.wizard.wizard.WizardObject
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList


object BlockHeadTools {
    fun openTradeUI(player: Player, recipeList: List<TradingRecipe>) {
        val TradeUI: String = recipeList.get(0).getResultItem().getItemMeta()?.getDisplayName()?.let {
            BlockHead.settings.getString("GUI.TradeUI", "....")
                ?.replace("&", "§")
                ?.replace("{Items}", it)
                ?.replace("{Shopkeeper}", BlockHeadObject.titleNameMap[player.getUniqueId()]!!)
        } ?: "null"

        val merchant = Bukkit.createMerchant(TradeUI)

        val merchantList: MutableList<MerchantRecipe> = ArrayList()
        for (i in 1 until recipeList.size) {
            val merchants = MerchantRecipe(recipeList.get(i).resultItem, Int.MAX_VALUE)
            val item1 = recipeList.get(i).item1 ?: ItemStack(Material.AIR)
            merchants.addIngredient(item1)
            val item2 = recipeList.get(i).item2 ?: ItemStack(Material.AIR)
            merchants.addIngredient(item2)
            merchantList.add(merchants)
        }

        merchant.recipes = merchantList
        //终极目标
        player.openMerchant(merchant, true)

        BlockHeadObject.titleMap.remove(player.getUniqueId())
        val type = BlockHeadObject.typeMap[player.getUniqueId()]
        val soundPack = SoundPack(BlockHead.settings.getString("sounds.$type.open", "sounds.default.open"))
        soundPack.play(player)
    }

    fun runGashapon(player: Player, item: ItemStack) {
        runStart(player,item)
        val im = item.itemMeta as BlockStateMeta
        val shulker = im.blockState as ShulkerBox
        val list = ArrayList<ItemStack>()
        for (n in 0..25) {
            val itemShulkers = shulker.inventory.getItem(n) ?: break
            if (itemShulkers.type == Material.AIR) {
                list.add(ItemStack(Material.BARRIER))
                break
            }
            list.add(itemShulkers)
        }
        for (i in 0 until BlockHeadObject.shulkerMap[player.getUniqueId()]!!) {
            val random = Random().nextInt(list.size)
            if (list.size <= 0) {
                break
            }
            val itemStack1 = list[random]
            if (itemStack1.type == Material.BARRIER) {
                break
            }
            player.getInventory().addItem(itemStack1)
        }
        player.sendMessage(BlockHead.getTitle() + "抽取完毕 请查收")
        list.clear()
    }

    fun runCommands(player: Player, item: ItemStack) {
        if (item.itemMeta?.hasLore()!!) {
            val lores = item.itemMeta!!.lore?.toTypedArray() ?: return
            BlockHeadObject.booleanMapEnd.put(player.uniqueId, true)
            for (s in lores) {
                if(BlockHeadObject.booleanMapEnd.get(player.uniqueId) == false){
                    BlockHeadObject.booleanMapEnd.remove(player.getUniqueId())
                    return
                }
                val lore = s.split(": ".toRegex()).toTypedArray()
                val lorecolor = lore[0].replace("§f", "").replace("- ", "")
                val loreInfo = TabooLibAPI.getPluginBridge().setPlaceholders(player, lore[1]) ?: return
                baseRunAction(player, lorecolor, loreInfo)
            }
            BlockHeadObject.booleanMap.remove(player.getUniqueId())
            BlockHeadObject.booleanMapEnd.remove(player.getUniqueId())
        }
    }

    fun runStart(player: Player, item: ItemStack) {
        when (item.type) {
            Material.WRITTEN_BOOK -> BookFormatter.forceOpen(player, item)
            Material.COMMAND_BLOCK_MINECART -> runCommands(player, item)
            Material.WHITE_SHULKER_BOX -> runGashapon(player, item)
            else -> BlockHeadObject.recipeMap.get(player.uniqueId)?.let { openTradeUI(player, it) }
        }
    }

    fun baseRunAction(player: Player, type: String, command: String) {
        when (type) {
            "cmdP","command=player" -> baseCommand(player, command)
            "cmdO","command=op" -> baseOPCommand(player, command)
            "cmdS","command=server" -> baseServerCommand(player,command)
            "powA","power=add" -> basePowerAdd(player,command)
            "powT","power=take" -> basePowerTake(player,command)
            "powH","power=has" -> baseHasPower(player,command)
            "msgR","message=Run" -> baseMessage(player,command)
            "msgT","message=To" -> baseTell(player,command)
            "msgN","message=No" -> baseElse(player,command)
            "ecoT","economy=Take" -> baseEcoTake(player,command)
            "ecoA","economy=Add" -> baseEcoAdd(player,command)
            "team","group" -> baseGroup(player,command)
            "gas","gashapon=V" -> baseGashapon(player,command)
            "end","stop" -> baseEnd(player)
            "endtry","stoptry" -> baseEndN(player)
            "if" -> baseIf(player,command)
            "else if" -> baseElseIf(player,command)
            "js","javascript" -> baseRunJavaScript(player,command)
            "import" -> baseImport(player,command)
            else -> return
        }
    }

    fun baseCommand(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        Commands.dispatchCommand(player, command)
    }

    fun baseOPCommand(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        val isOp = player.isOp
        try {
            player.isOp = true
            Commands.dispatchCommand(player, command)
        } catch (ignored: Exception) {
        } finally {
            player.isOp = isOp
        }
    }

    fun baseServerCommand(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        Commands.dispatchCommand(Bukkit.getConsoleSender() as CommandSender, command)
    }

    fun baseEcoTake(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        TabooLibAPI.getPluginBridge().economyTake(player, command.toDouble())
    }

    fun baseEcoAdd(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        TabooLibAPI.getPluginBridge().economyGive(player, command.toDouble())
    }

    fun basePowerAdd(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        TabooLibAPI.getPluginBridge().permissionAdd(player, command)
    }

    fun basePowerTake(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        TabooLibAPI.getPluginBridge().permissionRemove(player, command)
    }

    fun baseMessage(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        player.sendMessage(command)
    }

    fun baseTell(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        val commands = command.split(" to ")
        //tell: PlayerName to Message
        val players = Bukkit.getPlayerExact(commands[0]) ?: return
        players.sendMessage(commands[1])
    }

    fun baseElseIf(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == true) return
        val trues = Scripts.compile(command).eval() as Boolean
        BlockHeadObject.booleanMap.put(player.uniqueId, trues)
    }

    fun baseIf(player: Player, command: String) {
        val trues = Scripts.compile(command).eval() as Boolean
        BlockHeadObject.booleanMap.put(player.uniqueId, trues)
    }

    fun baseHasPower(player: Player, command: String) {
        BlockHeadObject.booleanMap.put(player.uniqueId, false)
        if (!player.hasPermission(command)) {
            BlockHeadObject.booleanMap.put(player.uniqueId, true)
        }
    }

    fun baseRunJavaScript(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        Scripts.compile(command)
    }

    fun baseGashapon(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        BlockHeadObject.shulkerMap.put(player.uniqueId, command.toInt())
    }

    fun baseEndN(player: Player) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        BlockHeadObject.booleanMapEnd.put(player.uniqueId, false)
    }

    fun baseEnd(player: Player) {
        BlockHeadObject.booleanMapEnd.put(player.uniqueId, false)
    }

    fun baseGroup(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        Bukkit.getScheduler().runTask(BlockHead.getPlugin(), Runnable {
            val key = command.split("_".toRegex()).toTypedArray()
            val recipeList = BlockHeadObject.recipeMap.get(player.uniqueId) ?: return@Runnable
            val item = if (key[1].equals("1")) {
                recipeList.get(key[0].toInt()).item1
            } else if (key[1].equals("2")) {
                recipeList.get(key[0].toInt()).item2
            } else {
                recipeList.get(key[0].toInt()).resultItem
            } ?: return@Runnable
            runStart(player,item)
        })
    }

    fun baseImport(player: Player,command: String){
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == false) return
        Signs.fakeSign(player, Consumer { lines: Array<String?>? ->
            if (lines != null) {
                WizardObject.setIntegral(player,command, lines.get(0))
            }
            BlockHeadObject.inventoryMap[player.uniqueId]?.let { player.openInventory(it) }
        })
    }

    fun baseElse(player: Player, command: String) {
        if (BlockHeadObject.booleanMap[player.uniqueId] != null && BlockHeadObject.booleanMap[player.uniqueId] == true) return
        player.sendMessage(command)
    }
}