package com.template.webserver;

import com.template.contracts.InvoiceContract;
import com.template.dto.CreateInvoiceDTO;

import com.template.dto.RequestForFundsDTO;
import com.template.flows.CreateInvoiceFlow;
import com.template.flows.RequestForFundsFlow;
import com.template.model.InvoiceProperties;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Currency;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
    }

    @PostMapping(value = "/create-invoice", consumes = "application/json", produces = "application/json")
    private ResponseEntity createInvoice(@RequestBody  CreateInvoiceDTO invoiceDTO) {

        try {
            LocalDate invoiceDate = LocalDate.parse(invoiceDTO.invoiceDate);
            LocalDate payDate = LocalDate.parse(invoiceDTO.payDate);
            Party buyer = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(invoiceDTO.buyer));
            Party myIdentity = proxy.nodeInfo().getLegalIdentities().get(0);
            System.out.println("shdosahd" + myIdentity.toString());
            Party regulator = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse("O=Regulator,L=New York,C=US"));
            InvoiceProperties invoiceProperties = new InvoiceProperties(invoiceDTO.invoiceId, invoiceDate, Long.parseLong(invoiceDTO.term), payDate, invoiceDTO.itemsList, new Amount(Long.parseLong(invoiceDTO.amount), Currency.getInstance("GBP")));
            String stx = proxy.startFlowDynamic(CreateInvoiceFlow.Initiator.class, myIdentity, buyer, invoiceProperties, regulator).getReturnValue().get();
            System.out.println("transaction id" + stx);
        }catch (Exception ex) {
            System.out.println("Caught exception" +ex);
        }
        return  ResponseEntity.ok().body("{\"result\":123}");
    }

    @PostMapping(value = "/requestFunds", consumes = "application/json", produces = "application/json")
    private ResponseEntity requestFunds(@RequestBody RequestForFundsDTO requestForFundsDTO) {
        String linearId = requestForFundsDTO.getLinearId();
        String bank = requestForFundsDTO.getBank();
        try {
            Party bankNode = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(bank));
            Party regulator = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse("O=Regulator,L=New York,C=US"));
            SignedTransaction stx = proxy.startFlowDynamic(RequestForFundsFlow.Initiator.class, UniqueIdentifier.Companion.fromString(linearId), bankNode, regulator).getReturnValue().get();
            System.out.println("transaction id" + stx.getId());
        }catch (Exception ex) {
            System.out.println("Caught exception" +ex);
        }
        return  ResponseEntity.ok().body("{\"result\":123}");
    }
}