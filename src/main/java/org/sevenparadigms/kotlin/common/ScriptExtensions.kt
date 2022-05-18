package org.sevenparadigms.kotlin.common

import javax.script.SimpleBindings

fun SimpleBindings.add(name: Enum<*>, value: Any) = put(name.name, value)