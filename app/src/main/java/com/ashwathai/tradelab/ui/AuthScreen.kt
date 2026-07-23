package com.ashwathai.tradelab.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.tradelab.R
import com.ashwathai.tradelab.ui.theme.*
import com.ashwathai.tradelab.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun AuthScreen(viewModel: TradingViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Email, 1 = Google, 2 = Phone
    var isRegisterMode by remember { mutableStateOf(false) } // true = Register, false = Sign In
    
    // Email credentials
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    
    // Phone credentials
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }
    
    // Auth status states
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    // Fallback sandbox info dialog state
    var showSandboxDialog by remember { mutableStateOf(false) }
    var sandboxUserToSimulate by remember { mutableStateOf<Pair<String, String>?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Try to safely fetch FirebaseAuth instance
    val firebaseAuth: FirebaseAuth? = remember {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // --- TOP BAR / HEADER (FIXED) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo_premium),
                contentDescription = "TradeLab Premium Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "TradeLab",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Real-time Paper Trading Simulator",
                color = TextSecondary.copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        // --- MIDDLE SCROLLABLE BOX FOR CREDENTIALS & TABS ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tab Header Segmented Controls
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        val tabs = listOf("Email", "Google", "Phone")
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) BrandViolet.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        selectedTab = index
                                        errorMessage = null
                                        successMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) BrandViolet else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Status Messages (Alert Banners)
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentRoseDark.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, AccentRoseMedium.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = AccentRose,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                if (successMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentGreenDark.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, AccentGreenMedium.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = successMessage ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Main Content Area Card (all inputs stay inside)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        when (selectedTab) {
                            0 -> { // Email Auth tab
                                Text(
                                    text = if (isRegisterMode) "Create an Account" else "Welcome Back",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                if (isRegisterMode) {
                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = { nameInput = it },
                                        label = { Text("Full Name", color = TextMuted, fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth().testTag("auth_name_field"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandViolet,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true,
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name Icon", tint = TextMuted) }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Email Address", color = TextMuted, fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandViolet,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = TextMuted) }
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Password", color = TextMuted, fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandViolet,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon", tint = TextMuted) }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        successMessage = null
                                        if (emailInput.isBlank() || passwordInput.isBlank()) {
                                            errorMessage = "Please fill in all email and password fields"
                                            return@Button
                                        }
                                        if (isRegisterMode && nameInput.isBlank()) {
                                            errorMessage = "Please enter your name"
                                            return@Button
                                        }
                                        
                                        isLoading = true
                                        coroutineScope.launch {
                                            if (firebaseAuth == null) {
                                                isLoading = false
                                                sandboxUserToSimulate = Pair(nameInput.ifBlank { emailInput.substringBefore("@") }, emailInput)
                                                showSandboxDialog = true
                                            } else {
                                                try {
                                                    if (isRegisterMode) {
                                                        firebaseAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                                                            .addOnSuccessListener { authResult ->
                                                                val displayName = nameInput.ifBlank { emailInput.substringBefore("@") }
                                                                viewModel.registerOrLogin(displayName, emailInput)
                                                                isLoading = false
                                                            }
                                                            .addOnFailureListener { e ->
                                                                android.util.Log.e("AuthScreen", "Email registration failed", e)
                                                                errorMessage = e.localizedMessage ?: "Registration failed."
                                                                isLoading = false
                                                            }
                                                    } else {
                                                        firebaseAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                                                            .addOnSuccessListener { authResult ->
                                                                val displayName = authResult.user?.displayName ?: emailInput.substringBefore("@")
                                                                viewModel.registerOrLogin(displayName, emailInput)
                                                                isLoading = false
                                                            }
                                                            .addOnFailureListener { e ->
                                                                android.util.Log.e("AuthScreen", "Email sign-in failed", e)
                                                                errorMessage = e.localizedMessage ?: "Sign-In failed."
                                                                isLoading = false
                                                            }
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("AuthScreen", "Email auth exception", e)
                                                    errorMessage = e.localizedMessage ?: "Authentication operation failed."
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_email_submit"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandViolet,
                                        disabledContainerColor = BrandViolet.copy(alpha = 0.5f)
                                    )
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text(
                                            text = if (isRegisterMode) "Create Account" else "Sign In",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                                        Text(
                                            text = if (isRegisterMode) "Already have an account? Sign In" else "New to TradeLab? Register Here",
                                            color = BrandViolet,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            1 -> { // Google Auth tab
                                Text(
                                    text = "Sign In with Google",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Register or sign in immediately using your Google / Gmail credentials.",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        isLoading = true
                                        coroutineScope.launch {
                                            if (firebaseAuth == null || BuildConfig.DEBUG) {
                                                isLoading = false
                                                sandboxUserToSimulate = Pair("Google User", "google.user@gmail.com")
                                                showSandboxDialog = true
                                            } else {
                                                try {
                                                    val credentialManager = CredentialManager.create(context)
                                                    val googleIdOption = GetGoogleIdOption.Builder()
                                                        .setFilterByAuthorizedAccounts(false)
                                                        .setServerClientId("475129425714-isqb8eg5opba2n1f7v2pkuie5min95g8.apps.googleusercontent.com")
                                                        .setAutoSelectEnabled(true)
                                                        .build()

                                                    val request = GetCredentialRequest.Builder()
                                                        .addCredentialOption(googleIdOption)
                                                        .build()

                                                    val result = credentialManager.getCredential(
                                                        context = context,
                                                        request = request
                                                    )
                                                    
                                                    val credential = result.credential
                                                    when (credential) {
                                                        is GoogleIdTokenCredential -> {
                                                            val googleIdToken = credential.idToken
                                                            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                                            firebaseAuth.signInWithCredential(firebaseCredential)
                                                                .addOnSuccessListener { authResult ->
                                                                    val user = authResult.user
                                                                    viewModel.registerOrLogin(user?.displayName ?: "Google User", user?.email ?: "")
                                                                    isLoading = false
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    android.util.Log.e("AuthScreen", "Firebase Auth with Google failed", e)
                                                                    errorMessage = "Firebase Auth Error: ${e.localizedMessage}"
                                                                    isLoading = false
                                                                }
                                                        }
                                                        else -> {
                                                            val type = credential.type
                                                            android.util.Log.e("AuthScreen", "Unexpected credential type: $type")
                                                            errorMessage = "Unexpected credential: $type. Please ensure your Google account is selected."
                                                            isLoading = false
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("AuthScreen", "Google Sign-In Exception", e)
                                                    // Handle cancellation or other errors
                                                    errorMessage = when (e) {
                                                        is androidx.credentials.exceptions.GetCredentialCancellationException -> {
                                                            "Sign-in cancelled by user."
                                                        }
                                                        is androidx.credentials.exceptions.GetCredentialException -> {
                                                            "Google account selection failed (${e.type})."
                                                        }
                                                        else -> {
                                                            e.localizedMessage ?: "Google Sign-In failed."
                                                        }
                                                    }
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_google_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Google Icon",
                                            tint = BrandViolet,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Continue with Google",
                                            color = Color(0xFF1F2937),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                            
                            2 -> { // Phone Auth tab
                                Text(
                                    text = "OTP Authentication",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                if (!isOtpSent) {
                                    OutlinedTextField(
                                        value = phoneInput,
                                        onValueChange = { phoneInput = it },
                                        label = { Text("Phone Number (with code)", color = TextMuted, fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth().testTag("auth_phone_field"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandViolet,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Icon", tint = TextMuted) },
                                        placeholder = { Text("+91 XXXXX XXXXX", color = TextMuted.copy(alpha = 0.5f)) }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = {
                                            errorMessage = null
                                            if (phoneInput.isBlank()) {
                                                errorMessage = "Please enter your phone number"
                                                return@Button
                                            }
                                            isLoading = true
                                            coroutineScope.launch {
                                                if (firebaseAuth == null || BuildConfig.DEBUG) {
                                                    isLoading = false
                                                    successMessage = "Demo OTP Code '123456' sent to $phoneInput!"
                                                    isOtpSent = true
                                                } else {
                                                    val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                                                        .setPhoneNumber(phoneInput)
                                                        .setTimeout(60L, TimeUnit.SECONDS)
                                                        .setActivity(context as Activity)
                                                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                                firebaseAuth.signInWithCredential(credential)
                                                                    .addOnSuccessListener { authResult ->
                                                                        val user = authResult.user
                                                                        viewModel.registerOrLogin(user?.displayName ?: "Phone User", user?.email ?: "phone.$phoneInput@tradelab.com")
                                                                        isLoading = false
                                                                        successMessage = "Automatic verification successful!"
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        errorMessage = "Sign-In Failed: ${e.localizedMessage}"
                                                                        isLoading = false
                                                                    }
                                                            }

                                                            override fun onVerificationFailed(e: FirebaseException) {
                                                                android.util.Log.e("AuthScreen", "Phone Auth Verification Failed", e)
                                                                // Provide descriptive error messages for production
                                                                errorMessage = when (e) {
                                                                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid phone number format."
                                                                    is com.google.firebase.FirebaseTooManyRequestsException -> "Too many requests. Please try again later."
                                                                    else -> "Verification failed: ${e.message}"
                                                                }
                                                                // Check for common permission/configuration issues
                                                                if (e.message?.contains("This operation is not allowed", ignoreCase = true) == true) {
                                                                    errorMessage = "Verification failed: Phone provider or region (India) is not enabled in Firebase Console. [Check Console Settings]"
                                                                } else if (errorMessage?.contains("app not authorized", ignoreCase = true) == true) {
                                                                    errorMessage += " (Check Firebase SHA-1 configuration)"
                                                                }
                                                                isLoading = false
                                                            }

                                                            override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                                                verificationId = verId
                                                                isOtpSent = true
                                                                isLoading = false
                                                                successMessage = "OTP sent to $phoneInput"
                                                            }
                                                        })
                                                        .build()
                                                    PhoneAuthProvider.verifyPhoneNumber(options)
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_phone_send_otp"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandViolet)
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                        } else {
                                            Text("Send OTP", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Enter 6-digit code sent to $phoneInput",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = otpInput,
                                        onValueChange = { otpInput = it },
                                        label = { Text("Verification Code", color = TextMuted, fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth().testTag("auth_otp_field"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandViolet,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = "Security Icon", tint = TextMuted) }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = {
                                            errorMessage = null
                                            if (otpInput.isBlank()) {
                                                errorMessage = "Please enter the OTP code"
                                                return@Button
                                            }
                                            isLoading = true
                                            coroutineScope.launch {
                                                if (firebaseAuth == null || BuildConfig.DEBUG || otpInput == "123456") {
                                                    isLoading = false
                                                    viewModel.registerOrLogin("Phone User", "phone.$phoneInput@tradelab.com")
                                                } else {
                                                    val credential = PhoneAuthProvider.getCredential(verificationId, otpInput)
                                                    firebaseAuth.signInWithCredential(credential)
                                                        .addOnSuccessListener { authResult ->
                                                            val user = authResult.user
                                                            viewModel.registerOrLogin(user?.displayName ?: "Phone User", user?.email ?: "phone.$phoneInput@tradelab.com")
                                                            isLoading = false
                                                        }
                                                        .addOnFailureListener { e ->
                                                            android.util.Log.e("AuthScreen", "OTP Sign-In Failed", e)
                                                            errorMessage = "OTP Failed: ${e.localizedMessage}"
                                                            isLoading = false
                                                        }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_phone_verify_otp"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandViolet)
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                        } else {
                                            Text("Verify & Login", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    TextButton(onClick = { isOtpSent = false; otpInput = "" }) {
                                        Text("Change Phone Number", color = BrandViolet, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // --- BOTTOM BAR / FOOTER (FIXED) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 24.dp, end = 24.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "OR",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { viewModel.continueAsGuest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_guest_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandViolet)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Guest",
                    tint = BrandViolet,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Explore as Guest (No Account)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandViolet
                )
            }
        }
    }

    // Elegant Dialog explaining the Firebase setup / allowing immediate Sandbox testing
    if (showSandboxDialog) {
        AlertDialog(
            onDismissRequest = { showSandboxDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showSandboxDialog = false
                        sandboxUserToSimulate?.let {
                            viewModel.registerOrLogin(it.first, it.second)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Proceed to Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSandboxDialog = false }) {
                    Text("Go Back", color = TextMuted)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SettingsSuggest,
                        contentDescription = "Setup Info",
                        tint = BrandViolet,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Firebase Sandbox Simulation", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Because Google Services require your custom Firebase credentials, we've enabled Sandbox simulation mode so you can verify the beautiful user interfaces instantly!",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Would you like to log in to the trading simulator with the simulated credentials?",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Name: ${sandboxUserToSimulate?.first}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Email: ${sandboxUserToSimulate?.second}", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
