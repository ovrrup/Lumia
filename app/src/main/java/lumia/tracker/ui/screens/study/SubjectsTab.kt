package lumia.tracker.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import lumia.tracker.ui.theme.glassBar
import lumia.tracker.ui.theme.bouncyScale
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.Subject
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.viewmodel.ScholarViewModel

import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.BouncyFloatingActionButton
import lumia.tracker.ui.components.GlassCard

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SubjectsTab(
    navController: NavController,
    viewModel: ScholarViewModel,
    bottomPadding: PaddingValues,
    onEditSubject: (Subject) -> Unit,
    onAddSubjectClick: () -> Unit
) {
    var subjectToEdit by remember { mutableStateOf<lumia.tracker.model.Subject?>(null) }
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current
    val betaEnhancedHeader by viewModel.betaEnhancedHeader.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            BouncyFloatingActionButton(
                onClick = onAddSubjectClick,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding())
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Subject")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, 
                top = bottomPadding.calculateTopPadding() + 16.dp, bottom = bottomPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (subjects.isEmpty()) {
                item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No subjects yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            items(subjects, key = { it.id }) { subject ->
                var expanded by remember { mutableStateOf(false) }
                var showDetails by remember { mutableStateOf(false) }
                val linkedCourses = remember(courses, subject) {
                    courses.filter { course ->
                        course.subjectId == subject.id || 
                        course.subjectIds.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(subject.id)
                    }
                }
                
                GlassCard(
                    onClick = { navController.navigate("subjectDetail/${subject.id}") },
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.tertiaryContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = subject.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Box {
                                BouncyIconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            expanded = false
                                            subjectToEdit = subject
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            expanded = false
                                            viewModel.deleteSubject(subject)
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sell,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tags: ${if(subject.tags.isNotBlank()) subject.tags else "None"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (linkedCourses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Connected Courses",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (course in linkedCourses) {
                                    val courseColor = try { 
                                        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(course.colorHex)) 
                                    } catch (e: Exception) { 
                                        MaterialTheme.colorScheme.secondary 
                                    }
                                    SuggestionChip(
                                        onClick = { navController.navigate("courseDetail/${course.id}") },
                                        label = { 
                                            Text(
                                                text = if (course.code.isNotBlank()) "${course.code}: ${course.name}" else course.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = courseColor.copy(alpha = 0.15f),
                                            labelColor = courseColor
                                        ),
                                        border = SuggestionChipDefaults.suggestionChipBorder(
                                            enabled = true,
                                            borderColor = courseColor.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
    if (subjectToEdit != null) {
        lumia.tracker.ui.screens.study.EditSubjectDialog(
            subject = subjectToEdit!!,
            viewModel = viewModel,
            onDismiss = { subjectToEdit = null }
        )
    }
}
