package lumia.tracker.util

object NotificationContent {
    
    fun getPersonalizedContent(
        type: String, 
        title: String, 
        desc: String, 
        tone: String, 
        interconnections: String = ""
    ): Pair<String, String> {
        val isFormal = tone == "Formal" || tone == "Motivational"
        val titles = mutableListOf<String>()
        val descs = mutableListOf<String>()
        
        when (type) {
            "class_start" -> {
                if (isFormal) {
                    titles.addAll(NotificationTemplates.formalClassStartTitles.map { it.format(title) })
                    descs.addAll(listOf(
                        desc, "Please prepare for your upcoming session.",
                        "Your scheduled class is starting shortly. $desc",
                        "Time to transition to your next class. $desc",
                        "Ensure you have all necessary materials ready."
                    ))
                } else {
                    titles.addAll(NotificationTemplates.informalClassStartTitles.map { it.format(title) })
                    descs.addAll(listOf(
                        desc, "Get a move on, you don't want to miss anything!",
                        "Stop procrastinating and get to class! $desc",
                        "Every minute counts. Head over now!",
                        "You're almost out of time for this one."
                    ))
                }
            }
            "class_end" -> {
                if (isFormal) {
                    titles.addAll(NotificationTemplates.formalClassEndTitles.map { it.format(title) })
                    descs.addAll(listOf(
                        "Please update your attendance status.", "Take a moment to review your notes.",
                        "Class is over. Make sure to log any new assignments.",
                        "Don't forget to mark your attendance for this session.",
                        "Session complete. Prepare for your next activity."
                    ))
                } else {
                    titles.addAll(NotificationTemplates.informalClassEndTitles.map { it.format(title) })
                    descs.addAll(listOf(
                        "Did you pay attention? Log your attendance now!",
                        "Quickly update your attendance before you forget.",
                        "Take a breather, but update the app first.",
                        "Good job getting through that one. Mark it down.",
                        "Don't slack off now, keep your records straight!"
                    ))
                }
            }
            "task", "assignment" -> {
                val typeName = if (type == "task") "Task" else "Assignment"
                if (isFormal) {
                    titles.addAll(listOf(
                        "Deadline Reminder: $title", "Upcoming $typeName: $title",
                        "$typeName Due Soon: $title", "Action Required: $title",
                        "Approaching Deadline: $title", "Pending $typeName: $title"
                    ))
                    descs.addAll(listOf(
                        desc, "Please ensure this is completed on time.",
                        "Your attention is needed for this item.",
                        "Review your progress on this $typeName."
                    ))
                } else {
                    titles.addAll(listOf(
                        "URGENT: $title is DUE!", "Don't forget: $title!",
                        "You're running out of time for $title!", "Tick tock! $title is waiting.",
                        "Alert! $title needs you.", "Focus time! Finish $title."
                    ))
                    descs.addAll(listOf(
                        "Are you procrastinating? $desc", "Stop scrolling and start working on this!",
                        "You know you have to do this. Get to it.", "No more excuses, time to grind."
                    ))
                }
            }
            "daily_digest" -> {
                if (isFormal) {
                    titles.addAll(listOf("Daily Digest: $title", "Daily Summary: $title", "Today's Overview: $title"))
                    descs.addAll(listOf(desc, "Here is a summary of your upcoming items. $desc", "Review your schedule. $desc"))
                } else {
                    titles.addAll(listOf("Wake up! $title", "Brace yourself! $title", "It's grind time! $title"))
                    descs.addAll(listOf("Tick tock! $desc", "Are you ready to crush this? $desc", "Time is running out. $desc"))
                }
            }
            else -> {
                titles.add(title)
                descs.add(desc)
            }
        }
        
        val finalTitle = titles.random()
        var finalDesc = descs.random()
        
        if (interconnections.isNotBlank()) {
            finalDesc += if (isFormal) "\nLinked with: $interconnections" else "\nIt's linked to: $interconnections."
        }
        
        return Pair(finalTitle, finalDesc)
    }
}
