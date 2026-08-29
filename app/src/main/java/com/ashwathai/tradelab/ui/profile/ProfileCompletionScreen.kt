package com.ashwathai.tradelab.ui.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.tradelab.data.UserProfile
import com.ashwathai.tradelab.ui.theme.*

private val GenderOptions = listOf("Male", "Female", "Other", "Prefer not to say")
private val ReferralOptions = listOf("Instagram", "YouTube", "Twitter / X", "Friend / Family", "Google Search", "App Store", "Podcast", "Other")
private val InterestOptions = listOf("Stocks (Equity)", "Futures & Options", "Mutual Funds", "IPOs", "Technical Analysis", "Portfolio Management", "Risk Management")

@Composable
fun FlowLayout(
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) = SubcomposeLayout(modifier) { constraints ->
    val rows = mutableListOf<MutableList<Pair<Placeable, Int>>>()

    // Subcompose the content once to get all children
    val slots = subcompose(SlotId(0), content)

    var currentRowWidth = 0
    slots.forEach { slot ->
        val placeable = slot.measure(constraints)
        val width = placeable.width
        val height = placeable.height

        if (rows.isEmpty() || currentRowWidth + width > constraints.maxWidth) {
            rows.add(mutableListOf())
            currentRowWidth = 0
        }

        val x = currentRowWidth
        currentRowWidth += width + horizontalSpacing.roundToPx()
        rows.last().add(Pair(placeable, x))
    }

    // Calculate total height
    val layoutWidth = constraints.maxWidth
    val layoutHeight = if (rows.isNotEmpty()) {
        var totalHeight = 0
        rows.forEachIndexed { idx, row ->
            var rowHeight = 0
            row.forEach { (placeable, _) ->
                rowHeight = maxOf(rowHeight, placeable.height)
            }
            if (idx == rows.size - 1) {
                totalHeight += rowHeight
            } else {
                totalHeight += rowHeight + verticalSpacing.roundToPx()
            }
        }
        totalHeight
    } else 0

    layout(layoutWidth, layoutHeight) {
        var y = 0
        rows.forEachIndexed { rowIdx, row ->
            var rowHeight = 0
            row.forEach { (placeable, _) ->
                rowHeight = maxOf(rowHeight, placeable.height)
            }
            row.forEach { (placeable, x) ->
                placeable.placeRelative(x, y)
            }
            y += rowHeight
            if (rowIdx < rows.size - 1) y += verticalSpacing.roundToPx()
        }
    }
}

