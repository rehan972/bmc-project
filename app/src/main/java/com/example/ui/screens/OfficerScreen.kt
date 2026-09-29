package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.viewmodel.TriageViewModel

@Composable
fun OfficerScreen(
    viewModel: TriageViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val officerComplaints by viewModel.officerComplaints.collectAsState()

    var selectedComplaint by remember { mutableStateOf<Complaint?>(null) }
    var filterStatus by remember { mutableStateOf("ALL") }

    val filtered = when (filterStatus) {
        "ALL" -> officerComplaints
        "OVERDUE" -> officerComplaints.filter { viewModel.isComplaintOverdue(it) }
        else -> officerComplaints.filter { it.status.equals(filterStatus, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Officer Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFDBEAFE), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Engineering, contentDescription = null, tint = Color(0xFF1E40AF), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Officer Task Workspace",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${currentUser?.name ?: "Field Officer"} • ${currentUser?.department ?: "Roads Department"}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "ASSIGNED", "IN PROGRESS", "RESOLVED", "OVERDUE").forEach { status ->
                val isSelected = filterStatus == status
                val isOverdueChip = status == "OVERDUE"
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) {
                        if (isOverdueChip) Color(0xFFDC2626) else Color(0xFF1E3A8A)
                    } else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color.Transparent else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.clickable { filterStatus = status }
                ) {
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else if (isOverdueChip) Color(0xFFDC2626) else Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Complaints List
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks currently assigned matching filter.", color = Color(0xFF64748B), fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { complaint ->
                    OfficerTaskCard(
                        complaint = complaint,
                        isOverdue = viewModel.isComplaintOverdue(complaint),
                        onClick = { selectedComplaint = complaint },
                        onAccept = { viewModel.acceptTask(complaint.id) },
                        onStartWork = { viewModel.startWork(complaint.id) }
                    )
                }
            }
        }
    }

    // Officer Action Dialog
    if (selectedComplaint != null) {
        OfficerActionDialog(
            complaint = selectedComplaint!!,
            isOverdue = viewModel.isComplaintOverdue(selectedComplaint!!),
            onDismiss = { selectedComplaint = null },
            onStartWork = {
                viewModel.startWork(selectedComplaint!!.id)
                selectedComplaint = selectedComplaint!!.copy(status = "IN PROGRESS")
            },
            onUpdateRemarks = { remarks ->
                viewModel.updateOfficerRemarks(selectedComplaint!!.id, remarks)
            },
            onResolve = { remarks, imageUri ->
                viewModel.resolveComplaint(selectedComplaint!!.id, remarks, imageUri)
                selectedComplaint = null
            }
        )
    }
}

@Composable
fun OfficerTaskCard(
    complaint: Complaint,
    isOverdue: Boolean,
    onClick: () -> Unit,
    onAccept: () -> Unit,
    onStartWork: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("officer_task_${complaint.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOverdue) Color(0xFFFCA5A5) else Color(0xFFCBD5E1)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = complaint.id,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PriorityBadge(priority = complaint.priority)
                }
                StatusBadge(status = complaint.status)
            }

            Text(
                text = "${complaint.category} • ${complaint.ward}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = "📍 ${complaint.location}",
                fontSize = 11.sp,
                color = Color(0xFF475569)
            )

            Text(
                text = complaint.summary,
                fontSize = 11.sp,
                color = Color(0xFF334155)
            )

            if (isOverdue) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFEE2E2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171))
                ) {
                    Text(
                        text = "🔴 SLA OVERDUE - Immediate Field Inspection Required",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (!complaint.remarks.isNullOrBlank()) {
                Text(
                    text = "Remarks: ${complaint.remarks}",
                    fontSize = 11.sp,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (complaint.status == "NEW") {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier.weight(1f).height(34.dp).testTag("accept_task_btn")
                    ) {
                        Text("Accept Task", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (complaint.status == "ASSIGNED") {
                    Button(
                        onClick = onStartWork,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        modifier = Modifier.weight(1f).height(34.dp).testTag("start_work_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Work", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text("Manage & Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OfficerActionDialog(
    complaint: Complaint,
    isOverdue: Boolean,
    onDismiss: () -> Unit,
    onStartWork: () -> Unit,
    onUpdateRemarks: (String) -> Unit,
    onResolve: (String, String?) -> Unit
) {
    var remarkText by remember(complaint.remarks) { mutableStateOf(complaint.remarks ?: "") }
    var resolutionRemark by remember(complaint.resolutionRemarks) { mutableStateOf(complaint.resolutionRemarks ?: "") }
    var resolutionImageUri by remember(complaint.resolutionImageUri) { mutableStateOf(complaint.resolutionImageUri) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) resolutionImageUri = uri.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Task: ${complaint.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${complaint.category} • ${complaint.ward}", fontSize = 12.sp, color = Color(0xFF2563EB))
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
                            text = "🔴 SLA BREACHED: Overdue for department response.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                Text("Citizen: ${complaint.citizenName} (${complaint.citizenContact})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(complaint.description, fontSize = 12.sp, color = Color(0xFF0F172A))
                Text("Location: ${complaint.location}", fontSize = 11.sp, color = Color(0xFF475569))

                if (!complaint.imageUri.isNullOrBlank()) {
                    Text("Citizen Evidence Photo:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    AsyncImage(
                        model = complaint.imageUri,
                        contentDescription = "Citizen photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Actions: Start Work
                if (complaint.status == "ASSIGNED") {
                    Button(
                        onClick = onStartWork,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("Set In Progress (Start Work)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Progress Remarks
                OutlinedTextField(
                    value = remarkText,
                    onValueChange = { remarkText = it },
                    label = { Text("Progress Remark / Note") },
                    placeholder = { Text("e.g., Road repair crew arrived with premix.") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onUpdateRemarks(remarkText) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Text("Save Progress Note", fontSize = 12.sp)
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Resolution Section
                Text("Resolution & Closeout Proof:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))

                OutlinedTextField(
                    value = resolutionRemark,
                    onValueChange = { resolutionRemark = it },
                    label = { Text("Resolution Remark (Mandatory)") },
                    placeholder = { Text("e.g., Pothole filled and leveled. Normal flow restored.") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (resolutionImageUri != null) {
                    Text("Attached Resolution Photo Proof:", fontSize = 11.sp, color = Color(0xFF047857))
                    AsyncImage(
                        model = resolutionImageUri,
                        contentDescription = "Resolution proof",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    OutlinedButton(
                        onClick = {
                            photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Attach Resolution Proof Photo", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = {
                        val remark = resolutionRemark.ifBlank { "Issue verified and resolved by field team." }
                        onResolve(remark, resolutionImageUri)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("mark_resolved_btn")
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark as Resolved", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
