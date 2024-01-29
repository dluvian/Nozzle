package com.dluvian.nozzle.data.utils

import org.junit.Test

class UrlUtilsTest {

    @Test
    fun extractUrls() {
        val urls =
            "https://files.sovbit.host/media/public/f9916d6f0284cf51b6c1f8a970e5f47f74616bfc4e0fa5fbcc0c255ab12851ad.webp#m=image%2Fwebp&dim=720x960&blurhash=_5G8WH00_00LEjjF%25F0pnNf%2Bi%5E%25Laf4%3BNCELT0%7EU0i%5E*xZTL4%3A%3DtD*-QxbM%7BRQ_19Jk89ds%3A%250%24%2CE1%24fxCxYIV%25LIWj%3FNfE1Nbs%3Bk9xZ%251IT-n-%3AW%3B%25L57%252E1W%3DM%7BxuRk&x=24707b14e21e2f1bec6df0d0ef36358db9744777e6601392da582f7ce1ed0776\n" +
                    "https://files.sovbit.host/media/public/70f9eb7165121542ef8d7de75a3e60e73701972ea1b8f511d725afb3e0060043.webp#m=image%2Fwebp&dim=720x960&blurhash=_KHd%7EfD%25%7EVD%2BNIM%7CX95b%24%25NH%250s.oes%3A%5E%25t6E1M%7DRkazS4EnR-V%3F%251t5ofRjM%7BxZS5ofofs%3AWqo%7DR-slofaxj%5DRkW%3Fofj%5Ds%3ARjWBWUWBxaayNGR*M%7Bj%5DX8kDNGWBWBa%7Cs%3A&x=179c45db570cfb63c89a98ad71dec3d4a2479801f3774a7bd113a2b175a18881"

        val result = UrlUtils.extractUrls(urls)

        assert(result.size == 2)
        assert(result[0].value == "https://files.sovbit.host/media/public/f9916d6f0284cf51b6c1f8a970e5f47f74616bfc4e0fa5fbcc0c255ab12851ad.webp#m=image%2Fwebp&dim=720x960&blurhash=_5G8WH00_00LEjjF%25F0pnNf%2Bi%5E%25Laf4%3BNCELT0%7EU0i%5E*xZTL4%3A%3DtD*-QxbM%7BRQ_19Jk89ds%3A%250%24%2CE1%24fxCxYIV%25LIWj%3FNfE1Nbs%3Bk9xZ%251IT-n-%3AW%3B%25L57%252E1W%3DM%7BxuRk&x=24707b14e21e2f1bec6df0d0ef36358db9744777e6601392da582f7ce1ed0776")
        assert(result[1].value == "https://files.sovbit.host/media/public/70f9eb7165121542ef8d7de75a3e60e73701972ea1b8f511d725afb3e0060043.webp#m=image%2Fwebp&dim=720x960&blurhash=_KHd%7EfD%25%7EVD%2BNIM%7CX95b%24%25NH%250s.oes%3A%5E%25t6E1M%7DRkazS4EnR-V%3F%251t5ofRjM%7BxZS5ofofs%3AWqo%7DR-slofaxj%5DRkW%3Fofj%5Ds%3ARjWBWUWBxaayNGR*M%7Bj%5DX8kDNGWBWBa%7Cs%3A&x=179c45db570cfb63c89a98ad71dec3d4a2479801f3774a7bd113a2b175a18881")
    }
}
