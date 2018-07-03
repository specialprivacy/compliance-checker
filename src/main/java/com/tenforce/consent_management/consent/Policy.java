package com.tenforce.consent_management.consent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by langens-jonathan on 3/28/18.
 *
 * This class is intended to capture all information that we know about a complex policy.
 * It is the class that is used to transform incoming JSON strings on the kafka to in
 * memory objects.
 *
 * Further the class also has support for turning itself into an OWL string
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Policy {
    private long timestamp;
    private String userID;
    private List<SimplePolicy> simplePolicies = new ArrayList<SimplePolicy>();

    private static final OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public List<SimplePolicy> getSimplePolicies() {
        return simplePolicies;
    }
    public void setSimplePolicies(List<SimplePolicy> simplePolicies) {
        this.simplePolicies = simplePolicies;
    }

    public OWLClassExpression toOWL() {
        Set<OWLClassExpression> expressions = this.simplePolicies.stream()
               .map(SimplePolicy::toOWL)
               .collect(Collectors.toSet());
        return dataFactory.getOWLObjectUnionOf(expressions);
    }
}
