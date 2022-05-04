package org.sevenparadigms.kotlin.common

import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.apache.commons.codec.digest.MurmurHash2
import org.apache.commons.codec.digest.MurmurHash3
import java.security.MessageDigest

fun String.murmur32(): Int = MurmurHash2.hash32(this)

fun String.murmur64(): Long = MurmurHash2.hash64(this.toByteArray(), this.toByteArray().size)

fun String.murmur128(): LongArray = MurmurHash3.hash128x64(this.toByteArray())

fun String.sha512(): String = toByteArray().sha512()

fun ByteArray.sha512(): String = sha512ByteArray().convertString()

fun ByteArray.sha512ByteArray(): ByteArray = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512).digest(this)

fun ByteArray.sha512_256(): String = sha512_256ByteArray().convertString()

fun ByteArray.sha512_256ByteArray(): ByteArray = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512_256).digest(this)