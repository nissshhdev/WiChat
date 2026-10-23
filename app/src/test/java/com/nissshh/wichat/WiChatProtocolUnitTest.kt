package com.nissshh.wichat

import com.nissshh.wichat.core.network.protocol.ProtocolMessageType
import com.nissshh.wichat.core.network.protocol.WiChatPacket
import com.nissshh.wichat.core.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WiChatProtocolUnitTest {

    @Test
    fun testPacketSerializationAndDeserialization() {
        val originalPacket = WiChatPacket(
            version = 1,
            type = ProtocolMessageType.CHAT_MESSAGE,
            packetId = "test-msg-1234",
            senderPeerId = "peer-abc",
            payloadJson = "{\"convId\":\"c1\",\"text\":\"Hello over Wi-Fi!\"}",
            timestamp = 1690000000000L
        )

        val raw = originalPacket.serialize()
        assertNotNull(raw)
        assertTrue(raw.contains("test-msg-1234"))

        val deserialized = WiChatPacket.deserialize(raw)
        assertNotNull(deserialized)
        assertEquals(originalPacket.version, deserialized!!.version)
        assertEquals(originalPacket.type, deserialized.type)
        assertEquals(originalPacket.packetId, deserialized.packetId)
        assertEquals(originalPacket.senderPeerId, deserialized.senderPeerId)
        assertEquals(originalPacket.payloadJson, deserialized.payloadJson)
        assertEquals(originalPacket.timestamp, deserialized.timestamp)
    }

    @Test
    fun testPasscodeHashAndVerification() {
        val testPin = "123456"
        val (hash, salt) = SecurityManager.hashPasscode(testPin)

        assertTrue(hash.isNotEmpty())
        assertTrue(salt.isNotEmpty())

        val isValid = SecurityManager.verifyPasscode(testPin, hash, salt)
        assertTrue("Valid passcode should verify successfully", isValid)

        val isInvalid = SecurityManager.verifyPasscode("654321", hash, salt)
        assertTrue("Wrong passcode must fail verification", !isInvalid)
    }
}
