package connect.eyowo.android

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import connect.eyowo.pos.EyowoSdk

class MainViewModel : ViewModel() {
    var paymentItemUiState: PaymentItemUiState by mutableStateOf(PaymentItemUiState())

    fun updateAmount(amount: String) {
        paymentItemUiState =
            paymentItemUiState.copy(amount = amount, result = "", printResponse = "")
    }

    fun updatePrintStatus(checked: Boolean) {
        paymentItemUiState = paymentItemUiState.copy(isPrintingChecked = checked)
    }

    fun updateShowDialog(status: Boolean) {
        paymentItemUiState = paymentItemUiState.copy(openDialog = status)
    }
    fun print() {
        EyowoSdk.pay
            .print(POSPrintTextFormatterUtil().printReceipt(createOrderModel()))
            .process(object : EyowoSdk.ProcessPrintListener {
                override fun onPrintResponse(printResponse: EyowoSdk.PrintResponse) {
                    paymentItemUiState =
                        paymentItemUiState.copy(printResponse = printResponse.data)
                }

                override fun onPrintError(throwable: Throwable?) {
                    paymentItemUiState =
                        paymentItemUiState.copy(printResponse = throwable?.localizedMessage.toString())
                }
            })
    }

    fun printHistory() {
        EyowoSdk.pay
            .printHistory(object : EyowoSdk.ProcessPrintListener {
                override fun onPrintResponse(printResponse: EyowoSdk.PrintResponse) {
                    paymentItemUiState =
                        paymentItemUiState.copy(printResponse = printResponse.data)
                }

                override fun onPrintError(throwable: Throwable?) {
                    paymentItemUiState =
                        paymentItemUiState.copy(printResponse = throwable?.localizedMessage.toString())
                }
            })
    }

    fun processPayment() {
        val number: Double = paymentItemUiState.amount.toDouble()
        val formatedAmount = String.format("%.2f", number).toDouble()
        val transaction = EyowoSdk.Transaction("", formatedAmount.toString())

        /*viewModelScope.launch {
            val result = EyowoSdk.Pay(transaction)
                .auth("")
                .paymentChannels(listOf(EyowoSdk.PaymentChannels.TERMINAL))
                .processWithAwait()
            Log.e("Yeah", "returned: ${result.first?.data}")
        }*/
//        val appkey = "81f9764df00e58da15f1b9b21d894b0a" //"ab0d0de94c4586c3558e9bcd2d328f77"// "81f9764df00e58da15f1b9b21d894b0a" //"81f9764df00e58da15f1b9b21d894b0a"
        val appkey = "684d237f7de54964d0df329fee116c31" //""
        paymentItemUiState = paymentItemUiState.copy(processingPayment = true)
        EyowoSdk.pay
            .auth(appkey)
            .transaction(transaction)
            .paymentChannels(listOf(EyowoSdk.PaymentChannels.CARD))
            .process(object : EyowoSdk.ProcessPaymentListener {
                override fun onSuccess(transactionResponse: EyowoSdk.TransactionResponse) {
                    paymentItemUiState = paymentItemUiState.copy(
                        processingPayment = false,
                        result = transactionResponse.data,
                        openDialog = transactionResponse.data.lowercase().contains("successful")
                    )
                }

                override fun onError(
                    throwable: Throwable,
                    transactionResponse: EyowoSdk.TransactionResponse?
                ) {
                    paymentItemUiState = paymentItemUiState.copy(
                        processingPayment = false,
                        result = throwable.localizedMessage
                    )
                }
            })
    }
}