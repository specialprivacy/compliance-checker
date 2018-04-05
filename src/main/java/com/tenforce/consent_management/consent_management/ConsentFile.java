package com.tenforce.consent_management.consent_management;

import com.tenforce.consent_management.compliance.ComplianceChecker;
import com.tenforce.consent_management.consent.Policy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by langens-jonathan on 3/20/18.
 */
public class ConsentFile {
    private static String getFileStart() {
        return "<?xml version=\"1.0\"?>\n" +
                "<rdf:RDF xmlns=\"http://www.semanticweb.org/langens-jonathan/ontologies/2018/2/untitled-ontology-16#\"\n" +
                "     xml:base=\"http://www.semanticweb.org/langens-jonathan/ontologies/2018/2/untitled-ontology-16\"\n" +
                "     xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                "     xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
                "     xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"\n" +
                "     xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
                "     xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n" +
                "    <owl:Ontology rdf:about=\"http://www.semanticweb.org/langens-jonathan/ontologies/policies/data-subject-policies/1\">\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/recipients\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/purposes\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/duration\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/data\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/locations\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/processing\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.semanticweb.org/langens-jonathan/ontologies/data-property-ontology\"/>\n" +
                "    </owl:Ontology>\n" +
                "\n";
    }

    private static String getPolicyStart(String dataSubjectPolciyClassName) {
        return "    <!-- " + dataSubjectPolciyClassName + "-->\n" +
                "\n" +
                "    <owl:Class rdf:about=\"" + dataSubjectPolciyClassName + "\">\n" +
                "        <owl:equivalentClass>\n" +
                "            <owl:Class>";
    }

    private static String getPolicyEnd() {
        return "\n" +
                "            </owl:Class>\n" +
                "        </owl:equivalentClass>\n" +
                "        <rdfs:subClassOf rdf:resource=\"http://www.semanticweb.org/langens-jonathan/ontologies/2018/2/untitled-ontology-16#DataSubjectPolicies\"/>\n" +
                "    </owl:Class>\n" +
                "</rdf:RDF>\n";
    }

    private static String getFileEnd() {
        return "";
    }

    private static String generatePolicyFileContent(Policy policy, String dataSubjectID) {
        return ConsentFile.getFileStart() +
                ConsentFile.getPolicyStart(ComplianceChecker.getDataSubjectPolicyClassName(dataSubjectID)) +
                policy.toString() +
                ConsentFile.getPolicyEnd() +
                ConsentFile.getFileEnd();
    }

    private static String getDataSubjectPolicyFileName(String dataSubjectID) {
        return System.getenv("RULESDIRECTORY") + "/Policies/DataSubjectPolicies/" + dataSubjectID + ".owl";
    }

    public static void updatePolicyFile (Policy policy, String dataSubjectID) throws FileNotFoundException {
        String filename = ConsentFile.getDataSubjectPolicyFileName(dataSubjectID);
        File f = new File(filename);
        if(f.exists() && !f.isDirectory()) {
            f.delete();
        }
        PrintWriter out = new PrintWriter(filename);
        out.print(ConsentFile.generatePolicyFileContent(policy, dataSubjectID));
        out.flush();
        out.close();
    }
}
