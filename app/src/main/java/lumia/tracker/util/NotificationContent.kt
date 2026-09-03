package lumia.tracker.util

/**
 * Generates notification titles and descriptions matching the configured tone.
 * Notification templates are rule-based and designed to provide clear, actionable schedule reminders.
 */
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
        val cleanDesc = desc.trim()
        val hasDesc = cleanDesc.isNotBlank()
        
        when (type) {
            "class_start" -> {
                if (isFormal) {
                    titles.addAll(NotificationTemplates.formalClassStartTitles.map { it.format(title) })
                    descs.addAll(
                        if (hasDesc) {
                            listOf(
                                cleanDesc,
                                "Starting shortly: $cleanDesc",
                                "$cleanDesc. Please prepare your materials.",
                                "Your scheduled class is starting shortly. $cleanDesc"
                            )
                        } else {
                            listOf(
                                "Your scheduled class is starting shortly.",
                                "Please prepare for your upcoming session.",
                                "Time to transition to your next class.",
                                "Ensure you have all necessary materials ready."
                            )
                        }
                    )
                } else {
                    titles.addAll(NotificationTemplates.informalClassStartTitles.map { it.format(title) })
                    descs.addAll(
                        if (hasDesc) {
                            listOf(
                                cleanDesc,
                                "$cleanDesc — time to head over!",
                                "Class is starting shortly. $cleanDesc",
                                "Starting soon: $cleanDesc"
                            )
                        } else {
                            listOf(
                                "Class is starting shortly. Head over when you're ready!",
                                "Time to transition to your next class.",
                                "Every minute counts. Get ready to begin!",
                                "Your next session is about to start."
                            )
                        }
                    )
                }
            }
            "class_end" -> {
                if (isFormal) {
                    titles.addAll(NotificationTemplates.formalClassEndTitles.map { it.format(title) })
                    descs.addAll(listOf(
                        "Please update your attendance status.",
                        "Session concluded. Take a moment to log your attendance.",
                        "Class is over. Make sure to log any new assignments and attendance.",
                        "Don't forget to mark your attendance for this session.",
                        "Session complete. Review your notes and log attendance."
                    ))
                } else {
                    titles.addAll(NotificationTemplates.informalClassEndTitles.map { it.format(title) })
                    descs.addAll(listOf(
                        "Class is done! Remember to log your attendance.",
                        "Take a breather and log your attendance.",
                        "Great job wrapping up that class. Log your attendance when ready.",
                        "Session concluded. Mark your attendance in the app.",
                        "Quickly update your attendance while it's fresh."
                    ))
                }
            }
            "task", "assignment" -> {
                val typeName = if (type == "task") "Task" else "Assignment"
                if (isFormal) {
                    titles.addAll(listOf(
                        "Deadline Reminder: $title",
                        "$typeName Due Soon: $title",
                        "Approaching Deadline: $title",
                        "Action Required: $title",
                        "Pending $typeName: $title"
                    ))
                    descs.addAll(
                        if (hasDesc) {
                            listOf(
                                cleanDesc,
                                "$cleanDesc. Please review your progress on this $typeName.",
                                "Reminder: $cleanDesc."
                            )
                        } else {
                            listOf(
                                "Please ensure this $typeName is completed on time.",
                                "Your attention is needed for this $typeName.",
                                "Review your progress on this $typeName."
                            )
                        }
                    )
                } else {
                    titles.addAll(listOf(
                        "Reminder: $title",
                        "$typeName due soon: $title",
                        "Upcoming deadline: $title",
                        "Time to review $title",
                        "Don't forget: $title"
                    ))
                    descs.addAll(
                        if (hasDesc) {
                            listOf(
                                cleanDesc,
                                "$cleanDesc — stay focused and make some progress!",
                                "$cleanDesc — keep up the momentum and finish strong."
                            )
                        } else {
                            listOf(
                                "You have an upcoming deadline for this item.",
                                "Stay focused and make some progress on this today.",
                                "Keep up the momentum and wrap this item up."
                            )
                        }
                    )
                }
            }
            "daily_digest" -> {
                if (isFormal) {
                    titles.addAll(listOf("Daily Digest: $title", "Daily Summary: $title", "Today's Schedule: $title"))
                    descs.addAll(
                        if (hasDesc) {
                            listOf(
                                cleanDesc,
                                "Here is a summary of your upcoming schedule: $cleanDesc",
                                "Today's overview: $cleanDesc"
                            )
                        } else {
                            listOf(
                                "Here is an overview of your schedule and tasks for today.",
                                "Review your agenda to stay organized and on track.",
                                "Check your upcoming classes and deadlines for today."
                            )
                        }
                    )
                } else {
                    titles.addAll(listOf("Today's Agenda: $title", "Your Daily Overview: $title", "Ready for Today: $title"))
                    descs.addAll(
                        if (hasDesc) {
                            listOf(
                                cleanDesc,
                                "Here's what you have on deck today: $cleanDesc",
                                "Today's plan: $cleanDesc"
                            )
                        } else {
                            listOf(
                                "Here is a quick look at your schedule and tasks for today.",
                                "Stay organized and have a productive day ahead!",
                                "Check your schedule and tackle what's next."
                            )
                        }
                    )
                }
            }
            else -> {
                titles.add(title)
                descs.add(if (hasDesc) cleanDesc else "Reminder for $title")
            }
        }
        
        val finalTitle = titles.random()
        var finalDesc = descs.random()
        
        if (interconnections.isNotBlank()) {
            finalDesc += "\nLinked with: ${interconnections.trim()}"
        }
        
        return Pair(finalTitle, finalDesc)
    }
}
