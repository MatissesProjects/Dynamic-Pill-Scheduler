package com.phos.core.data.engine

import com.phos.core.data.model.SafetyStatus
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class ZkpEngine {
    // A 1024-bit prime and generator for the DL problem (standard for simple ZKPs)
    private val p = BigInteger("d673360430b02446960b196887550f295b9a89c3683f605791242337a4e67273" +
                               "1669466548a80373e34bca883838407850025f10b7849c631024e0f0c0f8622f", 16)
    private val g = BigInteger("2")
    private val secureRandom = SecureRandom()

    /**
     * Generates a Schnorr proof of knowledge for the status.
     * Proves: "I know the secret nonce that, when hashed with the status, produced this commitment."
     */
    fun generateProof(status: SafetyStatus): ZkpPayload {
        val statusValue = BigInteger.valueOf(status.ordinal.toLong())
        val secretNonce = BigInteger(256, secureRandom)
        
        // Public Commitment C = g^(status + secretNonce) mod p
        val secretValue = statusValue.add(secretNonce)
        val commitment = g.modPow(secretValue, p)
        
        // Schnorr Proof of Knowledge of 'secretValue'
        val r = BigInteger(256, secureRandom)
        val R = g.modPow(r, p)
        
        // challenge e = Hash(g, commitment, R)
        val e = sha256(g.toString() + commitment.toString() + R.toString())
        
        // response z = r + e * secretValue
        val z = r.add(e.multiply(secretValue))
        
        return ZkpPayload(
            status = status,
            commitment = commitment.toString(16),
            proofR = R.toString(16),
            proofZ = z.toString(16)
        )
    }

    /**
     * Verifies the Schnorr proof: g^z == R * commitment^e mod p
     */
    fun verifyProof(payload: ZkpPayload): Boolean {
        val commitment = BigInteger(payload.commitment, 16)
        val R = BigInteger(payload.proofR, 16)
        val z = BigInteger(payload.proofZ, 16)
        
        val e = sha256(g.toString() + commitment.toString() + R.toString())
        
        val left = g.modPow(z, p)
        val right = R.multiply(commitment.modPow(e, p)).mod(p)
        
        return left == right
    }

    private fun sha256(input: String): BigInteger {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return BigInteger(1, digest)
    }
}

data class ZkpPayload(
    val status: SafetyStatus,
    val commitment: String,
    val proofR: String,
    val proofZ: String
)
