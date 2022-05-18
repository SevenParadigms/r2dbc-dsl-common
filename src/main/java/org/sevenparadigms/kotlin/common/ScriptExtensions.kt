package org.sevenparadigms.kotlin.common

import javax.script.SimpleBindings

fun SimpleBindings.put(name: Enum<*>, value: Any) = put(name.name, value)