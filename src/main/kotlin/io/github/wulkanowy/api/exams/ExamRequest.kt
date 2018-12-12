package io.github.wulkanowy.api.exams

import com.google.gson.annotations.SerializedName
import java.util.*

data class ExamRequest(

        @SerializedName("data")
        val date: Date,

        @SerializedName("rokSzkolny")
        val schoolYear: Int
)
