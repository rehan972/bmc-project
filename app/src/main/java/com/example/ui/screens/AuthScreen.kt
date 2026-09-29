package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.viewmodel.TriageViewModel

@Composable
fun AuthDialog(
    viewModel: TriageViewModel,
    onDismiss: () -> Unit
) {
    val demoUsers by viewModel.availableDemoUsers.collectAsState()
    val authError by viewModel.authError.collectAsState()

    val phone by viewModel.phoneInput.collectAsState()
    val otp by viewModel.otpInput.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val generatedDemoOtp by viewModel.generatedDemoOtp.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Phone + OTP, 1: Password Login, 2: Quick Demo Switch

    var loginIdentifier by remember { mutableStateOf("demo_citizen") }
    var loginPassword by remember { mutableStateOf("password") }

    var regName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    text = "NULL POINTERS Authentication",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Sign in to access Citizen, Officer, or Admin portals",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFF1F5F9),
                contentColor = Color(0xFF1E3A8A)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Phone OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Password", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Demo Access", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            if (authError != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = authError ?: "",
                        color = Color(0xFFB91C1C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Option A: Phone Number + OTP
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Citizen Mobile Login",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { viewModel.phoneInput.value = it },
                            label = { Text("Mobile Number (+91)") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF64748B))
                            },
                            singleLine = true,
                            enabled = !isOtpSent,
                            modifier = Modifier.fillMaxWidth().testTag("phone_input")
                        )

                        if (!isOtpSent) {
                            Button(
                                onClick = { viewModel.sendOtp() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("send_otp_button")
                            ) {
                                Icon(imageVector = Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send OTP", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Demo OTP banner
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "📱 Demo OTP Mode Active",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                    Text(
                                        text = "Verification code generated: ${generatedDemoOtp ?: "123456"}\n(SMS provider not configured for hackathon demo)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = otp,
                                onValueChange = { viewModel.otpInput.value = it },
                                label = { Text("Enter 6-digit OTP") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("otp_input")
                            )

                            Button(
                                onClick = {
                                    viewModel.verifyOtp {
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("verify_otp_button")
                            ) {
                                Text("Verify & Enter Dashboard", fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "Didn't receive code? Resend OTP",
                                fontSize = 11.sp,
                                color = Color(0xFF2563EB),
                                modifier = Modifier.clickable { viewModel.sendOtp() }
                            )
                        }
                    }
                }

                1 -> {
                    // Option B: Username / Mobile + Password Login
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Username / Password Login",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )

                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            label = { Text("Username or Mobile Number") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B))
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_identifier_input")
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xFF64748B))
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                        )

                        Button(
                            onClick = {
                                viewModel.loginWithPassword(loginIdentifier, loginPassword) {
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("login_button")
                        ) {
                            Text("Login to Account", fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        // Quick Registration link
                        Text("New citizen? Register account:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Username (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                viewModel.registerCitizen(regName, loginIdentifier, regUsername, loginPassword) {
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("Register Citizen Account")
                        }
                    }
                }

                2 -> {
                    // Fast Hackathon Demo Switcher
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚡ Hackathon Demo Access: Tap any user below to immediately switch role and context without SMS delay:",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        demoUsers.forEach { user ->
                            DemoUserCard(
                                user = user,
                                onSelect = {
                                    viewModel.switchUser(user)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun DemoUserCard(
    user: User,
    onSelect: () -> Unit
) {
    val (icon, roleColor, roleBg) = when (user.role) {
        "ADMIN" -> Triple(Icons.Default.AdminPanelSettings, Color(0xFFDC2626), Color(0xFFFEE2E2))
        "OFFICER" -> Triple(Icons.Default.Engineering, Color(0xFF2563EB), Color(0xFFDBEAFE))
        else -> Triple(Icons.Default.Person, Color(0xFF059669), Color(0xFFD1FAE5))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("switch_to_${user.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(roleBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = roleColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "User: ${user.username ?: user.contact} • Pass: ${user.password}",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = roleBg
            ) {
                Text(
                    text = user.role,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = roleColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
