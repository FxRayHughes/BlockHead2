package ray.mintcat.blockhead

import com.nisovin.shopkeepers.api.events.ShopkeeperOpenUIEvent
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.tellraw.TellrawJson
import io.izzel.taboolib.util.lite.SoundPack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryType
import ray.mintcat.blockhead.BlockHeadObject.recipeMap
import ray.mintcat.blockhead.BlockHeadObject.titleMap
import ray.mintcat.blockhead.BlockHeadObject.titleNameMap
import ray.mintcat.blockhead.BlockHeadObject.typeMap

@TListener
class BlockHeadOpenInvListener : Listener {

    @EventHandler
    fun onShopkeeperOpenUIEvent(event: ShopkeeperOpenUIEvent){
        val player = event.player
        val uuid = player.uniqueId
        if (player.isSneaking) return
        typeMap.put(event.player.uniqueId, event.shopkeeper.type.toString())
        if (player.isOp && player.inventory.itemInMainHand.type == Material.STICK) {
            TellrawJson.create()
                    .append("§7§l[§f§lBlockHead§7§l] " + "§7目标的 TypeID: §f" + event.shopkeeper.type.toString())
                    .clickSuggest(event.shopkeeper.type.toString())
                    .hoverText("点击复制到聊天框")
                    .send(player)
            event.isCancelled = true
            return
        }
        val shopkeeper = event.shopkeeper
        val recipeList = shopkeeper.getTradingRecipes(player)
        val title = BlockHead.settings.getString("GUI.Title", "对话") ?: "对话"
        val titles = title.replace("&", "§").replace("{Shopkeeper}", shopkeeper.name)
        titleNameMap.put(uuid, shopkeeper.name)
        titleMap.put(uuid,titles)
        recipeMap.put(uuid,recipeList)
        if (recipeList.size >= 1) {
            BlockHeadObject.inventoryMap.remove(uuid)
            if (recipeList[0].resultItem != recipeList[0].item1) {
                return
            }
            event.isCancelled = true
            val inventory = Bukkit.createInventory(null, InventoryType.HOPPER, titles)
            inventory.setItem(2, recipeList[0].resultItem)
            BlockHeadObject.inventoryMap.put(uuid,inventory)
            player.openInventory(inventory)
            val soundPack = SoundPack(BlockHead.settings.getString("sounds." + event.shopkeeper.type.toString() + ".trade", "sounds.default.trade"))
            soundPack.play(player)
        }

    }

}