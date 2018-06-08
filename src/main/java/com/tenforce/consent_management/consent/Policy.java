package com.tenforce.consent_management.consent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

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
    List<SimplePolicy> simplePolicies = new ArrayList<SimplePolicy>();

    public List<SimplePolicy> getSimplePolicies() {
        return simplePolicies;
    }

    public void setSimplePolicies(List<SimplePolicy> simplePolicies) {
        this.simplePolicies = simplePolicies;
    }

    public String toString() {
        String toreturn = "\n                <owl:unionOf rdf:parseType=\"Collection\">\n";
        for(SimplePolicy policy : this.simplePolicies) {
            toreturn += policy.toString();
        }
        toreturn += "                </owl:unionOf>\n";
        return toreturn;
    }
}
