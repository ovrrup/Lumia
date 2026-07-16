import re

with open("app/src/main/java/lumia/tracker/ui/screens/study/SubjectDetailScreen.kt", "r") as f:
    content = f.read()

target = """                                        if (tagsList.isNotEmpty()) {
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
                                        }"""

replacement = """                                        if (tagsList.isNotEmpty()) {
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
                                        }"""

if target in content:
    new_content = content.replace(target, replacement)
    with open("app/src/main/java/lumia/tracker/ui/screens/study/SubjectDetailScreen.kt", "w") as f:
        f.write(new_content)
    print("Patched Tasks Fix.")
else:
    print("Not found.")
