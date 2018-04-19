package com.tenforce.consent_management.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.consent_management.compliance.ComplianceExplanation;
import com.tenforce.consent_management.compliance.ComplianceService;
import com.tenforce.consent_management.config.Configuration;
import com.tenforce.consent_management.consent.*;
import com.tenforce.consent_management.consent_management.ConsentFile;
import com.tenforce.consent_management.kafka.PolicyConsumer;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
public class RootController {
  // default logger
  private static final Logger log = LoggerFactory.getLogger(RootController.class);

  private ComplianceService complianceService = new ComplianceService();

  private PolicyConsumer policyConsumer = null;

  @PostConstruct
  public void startKafkaConsumer() {
    // start the kafka policy consumer
//    policyConsumer = new PolicyConsumer(Configuration.getKafkaTopicPolicy());
//    policyConsumer.start();
  }

  /**
   * HTTP endpoint that offers the possibility to check if a data controller policy has the compliance of a
   * certain data subject. We do this by checking that the data controller's policy is a subset of the data
   * subject's policy.
   *
   * @example curl http://localhost:8080/consent/4/1 -> HTTP 1.0 200 OK iff controller_policy(1) is a subset of
   *                                                    data_subject_policy(4)
   * @example curl http://localhost:8080/consent/2/1 -> HTTP 1.0 403 FORBIDDEN iff controller_policy(1) is not a subset
   *                                                    of data_subject_policy(2)
   * @param dataSubjectId
   * @param dataControllerId
   * @return HTTP.OK if policy X has the compliance of subject Y
   * @return HTTP.FORBIDDEN if policy X does not have the compliance of subject Y
   */
  // compliance service is supposed to be an abstraction between this controller and the actual compliance checker object
  @RequestMapping(path = "/consent/{dataSubjectId}/{dataControllerId}", method=RequestMethod.GET)
  public ResponseEntity<String> getConsent(@PathVariable(value="dataSubjectId") String dataSubjectId,
                                           @PathVariable(value="dataControllerId") String dataControllerId){
    // TODO every time a request comes in I re-initialize the compliance checker completely
    // because I don't yet offer methods to change compliance. This shoudl still be done at some point.
    this.complianceService.instantiateComplianceChecker();

    // checks if there is a consent
    try {
      boolean consent = this.complianceService.hasConsent(dataSubjectId, dataControllerId);

      if (consent) {
        log.info("[*] " + dataControllerId + " accessed " + dataSubjectId + "'s data legitimately.");
        return new ResponseEntity<String>("", HttpStatus.OK);
      } else {
        log.info("[!] " + dataControllerId + " accessed " + dataSubjectId + "'s data without permission!");
        return new ResponseEntity<String>("", HttpStatus.FORBIDDEN);
      }
    } catch (OWLOntologyCreationException e) {
      e.printStackTrace();
      return new ResponseEntity<String>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * HTTP endpoint that offers the possibility to write a data subjects policy to the reasoner's database.
   * This is done by saving it on disk at the moment. The Policy itself is expected as JSON in the body of
   * the POST call.
   *
   * @example body:
   * {
   * "simplePolicies":[
   *  {
   *  "data":"http://www.specialprivacy.eu/vocabs/data#Anonymized",
   *  "processing":"http://www.specialprivacy.eu/langs/usage-policy#AnyProcessing",
   *  "purpose":"http://www.specialprivacy.eu/langs/usage-policy#AnyPurpose",
   *  "recipient":"http://www.specialprivacy.eu/langs/usage-policy#AnyRecipient",
   *  "storage":"http://www.specialprivacy.eu/langs/usage-policy#AnyDuration"
   *  },{
   *  "data":"http://www.specialprivacy.eu/vocabs/data#AnyData",
   *  "processing":"http://www.specialprivacy.eu/langs/usage-policy#AnyProcessing",
   *  "purpose":"http://www.specialprivacy.eu/langs/usage-policy#Charity",
   *  "recipient":"http://www.specialprivacy.eu/langs/usage-policy#AnyRecipient",
   *  "storage":"http://www.specialprivacy.eu/langs/usage-policy#AnyDuration"
   *  }
   * ]
   * }
   *
   * @example curl -X POST http://localhost:8080/consent/4 -> HTTP 1.0 200 OK iff controller_policy(4) was written
   *                                                          to the complaince checker database
   * @example curl -X POST http://localhost:8080/consent/4 -> HTTP 1.0 500 INTERNAL SERVER ERROR iff the we were unable
   *                                                          to write the policy to disk (for any reason).
   * @param dataSubjectId
   * @return HTTP.OK the policy for data subject x has been written to disk
   * @return HTTP.INTERNAL_SERVER_ERROR the policy was not saved to disk for any reason
     */
  @RequestMapping(path = "/consent/{dataSubjectId}", method=RequestMethod.POST)
  public ResponseEntity<String> setConsent(@PathVariable(value="dataSubjectId") String dataSubjectId,  @RequestBody String policy){
    ObjectMapper mapper = new ObjectMapper();
    try {
      Policy postedPolicy = mapper.readValue(policy, Policy.class);
      ConsentFile.updatePolicyFile(postedPolicy, dataSubjectId);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return new ResponseEntity<String>("Error: Unable to write policy to disk.", HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity<String>("Error: Unable to instantiate policy, please check the body that was sent: " + policy, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<String>("Policy for data subject " + dataSubjectId + " created", HttpStatus.OK);
  }

  /**
   * HTTP endpoint that offers the possibility to request an explanation of why a data controller policy has the
   * compliance of a certain data subject.
   *
   * @example curl http://localhost:8080/consent/4/1 -> HTTP 1.0 200 OK
   *                                                    BODY: JSON object containing all (if any) consent chains
   * @param dataSubjectId
   * @param dataControllerId
   * @return HTTP.OK consent chains in HTTP Body
   */
  @RequestMapping("/explain/{dataSubjectId}/{dataControllerId}")
  public ResponseEntity<String> getExplanations(@PathVariable(value="dataSubjectId") String dataSubjectId,
                                              @PathVariable(value="dataControllerId") String dataControllerId){
    // TODO every time a request comes in I re-initialize the compliance checker completely
    // because I don't yet offer methods to change compliance. This shoudl still be done at some point.
    this.complianceService.instantiateComplianceChecker();

    ObjectMapper mapper = new ObjectMapper();

    // checks if there is a consent
    ComplianceExplanation explanation= this.complianceService.getExplanationForConsent(dataSubjectId, dataControllerId);

    try {
      return new ResponseEntity<String>(mapper.writeValueAsString(explanation), HttpStatus.OK);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return new ResponseEntity<String>("creating JSON object failed", HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }
}