package connect.eyowo.android

data class PaymentItemUiState(
    val amount: String = "",
    val processingPayment: Boolean = false,
    val result: String = "",
    val printResponse: String = "",
    val isPrintingChecked: Boolean = false,
    val openDialog: Boolean = false
){
    val isValidAmount = amount.isNotEmpty()
    val buttonEnabled = isValidAmount && !processingPayment
}