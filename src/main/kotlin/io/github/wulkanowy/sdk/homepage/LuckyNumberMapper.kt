package io.github.wulkanowy.sdk.homepage

import io.github.wulkanowy.sdk.pojo.LuckyNumber
import io.github.wulkanowy.api.home.LuckyNumber as ScrapperLuckyNumber

fun List<ScrapperLuckyNumber>.mapLuckyNumbers(): List<LuckyNumber> {
    return map {
        LuckyNumber(
            originalContent = it.originalContent,
            number = it.number,
            schoolName = it.schoolName
        )
    }
}
