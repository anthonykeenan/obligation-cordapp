package net.corda.examples.obligation

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.OpaqueBytes
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.examples.obligation.flows.IssueObligation
import net.corda.examples.obligation.flows.SettleObligation
import net.corda.finance.flows.CashIssueFlow
import org.slf4j.Logger
import java.util.*

fun main(args: Array<String>) {
    ExampleClientRPC().main(args)
}

private class ExampleClientRPC {
    companion object {
        val logger: Logger = loggerFor<ExampleClientRPC>()
        //private fun logState(state: StateAndRef<IOUState>) = logger.info("{}", state.state.data)
    }

    fun main(args: Array<String>) {
        val nodeAddress = NetworkHostAndPort.parse("localhost:10006")
        val client = CordaRPCClient(nodeAddress)

        // Can be amended in the com.example.MainKt file.
        val proxy = client.start("user1", "test").proxy

        val partyb = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse("O=PartyB,L=New York,C=US"))
        val currency = Currency.getInstance("GBP")

//        System.out.println("Starting flow")
//        val blah = proxy.startFlow(IssueObligation::Initiator,  Amount(1000.toLong() * 100, currency), partyb!!, false)
//        blah.returnValue
//
//        System.out.println("Issued Obligation")

        // 1. Prepare issue request.
//        val issueAmount = Amount(1000.toLong() * 100, currency)
//        val notary = proxy.notaryIdentities().firstOrNull() ?: throw IllegalStateException("Could not find a notary.")
//        val issueRef = OpaqueBytes.of(0)
//        val issueRequest = CashIssueFlow.IssueRequest(issueAmount, issueRef, notary)
//        val flowHandle = proxy.startFlowDynamic(CashIssueFlow::class.java, issueRequest)
//        flowHandle.returnValue.getOrThrow()
//
//        System.out.println("Issued Money")

        System.out.println("Querying outstanding obligation")
        val state = proxy.vaultQuery(Obligation::class.java).states.first()

        System.out.println("Settling")
        val blah2 = proxy.startFlow(SettleObligation::Initiator, state.state.data.linearId, Amount(1000.toLong() * 100, currency), true)
        blah2.returnValue
        System.out.println("Settled")

        // Grab all existing and future IOU states in the vault.
        //val (snapshot, updates) = proxy.vaultTrack(IOUState::class.java)

        // Log the 'placed' IOU states and listen for new ones.
//        snapshot.states.forEach { logState(it) }
//        updates.toBlocking().subscribe { update ->
//            update.produced.forEach { logState(it) }
//        }
    }
}