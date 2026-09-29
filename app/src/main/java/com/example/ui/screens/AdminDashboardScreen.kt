package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Complaint
import com.example.data.User
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.viewmodel.TriageViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    viewModel: TriageViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allComplaints by viewModel.allComplaints.collectAsState()
    val filteredComplaints by viewModel.filteredComplaints.collectAsState()
    val demoUsers by viewModel.availableDemoUsers.collectAsState()
    val hotspots by viewModel.hotspotAreas.collectAsState()

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val selectedDepartment by viewModel.selectedDepartment.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedWard by viewModel.selectedWard.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val detailComplaint by viewModel.detailComplaint.collectAsState()

    // Calculated Statistics
    val totalCount = allComplaints.size
    val criticalCount = allComplaints.count { it.priority.equals("CRITICAL", ignoreCase = true) }
    val inProgressCount = allComplaints.count { it.status.equals("IN PROGRESS", ignoreCase = true) }
    val resolvedCount = allComplaints.count {
        it.status.equals("RESOLVED", ignoreCase = true) || it.status.equals("CLOSED", ignoreCase = true)
    }
    val overdueCount = allComplaints.count { viewModel.isComplaintOverdue(it) }

    var showFilterPanel by remember { mutableStateOf(false) }
    var showAnalyticsPanel by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // Top Statistics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatCard(
                title = "Total",
                count = totalCount,
                icon = Icons.Default.AssignmentTurnedIn,
                accentColor = Color(0xFF2563EB),
                modifier = Modifier.weight(1f).testTag("stat_total")
            )
            StatCard(
                title = "Critical",
                count = criticalCount,
                icon = Icons.Default.Dangerous,
                accentColor = Color(0xFFDC2626),
                modifier = Modifier.weight(1f).testTag("stat_critical")
            )
            StatCard(
                title = "Overdue",
                count = overdueCount,
                icon = Icons.Default.HourglassBottom,
                accentColor = Color(0xFFB91C1C),
                modifier = Modifier.weight(1f).testTag("stat_overdue")
            )
            StatCard(
                title = "In Progress",
                count = inProgressCount,
                icon = Icons.Default.PendingActions,
                accentColor = Color(0xFFD97706),
                modifier = Modifier.weight(1f).testTag("stat_in_progress")
            )
            StatCard(
                title = "Resolved",
                count = resolvedCount,
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF16A34A),
                modifier = Modifier.weight(1f).testTag("stat_resolved")
            )
        }

        // Demo Data Controls Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Demo Data Control:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E40AF)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { viewModel.loadDemoComplaints() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier.height(30.dp).testTag("load_demo_data_button")
                    ) {
                        Text("+ Load Demo Complaints", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearDemoComplaints() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.height(30.dp).testTag("clear_demo_data_button")
                    ) {
                        Text("Clear Demo Data", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search & Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search by ID, Ward, officer, category...", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(15.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("admin_search_input"),
                shape = RoundedCornerShape(8.dp)
            )

            // Filter Toggle
            OutlinedButton(
                onClick = { showFilterPanel = !showFilterPanel },
                modifier = Modifier.height(46.dp).testTag("toggle_filters_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (showFilterPanel) Color(0xFFEFF6FF) else Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Filters", fontSize = 11.sp)
            }

            // Analytics Toggle
            OutlinedButton(
                onClick = { showAnalyticsPanel = !showAnalyticsPanel },
                modifier = Modifier.height(46.dp).testTag("toggle_analytics_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (showAnalyticsPanel) Color(0xFFEFF6FF) else Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Analytics", fontSize = 11.sp)
            }

            // Settings Button
            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.height(46.dp).testTag("admin_settings_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
            }
        }

        // Expandable Filter Bar
        if (showFilterPanel) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter Complaints", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { viewModel.resetFilters() }) {
                            Text("Reset All", fontSize = 11.sp, color = Color(0xFF2563EB))
                        }
                    }

                    FilterRow(
                        title = "Priority",
                        options = listOf("ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW"),
                        selected = selectedPriority,
                        onSelect = { viewModel.selectedPriority.value = it }
                    )

                    FilterRow(
                        title = "Status",
                        options = listOf("ALL", "NEW", "ASSIGNED", "IN PROGRESS", "RESOLVED", "CLOSED", "PENDING_AI"),
                        selected = selectedStatus,
                        onSelect = { viewModel.selectedStatus.value = it }
                    )

                    FilterRow(
                        title = "Ward",
                        options = listOf("ALL", "Ward K-West", "Ward G-North", "Ward H-West", "Ward L", "Ward T", "Ward P-South", "Ward H-East"),
                        selected = selectedWard,
                        onSelect = { viewModel.selectedWard.value = it }
                    )

                    FilterRow(
                        title = "Category",
                        options = listOf(
                            "ALL",
                            "Pothole / Road Damage",
                            "Drainage / Waterlogging",
                            "Waste / Garbage",
                            "Water Supply",
                            "Streetlight / Electrical",
                            "Sewage",
                            "Traffic / Road Obstruction"
                        ),
                        selected = selectedCategory,
                        onSelect = { viewModel.selectedCategory.value = it }
                    )
                }
            }
        }

        // Expandable Analytics & Hotspots Panel
        if (showAnalyticsPanel) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Civic Hotspot Areas & Department Analytics", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                    if (hotspots.isEmpty()) {
                        Text("No critical hotspots detected currently.", fontSize = 11.sp, color = Color(0xFF64748B))
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            hotspots.forEach { hotspot ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (hotspot.highestPriority == "CRITICAL") Color(0xFFFEE2E2) else Color(0xFFFFEDD5),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (hotspot.highestPriority == "CRITICAL") Color(0xFFFCA5A5) else Color(0xFFFDBA74))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("🔥 ${hotspot.ward}: ${hotspot.count} Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                        Text("Main Issue: ${hotspot.topCategory}", fontSize = 10.sp, color = Color(0xFF78350F))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Table Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ticket ID & Ward", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(1.3f))
                Text("Category & Dept", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(1.5f))
                Text("Priority", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(0.9f))
                Text("Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(1.0f))
            }
        }

        // Complaints Table Rows
        if (filteredComplaints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No complaints found.", color = Color(0xFF64748B), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(onClick = { viewModel.resetFilters() }) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredComplaints, key = { it.id }) { complaint ->
                    AdminComplaintRow(
                        complaint = complaint,
                        isOverdue = viewModel.isComplaintOverdue(complaint),
                        onClick = { viewModel.openDetail(complaint) }
                    )
                }
            }
        }
    }

    // Detail & Officer Assignment Modal
    if (detailComplaint != null) {
        val officers = demoUsers.filter { it.role == "OFFICER" }
        AdminDetailDialog(
            complaint = detailComplaint!!,
            officers = officers,
            isOverdue = viewModel.isComplaintOverdue(detailComplaint!!),
            onDismiss = { viewModel.closeDetail() },
            onAssignOfficer = { officer -> viewModel.assignOfficer(detailComplaint!!.id, officer) },
            onStatusChange = { newStatus -> viewModel.updateStatus(detailComplaint!!.id, newStatus) },
            onRunAi = { viewModel.runAiAnalysisOnPending(detailComplaint!!) },
            onDelete = { viewModel.deleteComplaint(detailComplaint!!.id) }
        )
    }
}