class SlotId(val id: Int)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileCompletionScreen(
    userProfile: UserProfile?,
    loginMethod: String = "",
    isEditMode: Boolean = false,
    onComplete: (
        phone: String,
        email: String,
        dateOfBirth: String,
        gender: String,
        city: String,
        referralSource: String,
        interests: String,
        optedIntoEmails: Boolean
    ) -> Unit,
    onSkip: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Pre-fill from existing profile
    var name by remember { mutableStateOf(userProfile?.userName?.ifBlank { "Ashwath Trader" } ?: "Ashwath Trader") }
    var phone by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(userProfile?.userEmail ?: "") }
    var dateOfBirth by remember { mutableStateOf(userProfile?.dateOfBirth ?: "") }
    var gender by remember { mutableStateOf(userProfile?.gender ?: "") }
    var city by remember { mutableStateOf(userProfile?.city ?: "") }
    var referralSource by remember { mutableStateOf(userProfile?.referralSource ?: "") }
    var selectedInterests by remember { mutableStateOf((userProfile?.interests ?: "").split(",").filter { it.isNotBlank() }.toMutableSet()) }
    var optedIntoEmails by remember { mutableStateOf(if (userProfile?.hasCompletedProfile == true) userProfile.optedIntoEmails else true) }
    var avatarUrl by remember { mutableStateOf(userProfile?.profilePictureUrl ?: "") }

    // Determine which fields are locked (only in post-login mode, not edit mode)
    val isLegacyUser = loginMethod.isBlank()
    val isNameLocked = !isEditMode && !isLegacyUser && (loginMethod == "GOOGLE" || loginMethod == "EMAIL") && userProfile?.userName?.isNotBlank() == true
    val isPhoneLocked = !isEditMode && !isLegacyUser && loginMethod == "PHONE" && userProfile?.phoneNumber?.isNotBlank() == true
    val isEmailLocked = !isEditMode && !isLegacyUser && (loginMethod == "GOOGLE" || loginMethod == "EMAIL") && userProfile?.userEmail?.isNotBlank() == true

    var showGenderDropdown by remember { mutableStateOf(false) }
    var showReferralDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar - no skip button at top right (only bottom skip remains)

            Spacer(modifier = Modifier.height(8.dp))

            // Avatar placeholder (no pencil icon - only fetches from Google login)
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BrandViolet.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "A",
                        color = BrandViolet,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isEditMode) "Edit Profile" else "Complete Your Profile",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isEditMode) "Update your profile details" else "Help us personalize your TradeLab experience",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Name (locked)
            ProfileFieldLocked(
                label = "Name",
                value = name
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone (locked if from phone login, editable otherwise)
            if (isPhoneLocked) {
                ProfileFieldLocked(
                    label = "Phone Number",
                    value = phone
                )
            } else {
                ProfileFieldEditable(
                    label = "Phone Number",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "+91 98765 43210"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email (locked if from email/Google login, editable otherwise)
            if (isEmailLocked) {
                ProfileFieldLocked(
                    label = "Email Address",
                    value = email
                )
            } else {
                ProfileFieldEditable(
                    label = "Email Address",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "you@example.com"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth
            ProfileFieldEditable(
                label = "Date of Birth",
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                placeholder = "DD/MM/YYYY"
            )

            Text(
                text = "\uD83C\uDF81 Get a surprise gift on your birthday!",
                color = AccentYellow,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender dropdown
            ExposedDropdownMenuBox(
                expanded = showGenderDropdown,
                onExpandedChange = { showGenderDropdown = it }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender", color = TextMuted) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderDropdown) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandViolet,
                        unfocusedBorderColor = DarkBorder,
                        disabledTextColor = Color.White,
                        disabledBorderColor = DarkBorder,
                        disabledLabelColor = TextMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showGenderDropdown,
                    onDismissRequest = { showGenderDropdown = false }
                ) {
                    GenderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.White) },
                            onClick = {
                                gender = option
                                showGenderDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // City
            ProfileFieldEditable(
                label = "City",
                value = city,
                onValueChange = { city = it },
                placeholder = "Mumbai, Bangalore, Delhi..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Referral source dropdown
            ExposedDropdownMenuBox(
                expanded = showReferralDropdown,
                onExpandedChange = { showReferralDropdown = it }
            ) {
                OutlinedTextField(
                    value = referralSource,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("How did you find us?", color = TextMuted) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showReferralDropdown) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandViolet,
                        unfocusedBorderColor = DarkBorder,
                        disabledTextColor = Color.White,
                        disabledBorderColor = DarkBorder,
                        disabledLabelColor = TextMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showReferralDropdown,
                    onDismissRequest = { showReferralDropdown = false }
                ) {
                    ReferralOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.White) },
                            onClick = {
                                referralSource = option
                                showReferralDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interests chips
            Text(
                text = "What interests you?",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowLayout(
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp
            ) {
                InterestOptions.forEach { interest ->
                    val isSelected = interest in selectedInterests
                    val chipColor by animateColorAsState(
                        targetValue = if (isSelected) BrandViolet else Color.Transparent,
                        label = "chipColor"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else TextMuted,
                        label = "textColor"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipColor)
                            .then(
                                if (!isSelected) Modifier.border(1.dp, DarkBorder, RoundedCornerShape(20.dp)) else Modifier
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) selectedInterests.remove(interest)
                                else selectedInterests.add(interest)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = interest,
                                color = textColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Email opt-in (SEBI-safe)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { optedIntoEmails = !optedIntoEmails }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = optedIntoEmails,
                    onCheckedChange = { optedIntoEmails = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = BrandViolet,
                        uncheckedColor = DarkBorder
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Stay updated with new features and learning content",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Complete Profile button
            Button(
                onClick = {
                    onComplete(
                        phone,
                        email,
                        dateOfBirth,
                        gender,
                        city,
                        referralSource,
                        selectedInterests.joinToString(","),
                        optedIntoEmails
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isEditMode) "Save Changes" else "Complete Profile",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Skip link at bottom
            TextButton(onClick = onSkip) {
                Text("Skip for now", color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ProfileFieldLocked(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label, color = TextMuted) },
            trailingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.White.copy(alpha = 0.7f),
                disabledBorderColor = DarkBorder,
                disabledLabelColor = TextMuted,
                disabledTrailingIconColor = TextMuted
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProfileFieldEditable(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted) },
        placeholder = { Text(placeholder, color = TextSubtle, fontSize = 13.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = BrandViolet,
            unfocusedBorderColor = DarkBorder,
            cursorColor = BrandViolet
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
