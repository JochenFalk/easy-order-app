package com.easysystems.easyorder.data

import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.helpclasses.AppSettings
import com.easysystems.easyorder.repositories.OrderRepository
import com.easysystems.easyorder.repositories.PaymentRepository
import com.easysystems.easyorder.repositories.SessionRepository
import com.fasterxml.jackson.annotation.JsonProperty
import org.koin.java.KoinJavaComponent.inject
import java.io.Serializable
import java.text.DecimalFormat
import java.text.NumberFormat

data class SessionDTO(
    @JsonProperty("id")
    var id: Int? = null,
    @JsonProperty("status")
    var status: Status? = null,
    @JsonProperty("tabletopDTO")
    var tabletop: TabletopDTO? = null,
    @JsonProperty("total")
    var total: Double? = null,
    @JsonProperty("orders")
    var orders: ArrayList<OrderDTO>? = null,
    @JsonProperty("payments")
    var payments: ArrayList<MolliePaymentDTO>? = null
) : Serializable {

    private val sessionRepository: SessionRepository by inject(SessionRepository::class.java)
    private val orderRepository: OrderRepository by inject(OrderRepository::class.java)
    private val paymentRepository: PaymentRepository by inject(PaymentRepository::class.java)

    enum class Status {
        OPENED,
        CLOSED,
        LOCKED,
        CHANGED
    }

    fun updateSession(callback: (SessionDTO?) -> Unit) {
        sessionRepository.updateSession(this) { sessionDTO ->

            if (sessionDTO != null) {

                sessionDTO.orders?.sortBy { it.id }
                sessionDTO.payments?.sortBy { it.molliePaymentId }
                MainActivity.sessionDTO = sessionDTO

                if (sessionDTO.payments?.size != 0) {
                    MainActivity.paymentDTO = sessionDTO.payments?.last()
                }

                callback(sessionDTO)
            }
        }
    }

    fun addNewPaymentToSession(callback: (MolliePaymentDTO?) -> Unit) {
        this.id?.let { id ->

            val paymentMethod = MainActivity.paymentMethod

            val decimal: NumberFormat = DecimalFormat("0.00")
            val amount = decimal.format(this.total).replace(',', '.')

            val ngRokBaseURL = AppSettings.ngRokBaseURL
            val mollieRedirectUrl = AppSettings.mollieRedirectUrl
            val mollieWebhookMapping = AppSettings.mollieWebhookMapping

            val jsonString = "{\n" +
                    "   \"description\": \"Session #${this.id.toString()}\",\n" +
                    "   \"redirectUrl\": \"$mollieRedirectUrl\",\n" +
                    "   \"webhookUrl\": \"$ngRokBaseURL$mollieWebhookMapping\",\n" +
                    "   \"method\": \"$paymentMethod\",\n" +
                    "   \"amount\": \n" +
                    "  {\n" +
                    "    \"currency\": \"EUR\",\n" +
                    "    \"value\": \"$amount\"\n" +
                    "  }\n" +
                    "}"

            paymentRepository.retrievePaymentFromMollie(
                jsonString,
                id
            ) { paymentFromMollie ->

                if (paymentFromMollie != null) {

                    paymentFromMollie.sessionId = this.id!!
                    val paymentFromMollieDTO = paymentFromMollie.convertPaymentToPaymentDTO()

                    paymentRepository.createPaymentOnBackend(
                        this,
                        paymentFromMollieDTO
                    ) { paymentFromBackendDTO ->

                        if (paymentFromBackendDTO != null) {
                            this.payments?.add(paymentFromBackendDTO)
                            this.status = Status.CHANGED
                            callback(paymentFromBackendDTO)
                        }
                    }
                }
            }
        }
    }

    fun resetPaymentStatus(callback: (SessionDTO?) -> Unit) {

        if (this.status != Status.CLOSED) {
            this.status = Status.OPENED
            this.orders?.last()?.status = OrderDTO.Status.OPENED
            callback(this)
        }
    }

    fun addNewOrderToSession(callback: (OrderDTO?) -> Unit) {
        this.id?.let { id ->

            orderRepository.createOrder(id) { orderDTO ->

                this.orders?.add(orderDTO as OrderDTO)
                callback(orderDTO)
            }
        }
    }

    fun resetSession() {

        if (this.status != Status.CLOSED) {

            this.status = Status.OPENED

            if (this.orders?.last()?.status != OrderDTO.Status.OPENED) {
                this.addNewOrderToSession {
                    this.updateSession { }
                }
            } else {
                this.updateSession { }
            }
        }
    }

    fun closeSession(callback: (SessionDTO?) -> Unit) {

        val lastOrder = this.orders?.last()

        if (lastOrder != null) {
            if ((lastOrder.items?.size == 0) && (lastOrder.total == 0.0)) {
                lastOrder.status = OrderDTO.Status.CANCELED
            }
        }

        this.status = Status.CLOSED

        MainActivity.sessionDTO = SessionDTO()
        MainActivity.paymentDTO = null
        MainActivity.paymentMethod = ""

        callback(this)
    }
}