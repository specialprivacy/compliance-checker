package com.tenforce.consent_management.compliance;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Compliance checker
 *
 * This class holds an OWLOntologyManager and a Reasoner to retain state and offers some methods
 * to manipulate/abstragate them.
 *
 * It also offers an easy method to check if a class is a subclass of another. This will be done by creating an
 * OWLAxiom and then checking the validity of that axiom.
 *
 * There is need for an implementation of an OWLReasonerFactory, the default implemenation is the HermiTReasonerFactory.
 *
 * TODO we now only support the subclass axiom, this should be better abstracted to support any axiom
 */
public class ComplianceChecker {
    // default logger
    private static final Logger log = LoggerFactory.getLogger(ComplianceChecker.class);

    // the ontology manager, this is expected to keep the state of all ontologies
    private OWLDataFactory factory = OWLManager.getOWLDataFactory();
    private OWLReasoner reasoner;

    /**
     * default constructor
     * This takes a folder name and will load all OWL ontologies in that folder and subfolder
     * and initialize this.reasoner with that set of ontologies.
     *
     * @param folderName the folder with all ontologies that should be initially loaded
     */
    public ComplianceChecker(@NotNull OWLReasonerFactory reasonerFactory, @NotNull String folderName)
        throws OWLOntologyCreationException
    {
        File folder = new File(folderName);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        // loading all owl files in that folder into a separate OWLOntology and
        // then adding that ontology to a Set
        Set<OWLOntology> ontologies = this.getAllOntologiesFromFolder(
                manager,
                folder,
                true,
                new HashSet<>(Arrays.asList("Policies", ".git"))
        );
        OWLOntology ont = manager
                .createOntology(IRI.create("http://tenforce.com/ontology/base"), ontologies);
        reasoner = reasonerFactory.instantiateReasoner(ont);
    }

    /**
     * Will load every .owl file in the passed folder into the current OWLOntologyManager. This will also work if
     * it gets passed an empty folder, or a folder containing no OWL files. It will however fail if no valid folder
     * is passed.
     * <p>
     * There is a flag that allows for the recursive loading as well.
     *
     * @param folder         the java.util.File object that is of type folder and holds the base set of ontologies to load
     * @param loadSubFolders if this is set to true the sub folders will also be checked for .owl files
     * @param blackList      a set of Strings, all sub folders with names that appear in the blacklist will not be laoded
     * @return a Set of OWLOntologies one of each found ontology
     * @throws OWLOntologyCreationException if any of the .owl files in the passed folder(s) is not valid OWL
     */
    public Set<OWLOntology> getAllOntologiesFromFolder(OWLOntologyManager manager, File folder, boolean loadSubFolders, Set<String> blackList) throws OWLOntologyCreationException {
        blackList.add("Policies");
        Set<OWLOntology> ontologies = new HashSet<>();
        Set<File> subFolders = new HashSet<>();

        File[] files = folder.listFiles();
        if (files == null) return ontologies;
        Arrays.sort(files); // files are not necessarily in lexographical order

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".owl")) {
                ontologies.add(this.loadOntology(manager, file.getAbsolutePath()));
            } else if (file.isDirectory()) {
                subFolders.add(file);
            }
        }
        if (loadSubFolders) {
            for (File subFolder : subFolders) {
                if (!blackList.contains(subFolder.getName())) {
                    ontologies.addAll(this.getAllOntologiesFromFolder(manager, subFolder, true, blackList));
                }
            }
        }
        return ontologies;
    }

    /**
     * Loads an ontology in the current OWLOntolgoyManager. That ontology should be in the
     * file with the passed filename. We expect the full path here. IF that file does not
     * exist or it does not contain valid OWL then an OWLOntologyCreationException will be
     * thrown.
     *
     * @param filename the full pathname for the ontology file
     * @return void -- this function returns nothing
     * @throws OWLOntologyCreationException if the file does not contain a valid OWL ontology
     */
    private OWLOntology loadOntology(OWLOntologyManager manager, String filename) throws OWLOntologyCreationException {
        log.info("[*] loading ontology file... " + filename);
        return manager.loadOntology(IRI.create(new File(filename)));
    }

    public boolean hasConsent(OWLClassExpression logClass, OWLClassExpression policyClass) {
        return reasoner.isEntailed(factory.getOWLSubClassOfAxiom(logClass, policyClass));
    }
}
