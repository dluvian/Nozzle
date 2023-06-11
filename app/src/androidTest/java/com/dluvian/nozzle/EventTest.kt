package com.dluvian.nozzle

import com.dluvian.nozzle.data.nostr.client.model.Event
import org.junit.Test

internal class EventTest {

    @Test
    fun verifyReturnsTrueOnValidEvent() {
        val jsonStr = "{\n" +
                "  \"id\": \"4ebce2dacee0d980eaf63577d2bfe1edb6266cdb9735361408fd371bc318445e\",\n" +
                "  \"pubkey\": \"e1ff3bfdd4e40315959b08b4fcc8245eaa514637e1d4ec2ae166b743341be1af\",\n" +
                "  \"created_at\": 1673527944,\n" +
                "  \"kind\": 1,\n" +
                "  \"tags\": [\n" +
                "    [\n" +
                "      \"e\",\n" +
                "      \"5aec96ff6cdac68fb2a573bdafbbe9f7c6522032f986052acd4ea0da61ce2ee5\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"e\",\n" +
                "      \"98f6bd43dd55b1fc7ca630f4b9d8fe0e2c2272db1ee8ce6c10f4834f14dad461\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"p\",\n" +
                "      \"65594f279a789982b55c02a38c92a99b986f891d2814c5f553d1bbfe3e23853d\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"p\",\n" +
                "      \"aea4ad900a3af5371fd97b0e503d60af9d8dc1960b4d01628eb100b4260ce5fc\"\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"content\": \"Bitcoin Core's only flaw\",\n" +
                "  \"sig\": \"5461a4d9a82a288a0a83ab3005f7fa776aeb44d6933daaf0bac3df49fc8b26227cb8452f2586e71e59c0b518e98eab887683d93adfb3a987aab51781939bc256\"\n" +
                "}"
        val event = Event.fromJson(jsonStr).getOrThrow()

        val result = event.verify()

        assert(result)
    }
}
