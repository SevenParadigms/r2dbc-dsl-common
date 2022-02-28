package org.sevenparadigms.kotlin.common

import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.apache.commons.codec.digest.MurmurHash2
import org.apache.commons.codec.digest.MurmurHash3
import java.security.MessageDigest

fun String.murmur32(): Int = MurmurHash2.hash32(this)

fun String.murmur64(): Long = MurmurHash2.hash64(this.toByteArray(), this.toByteArray().size)

fun String.murmur128(): LongArray = MurmurHash3.hash128x64(this.toByteArray())

fun String.sha512(): String = toByteArray().sha512()

fun ByteArray.sha512(): String = sha512AsBytes().convertString()

fun ByteArray.sha512AsBytes(): ByteArray = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512).digest(this)