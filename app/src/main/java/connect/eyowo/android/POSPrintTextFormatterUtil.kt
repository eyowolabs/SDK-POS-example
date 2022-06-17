package connect.eyowo.android

import android.os.Environment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import connect.eyowo.pos.EyowoSdk
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class POSPrintTextFormatterUtil() {

    fun printBill(order: OrderModel): EyowoSdk.PrintObject {
        val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
        val logoPath = "$extStorageDirectory/business_logo.PNG"

        val printFields: ArrayList<EyowoSdk.PrintField> = arrayListOf()
        val stringFields: ArrayList<EyowoSdk.StringField> = arrayListOf()

        //Separator
        val separatorHeader =
            EyowoSdk.TextField("----------------------------", "center", "normal", false)
        val separatorBody = EyowoSdk.TextField("", "center", "normal", false)
        val separatorStringField = EyowoSdk.StringField(true, separatorHeader, separatorBody)

        //Space
        val spaceHeader = EyowoSdk.TextField("", "center", "large", true)
        val spaceBody = EyowoSdk.TextField("", "center", "large", false)
        val spaceStringField = EyowoSdk.StringField(true, spaceHeader, spaceBody)
        stringFields.add(spaceStringField)

        //Logo
        val printField = EyowoSdk.PrintField(logoPath, 5, stringFields)

        //Name of Business
        val businessNameHeader = EyowoSdk.TextField("", "center", "large", true)
        val businessNameBody = EyowoSdk.TextField(order.workspace.name, "center", "large", false)
        stringFields.add(EyowoSdk.StringField(true, businessNameHeader, businessNameBody))

        //Chanel
        val branchNameHeader = EyowoSdk.TextField("", "center", "normal", false)
        val branchNameBody = EyowoSdk.TextField(order.channel.name, "center", "normal", false)
        stringFields.add(EyowoSdk.StringField(true, branchNameHeader, branchNameBody))

        //Space
        stringFields.add(spaceStringField)

        //order amount
        val amountHeader = EyowoSdk.TextField("", "center", "large", true)
        val amountBilledBody = EyowoSdk.TextField(
            "NGN${order.totalAmount.convertToNaira().formatCurrency()}",
            "center",
            "large",
            true
        )
        stringFields.add(EyowoSdk.StringField(true, amountHeader, amountBilledBody))

        //order date
        val dateHeader = EyowoSdk.TextField("", "center", "normal", false)
        val dateBody =
            EyowoSdk.TextField("Created ${order.createdAt}", "center", "normal", false)
        stringFields.add(EyowoSdk.StringField(true, dateHeader, dateBody))

        //Separator
        stringFields.add(spaceStringField)

        //Order number
        val branchLocationHeader = EyowoSdk.TextField("Order number", "center", "normal", false)
        val branchLocationBody = EyowoSdk.TextField(order.orderNumber, "center", "normal", true)
        stringFields.add(EyowoSdk.StringField(false, branchLocationHeader, branchLocationBody))
        //}

        stringFields.add(separatorStringField)

        //Merchant
        order.merchant?.let {
            val tillNameHeader = EyowoSdk.TextField("Sold by", "center", "normal", false)
            val tillNameBody =
                EyowoSdk.TextField("${it.firstName} ${it.lastName}", "center", "normal", true)
            stringFields.add(EyowoSdk.StringField(false, tillNameHeader, tillNameBody))

            //Separator
            stringFields.add(separatorStringField)
        }

        //Cart items
        order.cart.items.forEach {
            val serviceHeader =
                EyowoSdk.TextField("${it.name} X ${it.quantity}", "center", "normal", true)
            val serviceBody = EyowoSdk.TextField(
                "NGN${it.price.convertToNaira().formatCurrency()}",
                "center",
                "normal",
                true
            )
            stringFields.add(EyowoSdk.StringField(false, serviceHeader, serviceBody))

            //Separator
            stringFields.add(separatorStringField)
        }

        //subtotal
        val subtotalHeader = EyowoSdk.TextField("Subtotal", "center", "normal", false)
        val subtotalBody = EyowoSdk.TextField(
            "NGN${order.cart.subTotal.convertToNaira().formatCurrency()}",
            "center",
            "normal",
            false
        )
        stringFields.add(EyowoSdk.StringField(false, subtotalHeader, subtotalBody))

        //tax
        val taxHeader = EyowoSdk.TextField("Tax", "center", "normal", false)
        val taxBody = EyowoSdk.TextField(
            "NGN${order.cart.tax.convertToNaira().formatCurrency()}",
            "center",
            "normal",
            false
        )
        stringFields.add(EyowoSdk.StringField(false, taxHeader, taxBody))

        //total
        val totalHeader = EyowoSdk.TextField("Total", "center", "normal", true)
        val totalBody = EyowoSdk.TextField(
            "NGN${order.cart.total.convertToNaira().formatCurrency()}",
            "center",
            "normal",
            true
        )
        stringFields.add(EyowoSdk.StringField(false, totalHeader, totalBody))

        //Space
        stringFields.add(spaceStringField)

        //Merchant Number
        order.merchant?.let {
            val merchantNumberHeader = EyowoSdk.TextField("", "center", "normal", true)
            val merchantNumberBody = EyowoSdk.TextField(
                "Have problem with this bill? \nCall ${it.phone}",
                "center",
                "normal",
                false
            )
            stringFields.add(EyowoSdk.StringField(true, merchantNumberHeader, merchantNumberBody))

            //Space
            stringFields.add(spaceStringField)
        }

        //Footer
        val footerHeader1 = EyowoSdk.TextField("", "center", "normal", true)
        val footerBody1 = EyowoSdk.TextField(
            "Invoicing and receipts are powered by <Company Name>.",
            "center",
            "normal",
            false
        )
        stringFields.add(EyowoSdk.StringField(true, footerHeader1, footerBody1))


        //Space
        stringFields.add(spaceStringField)

        val footerHeader2 = EyowoSdk.TextField("", "center", "normal", true)
        val footerBody2 = EyowoSdk.TextField(
            "2022 <Company Name> Limited. All rights reserved.",
            "center",
            "normal",
            false
        )
        stringFields.add(EyowoSdk.StringField(true, footerHeader2, footerBody2))

        //Compile
        printFields.add(printField)
        val printObject = EyowoSdk.PrintObject(printFields)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter<Any>(EyowoSdk.PrintObject::class.java)
        return printObject
    }

    fun printReceipt(order: OrderModel): EyowoSdk.PrintObject {
        val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
        val logoPath = "$extStorageDirectory/business_logo.PNG"

        val printFields: ArrayList<EyowoSdk.PrintField> = arrayListOf()
        val stringFields: ArrayList<EyowoSdk.StringField> = arrayListOf()

        //Separator
        val separatorHeader =
            EyowoSdk.TextField("----------------------------", "center", "normal", false)
        val separatorBody = EyowoSdk.TextField("", "center", "normal", false)
        val separatorStringField = EyowoSdk.StringField(true, separatorHeader, separatorBody)

        //Space
        val spaceHeader = EyowoSdk.TextField("", "center", "large", true)
        val spaceBody = EyowoSdk.TextField("", "center", "large", false)
        val spaceStringField = EyowoSdk.StringField(true, spaceHeader, spaceBody)
        stringFields.add(spaceStringField)

        //Logo
        val printField = EyowoSdk.PrintField(logoPath, 5, stringFields)

        //Name of Business
        val businessNameHeader = EyowoSdk.TextField("", "center", "large", true)
        val businessNameBody = EyowoSdk.TextField(order.workspace.name, "center", "large", false)
        stringFields.add(EyowoSdk.StringField(true, businessNameHeader, businessNameBody))

        //Chanel
        val branchNameHeader = EyowoSdk.TextField("", "center", "normal", false)
        val branchNameBody = EyowoSdk.TextField(order.channel.name, "center", "normal", false)
        stringFields.add(EyowoSdk.StringField(true, branchNameHeader, branchNameBody))

        //Space
        stringFields.add(spaceStringField)

        //order amount
        val amountHeader = EyowoSdk.TextField("", "center", "large", true)
        val amountBilledBody = EyowoSdk.TextField(
            "NGN${order.totalAmount.convertToNaira().formatCurrency()}",
            "center",
            "large",
            true
        )
        stringFields.add(EyowoSdk.StringField(true, amountHeader, amountBilledBody))

        //order date
        val dateHeader = EyowoSdk.TextField("", "center", "normal", false)
        val dateBody =
            EyowoSdk.TextField("Created ${order.createdAt}", "center", "normal", false)
        stringFields.add(EyowoSdk.StringField(true, dateHeader, dateBody))

        //Separator
        stringFields.add(spaceStringField)

        //Order number
        val branchLocationHeader = EyowoSdk.TextField("Order number", "center", "normal", false)
        val branchLocationBody = EyowoSdk.TextField(order.orderNumber, "center", "normal", true)
        stringFields.add(EyowoSdk.StringField(false, branchLocationHeader, branchLocationBody))
        //}

        stringFields.add(separatorStringField)

        //Merchant
        order.merchant?.let {
            val tillNameHeader = EyowoSdk.TextField("Sold by", "center", "normal", false)
            val tillNameBody = EyowoSdk.TextField(
                "${order.merchant.firstName} ${order.merchant.lastName}",
                "center",
                "normal",
                true
            )
            stringFields.add(EyowoSdk.StringField(false, tillNameHeader, tillNameBody))

            //Separator
            stringFields.add(separatorStringField)
        }

        //Cart items
        order.cart.items.forEach {
            val serviceHeader =
                EyowoSdk.TextField("${it.name} X ${it.quantity}", "center", "normal", true)
            val serviceBody = EyowoSdk.TextField(
                "NGN${it.price.convertToNaira().formatCurrency()}",
                "center",
                "normal",
                true
            )
            stringFields.add(EyowoSdk.StringField(false, serviceHeader, serviceBody))

            //Separator
            stringFields.add(separatorStringField)
        }

        //subtotal
        val subtotalHeader = EyowoSdk.TextField("Subtotal", "center", "normal", false)
        val subtotalBody = EyowoSdk.TextField(
            "NGN${order.cart.subTotal.convertToNaira().formatCurrency()}",
            "center",
            "normal",
            false
        )
        stringFields.add(EyowoSdk.StringField(false, subtotalHeader, subtotalBody))

        //tax
        val taxHeader = EyowoSdk.TextField("Tax", "center", "normal", false)
        val taxBody = EyowoSdk.TextField(
            "NGN${order.cart.tax.convertToNaira().formatCurrency()}",
            "center",
            "normal",
            false
        )
        stringFields.add(EyowoSdk.StringField(false, taxHeader, taxBody))

        //total
        val totalHeader = EyowoSdk.TextField("Total", "center", "normal", true)
        val totalBody = EyowoSdk.TextField(
            "NGN${order.cart.total.convertToNaira().formatCurrency()}",
            "center",
            "normal",
            true
        )
        stringFields.add(EyowoSdk.StringField(false, totalHeader, totalBody))

        //Space
        stringFields.add(spaceStringField)

        //payment reference
        order.payments.forEach { payment ->
            val paymentRefHeader =
                EyowoSdk.TextField("Payment reference", "center", "normal", false)
            val paymentRefBody = EyowoSdk.TextField(payment.reference, "center", "normal", true)
            stringFields.add(EyowoSdk.StringField(false, paymentRefHeader, paymentRefBody))

            //Seperator
            stringFields.add(separatorStringField)

            val paymentMethodHeader =
                EyowoSdk.TextField("Payment method", "center", "normal", false)
            val paymentMethodBody = EyowoSdk.TextField(payment.method, "center", "normal", true)
            stringFields.add(EyowoSdk.StringField(false, paymentMethodHeader, paymentMethodBody))

            //Space
            stringFields.add(spaceStringField)
        }

        //Merchant Number
        order.merchant?.let {
            val merchantNumberHeader = EyowoSdk.TextField("", "center", "normal", true)
            val merchantNumberBody = EyowoSdk.TextField(
                "Have problem with this bill? \nCall ${it.phone}",
                "center",
                "normal",
                false
            )
            stringFields.add(EyowoSdk.StringField(true, merchantNumberHeader, merchantNumberBody))

            //Space
            stringFields.add(spaceStringField)
        }

        //Footer
        val footerHeader1 = EyowoSdk.TextField("", "center", "normal", true)
        val footerBody1 = EyowoSdk.TextField(
            "Invoicing and receipts are powered by <Company Name>.",
            "center",
            "normal",
            false
        )
        stringFields.add(EyowoSdk.StringField(true, footerHeader1, footerBody1))


        //Space
        stringFields.add(spaceStringField)

        val footerHeader2 = EyowoSdk.TextField("", "center", "normal", true)
        val footerBody2 = EyowoSdk.TextField(
            "2022 <Company Name> Limited. All rights reserved.",
            "center",
            "normal",
            false
        )
        stringFields.add(EyowoSdk.StringField(true, footerHeader2, footerBody2))

        //Compile
        printFields.add(printField)
        val printObject = EyowoSdk.PrintObject(printFields)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter<Any>(EyowoSdk.PrintObject::class.java)
        return printObject
    }

    private fun formatDate(orderDate: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val output = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault())

        var d: Date? = null
        try {
            d = input.parse(orderDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return output.format(d)
    }
}

data class OrderModel(
    val merchant: Merchant,
    val workspace: Workspace,
    val channel: Channel,
    val cart: Cart,
    val orderNumber: String,
    val totalAmount: Double,
    val createdAt: String,
    val payments: List<Payment>
)

data class Merchant(
    val firstName: String,
    val lastName: String,
    val phone: String,
)

data class Channel(
    val name: String
)

data class Workspace(
    val name: String
)

data class Cart(
    val items: List<Item>,
    val subTotal: Double,
    val tax: Double,
    val total: Double,
)

data class Item(
    val name: String,
    val quantity: String,
    val price: Double,
)

data class Payment(
    val reference: String,
    val method: String
)

fun Double.convertToNaira(): String {
    return (this * 100).toString()
}

fun String.formatCurrency(): String {
    return this
}

fun createOrderModel(): OrderModel {
    return OrderModel(
        merchant = Merchant("Merchant", "Name", "0800000000"),
        workspace = Workspace("Workspace"),
        channel = Channel("Channel"),
        cart = Cart(
            items = listOf(
                Item("Item 1", "10", 1000.00),
                Item("Item 2", "100", 200.00)
            ),
            subTotal = 1200.00,
            tax = 10.00,
            total = 1210.00
        ),
        orderNumber = "235636290391234",
        totalAmount = 1210.00,
        createdAt = "2022-05-01 08:00:00",
        payments = listOf(
            Payment("2022-05-01 08:00:00", "CARD")
        )

    )
}