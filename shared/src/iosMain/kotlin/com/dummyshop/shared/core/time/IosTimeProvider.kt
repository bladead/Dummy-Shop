package com.dummyshop.shared.core.time

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

class IosTimeProvider : TimeProvider {
    override fun nowMs(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
}
