package net.corda.examples.obligation

import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.NullKeys
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.utilities.toBase58String
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@BelongsToContract(ObligationContract::class)
data class Obligation(val amount: Amount<Currency>,
                      val lender: AbstractParty,
                      val borrower: AbstractParty,
                      val paid: Amount<Currency> = Amount(0, amount.token),
                      val remark: String,
                      override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState, QueryableState {
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ObligationSchemaV1)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ObligationSchemaV1 -> ObligationSchemaV1.PersistentObligation(
                    this.lender.nameOrNull().toString(),
                    this.borrower.nameOrNull().toString(),
                    this.amount.token.toString(),
                    this.amount.quantity,
                    this.paid.token.toString(),
                    this.paid.quantity,
                    this.remark,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override val participants: List<AbstractParty> get() = listOf(lender, borrower)

    fun pay(amountToPay: Amount<Currency>) = copy(paid = paid + amountToPay)
    fun withNewLender(newLender: AbstractParty) = copy(lender = newLender)
    fun withoutLender() = copy(lender = NullKeys.NULL_PARTY)

    override fun toString(): String {
        val lenderString = (lender as? Party)?.name?.organisation ?: lender.owningKey.toBase58String()
        val borrowerString = (borrower as? Party)?.name?.organisation ?: borrower.owningKey.toBase58String()
        return "Obligation($linearId): $borrowerString owes $lenderString $amount and has paid $paid so far."
    }
}


/**
 * The family of schemas for IOUState.
 */
object ObligationSchema

/**
 * An IOUState schema.
 */
object ObligationSchemaV1 : MappedSchema(
        schemaFamily = ObligationSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentObligation::class.java)) {
    @Entity
    @Table(name = "obligation_states")
    class PersistentObligation(
            @Column(name = "lender")
            var lenderName: String,

            @Column(name = "borrower")
            var borrowerName: String,

            @Column(name = "amount_currency")
            var amountCurrency: String,

            @Column(name = "amount")
            var amount: Long,

            @Column(name = "paid_currency")
            var paidCurrency: String,

            @Column(name = "paid")
            var paid: Long,

            @Column(name = "remark")
            var remark: String,

            @Column(name = "linear_id")
            @Type(type = "uuid-char")
            var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", "", "", 0, "", 0, "", UUID.randomUUID())
    }
}