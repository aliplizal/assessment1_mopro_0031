package com.aliplizal607062300031.assessment1.ui.screen

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.aliplizal607062300031.assessment1.R
import com.aliplizal607062300031.assessment1.navigation.Screen
import com.aliplizal607062300031.assessment1.ui.theme.Assessment1Theme
import kotlin.math.pow
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    var rawLoanAmount by rememberSaveable { mutableStateOf("") }
    var loanAmount by rememberSaveable { mutableStateOf("") }
    var loanAmountError by rememberSaveable { mutableStateOf(false) }

    var interestRate by rememberSaveable { mutableStateOf("") }
    var interestRateError by rememberSaveable { mutableStateOf(false) }

    val durations = listOf("1 Tahun", "5 Tahun", "10 Tahun")
    var selectedDuration by rememberSaveable { mutableStateOf(durations[0]) }

    var monthlyPayment by rememberSaveable { mutableStateOf(0.0) }
    var resultText by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.About.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.about_app),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.loan_image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Text(
                text = stringResource(id = R.string.loan_intro),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = loanAmount,
                onValueChange = { input ->
                    val raw = input.replace(".", "").filter { it.isDigit() }
                    rawLoanAmount = raw
                    loanAmount = formatRupiah(raw)
                },
                label = { Text(text = stringResource(R.string.loan_amount)) },
                leadingIcon = { IconPicker(loanAmountError, "Rp") },
                supportingText = { ErrorHint(loanAmountError) },
                isError = loanAmountError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = interestRate,
                onValueChange = { interestRate = it },
                label = { Text(text = stringResource(R.string.interest_rate)) },
                trailingIcon = { IconPicker(interestRateError, "%") },
                supportingText = { ErrorHint(interestRateError) },
                isError = interestRateError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(text = stringResource(R.string.duration_label))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                durations.forEach { duration ->
                    LoanDurationOption(
                        label = duration,
                        isSelected = selectedDuration == duration,
                        modifier = Modifier
                            .selectable(
                                selected = selectedDuration == duration,
                                onClick = { selectedDuration = duration },
                                role = Role.RadioButton
                            )
                            .padding(8.dp)
                    )
                }
            }

            Button(
                onClick = {
                    loanAmountError = (rawLoanAmount.isEmpty() || rawLoanAmount == "0")
                    interestRateError = (interestRate.isEmpty() || interestRate == "0")
                    if (loanAmountError || interestRateError) return@Button

                    val principal = rawLoanAmount.toDouble()
                    val annualRate = interestRate.toDouble()
                    val durationInYears = selectedDuration.split(" ")[0].toInt()

                    val monthlyRate = annualRate / 1200
                    val numberOfPayments = durationInYears * 12

                    monthlyPayment = if (monthlyRate != 0.0)
                        principal * monthlyRate / (1 - (1 + monthlyRate).pow(-numberOfPayments))
                    else
                        principal / numberOfPayments

                    val formatter = DecimalFormat("Rp #,###.00", DecimalFormatSymbols().apply {
                        groupingSeparator = '.'
                        decimalSeparator = ','
                    })
                    resultText = context.getString(R.string.monthly_payment, formatter.format(monthlyPayment))
                },
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Text(text = stringResource(R.string.calculate))
            }

            if (monthlyPayment != 0.0) {
                Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp)
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.titleLarge
                )
                Button(
                    onClick = {
                        val formatter = DecimalFormat("Rp #,###.00", DecimalFormatSymbols().apply {
                            groupingSeparator = '.'
                            decimalSeparator = ','
                        })
                        val formattedPayment = formatter.format(monthlyPayment)

                        shareData(
                            context = context,
                            message = context.getString(
                                R.string.share_template,
                                loanAmount,
                                interestRate,
                                selectedDuration,
                                formattedPayment
                            )
                        )
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(R.string.share))
                }
            }
        }
    }
}

fun formatRupiah(input: String): String {
    if (input.isEmpty()) return ""
    val formatter = DecimalFormat("#,###", DecimalFormatSymbols().apply { groupingSeparator = '.' })
    return try {
        formatter.format(input.toLong())
    } catch (e: NumberFormatException) {
        input
    }
}

@Composable
fun LoanDurationOption(label: String, isSelected: Boolean, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun IconPicker(isError: Boolean, unit: String) {
    if (isError) {
        Icon(imageVector = Icons.Filled.Warning, contentDescription = null)
    } else {
        Text(text = unit)
    }
}

@Composable
fun ErrorHint(isError: Boolean) {
    if (isError) {
        Text(text = stringResource(R.string.invalid_input))
    }
}

private fun shareData(context: Context, message: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    if (shareIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(shareIntent)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    Assessment1Theme {
        MainScreen(rememberNavController())
    }
}
