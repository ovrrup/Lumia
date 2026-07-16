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

            // 5. Subject Notes Section"""

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
                                onClick = { viewModel.toggleAssignmentCompleted(assignment) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (assignment.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Toggle Complete",
                                    tint = if (assignment.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = assignment.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (assignment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    val catColor = try { Color(android.graphics.Color.parseColor(assignment.categoryColor)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(catColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(assignment.category, style = MaterialTheme.typography.bodySmall, color = catColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (assignment.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = assignment.description, 
                                        style = MaterialTheme.typography.bodyMedium, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                val tagsList = assignment.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                if (tagsList.isNotEmpty() || assignment.dueDateMillis > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically, 
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (assignment.dueDateMillis > 0) {
                                            val formattedDate = SimpleDateFormat("MMM dd", Locale.getDefault()).format(assignment.dueDateMillis)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically, 
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Rounded.Alarm, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                                                Text(formattedDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (tagsList.isNotEmpty()) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                tagsList.take(2).forEach { tag ->
                                                    val tagColors = getTagColors(tag)
                                                    Text(
                                                        text = tag,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = tagColors.second,
                                                        modifier = Modifier
                                                            .background(tagColors.first, RoundedCornerShape(8.dp))
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
                                    onClick = { showAssignmentMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Assignment Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = showAssignmentMenu,
                                    onDismissRequest = { showAssignmentMenu = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Start Pomodoro") },
                                        onClick = {
                                            showAssignmentMenu = false
                                            val courseParam = assignment.courseId?.let { "&courseId=$it" } ?: ""
                                            navController.navigate("pomodoro?subjectId=${subjectId}&assignmentId=${assignment.id}$courseParam")
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Timer, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit Assignment") },
                                        onClick = {
                                            showAssignmentMenu = false
                                            assignmentToEdit = assignment
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Assignment", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showAssignmentMenu = false
                                            viewModel.deleteAssignment(assignment)
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Subject Notes Section"""

start_idx = content.find(target)
end_idx = content.find(end_target)

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + replacement + content[end_idx + len(end_target):]
    with open("app/src/main/java/lumia/tracker/ui/screens/study/SubjectDetailScreen.kt", "w") as f:
        f.write(new_content)
    print("Patched Assignments.")
else:
    print("Assignments not found.")