@Composable
fun AdminDetailDialog(
    complaint: Complaint,
    officers: List<User>,
    isOverdue: Boolean,
    onDismiss: () -> Unit,
    onAssignOfficer: (User) -> Unit,
    onStatusChange: (String) -> Unit,
    onRunAi: () -> Unit,
    onDelete: () -> Unit
) {
    var showOfficerMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Ticket #${complaint.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Ward: ${complaint.ward} • AI Confidence: ${complaint.confidence}", fontSize = 11.sp, color = Color(0xFF2563EB))
                }
                PriorityBadge(priority = complaint.priority)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isOverdue) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFEE2E2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔴 SLA ESCALATION: Complaint is OVERDUE for Department SLA.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                if (complaint.status == "PENDING_AI") {
                    Button(
                        onClick = onRunAi,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("run_ai_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run AI Analysis on this Ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // AI Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("AI Triage Summary:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                        Text(complaint.summary, fontSize = 12.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Priority Reason: ${complaint.priorityReason}", fontSize = 11.sp, color = Color(0xFF475569))
                    }
                }

                // Citizen Report
                Text("Citizen: ${complaint.citizenName} (${complaint.citizenContact})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(complaint.description, fontSize = 12.sp, color = Color(0xFF0F172A))
                Text("Location: ${complaint.location}", fontSize = 11.sp, color = Color(0xFF475569))

                // Citizen Photo if available
                if (!complaint.imageUri.isNullOrBlank()) {
                    Text("Citizen Evidence Photo:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    AsyncImage(
                        model = complaint.imageUri,
                        contentDescription = "Citizen photo",
                        modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Resolution Photo if available
                if (!complaint.resolutionImageUri.isNullOrBlank()) {
                    Text("Officer Resolution Evidence Photo:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                    AsyncImage(
                        model = complaint.resolutionImageUri,
                        contentDescription = "Resolution photo",
                        modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Officer Assignment
                Text("Officer Assignment:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Current: ${complaint.assignedOfficerName ?: "Unassigned"}", fontSize = 12.sp, color = Color(0xFF475569))

                Box {
                    Button(
                        onClick = { showOfficerMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Assign / Reassign Officer")
                    }
                    DropdownMenu(
                        expanded = showOfficerMenu,
                        onDismissRequest = { showOfficerMenu = false }
                    ) {
                        officers.forEach { officer ->
                            DropdownMenuItem(
                                text = { Text("${officer.name} (${officer.department})") },
                                onClick = {
                                    onAssignOfficer(officer)
                                    showOfficerMenu = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Status updates
                Text("Change Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                listOf("NEW", "ASSIGNED", "IN PROGRESS", "RESOLVED", "CLOSED", "REJECTED").forEach { statusOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatusChange(statusOption) }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = complaint.status.equals(statusOption, ignoreCase = true),
                            onClick = { onStatusChange(statusOption) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(status = statusOption)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = { onDelete(); onDismiss() }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", color = Color(0xFFDC2626))
            }
        }
    )
}

@Composable
fun FilterRow(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$title:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569),
            modifier = Modifier.width(55.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { opt ->
                val isSelected = opt.equals(selected, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFFF1F5F9),
                    modifier = Modifier.clickable { onSelect(opt) }
                ) {
                    Text(
                        text = opt,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminComplaintRow(
    complaint: Complaint,
    isOverdue: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("admin_complaint_row_${complaint.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOverdue) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ID & Ward
            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = "#${complaint.id}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = complaint.ward,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }

            // Category & Dept
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = complaint.category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    maxLines = 1
                )
                Text(
                    text = complaint.department,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }

            // Priority
            Box(modifier = Modifier.weight(0.9f)) {
                PriorityBadge(priority = complaint.priority)
            }

            // Status & Overdue flag
            Column(modifier = Modifier.weight(1.0f), horizontalAlignment = Alignment.Start) {
                StatusBadge(status = complaint.status)
                if (isOverdue) {
                    Text(
                        text = "OVERDUE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}
