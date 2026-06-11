package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class CourseGrade(val id: Int, var credits: String = "", var grade: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CgpaCalculatorScreen(navController: NavController) {
    var nextId by remember { mutableIntStateOf(3) }
    val courseGrades = remember { mutableStateListOf(CourseGrade(0), CourseGrade(1), CourseGrade(2)) }
    var gpaResult by remember { mutableDoubleStateOf(0.0) }

    fun calculateGPA() {
        var totalCredits = 0.0
        var totalPoints = 0.0
        courseGrades.forEach { cg ->
            val c = cg.credits.toDoubleOrNull() ?: 0.0
            val g = cg.grade.toDoubleOrNull() ?: 0.0
            totalCredits += c
            totalPoints += c * g
        }
        gpaResult = if (totalCredits > 0) totalPoints / totalCredits else 0.0
    }

    val isGlass = com.example.ui.theme.LocalGlassMode.current
    Scaffold(
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("GPA/CGPA Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                courseGrades.add(CourseGrade(nextId))
                nextId++
            }) {
                Icon(Icons.Default.Add, "Add Course")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            com.example.ui.components.GlassHeroCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your GPA", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        text = String.format("%.2f", gpaResult),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(courseGrades, key = { _, item -> item.id }) { index, cg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = cg.credits,
                            onValueChange = { 
                                courseGrades[index] = courseGrades[index].copy(credits = it)
                                calculateGPA()
                            },
                            label = { Text("Credits (e.g. 3)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = cg.grade,
                            onValueChange = { 
                                courseGrades[index] = courseGrades[index].copy(grade = it)
                                calculateGPA()
                            },
                            label = { Text("Grade (e.g. 4.0)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        IconButton(onClick = { 
                            if(courseGrades.size > 1) {
                                courseGrades.removeAt(index)
                                calculateGPA()
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
