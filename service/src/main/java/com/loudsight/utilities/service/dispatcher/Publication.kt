package com.loudsight.utilities.service.dispatcher

import com.loudsight.meta.annotation.Introspect
import com.loudsight.utilities.permission.Subject
import com.loudsight.utilities.service.dispatcher.bridge.BridgeMessageType

@Introspect(Publication::class)
open class Publication(
    var to: Address,
    var replyTo: Address?,
    var recipient: Subject,
    var sender: Subject,
    var payload: Any?,
    var publicationType: BridgeMessageType
) {

    fun <T> getData(): T? {
        return if (payload is NullValue) {
            null
        } else {
            payload as T
        }
    }
}
