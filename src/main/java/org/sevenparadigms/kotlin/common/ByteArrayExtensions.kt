package org.sevenparadigms.kotlin.common

import org.springframework.util.ResourceUtils
import java.nio.file.Files
import java.util.*

fun ByteArray.encode(): ByteArray = Base64.getMimeEncoder().encode(this)

fun ByteArray.decode(): ByteArray = Base64.getMimeDecoder().decode(this)

fun String.loadResource(): ByteArray = Files.readAllBytes(ResourceUtils.getFile("classpath:$this").toPath())

fun ByteArray.convertString(): String = String(this, Charsets.UTF_8)