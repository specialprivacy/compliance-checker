package com.tenforce.consent_management.compliance;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.owlapi.explanation.SatisfiabilityConverter;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import java.util.Collections;
import java.util.Set;

/**
 * Created by langens-jonathan on 3/9/18.
 */
public class ComplianceService {
    private static final Logger log = LoggerFactory.getLogger(ComplianceService.class);

    private ComplianceChecker complianceChecker = null;

    /**
     * The ComplianceService holds a ComplianceChecker object that is responsible for knowing
     * the current state of all policies. This will instantiate that compliance checker and the
     * reasoner that comes with it.
     */
    public void instantiateComplianceChecker() {
        String foldername = System.getenv("RULESDIRECTORY");
        this.complianceChecker = new ComplianceChecker(foldername);
    }

     /**
     * Returns true if the policy of the data controller is a sub set of the policy of the
     * data subject.
     *
     * @param dataSubjectId the id of the data subject's policy
     * @param dataControllerId the id of the data controller's policy
     * @return true if data controller policy C= data subject policy
     */
    public boolean hasConsent(String dataSubjectId, String dataControllerId) {

        return this.complianceChecker.isSubSetOf(this.complianceChecker.getDataControllerPolicyClassName(dataControllerId),
                this.complianceChecker.getDataSubjectPolicyClassName(dataSubjectId));
    }

    /**
     * Returns an object that represent the compliance and all of it's explanations of why a certain data controller's
     * policy is allowed by the complete set of a certain data subject's policies.
     *
     * @param dataSubjectId the id of the data subject
     * @param dataControllerId the id of the data controller
     * @return a ComplianceExplanation object that holds the compliance and it's explanations
     */
    public ComplianceExplanation getExplanationForConsent(String dataSubjectId, String dataControllerId) {
        return this.complianceChecker.getComplianceExplanation(dataSubjectId, dataControllerId);
    }
}

