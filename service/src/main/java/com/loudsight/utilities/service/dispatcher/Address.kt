package com.loudsight.utilities.service.dispatcher

import com.loudsight.meta.annotation.Introspect

@Introspect(Address::class)
open class Address(var scope: String, var topic: String) {
    override fun equals(other: Any?): Boolean {
        if (other is Address) {
            return other.scope == scope &&
                    other.topic == topic
        }
        return false;
    }

    override fun hashCode(): Int {
        return 0x70 xor scope.hashCode() xor topic.hashCode()
    }

    companion object {
        val NO_REPLY = Address("no-reply", "no-reply")
    }
}