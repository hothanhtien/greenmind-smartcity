package com.vodang.greenmind.preappsurvey

// ── Design tokens (shared with PreAppSurveyScreen) ────────────────────────────
private val green800 = androidx.compose.ui.graphics.Color(0xFF2E7D32)
private val green600 = androidx.compose.ui.graphics.Color(0xFF388E3C)
private val green400 = androidx.compose.ui.graphics.Color(0xFF66BB6A)
private val green50  = androidx.compose.ui.graphics.Color(0xFFE8F5E9)
private val surfaceColor = androidx.compose.ui.graphics.Color(0xFFF9FBF9)

// ── Data ──────────────────────────────────────────────────────────────────────

data class SurveyOption(val value: String, val label: String, val emoji: String)
data class SurveyQuestion(
    val key: String,
    val emoji: String,
    val questionEn: String,
    val questionVi: String,
    val options: List<SurveyOption> = emptyList(),
    val isNumericInput: Boolean = false,
    val inputHintEn: String = "",
    val inputHintVi: String = "",
    val inputSuffixEn: String = "",
    val inputSuffixVi: String = "",
)

val questions = listOf(
    // daily_spending: free numeric input (VND, > 0)
    SurveyQuestion(
        key = "daily_spending",
        emoji = "💸",
        questionEn = "How much do you typically spend per day on consumer purchases?",
        questionVi = "Bạn thường chi tiêu bao nhiêu mỗi ngày cho mua sắm tiêu dùng?",
        isNumericInput = true,
        inputHintEn = "e.g. 150000",
        inputHintVi = "ví dụ: 150000",
        inputSuffixEn = "VND / day",
        inputSuffixVi = "đ / ngày",
    ),
    // spending_variation: 1–5 scale
    SurveyQuestion(
        key = "spending_variation",
        emoji = "📊",
        questionEn = "How much does your spending vary from week to week?",
        questionVi = "Chi tiêu của bạn thay đổi bao nhiêu giữa các tuần?",
        options = listOf(
            SurveyOption("1", "Very stable",       "🧊"),
            SurveyOption("2", "Mostly stable",     "😊"),
            SurveyOption("3", "Somewhat varied",   "🤔"),
            SurveyOption("4", "Quite varied",      "🌊"),
            SurveyOption("5", "Very unpredictable","🎲"),
        )
    ),
    // brand_trial: 1–5 scale
    SurveyQuestion(
        key = "brand_trial",
        emoji = "🛍️",
        questionEn = "How often do you try new brands or products?",
        questionVi = "Bạn có thường xuyên thử thương hiệu hoặc sản phẩm mới không?",
        options = listOf(
            SurveyOption("1", "Never",     "🚫"),
            SurveyOption("2", "Rarely",    "😴"),
            SurveyOption("3", "Sometimes", "🙂"),
            SurveyOption("4", "Often",     "😄"),
            SurveyOption("5", "Always",    "🤩"),
        )
    ),
    // shopping_list: 1–5 scale
    SurveyQuestion(
        key = "shopping_list",
        emoji = "📋",
        questionEn = "Do you usually shop with a planned list?",
        questionVi = "Bạn có thường mua sắm theo danh sách đã lên kế hoạch không?",
        options = listOf(
            SurveyOption("1", "Never",     "🚫"),
            SurveyOption("2", "Rarely",    "😅"),
            SurveyOption("3", "Sometimes", "🙂"),
            SurveyOption("4", "Usually",   "✅"),
            SurveyOption("5", "Always",    "📝"),
        )
    ),
    // daily_distance: free numeric input (km, > 0)
    SurveyQuestion(
        key = "daily_distance",
        emoji = "🚶",
        questionEn = "How far do you typically travel on a normal day?",
        questionVi = "Bạn thường di chuyển bao xa trong một ngày bình thường?",
        isNumericInput = true,
        inputHintEn = "e.g. 5",
        inputHintVi = "ví dụ: 5",
        inputSuffixEn = "km / day",
        inputSuffixVi = "km / ngày",
    ),
    // new_places: 1–5 scale
    SurveyQuestion(
        key = "new_places",
        emoji = "🗺️",
        questionEn = "How often do you visit new or unfamiliar places?",
        questionVi = "Bạn có thường xuyên ghé thăm những nơi mới không?",
        options = listOf(
            SurveyOption("1", "Never",       "🏠"),
            SurveyOption("2", "Rarely",      "😴"),
            SurveyOption("3", "Sometimes",   "🙂"),
            SurveyOption("4", "Often",       "🗺️"),
            SurveyOption("5", "Very often",  "✈️"),
        )
    ),
    // public_transport: 1–5 scale
    SurveyQuestion(
        key = "public_transport",
        emoji = "🚌",
        questionEn = "How often do you use public transport?",
        questionVi = "Bạn có thường xuyên sử dụng phương tiện công cộng không?",
        options = listOf(
            SurveyOption("1", "Never",     "🚫"),
            SurveyOption("2", "Rarely",    "😴"),
            SurveyOption("3", "Sometimes", "🙂"),
            SurveyOption("4", "Often",     "🚌"),
            SurveyOption("5", "Always",    "🌍"),
        )
    ),
    // stable_schedule: 1–5 scale
    SurveyQuestion(
        key = "stable_schedule",
        emoji = "🗓️",
        questionEn = "How stable and predictable is your daily routine?",
        questionVi = "Lịch trình hàng ngày của bạn có ổn định và dễ đoán không?",
        options = listOf(
            SurveyOption("1", "Very unpredictable",   "🎲"),
            SurveyOption("2", "Mostly unpredictable", "🌊"),
            SurveyOption("3", "Mixed",                "🤔"),
            SurveyOption("4", "Mostly stable",        "😊"),
            SurveyOption("5", "Very stable",          "⏰"),
        )
    ),
    // night_outings: 1–7 (nights per week)
    SurveyQuestion(
        key = "night_outings",
        emoji = "🌙",
        questionEn = "How many nights per week do you go out in the evening?",
        questionVi = "Bạn ra ngoài vào buổi tối mấy đêm mỗi tuần?",
        options = listOf(
            SurveyOption("1", "1 night / week",  "🏠"),
            SurveyOption("2", "2 nights / week", "😴"),
            SurveyOption("3", "3 nights / week", "🌆"),
            SurveyOption("4", "4 nights / week", "🌙"),
            SurveyOption("5", "5 nights / week", "🦉"),
            SurveyOption("6", "6 nights / week", "🌃"),
            SurveyOption("7", "Every night",     "🎉"),
        )
    ),
    // healthy_eating: 1–5 scale
    SurveyQuestion(
        key = "healthy_eating",
        emoji = "🥗",
        questionEn = "How would you rate your overall eating habits?",
        questionVi = "Bạn đánh giá thói quen ăn uống của mình thế nào?",
        options = listOf(
            SurveyOption("1", "Very unhealthy",   "🍔"),
            SurveyOption("2", "Mostly unhealthy", "😅"),
            SurveyOption("3", "Mixed",            "🤔"),
            SurveyOption("4", "Mostly healthy",   "🥗"),
            SurveyOption("5", "Very healthy",     "🌿"),
        )
    ),
    // social_media: 1–5 scale
    SurveyQuestion(
        key = "social_media",
        emoji = "📱",
        questionEn = "How much time do you spend on social media each day?",
        questionVi = "Bạn dành bao nhiêu thời gian cho mạng xã hội mỗi ngày?",
        options = listOf(
            SurveyOption("1", "< 30 min",       "⚡"),
            SurveyOption("2", "30 min – 1 hr",  "🙂"),
            SurveyOption("3", "1 – 2 hrs",      "😐"),
            SurveyOption("4", "2 – 4 hrs",      "😅"),
            SurveyOption("5", "> 4 hrs",        "📱"),
        )
    ),
    // goal_setting: 1–5 scale
    SurveyQuestion(
        key = "goal_setting",
        emoji = "🎯",
        questionEn = "How often do you set and actively track personal goals?",
        questionVi = "Bạn có thường xuyên đặt và theo dõi mục tiêu cá nhân không?",
        options = listOf(
            SurveyOption("1", "Never",     "🚫"),
            SurveyOption("2", "Rarely",    "😴"),
            SurveyOption("3", "Sometimes", "🙂"),
            SurveyOption("4", "Often",     "🎯"),
            SurveyOption("5", "Always",    "🏆"),
        )
    ),
    // mood_swings: 1–5 scale
    SurveyQuestion(
        key = "mood_swings",
        emoji = "🎭",
        questionEn = "How often do you experience significant mood swings?",
        questionVi = "Bạn có thường xuyên trải qua những thay đổi tâm trạng đáng kể không?",
        options = listOf(
            SurveyOption("1", "Never",      "😌"),
            SurveyOption("2", "Rarely",     "🙂"),
            SurveyOption("3", "Sometimes",  "😐"),
            SurveyOption("4", "Often",      "😟"),
            SurveyOption("5", "Very often", "🎭"),
        )
    ),
)

