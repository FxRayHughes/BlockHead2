package ray.mintcat.blockhead

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe
import org.bukkit.inventory.Inventory
import java.util.*
import kotlin.collections.HashMap

object BlockHeadObject {

    var recipeMap = HashMap<UUID, List<TradingRecipe>>()

    var titleMap = HashMap<UUID, String>()

    var titleNameMap = HashMap<UUID, String>()

    var typeMap = HashMap<UUID, String>()

    var booleanMap = HashMap<UUID, Boolean>()

    var booleanMapEnd = HashMap<UUID, Boolean>()

    var shulkerMap = java.util.HashMap<UUID, Int>()

    var inventoryMap = java.util.HashMap<UUID, Inventory>()

}