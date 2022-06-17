package connect.eyowo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import connect.eyowo.android.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    PaymentActivityScreen(mainViewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun PaymentActivityScreen(mainViewModel: MainViewModel) {
    PaymentScreen(
        mainViewModel.paymentItemUiState,
        updateAmount = mainViewModel::updateAmount,
        updateShowDialog = mainViewModel::updateShowDialog,
        updatePrintStatus = mainViewModel::updatePrintStatus,
        processPayment = { mainViewModel.processPayment() },
        print = mainViewModel::print,
        printHistory = mainViewModel::printHistory,
    )
}

@Composable
fun PaymentScreen(
    paymentItemUiState: PaymentItemUiState,
    updateAmount: (String) -> Unit,
    updateShowDialog: (Boolean) -> Unit,
    updatePrintStatus: (Boolean) -> Unit,
    processPayment: () -> Unit,
    print: () -> Unit,
    printHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = paymentItemUiState.amount,
            onValueChange = updateAmount,
            label = { Text(text = "Amount") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !paymentItemUiState.processingPayment
        )
        /*
        Spacer(modifier = Modifier.height(16.dp))
        Row() {
            Text(text = "Should include printing")
            Spacer(modifier = Modifier.width(8.dp))
            Checkbox(
                checked = paymentItemUiState.isPrintingChecked,
                onCheckedChange = updatePrintStatus
            )
        }
        */
        Spacer(modifier = Modifier.height(36.dp))
        if (paymentItemUiState.processingPayment)
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        if (!paymentItemUiState.processingPayment)
            Button(
                onClick = {
                    processPayment()
                    updateAmount("")
                },
                enabled = paymentItemUiState.buttonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Pay Now!")
            }
        Spacer(modifier = Modifier.height(64.dp))
        if (!paymentItemUiState.processingPayment && paymentItemUiState.result.isNotEmpty())
            Text(text = "Result: ${paymentItemUiState.result.substringBefore(":")}")
        if (paymentItemUiState.openDialog)
            AlertDialog(
                onDismissRequest = {
                    // Dismiss the dialog when the user clicks outside the dialog or on the back
                    // button. If you want to disable that functionality, simply use an empty
                    // onCloseRequest.
                    updateShowDialog(false)
                },
                title = {
                    Text(text = "Print")
                },
                text = {
                    Text("Print customer copy")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            updateShowDialog(false)
                            print()
                        }) {
                        Text("Print")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            updateShowDialog(false)
                        }) {
                        Text("Cancel")
                    }
                }
            )
        Spacer(modifier = Modifier.height(16.dp))
        if (paymentItemUiState.printResponse.isNotEmpty())
            Text(text = "Printing response: ${paymentItemUiState.printResponse}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
               printHistory()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Print History")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        val item = PaymentItemUiState()
        PaymentScreen(item, {}, {}, {}, {}, {}, {})
    }
}