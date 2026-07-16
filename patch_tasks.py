import re

with open("app/src/main/java/lumia/tracker/ui/screens/study/SubjectDetailScreen.kt", "r") as f:
    content = f.read()

target = """                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {"""

end_target = """                                }
                            }
                        }
                    }
                }
            }

            // 4. Assignments & Exams Section"""

replacement = """                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            lumia.tracker.ui.components.BouncyIconButton(
                                onClick = { viewModel.toggleTaskCompleted(task) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Toggle Complete",
                                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                )
                                if (task.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = task.description, 
                                        style = MaterialTheme.typography.bodyMedium, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                val tagsList = task.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                if (tagsList.isNotEmpty() || task.dueDateMillis != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically, 
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (task.dueDateMillis != null) {
                                            val formattedDate = SimpleDateFormat("MMM dd", Locale.getDefault()).format(task.dueDateMillis)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically, 
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                                                Text(formattedDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (tagsList.isNotEmpty()) {
                                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                tagsList.take(2).forEach { tag ->
                                                    val color = getTagColors(tag)
                                                    Text(
                                                        text = tag,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = color,
                                                        modifier = Modifier
                                                            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Box {
                                lumia.tracker.ui.components.BouncyIconButton(
                                    onClick = { showTaskMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Task Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = showTaskMenu,
                                    onDismissRequest = { showTaskMenu = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Start Pomodoro") },
                                        onClick = {
                                            showTaskMenu = false
                                            val courseParam = task.courseId?.let { "&courseId=$it" } ?: ""
                                            navController.navigate("pomodoro?subjectId=${subjectId}&taskId=${task.id}$courseParam")
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Timer, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit Task") },
                                        onClick = {
                                            showTaskMenu = false
                                            taskToEdit = task
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Task", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showTaskMenu = false
                                            viewModel.deleteTask(task)
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Assignments & Exams Section"""

start_idx = content.find(target)
end_idx = content.find(end_target)

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + replacement + content[end_idx + len(end_target):]
    with open("app/src/main/java/lumia/tracker/ui/screens/study/SubjectDetailScreen.kt", "w") as f:
        f.write(new_content)
    print("Patched Tasks.")
else:
    print("Tasks not found.")
