package earth.crystalmc.crystalDelivery.delivery

enum class DeliveryFailedCode(val code: Int) {
    MailboxFull(101);

    companion object {
        fun byCode(code: Int): DeliveryFailedCode? {
            return entries.find { it.code == code }
        }
    }
}