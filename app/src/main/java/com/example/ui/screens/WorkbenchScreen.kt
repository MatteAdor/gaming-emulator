package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArchitectureDocs
import com.example.ui.theme.*

@Composable
fun WorkbenchScreen() {
    val context = LocalContext.current
    var selectedSectionId by remember { mutableStateOf(ArchitectureDocs.sections.first().id) }
    val currentSection = ArchitectureDocs.sections.first { it.id == selectedSectionId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Direct Header
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "Specifiche Architetturali",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Documentazione di sistema, roadmap compilazione e bridge nativo per Box64, Wine e Mesa Turnip.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            HorizontalDivider(color = AppBorderSubtle)
        }

        // Clean Category Selector Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ArchitectureDocs.sections.forEach { section ->
                    val isSelected = section.id == selectedSectionId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSectionId = section.id },
                        label = {
                            Text(
                                text = section.title.substringBefore("."),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppSurfaceElevated,
                            selectedLabelColor = TextPrimary,
                            containerColor = AppBackground,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = AppBorder,
                            selectedBorderColor = AccentBlue
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("workbench_tab_${section.id}")
                    )
                }
            }
        }

        // Section Content
        item {
            Column(modifier = Modifier.fillMaxWidth().testTag("workbench_content_card")) {
                Text(
                    text = currentSection.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = currentSection.subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Text(
                    text = currentSection.detailsMarkdown,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                if (currentSection.codeSnippet.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Codice sorgente (${currentSection.codeLanguage})",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Code", currentSection.codeSnippet))
                                Toast.makeText(context, "Copiato", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("copy_snippet_btn")
                        ) {
                            Text("Copia", fontSize = 12.sp, color = AccentBlue)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp)),
                        color = AppSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
                    ) {
                        Text(
                            text = currentSection.codeSnippet,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
