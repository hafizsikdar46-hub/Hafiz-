package com.example.util

import com.example.model.PowerStatus

data class CommentaryContent(
    val headline: String,
    val subHeadline: String,
    val details: String
)

object BanglaCommentary {

    private val onVariations = listOf(
        CommentaryContent(
            headline = "🟢 কারেন্ট আছে ভাই! ⚡😂",
            subHeadline = "Wi-Fi-গুলো বেঁচে আছে, আশা করা যায় বিদ্যুৎও আছে!",
            details = "ফ্যান ছাড়ো, মোবাইল চার্জে দাও! রাউটারগুলো দিব্যি জীবিত হয়ে সিগন্যাল ছুড়ছে। 😎"
        ),
        CommentaryContent(
            headline = "🟢 বিদ্যুতের জোয়ার বইছে! ⚡🙌",
            subHeadline = "চারিদিকে ওয়াইফাইয়ের মেলা! লোডশেডিং এখন ছুটিতে।",
            details = "এসি অন করো ভাই, বিলের চিন্তা পরে! একাধিক রাউটার ফুল পাওয়ারে চলছে। 🚀"
        ),
        CommentaryContent(
            headline = "🟢 কারেন্ট জিন্দাবাদ! 💡⚡",
            subHeadline = "রাউটারগুলো সিগন্যালে ভেসে যাচ্ছে, ডেসকো/পল্লী বিদ্যুৎ মেহেরবান!",
            details = "স্ট্রং সিগন্যাল পাওয়া গেছে। চারপাশের প্রতিবেশীদের রাউটার একযোগে সার্ভিস দিচ্ছে!"
        ),
        CommentaryContent(
            headline = "🟢 ফ্যানের বাতাস উপভোগ করুন! 💨⚡",
            subHeadline = "সবাই শান্তিতে আছে, তারের মধ্যে রক্ত... থুক্কু বিদ্যুৎ দৌড়াচ্ছে!",
            details = "নেটওয়ার্ক সংখ্যা যথেষ্ট। গ্রিড বিদ্যুৎ সক্রিয় থাকার সম্ভাবনাই সবচেয়ে বেশি।"
        )
    )

    private val offVariations = listOf(
        CommentaryContent(
            headline = "🔴 কারেন্ট নাই মনে হচ্ছে! 😭",
            subHeadline = "Wi-Fi-ও চুপচাপ... ফ্যানের সাথে সম্পর্ক শেষ 💀",
            details = "চারিদিকে ঘুটঘুটে অন্ধকার আর নীরবতা! একটা রাউটারও ডাকছে না। হাতপাখা রেডি করো ভাই! 🪭"
        ),
        CommentaryContent(
            headline = "🔴 লোডশেডিং স্ট্রাইক! 🕯️💀",
            subHeadline = "রাউটার শহীদ হয়েছে! ডিজিটাল দুনিয়া এখন মোমবাতির আলোয়।",
            details = "কোনো ওয়াইফাই সিগন্যাল পাওয়া যায়নি। হয়তো পুরো পাড়ায় ব্ল্যাকআউট চলছে! 😭"
        ),
        CommentaryContent(
            headline = "🔴 কারেন্ট হাওয়া ভাই! 🔌🪫",
            subHeadline = "রাউটারদের গণমৃত্যু ঘটেছে! পল্লী বিদ্যুতের ভেলকিবাজি।",
            details = "আইপিএস থাকলে অন করো, নয়তো ছাদে বা বারান্দায় গিয়ে বাতাসের অপেক্ষায় বসো। 🕯️"
        ),
        CommentaryContent(
            headline = "🔴 বাত্তি গুল, মিটার ফুল! 🚫⚡",
            subHeadline = "বাতাস থেমে গেছে, রাউটারের বাত্তি বন্ধ!",
            details = "জিরো সিগন্যাল! এলাকায় ব্যাপক পাওয়ার কাট হওয়ার প্রবল সম্ভাবনা।"
        )
    )

    private val uncertainVariations = listOf(
        CommentaryContent(
            headline = "🟡 ব্যাপারটা সন্দেহজনক! 🤨",
            subHeadline = "নিজে গিয়ে সুইচ দেখে আসাই safest 😂",
            details = "মাত্র ১-২টা সিগন্যাল! পাশের বাসার আইপিএস নাকি কারো মোবাইলের হটস্পট—বুঝা মুশকিল। 🤷‍♂️"
        ),
        CommentaryContent(
            headline = "🟡 ধোঁয়াশা কাটছে না! 🤔",
            subHeadline = "রাউটার টিমটিম করছে, নিশ্চিত হওয়া যাচ্ছে না।",
            details = "সিগন্যাল খুব দুর্বল অথবা তথ্য সীমিত। চাক্ষুষ সুইচ পরীক্ষা ছাড়া ভরসা নাই! 🔌"
        ),
        CommentaryContent(
            headline = "🟡 কনফিউশন ম্যাক্সিমাম! 🧐",
            subHeadline = "কারেন্ট থাকতেও পারে, নাও থাকতে পারে! জেনারেটরের খেল?",
            details = "খুব সামান্য ওয়াইফাই দেখা যাচ্ছে। এটা ব্যাটারি ব্যাকআপ বা দূরবর্তী সিগন্যাল হতে পারে। 🔋"
        ),
        CommentaryContent(
            headline = "🟡 ৫০-৫০ সম্ভাবনা! ⚖️",
            subHeadline = "বিজ্ঞান এখানে দ্বিধাদ্বন্দ্বে! রাউটার দ্বিধায় ভুগছে।",
            details = "কিছু রাউটার হয়তো বন্ধ, কিছু হয়তো এখনো চলছে। সরাসরি ফ্যানের সুইচ টিপে দেখুন! 💡"
        )
    )

    fun getCommentary(status: PowerStatus, seed: Int): CommentaryContent {
        val list = when (status) {
            PowerStatus.PROBABLY_ON -> onVariations
            PowerStatus.PROBABLY_OFF -> offVariations
            PowerStatus.UNCERTAIN -> uncertainVariations
        }
        val index = (seed.coerceAtLeast(0)) % list.size
        return list[index]
    }
}
