package com.tenforce.consent_management;

import com.tenforce.consent_management.compliance.ComplianceChecker;
import com.tenforce.consent_management.config.Configuration;
import com.tenforce.consent_management.kafka.ApplicationLogConsumer;
import com.tenforce.consent_management.kafka.PolicyConsumer;
import org.rocksdb.RocksDBException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    // default logger
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static PolicyConsumer policyConsumer = null;
    private static ApplicationLogConsumer applicationLogConsumer = null;
    private static OWLOntologyManager owlOntologyManager = null;

    private static String getClassIRI(String policyId, Set<OWLClassExpression> expressions) {
        for (OWLClassExpression oce : expressions) {
            if (oce.toString().contains(policyId)) {
                return ((OWLClassImpl) oce).getIRI().toString();
            }
        }
        return null;
    }

    private static OWLClassExpression getOWLClass(String classIRI, OWLOntology ontology) {
//        Set<OWLClassExpression> sc = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(classIRI)).getEquivalentClasses(ontology);
//
//        if(sc.size() <= 0) {
//            return null;
//        }
//        return (OWLClassExpression)sc.toArray()[0];
        return null;
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
                        OWLOntology ontology = Main.applicationLogConsumer.getComplianceChecker().loadOntology(OWLManager.createOWLOntologyManager(), x);

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

    private static void preload_application_policies(String folder) {

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
            e.printStackTrace();
        }

        System.out.println("[*] Storing " + policyMap.size() + " items...");

        for(String policyId : policyMap.keySet()) {
            try {
                OWLClassExpression owlClassExpression = policyMap.get(policyId);
                System.out.println("[0] Storing " + policyId + " +> " + owlClassExpression.toString());
                policyConsumer.getPolicyStore().updatePolicy(policyId, owlClassExpression);
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        log.info("Compliance checker starting up");
        try {
            // Eagerly load and validate the config. We want to show errors when the operator is still looking
            Configuration config = Configuration.loadFromEnvironment();
            log.info("Using configuration: {}", config);

            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(config.getLoggingLevel());

            Main.owlOntologyManager = OWLManager.createOWLOntologyManager();

            policyConsumer = new PolicyConsumer(config);
            applicationLogConsumer = new ApplicationLogConsumer(config);

            log.info("Starting to preload application policies");
            preload_application_policies("/policies/compliance");

            final ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(policyConsumer);
            executor.submit(applicationLogConsumer);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Received request to stop. Gracefully terminating all kafka clients");
                applicationLogConsumer.shutdown();
                policyConsumer.shutdown();
                executor.shutdown();
                try {
                    if (executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                        log.info("Done stopping kafka clients.");
                    } else {
                        log.info("Done stopping kafka clients. Some did not shut down gracefully");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            Runtime.getRuntime().exit(1);
        } catch (Exception e) {
            log.error("Failed to initialize services");
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }
    }
}
