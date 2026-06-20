package xyz.devcmb.gnome.lib

import com.noxcrew.noxesium.core.fabric.mcc.MccNoxesiumEntrypoint
import com.noxcrew.noxesium.core.mcc.ClientboundMccStatisticPacket
import com.noxcrew.noxesium.core.mcc.MccPackets
import xyz.devcmb.gnome.util.isOnIsland

class GnomeNoxesiumEntrypoint : MccNoxesiumEntrypoint() {
    override fun initialize() {
        MccPackets.CLIENTBOUND_MCC_STATISTIC.addListener(
            this,
            ClientboundMccStatisticPacket::class.java
        ) { _, packet, _ ->
            if(!isOnIsland()) return@addListener

            val statistic = packet.statistic
            if(!statistic.startsWith("fishing_catch")) return@addListener

            // TODO: Hook up to the stat tracker
        }
    }
}