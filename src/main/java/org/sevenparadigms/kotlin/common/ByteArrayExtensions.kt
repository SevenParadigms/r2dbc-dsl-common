package org.sevenparadigms.kotlin.common

import org.springframework.util.ResourceUtils
import java.nio.file.Files
import java.util.*

fun ByteArray.encode() = Base64.getMimeEncoder().encode(this)

fun ByteArray.decode() = Base64.getMimeDecoder().decode(this)

fun ByteArray.convertString() = String(this)

fun String.loadResource() = Files.readAllBytes(ResourceUtils.getFile("classpath:$this").toPath())