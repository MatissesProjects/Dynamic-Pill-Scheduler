package com.phos.core.data.engine

import com.phos.core.data.model.SafetyStatus
import org.junit.Assert.assertTrue
import org.junit.Test

class ZkpEngineTest {

    private val engine = ZkpEngine()

    @Test
    fun `generate and verify proof for GREEN`() {
        val payload = engine.generateProof(SafetyStatus.GREEN)
        assertTrue(engine.verifyProof(payload))
    }

    @Test
    fun `generate and verify proof for RED`() {
        val payload = engine.generateProof(SafetyStatus.RED)
        assertTrue(engine.verifyProof(payload))
    }
}
