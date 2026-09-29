package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiTriageService
import com.example.data.AppDatabase
import com.example.data.AppLanguage
import com.example.data.AppNotification
import com.example.data.CategoryMapping
import com.example.data.Complaint
import com.example.data.ComplaintRepository
import com.example.data.DepartmentConfig
import com.example.data.HotspotArea
import com.example.data.LanguageManager
import com.example.data.SlaConfig
import com.example.data.SystemSetting
import com.example.data.TriageResult
import com.example.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class AdminFilterState(
    val category: String = "ALL",
    val priority: String = "ALL",
    val department: String = "ALL",
    val status: String = "ALL",
    val ward: String = "ALL",
    val query: String = ""
)

class TriageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ComplaintRepository
    private val aiService = AiTriageService()
    private val prefs = application.getSharedPreferences("bmc_triage_prefs", Context.MODE_PRIVATE)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ComplaintRepository(db.complaintDao())
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
            loadSavedSession()
        }
    }

    // --- Authentication & Session State ---
    val currentUser = MutableStateFlow<User?>(ComplaintRepository.getDemoUsers().first()) // Default logged-in as demo citizen
    val availableDemoUsers = MutableStateFlow(ComplaintRepository.getDemoUsers())
    val authError = MutableStateFlow<String?>(null)

    // Phone + OTP State
    val phoneInput = MutableStateFlow("+91 98200 98200")
    val otpInput = MutableStateFlow("")
    val isOtpSent = MutableStateFlow(false)
    val generatedDemoOtp = MutableStateFlow<String?>(null)

    // Password Setup / User Details state
    val regNameInput = MutableStateFlow("")
    val regUsernameInput = MutableStateFlow("")
    val regPasswordInput = MutableStateFlow("")

    private fun loadSavedSession() {
        val savedUserId = prefs.getString("active_user_id", null)
        if (savedUserId != null) {
            val user = ComplaintRepository.getDemoUsers().find { it.id == savedUserId }
            if (user != null) {
                currentUser.value = user
            }
        }
    }

    fun sendOtp() {
        val phone = phoneInput.value.trim()
        if (phone.length < 10) {
            authError.value = "Please enter a valid 10-digit mobile number."
            return
        }
        authError.value = null
        // Demo OTP mode: generates testing OTP
        val demoCode = "123456"
        generatedDemoOtp.value = demoCode
        isOtpSent.value = true
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        val enteredOtp = otpInput.value.trim()
        val expected = generatedDemoOtp.value ?: "123456"
        if (enteredOtp == expected || enteredOtp == "123456") {
            viewModelScope.launch {
                val user = repository.verifyOtpAndLogin(phoneInput.value, enteredOtp)
                currentUser.value = user
                prefs.edit().putString("active_user_id", user.id).apply()
                authError.value = null
                isOtpSent.value = false
                generatedDemoOtp.value = null
                onSuccess()
            }
        } else {
            authError.value = "Invalid OTP. Use Demo OTP: $expected"
        }
    }

    fun loginWithPassword(identifier: String, pass: String, onSuccess: () -> Unit) {
        if (identifier.isBlank() || pass.isBlank()) {
            authError.value = "Please enter username/mobile and password."
            return
        }
        viewModelScope.launch {
            val user = repository.authenticateUser(identifier.trim(), pass)
            if (user != null) {
                currentUser.value = user
                prefs.edit().putString("active_user_id", user.id).apply()
                authError.value = null
                onSuccess()
            } else {
                authError.value = "Invalid credentials. Try demo credentials or quick switch."
            }
        }
    }

    fun registerCitizen(name: String, contact: String, username: String?, pass: String, onSuccess: () -> Unit) {
        if (name.isBlank() || contact.isBlank()) {
            authError.value = "Name and mobile number are required."
            return
        }
        viewModelScope.launch {
            val user = repository.registerUser(name, contact, username, pass.ifBlank { "password" }, "CITIZEN")
            currentUser.value = user
            prefs.edit().putString("active_user_id", user.id).apply()
            authError.value = null
            onSuccess()
        }
    }

    fun switchUser(user: User) {
        currentUser.value = user
        prefs.edit().putString("active_user_id", user.id).apply()
        authError.value = null
    }

    fun logout() {
        prefs.edit().remove("active_user_id").apply()
        currentUser.value = null
        resetCitizenForm()
    }

    // --- Language State ---
    val currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)

    fun setLanguage(language: AppLanguage) {
        currentLanguage.value = language
    }

    fun getString(key: String): String {
        return LanguageManager.getString(key, currentLanguage.value)
    }

    // --- Database Collections ---
    val allComplaints: StateFlow<List<Complaint>> = repository.allComplaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val departments: StateFlow<List<DepartmentConfig>> = repository.allDepartments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ComplaintRepository.getDemoDepartments())

    val categoryMappings: StateFlow<List<CategoryMapping>> = repository.allCategoryMappings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ComplaintRepository.getDemoCategoryMappings())

    val slaConfigs: StateFlow<List<SlaConfig>> = repository.allSlaConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ComplaintRepository.getDemoSlaConfigs())

    val notifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myComplaints: StateFlow<List<Complaint>> = combine(
        allComplaints,
        currentUser
    ) { complaints, user ->
        if (user == null) emptyList() else complaints.filter { it.citizenId == user.id || it.citizenContact == user.contact }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val officerComplaints: StateFlow<List<Complaint>> = combine(
        allComplaints,
        currentUser
    ) { complaints, user ->
        if (user == null) emptyList() else complaints.filter {
            it.assignedOfficerId == user.id ||
                    (user.department != null && it.department.equals(user.department, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SLA Verification ---
    fun isComplaintOverdue(complaint: Complaint): Boolean {
        if (complaint.status.equals("RESOLVED", ignoreCase = true) ||
            complaint.status.equals("CLOSED", ignoreCase = true)
        ) return false

        val configs = slaConfigs.value
        val hoursLimit = configs.find { it.priority.equals(complaint.priority, ignoreCase = true) }?.maxHours
            ?: when (complaint.priority.uppercase()) {
                "CRITICAL" -> 12
                "HIGH" -> 24
                "MEDIUM" -> 72
                else -> 168
            }

        val elapsedMillis = System.currentTimeMillis() - complaint.timestamp
        val elapsedHours = elapsedMillis / (1000 * 3600)
        return elapsedHours > hoursLimit
    }

    // --- Admin Filters & Analytics ---
    val selectedCategory = MutableStateFlow("ALL")
    val selectedPriority = MutableStateFlow("ALL")
    val selectedDepartment = MutableStateFlow("ALL")
    val selectedStatus = MutableStateFlow("ALL")
    val selectedWard = MutableStateFlow("ALL")
    val searchQuery = MutableStateFlow("")

    private val filterState: StateFlow<AdminFilterState> = combine(
        selectedCategory,
        selectedPriority,
        selectedDepartment,
        selectedStatus,
        searchQuery
    ) { cat, pri, dept, stat, query ->
        AdminFilterState(cat, pri, dept, stat, selectedWard.value, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminFilterState())

    val filteredComplaints: StateFlow<List<Complaint>> = combine(
        allComplaints,
        filterState,
        selectedWard
    ) { complaints, filter, ward ->
        complaints.filter { c ->
            val matchCat = filter.category == "ALL" || c.category.equals(filter.category, ignoreCase = true)
            val matchPri = filter.priority == "ALL" || c.priority.equals(filter.priority, ignoreCase = true)
            val matchDept = filter.department == "ALL" || c.department.equals(filter.department, ignoreCase = true)
            val matchStat = filter.status == "ALL" || c.status.equals(filter.status, ignoreCase = true)
            val matchWard = ward == "ALL" || c.ward.equals(ward, ignoreCase = true)
            val matchQuery = filter.query.isBlank() ||
                    c.id.contains(filter.query, ignoreCase = true) ||
                    c.location.contains(filter.query, ignoreCase = true) ||
                    c.ward.contains(filter.query, ignoreCase = true) ||
                    c.description.contains(filter.query, ignoreCase = true) ||
                    c.category.contains(filter.query, ignoreCase = true) ||
                    (c.assignedOfficerName ?: "").contains(filter.query, ignoreCase = true)
            matchCat && matchPri && matchDept && matchStat && matchWard && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hotspot areas
    val hotspotAreas: StateFlow<List<HotspotArea>> = allComplaints.map { complaints ->
        repository.detectHotspots(complaints)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Citizen Form State ---
    val description = MutableStateFlow("")
    val location = MutableStateFlow("SV Road, Andheri West, Ward K-West, Mumbai")
    val latitude = MutableStateFlow(19.1197)
    val longitude = MutableStateFlow(72.8464)
    val ward = MutableStateFlow("Ward K-West")
    val isDetectingLocation = MutableStateFlow(false)
    val locationDetectedSuccess = MutableStateFlow(true)
    val showLocationFallback = MutableStateFlow(false)

    val imageBitmap = MutableStateFlow<Bitmap?>(null)
    val imageUri = MutableStateFlow<String?>(null)

    val isAnalyzing = MutableStateFlow(false)
    val triageResult = MutableStateFlow<TriageResult?>(null)
    val pendingComplaintId = MutableStateFlow("CIV-2026-000011")

    // Duplicate detection modal
    val duplicateComplaintFound = MutableStateFlow<Complaint?>(null)
    val showDuplicateModal = MutableStateFlow(false)

    val createdComplaint = MutableStateFlow<Complaint?>(null)
    val showAcknowledgement = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Voice recognition
    val isListening = MutableStateFlow(false)

    // Admin detail / assign dialog
    val detailComplaint = MutableStateFlow<Complaint?>(null)

    // System settings
    val demoModeEnabled = MutableStateFlow(true)
    val externalBmcEmail = MutableStateFlow("triage-demo@mcgm.gov.in")

    fun onDescriptionChange(text: String) {
        description.value = text
        errorMessage.value = null
    }

    fun onLocationChange(loc: String) {
        location.value = loc
        deriveWardFromText(loc)
    }

    private fun deriveWardFromText(loc: String) {
        if (loc.contains("Andheri", ignoreCase = true)) ward.value = "Ward K-West"
        else if (loc.contains("Dadar", ignoreCase = true)) ward.value = "Ward G-North"
        else if (loc.contains("Bandra", ignoreCase = true)) ward.value = "Ward H-West"
        else if (loc.contains("Kurla", ignoreCase = true)) ward.value = "Ward L"
        else if (loc.contains("Thane", ignoreCase = true)) ward.value = "Ward T"
        else if (loc.contains("Dharavi", ignoreCase = true)) ward.value = "Ward G-North"
        else if (loc.contains("Goregaon", ignoreCase = true)) ward.value = "Ward P-South"
        else if (loc.contains("Colaba", ignoreCase = true) || loc.contains("Fort", ignoreCase = true)) ward.value = "Ward A"
        else if (loc.contains("Borivali", ignoreCase = true)) ward.value = "Ward R-Central"
        else if (loc.contains("Chembur", ignoreCase = true)) ward.value = "Ward M-West"
    }

    fun detectMyLocation(context: Context) {
        isDetectingLocation.value = true
        errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                var bestLocation: Location? = null

                if (lm != null) {
                    try {
                        val providers = lm.getProviders(true)
                        for (provider in providers) {
                            val l = lm.getLastKnownLocation(provider) ?: continue
                            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                                bestLocation = l
                            }
                        }
                    } catch (sec: SecurityException) {
                        // Permission not granted or restricted
                    }
                }

                if (bestLocation != null) {
                    val lat = bestLocation.latitude
                    val lng = bestLocation.longitude
                    latitude.value = lat
                    longitude.value = lng

                    var addressStr = "Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}"
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val locality = addr.subLocality ?: addr.locality ?: "Mumbai"
                            val thoroughfare = addr.thoroughfare ?: "Near Reported Location"
                            addressStr = "$thoroughfare, $locality, Mumbai"
                        }
                    } catch (e: Exception) {
                        // Geocoder unavailable, use fallback coordinates description
                    }

                    location.value = addressStr
                    deriveWardFromText(addressStr)
                    locationDetectedSuccess.value = true
                } else {
                    // Running on emulator or GPS unavailable: trigger user-friendly area selector
                    withContext(Dispatchers.Main) {
                        showLocationFallback.value = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLocationFallback.value = true
                }
            } finally {
                isDetectingLocation.value = false
            }
        }
    }

    fun selectPresetArea(areaName: String, sampleWard: String, lat: Double, lng: Double) {
        location.value = "$areaName, $sampleWard, Mumbai"
        ward.value = sampleWard
        latitude.value = lat
        longitude.value = lng
        locationDetectedSuccess.value = true
        showLocationFallback.value = false
    }

    fun setImage(uri: Uri?, bitmap: Bitmap?) {
        imageUri.value = uri?.toString()
        imageBitmap.value = bitmap
        errorMessage.value = null
    }

    fun clearImage() {
        imageUri.value = null
        imageBitmap.value = null
    }

    fun onVoiceInput(text: String) {
        if (description.value.isBlank()) {
            description.value = text
        } else {
            description.value = "${description.value} $text"
        }
        isListening.value = false
    }

    fun resetCitizenForm() {
        description.value = ""
        location.value = "SV Road, Andheri West, Ward K-West, Mumbai"
        ward.value = "Ward K-West"
        imageBitmap.value = null
        imageUri.value = null
        triageResult.value = null
        duplicateComplaintFound.value = null
        showDuplicateModal.value = false
        createdComplaint.value = null
        showAcknowledgement.value = false
        errorMessage.value = null
    }

    fun loadSampleScenario(desc: String, loc: String, sampleWard: String) {
        description.value = desc
        location.value = "$loc, $sampleWard, Mumbai"
        ward.value = sampleWard
        triageResult.value = null
        duplicateComplaintFound.value = null
        showDuplicateModal.value = false
        createdComplaint.value = null
        showAcknowledgement.value = false
        errorMessage.value = null
    }

    fun submitComplaintFlow() {
        val currentDesc = description.value.trim()
        val currentBmp = imageBitmap.value
        val currentLoc = location.value.trim()

        if (currentDesc.isEmpty() && currentBmp == null) {
            errorMessage.value = "Please provide either a complaint description or attach a photo."
            return
        }

        errorMessage.value = null
        isAnalyzing.value = true

        viewModelScope.launch {
            try {
                // Generate next CIV ticket ID
                val count = repository.getCount() + 1
                val generatedId = "CIV-2026-" + String.format("%06d", count)
                pendingComplaintId.value = generatedId

                val result = aiService.analyzeComplaint(currentDesc, currentBmp, currentLoc)
                val triage = result.getOrNull() ?: TriageResult(
                    category = "Public Infrastructure",
                    priority = "MEDIUM",
                    department = "Roads Department",
                    summary = "Civic complaint logged for ward inspection.",
                    priorityReason = "Medium priority general municipal concern.",
                    confidence = "90%"
                )
                triageResult.value = triage

                // Check for duplicates
                val duplicates = repository.findSimilarComplaints(triage.category, currentLoc)
                if (duplicates.isNotEmpty()) {
                    duplicateComplaintFound.value = duplicates.first()
                    showDuplicateModal.value = true
                } else {
                    persistComplaint(triage, isDuplicate = false, masterId = null)
                }
            } catch (e: Exception) {
                // AI failure fallback: do NOT fail complaint submission!
                val fallbackTriage = TriageResult(
                    category = "Civic Issue",
                    priority = "MEDIUM",
                    department = "Roads Department",
                    summary = "Pending AI triage classification.",
                    priorityReason = "Saved with pending AI classification.",
                    confidence = "Pending"
                )
                triageResult.value = fallbackTriage
                persistComplaint(fallbackTriage, isDuplicate = false, masterId = null)
            } finally {
                isAnalyzing.value = false
            }
        }
    }

    fun submitAnyway() {
        showDuplicateModal.value = false
        val triage = triageResult.value ?: return
        val masterId = duplicateComplaintFound.value?.id
        persistComplaint(triage, isDuplicate = true, masterId = masterId)
    }

    private fun persistComplaint(triage: TriageResult, isDuplicate: Boolean, masterId: String?) {
        viewModelScope.launch {
            val user = currentUser.value ?: ComplaintRepository.getDemoUsers().first()
            val complaint = Complaint(
                id = pendingComplaintId.value,
                citizenId = user.id,
                citizenName = user.name,
                citizenContact = user.contact,
                description = description.value.ifBlank { "Citizen submitted photo evidence." },
                imageUri = imageUri.value,
                location = location.value,
                latitude = latitude.value,
                longitude = longitude.value,
                ward = ward.value,
                category = triage.category,
                priority = triage.priority,
                department = triage.department,
                summary = triage.summary,
                priorityReason = triage.priorityReason,
                confidence = triage.confidence,
                status = if (triage.confidence == "Pending") "PENDING_AI" else "NEW",
                isDuplicate = isDuplicate,
                masterComplaintId = masterId,
                isDemo = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insertComplaint(complaint)
            createdComplaint.value = complaint
            showAcknowledgement.value = true
        }
    }

    // --- Feedback ---
    fun submitFeedback(complaintId: String, rating: String, comment: String?, score: Int = 5) {
        viewModelScope.launch {
            repository.submitFeedback(complaintId, rating, comment, score)
            if (detailComplaint.value?.id == complaintId) {
                detailComplaint.value = detailComplaint.value?.copy(
                    feedbackRating = rating,
                    feedbackComment = comment,
                    feedbackScore = score
                )
            }
        }
    }

    // --- Officer Actions ---
    fun acceptTask(id: String) {
        viewModelScope.launch {
            repository.updateStatus(id, "ASSIGNED")
            if (detailComplaint.value?.id == id) {
                detailComplaint.value = detailComplaint.value?.copy(status = "ASSIGNED")
            }
        }
    }

    fun startWork(id: String) {
        viewModelScope.launch {
            repository.updateStatus(id, "IN PROGRESS")
            if (detailComplaint.value?.id == id) {
                detailComplaint.value = detailComplaint.value?.copy(status = "IN PROGRESS")
            }
        }
    }

    fun updateOfficerRemarks(id: String, remarks: String) {
        viewModelScope.launch {
            repository.updateRemarks(id, remarks)
            if (detailComplaint.value?.id == id) {
                detailComplaint.value = detailComplaint.value?.copy(remarks = remarks)
            }
        }
    }

    fun resolveComplaint(id: String, remarks: String, resolutionImageUri: String?) {
        viewModelScope.launch {
            repository.resolveComplaint(id, remarks, resolutionImageUri)
            if (detailComplaint.value?.id == id) {
                detailComplaint.value = detailComplaint.value?.copy(
                    status = "RESOLVED",
                    resolutionRemarks = remarks,
                    resolutionImageUri = resolutionImageUri,
                    resolvedTimestamp = System.currentTimeMillis()
                )
            }
        }
    }

    // --- Admin Actions ---
    fun assignOfficer(complaintId: String, officer: User) {
        viewModelScope.launch {
            repository.assignOfficer(complaintId, officer.id, officer.name)
            if (detailComplaint.value?.id == complaintId) {
                detailComplaint.value = detailComplaint.value?.copy(
                    assignedOfficerId = officer.id,
                    assignedOfficerName = officer.name,
                    status = "ASSIGNED"
                )
            }
        }
    }

    fun updateStatus(id: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateStatus(id, newStatus)
            if (detailComplaint.value?.id == id) {
                detailComplaint.value = detailComplaint.value?.copy(status = newStatus)
            }
        }
    }

    fun updateDepartment(id: String, department: String) {
        viewModelScope.launch {
            repository.updateDepartment(id, department)
            if (detailComplaint.value?.id == id) {
                detailComplaint.value = detailComplaint.value?.copy(department = department)
            }
        }
    }

    fun updatePriority(id: String, priority: String) {
        viewModelScope.launch {
            repository.updatePriority(id, priority)
            if (detailComplaint.value?.id == id) {
                detailComplaint.value = detailComplaint.value?.copy(priority = priority)
            }
        }
    }

    fun runAiAnalysisOnPending(complaint: Complaint) {
        viewModelScope.launch {
            val result = aiService.analyzeComplaint(complaint.description, null, complaint.location)
            result.onSuccess { triage ->
                repository.updateTriageResult(
                    id = complaint.id,
                    category = triage.category,
                    priority = triage.priority,
                    department = triage.department,
                    summary = triage.summary,
                    reason = triage.priorityReason,
                    confidence = triage.confidence,
                    status = "NEW"
                )
                if (detailComplaint.value?.id == complaint.id) {
                    detailComplaint.value = detailComplaint.value?.copy(
                        category = triage.category,
                        priority = triage.priority,
                        department = triage.department,
                        summary = triage.summary,
                        priorityReason = triage.priorityReason,
                        confidence = triage.confidence,
                        status = "NEW"
                    )
                }
            }
        }
    }

    fun deleteComplaint(id: String) {
        viewModelScope.launch {
            repository.deleteComplaint(id)
            if (detailComplaint.value?.id == id) {
                detailComplaint.value = null
            }
        }
    }

    fun openDetail(complaint: Complaint) {
        detailComplaint.value = complaint
    }

    fun closeDetail() {
        detailComplaint.value = null
    }

    fun resetFilters() {
        selectedCategory.value = "ALL"
        selectedPriority.value = "ALL"
        selectedDepartment.value = "ALL"
        selectedStatus.value = "ALL"
        selectedWard.value = "ALL"
        searchQuery.value = ""
    }

    // --- Demo Data Controls ---
    fun loadDemoComplaints() {
        viewModelScope.launch {
            repository.loadDemoComplaints()
        }
    }

    fun clearDemoComplaints() {
        viewModelScope.launch {
            repository.deleteDemoComplaints()
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToDemoData()
        }
    }

    // --- Admin Settings Updates ---
    fun updateDepartmentActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.updateDepartmentStatus(id, isActive)
        }
    }

    fun updateCategoryMapping(category: String, department: String, priority: String) {
        viewModelScope.launch {
            repository.insertCategoryMapping(CategoryMapping(category, department, priority))
        }
    }

    fun updateSlaHours(priority: String, hours: Int) {
        viewModelScope.launch {
            repository.updateSlaHours(priority, hours)
        }
    }

    fun exportCsv(): String {
        return repository.generateCsvExport(allComplaints.value)
    }
}
