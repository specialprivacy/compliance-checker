package com.tenforce.consent_management;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import special.reasoner.PolicyLogicReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MainTest {

    public static void main(String[] args) {
        OWLOntologyManager manager = owlOntologyManager;

        String useCaseFolder = "/home/jonathan/projects/special/test_use_cases/";

        HashMap<String, OWLClassExpression> policyMap = preload_application_policies(useCaseFolder);

        OWLClassExpression p = policyMap.get("C-ID_1");
        OWLClassExpression c = policyMap.get("S-F-ID_1");

        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        try {
            OWLReasoner reasoner = getReasoner("./rules");
            boolean entailed = reasoner.isEntailed(factory.getOWLSubClassOfAxiom(p, c));
            System.out.println("this policy is ok: " + entailed);
        } catch (OWLOntologyCreationException e) {
            System.out.println(e.toString());
        }
    }

    private static OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

    private static String getClassIRI(String policyId, Set<OWLClassExpression> expressions) {
        for (OWLClassExpression oce : expressions) {
            if (oce.toString().contains(policyId)) {
                return ((OWLClassImpl) oce).getIRI().toString();
            }
        }
        return null;
    }

    private static OWLClassExpression getOWLClass(String classIRI, OWLOntology ontology) {
        Set<OWLClassExpression> sc = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(classIRI)).getEquivalentClasses(ontology);
        if(sc.size() <= 0) {
            return null;
        }
        return (OWLClassExpression)sc.toArray()[0];
    }

    private static String getPolicyID(File file) {
        String filename = file.getName();
        return filename.substring(0, filename.lastIndexOf("."));
    }

    private static HashMap<String, OWLClassExpression> load_owl_files_from_folder(String folder) {
        HashMap<String, OWLClassExpression> policyMap = new HashMap<String, OWLClassExpression>();

        try (Stream<Path> walk = Files.walk(Paths.get(folder))) {

            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

            result.forEach(x -> {
                if(x.toLowerCase().endsWith((".owl"))) {
                    File owlFile = new File(x);
                    try {
                        //OWLOntology ontology = applicationLogConsumer.getComplianceChecker().loadOntology(OWLManager.createOWLOntologyManager(), x);
                        OWLOntology ontology = owlOntologyManager.loadOntology(IRI.create(new File(x)));
                        String policyId = getPolicyID(owlFile);

                        String iri = getClassIRI(policyId, ontology.getNestedClassExpressions());
                        if(iri == null) {
                            return;
                        }

                        OWLClassExpression p = getOWLClass(iri, ontology);

                        policyMap.put(policyId, p);
                    } catch(OWLOntologyCreationException e) {
//                        e.printStackTrace();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return policyMap;
    }

    private static HashMap<String, OWLClassExpression> preload_application_policies(String folder) {

        HashMap<String, OWLClassExpression> policyMap = new HashMap<String, OWLClassExpression>();
        try (Stream<Path> walk = Files.walk(Paths.get(folder))) {

            List<String> result = walk.filter(Files::isDirectory)
                    .map(x -> x.toString()).collect(Collectors.toList());

            result.forEach(x -> {
                System.out.println("in folder: " + x);
                HashMap<String, OWLClassExpression> localPolicyMap = load_owl_files_from_folder(x);
                for(String policyId : localPolicyMap.keySet()) {
                    if(policyMap.get(policyId) == null) {
                        policyMap.put(policyId, localPolicyMap.get(policyId));
                    }
                }
            });

        } catch (IOException e) {
//            e.printStackTrace();
        }

        return policyMap;
    }

    private static OWLReasoner getReasoner(String folderName) throws OWLOntologyCreationException {
        File folder = new File(folderName);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ont = manager
                    .createOntology(IRI.create("http://tenforce.com/ontology/base"), new HashSet<OWLOntology>());

        return new PolicyLogicReasonerFactory().createReasoner(ont);
    }
}
