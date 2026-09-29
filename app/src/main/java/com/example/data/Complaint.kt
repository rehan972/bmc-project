package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String, // e.g. "usr-citizen-1"
    val name: String,
    val contact: String, // mobile number or email
    val username: String? = null,
    val password: String = "123456",
    val role: String, // "CITIZEN", "OFFICER", "ADMIN"
    val department: String? = null // for officers (e.g. "Roads Department")
)

@Entity(tableName = "complaints")
data class Complaint(
    @PrimaryKey
    val id: String, // e.g. "CIV-2026-00001" or "BMC-2026-00001"
    val citizenId: String = "usr-citizen-1",
    val citizenName: String = "Ramesh Sharma",
    val citizenContact: String = "9820098200",
    val description: String,
    val imageUri: String? = null,
    val resolutionImageUri: String? = null,
    val location: String,
    val latitude: Double = 19.0760,
    val longitude: Double = 72.8777,
    val ward: String = "Ward K-West",
    val category: String,
    val priority: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    val department: String,
    val summary: String,
    val priorityReason: String,
    val confidence: String = "94%",
    val status: String = "NEW", // "NEW", "ASSIGNED", "IN PROGRESS", "RESOLVED", "CLOSED", "REJECTED", "PENDING_AI"
    val assignedOfficerId: String? = null,
    val assignedOfficerName: String? = null,
    val remarks: String? = null,
    val resolutionRemarks: String? = null,
    val isDuplicate: Boolean = false,
    val masterComplaintId: String? = null,
    val feedbackRating: String? = null, // "YES" or "NO"
    val feedbackComment: String? = null,
    val feedbackScore: Int = 0, // 1 to 5 stars
    val isDemo: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis(),
    val resolvedTimestamp: Long? = null
)

@Entity(tableName = "departments")
data class DepartmentConfig(
    @PrimaryKey
    val id: String,
    val name: String,
    val code: String,
    val isActive: Boolean = true
)

@Entity(tableName = "category_mappings")
data class CategoryMapping(
    @PrimaryKey
    val categoryName: String,
    val departmentName: String,
    val defaultPriority: String = "MEDIUM"
)

@Entity(tableName = "sla_configs")
data class SlaConfig(
    @PrimaryKey
    val priority: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    val maxHours: Int
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey
    val id: String,
    val targetRole: String, // "CITIZEN", "OFFICER", "ADMIN"
    val targetUserId: String? = null,
    val title: String,
    val message: String,
    val complaintId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "system_settings")
data class SystemSetting(
    @PrimaryKey
    val key: String,
    val value: String
)

data class TriageResult(
    val category: String,
    val priority: String,
    val department: String,
    val summary: String,
    val priorityReason: String,
    val confidence: String = "94%"
)
