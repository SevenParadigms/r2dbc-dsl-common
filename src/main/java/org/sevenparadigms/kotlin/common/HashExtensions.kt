package org.sevenparadigms.kotlin.common

import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.apache.commons.codec.digest.MurmurHash3
import java.security.MessageDigest

fun String.murmur(): Int = MurmurHash3.hash32x86(this.toByteArray())

fun String.sha512(): String = toByteArray().sha512()

fun ByteArray.sha512(): String = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512).digest(this).convertString()