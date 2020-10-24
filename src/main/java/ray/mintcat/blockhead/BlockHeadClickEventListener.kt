package ray.mintcat.blockhead

import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.book.BookFormatter
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.scheduler.BukkitRunnable

@TListener
class BlockHeadClickEventListener : Listener {

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent){
        val player = event.view.player as Player
        val uuid = player.uniqueId
        if (event.view.title != BlockHeadObject.titleMap[uuid]) return
        event.isCancelled = true

        if (event.rawSlot != 2 ) return
        val recipeList = BlockHeadObject.recipeMap[uuid] ?: return
        if (recipeList[0].item2 == null || recipeList[0].item2.type.equals(Material.AIR)){
            BlockHeadTools.openTradeUI(player,recipeList)
            return
        }
        Bukkit.getScheduler().runTask(BlockHead.getPlugin(), Runnable {
            BlockHeadTools.runStart(player, recipeList[0].item2)
        })
    }

}