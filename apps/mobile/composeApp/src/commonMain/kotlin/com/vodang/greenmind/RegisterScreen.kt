package com.vodang.greenmind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.location.Geo
import kotlinx.coroutines.launch
import com.vodang.greenmind.api.auth.RegisterEmailRequest
import com.vodang.greenmind.api.auth.registerWithEmail
import com.vodang.greenmind.api.cities.getCitiesByCountry
import com.vodang.greenmind.api.restcountries.CountryDto
import com.vodang.greenmind.api.restcountries.getAllCountries
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.register.classifyVietnamRegionByLatitude
import com.vodang.greenmind.register.pickerMillisToIsoDate
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onCancel: () -> Unit) {
    val s = LocalAppStrings.current
    var fullName     by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirm      by remember { mutableStateOf("") }
    var road         by remember { mutableStateOf("") }
    var city         by remember { mutableStateOf("") }
    var gender       by remember { mutableStateOf("other") }
    var dateOfBirth  by remember { mutableStateOf("") }
    var showDobPicker by remember { mutableStateOf(false) }

    var selectedCountry    by remember { mutableStateOf<CountryDto?>(null) }
    var showCountryPicker  by remember { mutableStateOf(false) }
    var countrySearch      by remember { mutableStateOf("") }
    var countries          by remember { mutableStateOf<List<CountryDto>>(emptyList()) }
    var countriesLoading    by remember { mutableStateOf(false) }

    var showCityPicker     by remember { mutableStateOf(false) }
    var citySearch         by remember { mutableStateOf("") }
    var citiesForCountry  by remember { mutableStateOf<List<String>>(emptyList()) }
    var citiesLoading       by remember { mutableStateOf(false) }

    var acceptTerms     by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }
    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentStep  by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    val green800 = Color(0xFF2E7D32)
    val green500 = Color(0xFF4CAF50)
    val fieldShape = RoundedCornerShape(12.dp)

    val step1Valid = fullName.isNotBlank() && email.isNotBlank() && dateOfBirth.isNotBlank()
    val step2Valid = password.isNotEmpty() && password == confirm
    val step3Valid = acceptTerms

    LaunchedEffect(currentStep) {
        if (currentStep == 3 && countries.isEmpty()) {
            countriesLoading = true
            countries = try { getAllCountries() } catch (_: Throwable) { emptyList() }
            countriesLoading = false
        }
    }

    LaunchedEffect(selectedCountry) {
        val countryName = selectedCountry?.name?.common ?: return@LaunchedEffect
        city = ""
        citiesForCountry = emptyList()
        citiesLoading = true
        citiesForCountry = try { getCitiesByCountry(countryName) } catch (_: Throwable) { emptyList() }
        citiesLoading = false
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFF1F8E9))))
    ) {
        val isSmall = maxHeight < 640.dp

        val cardPad = if (isSmall) 16.dp else 20.dp
        val fSpace  = if (isSmall) 10.dp else 14.dp
        val logoSz  = if (isSmall) 60.dp else 72.dp
        val logoFs  = if (isSmall) 26.sp else 32.sp
        val titleFs = if (isSmall) 18.sp else 26.sp
        val dotSz   = if (isSmall) 28.dp else 32.dp
        val dotFs   = if (isSmall) 11.sp else 13.sp
        val lblFs   = if (isSmall) 10.sp else 11.sp
        val divW    = if (isSmall) 24.dp else 40.dp
        val btnH    = if (isSmall) 46.dp else 52.dp
        val btnFs   = if (isSmall) 13.sp else 15.sp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(if (isSmall) 16.dp else 32.dp))

            Box(
                modifier = Modifier.size(logoSz).background(green800, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    modifier = Modifier.size(logoSz * 0.7f),
                    tint = Color.White,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(s.appName, fontSize = titleFs, fontWeight = FontWeight.Bold, color = green800)
            Text(s.createAccount, fontSize = if (isSmall) 11.sp else 13.sp, color = Color(0xFF66BB6A))

            Spacer(Modifier.height(if (isSmall) 10.dp else 16.dp))

            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val stepLabels = listOf(s.stepInfo, s.stepPassword, s.stepAddress)
                stepLabels.forEachIndexed { index, label ->
                    val step = index + 1
                    val isActive   = step == currentStep
                    val isComplete = step < currentStep
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(dotSz)
                                .background(
                                    color = when {
                                        isActive  -> Color(0xFF2E7D32)
                                        isComplete -> green500
                                        else       -> Color(0xFFBDBDBD)
                                    },
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isComplete) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            } else {
                                Text(
                                    "$step",
                                    color = Color.White,
                                    fontSize = dotFs,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            label,
                            fontSize = lblFs,
                            color = if (isActive) Color(0xFF2E7D32) else Color(0xFF9E9E9E),
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                    if (index < stepLabels.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.width(divW).padding(bottom = 18.dp),
                            color = if (step < currentStep) green500 else Color(0xFFBDBDBD),
                            thickness = 2.dp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(if (isSmall) 12.dp else 16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(cardPad).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(fSpace),
                ) {
                    val stepTitle = when (currentStep) {
                        1 -> s.yourInformation
                        2 -> s.setPassword
                        else -> s.addressAndTerms
                    }
                    Text(
                        stepTitle,
                        fontSize = if (isSmall) 17.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                    )

                    when (currentStep) {
                        1 -> {
                            OutlinedTextField(
                                value = fullName, onValueChange = { fullName = it },
                                label = { Text(s.fullName) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = fieldShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = green800,
                                    focusedLabelColor = green800,
                                    cursorColor = green800,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            )
                            OutlinedTextField(
                                value = email, onValueChange = { email = it },
                                label = { Text(s.emailAddress) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = fieldShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = green800,
                                    focusedLabelColor = green800,
                                    cursorColor = green800,
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done,
                                ),
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = dateOfBirth,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(s.dateOfBirth) },
                                    placeholder = { Text("YYYY-MM-DD", color = Color.Gray) },
                                    trailingIcon = { Text("📅") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = fieldShape,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = green800,
                                        focusedLabelColor = green800,
                                        cursorColor = green800,
                                    ),
                                )
                                Spacer(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { showDobPicker = true },
                                )
                            }
                            Text(s.gender, fontSize = if (isSmall) 12.sp else 13.sp, color = Color(0xFF757575))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf(
                                    "male"   to s.male,
                                    "female" to s.female,
                                    "other"  to s.other,
                                ).forEach { (value, lbl) ->
                                    val selected = gender == value
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) Color(0xFFE8F5E9) else Color.White,
                                        border = if (selected) null else BorderStroke(1.dp, Color(0xFFDDDDDD)),
                                        modifier = Modifier.clickable { gender = value },
                                    ) {
                                        Text(
                                            lbl,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            fontSize = if (isSmall) 12.sp else 13.sp,
                                            color = if (selected) green800 else Color(0xFF424242),
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = { currentStep = 2 },
                                enabled = step1Valid,
                                modifier = Modifier.fillMaxWidth().height(btnH),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = green800),
                            ) {
                                Text(s.next, fontSize = if (isSmall) 14.sp else 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        2 -> {
                            val hasMismatch = confirm.isNotEmpty() && confirm != password
                            OutlinedTextField(
                                value = password, onValueChange = { password = it },
                                label = { Text(s.password) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = fieldShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = green800,
                                    focusedLabelColor = green800,
                                    cursorColor = green800,
                                ),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                trailingIcon = {
                                    TextButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                    ) {
                                        Text(if (passwordVisible) s.hide else s.show, fontSize = 12.sp, color = green500)
                                    }
                                },
                            )
                            OutlinedTextField(
                                value = confirm, onValueChange = { confirm = it },
                                label = { Text(s.confirmPassword) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = fieldShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (hasMismatch) Color.Red else green800,
                                    focusedLabelColor = green800,
                                    cursorColor = green800,
                                ),
                                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                trailingIcon = {
                                    TextButton(
                                        onClick = { confirmVisible = !confirmVisible },
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                    ) {
                                        Text(if (confirmVisible) s.hide else s.show, fontSize = 12.sp, color = green500)
                                    }
                                },
                                supportingText = if (hasMismatch) {
                                    { Text(s.passwordMismatch, color = Color.Red, fontSize = 11.sp) }
                                } else null,
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier.weight(1f).height(btnH),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, green800),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = green800),
                                ) {
                                    Text(s.back, fontSize = btnFs, fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = { currentStep = 3 },
                                    enabled = step2Valid,
                                    modifier = Modifier.weight(1f).height(btnH),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = green800),
                                ) {
                                    Text(s.next, fontSize = btnFs, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        3 -> {
                            Text(s.address, fontSize = if (isSmall) 12.sp else 13.sp, color = Color(0xFF757575))

                            OutlinedTextField(
                                value = road, onValueChange = { road = it },
                                label = { Text(s.road) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = fieldShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = green800,
                                    focusedLabelColor = green800,
                                    cursorColor = green800,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            )

                            // Country selector
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedCountry?.let { "${it.flag}  ${it.name.common}" } ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(s.country) },
                                    placeholder = { Text(s.selectCountry, color = Color.Gray) },
                                    trailingIcon = {
                                        if (countriesLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = green800)
                                        } else {
                                            Text("▼", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = fieldShape,
                                    colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = green800,
                                    focusedLabelColor = green800,
                                    cursorColor = green800,
                                ),
                                )
                                Spacer(modifier = Modifier.matchParentSize().clickable(enabled = !countriesLoading) { showCountryPicker = true })
                            }

                            // City selector
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(s.city) },
                                    placeholder = { Text(s.selectCity, color = Color.Gray) },
                                    trailingIcon = {
                                        if (citiesLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = green800)
                                        } else {
                                            Text("▼", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = fieldShape,
                                    colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = green800,
                                    focusedLabelColor = green800,
                                    cursorColor = green800,
                                ),
                                )
                                Spacer(modifier = Modifier.matchParentSize()
                                    .clickable(enabled = selectedCountry != null && !citiesLoading) { showCityPicker = true })
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { acceptTerms = !acceptTerms }.padding(vertical = 4.dp),
                            ) {
                                Checkbox(
                                    checked = acceptTerms,
                                    onCheckedChange = { acceptTerms = it },
                                    modifier = Modifier.size(20.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = green800,
                                        checkmarkColor = Color.White,
                                        uncheckedColor = Color(0xFF9E9E9E),
                                    ),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(s.acceptTerms, fontSize = 13.sp, color = Color(0xFF424242))
                            }

                            if (errorMessage != null) {
                                Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier.weight(1f).height(btnH),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, green800),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = green800),
                                ) {
                                    Text(s.back, fontSize = btnFs, fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        if (!step3Valid) return@Button
                                        scope.launch {
                                            isLoading = true
                                            try {
                                                val countryName = selectedCountry?.name?.common
                                                val currentLat = Geo.service.locationUpdates.firstOrNull()?.latitude
                                                val region = currentLat?.let(::classifyVietnamRegionByLatitude) ?: "Central"
                                                val req = RegisterEmailRequest(
                                                    email = email,
                                                    password = password,
                                                    confirmPassword = confirm,
                                                    fullName = fullName.ifBlank { "User" },
                                                    dateOfBirth = dateOfBirth,
                                                    location = listOfNotNull(
                                                        road.ifBlank { null },
                                                        city.ifBlank { null },
                                                        countryName,
                                                    ).joinToString(", "),
                                                    gender = gender,
                                                    region = region,
                                                )
                                                val resp = registerWithEmail(req)
                                                SettingsStore.setAccessToken(resp.accessToken)
                                                SettingsStore.setRefreshToken(resp.refreshToken)
                                                SettingsStore.setUser(resp.user)
                                                onRegisterSuccess()
                                            } catch (t: Throwable) {
                                                errorMessage = t.message ?: "Unknown error"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    enabled = step3Valid && !isLoading,
                                    modifier = Modifier.weight(1f).height(btnH),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = green800),
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text(s.register, fontSize = btnFs, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(if (isSmall) 12.dp else 16.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = if (isSmall) 8.dp else 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
                    Text(s.or, fontSize = 12.sp, color = Color(0xFF9E9E9E))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(btnH),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFDBDBDB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3C4043)),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                        Spacer(Modifier.width(10.dp))
                        Text(s.continueWithGoogle, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(if (isSmall) 6.dp else 10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.alreadyHaveAccount, fontSize = 13.sp, color = Color(0xFF757575))
                    TextButton(onClick = onCancel) {
                        Text(s.signIn, fontSize = 13.sp, color = green800, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(if (isSmall) 20.dp else 32.dp))
        }
    }

    // ── Country picker ──────────────────────────────────────────────────────────
    if (showCountryPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCountryPicker = false; countrySearch = "" },
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(s.selectCountry, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = countrySearch,
                    onValueChange = { countrySearch = it },
                    placeholder = { Text(s.searchCountry, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green800,
                        focusedLabelColor = green800,
                        cursorColor = green800,
                    ),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray) },
                )
                Spacer(Modifier.height(8.dp))
            }
            val filtered = countries.filter { countrySearch.isBlank() || it.name.common.contains(countrySearch, ignoreCase = true) }
            LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                items(filtered, key = { it.cca2 }) { country ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedCountry = country
                            showCountryPicker = false
                            countrySearch = ""
                        }.padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(country.flag.ifBlank { "🏳️" }, fontSize = 24.sp)
                        Text(country.name.common, fontSize = 15.sp, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                        if (selectedCountry?.cca2 == country.cca2) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = green800)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                }
            }
        }
    }

    // ── City picker ─────────────────────────────────────────────────────────────
    if (showCityPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCityPicker = false; citySearch = "" },
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(s.selectCity, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = citySearch,
                    onValueChange = { citySearch = it },
                    placeholder = { Text(s.searchCity, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green800,
                        focusedLabelColor = green800,
                        cursorColor = green800,
                    ),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray) },
                )
                Spacer(Modifier.height(8.dp))
            }
            val filtered = citiesForCountry.filter { citySearch.isBlank() || it.contains(citySearch, ignoreCase = true) }
            if (filtered.isEmpty() && !citiesLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(citySearch.ifBlank { s.selectCountry }, fontSize = 14.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    items(filtered, key = { it }) { cityName ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                city = cityName
                                showCityPicker = false
                                citySearch = ""
                            }.padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp), tint = green800)
                            Text(cityName, fontSize = 15.sp, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                            if (city == cityName) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = green800)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    if (showDobPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDobPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) dateOfBirth = pickerMillisToIsoDate(picked)
                        showDobPicker = false
                    },
                ) { Text(s.select) }
            },
            dismissButton = {
                TextButton(onClick = { showDobPicker = false }) { Text(s.cancel) }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
