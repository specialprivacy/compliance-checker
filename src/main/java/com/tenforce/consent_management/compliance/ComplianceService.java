package com.tenforce.consent_management.compliance;

import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
        this.complianceChecker = new ComplianceChecker(
                new HermiTReasonerFactory(),
                com.tenforce.consent_management.config.Configuration.getRulesDirectory());
    }

     /**
     * Returns true if the policy of the data controller is a sub set of the policy of the
     * data subject.
     *
     * @param dataSubjectId the id of the data subject's policy
     * @param dataControllerId the id of the data controller's policy
     * @return true if data controller policy C= data subject policy
      * @throws OWLOntologyCreationException if one of the ontologies cannot be instantiated
     */
    public boolean hasConsent(String dataSubjectId, String dataControllerId) throws OWLOntologyCreationException {
        return this.complianceChecker.hasConsent(dataSubjectId, dataControllerId);
    }

    /**
     * Returns true if the policy of the data controller is a sub set of the policy of the
     * data subject.
     *
     * @param dataSubjectId the id of the data subject
     * @param dataControllerId the id of the data controller
     * @param dataControllerOntologyString the string that holds the ontology which contains the data controller policy
     * @return boolean consent
     * @throws OWLOntologyCreationException if one of the ontologies, or the combination, is inconsistent
     */
    public boolean hasConsent(String dataSubjectId, String dataControllerId, String dataControllerOntologyString) throws OWLOntologyCreationException {
        InputStream stream = new ByteArrayInputStream(dataControllerOntologyString.getBytes(StandardCharsets.UTF_8));
        OWLOntology o = complianceChecker.getManager().loadOntologyFromOntologyDocument(stream);
        return this.complianceChecker.hasConsent(dataSubjectId, dataControllerId, o);
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