// ── Vietnamese option labels ──────────────────────────────────────────────────

fun viOptionLabel(key: String, value: String): String = when (key) {
    "spending_variation" -> when (value) {
        "1" -> "Rất ổn định"; "2" -> "Khá ổn định"; "3" -> "Hơi thay đổi"; "4" -> "Khá thay đổi"; else -> "Rất thất thường"
    }
    "brand_trial" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Luôn luôn"
    }
    "shopping_list" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường"; else -> "Luôn luôn"
    }
    "new_places" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Rất thường xuyên"
    }
    "public_transport" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Luôn luôn"
    }
    "stable_schedule" -> when (value) {
        "1" -> "Rất thất thường"; "2" -> "Khá thất thường"; "3" -> "Hỗn hợp"; "4" -> "Khá ổn định"; else -> "Rất ổn định"
    }
    "night_outings" -> when (value) {
        "1" -> "1 đêm / tuần"; "2" -> "2 đêm / tuần"; "3" -> "3 đêm / tuần"
        "4" -> "4 đêm / tuần"; "5" -> "5 đêm / tuần"; "6" -> "6 đêm / tuần"
        else -> "Mỗi đêm"
    }
    "healthy_eating" -> when (value) {
        "1" -> "Rất không lành mạnh"; "2" -> "Không lành mạnh"; "3" -> "Hỗn hợp"; "4" -> "Khá lành mạnh"; else -> "Rất lành mạnh"
    }
    "social_media" -> when (value) {
        "1" -> "< 30 phút"; "2" -> "30 phút – 1 tiếng"; "3" -> "1 – 2 tiếng"; "4" -> "2 – 4 tiếng"; else -> "> 4 tiếng"
    }
    "goal_setting" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Luôn luôn"
    }
    "mood_swings" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Rất thường xuyên"
    }
    else -> value
}