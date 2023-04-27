package com.loudsight.utilities.service.dispatcher.bridge

class CircularBuffer<T>(val name: String) {

        inner class CircularBufferIterator: MutableIterator<T> {
            var position: Int = -1

            override fun hasNext(): Boolean {
                return position < first
            }

            override fun next(): T {
                position = ++position % buffer.size
                return buffer[position]
            }

            override fun remove() {
                last = ++last % buffer.size
            }
        }

        val buffer: Array<T> = arrayOfNulls<Any>(20) as Array<T>
        var first: Int = -1
        var last: Int = -1

        fun iterator(): MutableIterator<T> {
            return CircularBufferIterator()
        }

        fun add(element: T) {
            val next = ++first % buffer.size

            if (next < first) {
                // wrapped around
                if (next >= last) {
                    throw IllegalStateException("No space to add element")
                }
            }
            buffer[next] = element
            first = next
        }
    }
