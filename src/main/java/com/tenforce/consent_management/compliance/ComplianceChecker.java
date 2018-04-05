package com.tenforce.consent_management.compliance;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.owlapi.explanation.SatisfiabilityConverter;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import java.io.File;
import java.util.Collections;
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
 * TODO at the moment the reasoner is dependent on the HermiT reasoner, that should be properly abstracted
 * TODO we now only support the subclass axiom, this should be better abstracted to support any axiom
 */
public class ComplianceChecker {
    // default logger
    private static final Logger log = LoggerFactory.getLogger(ComplianceChecker.class);

    // the ontology manager, this is expected to keep the state of all ontologies
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    // the reasoner that will be initialized with all ontologies that the ontology manager retains
    private Reasoner reasoner;

    /**
     * default constructor
     * This takes a folder name and will load all OWL ontologies in that folder and subfolder
     * and initialize this.reasoner with that set of ontologies.
     *
     * @param folderName the folder with all ontologies that should be initially loaded
     */
    public ComplianceChecker(String folderName) {
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        File folder = new File(folderName);

        try {
            // loading all owl files in that folder into a separate OWLOntology and
            // then adding that ontology to a Set
            ontologies.addAll(this.getAllOntologiesFromFolder(folder));
            log.info("[&] compiling all ontologies from folder " + folderName + " to a common base ontology");
            OWLOntology baseOntology = this.manager.createOntology(IRI.create("http://base.ontologies.com/base"), ontologies);

            // this is just to verify that our base ontology is consistent
            this.reasoner = new Reasoner(baseOntology);
            log.info("[&] this base ontology is consistent: " + this.reasoner.isConsistent());
        } catch (OWLOntologyCreationException ooce) {
            ooce.printStackTrace();
        }
    }

    /**
     * Will load every .owl file in the passed folder into the current OWLOntologyManager. This will also work if
     * it gets passed an empty folder, or a folder containing no OWL files. It will however fail if no valid folder
     * is passed. This will be done recursively with respect to subfolders.
     *
     * @param folder the java.util.File object that is of type folder and holds the base set of ontologies to load
     * @return a Set of OWLOntologies one of each found ontology
     * @throws OWLOntologyCreationException if any of the .owl files in the passed folder(s) is not valid OWL
     */

    public Set<OWLOntology> getAllOntologiesFromFolder(File folder) throws OWLOntologyCreationException {
        return this.getAllOntologiesFromFolder(folder, true);
    }

    /**
     * Will load every .owl file in the passed folder into the current OWLOntologyManager. This will also work if
     * it gets passed an empty folder, or a folder containing no OWL files. It will however fail if no valid folder
     * is passed.
     *
     * There is a flag that allows for the recursive loading as well.
     *
     * @param folder the java.util.File object that is of type folder and holds the base set of ontologies to load
     * @param loadSubFolders if this is set to true the subfolders will also be checked for .owl files
     * @return a Set of OWLOntologies one of each found ontology
     * @throws OWLOntologyCreationException if any of the .owl files in the passed folder(s) is not valid OWL
     */
    public Set<OWLOntology> getAllOntologiesFromFolder(File folder, boolean loadSubFolders) throws OWLOntologyCreationException {
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        Set<File> subFolders = new HashSet<File>();
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".owl")) {
                ontologies.add(this.loadOntology(file.getAbsolutePath()));
            } else if(file.isDirectory()){
                subFolders.add(file);
            }
        }
        if(loadSubFolders) {
            for (File subFolder : subFolders) {
                ontologies.addAll(this.getAllOntologiesFromFolder(subFolder, loadSubFolders));
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
    private OWLOntology loadOntology(String filename) throws OWLOntologyCreationException {
        log.info("[*] loading ontology file... " + filename);
        return this.manager.loadOntology(IRI.create(new File(filename)));
    }

    /**
     * get the class that is being associated with the passed classname
     * @param className the name of the class
     * @return
     */
    public OWLClass getOWLClass(String className) {
        OWLOntologyManager manager = this.getManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        return dataFactory.getOWLClass(IRI.create(className));
    }

    /**
     * Returns an OWLAxiom that states that the OWLClass with class name 1 is a subset of the OWLClass with
     * class name 2
     *
     * @param className1 the name of the class that is suspected of being the subset
     * @param className2 the name of the class that is suspected of being the superset
     * @return an OWLAxiom that states that class1 C= class2
     */
    public OWLAxiom getSubSetAxiom(String className1, String className2) {
        OWLClass class1 = getOWLClass(className1);
        OWLClass class2 = getOWLClass(className2);

        return new OWLSubClassOfAxiomImpl(class1, class2, Collections.<OWLAnnotation>emptyList());
    }

    /**
     * returns true if class1 C= class2 or in other words if
     * "the class with classname1" is a subset of "the class with classname2"
     * @param className1 the name of the class that is the subset
     * @param className2 the name of the class that is the superset
     * @return class1 C= class2
     */
    public boolean isSubSetOf(String className1, String className2) {
        return this.reasoner.isEntailed(this.getSubSetAxiom(className1, className2));
    }

    /**
     * returns the name of the class of the data subject policy with id
     * @param id the id of the data subject policy
     * @return the classname of the data subject policy with the passed id
     */
    public static String getDataSubjectPolicyClassName(String id) {
        return System.getenv("POLICYCLASSBASE") + "DataSubjectPolicy_" + id;
    }

    /**
     * returns the name of the class of the data controller policy with id
     * @param id the id of the data controller policy
     * @return the classname of the data controller policy with the passed id
     */
    public static String getDataControllerPolicyClassName(String id) {
        return System.getenv("POLICYCLASSBASE") + "DataControllerPolicy_" + id;
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
        String dataControllerConsentClass = ComplianceChecker.getDataControllerPolicyClassName(dataControllerId);
        String dataSubjectConsentClass =ComplianceChecker.getDataSubjectPolicyClassName(dataSubjectId);
        return this.isSubSetOf(dataControllerConsentClass, dataSubjectConsentClass);
    }

    /**
     * Returns all sets of axioms that grant compliance between a datasubject and a datacontroller. The idea
     * is again that compliance of a data controller's policy with the set of policies that belong to a single
     * data subject means that the data controller's policy is a subset of the union of policies belonging to
     * that data subject
     *
     * @param dataSubjectId the id of the data subject
     * @param dataControllerId the id of the data controller's policy
     * @return an object representing the compliance explanation
     */
    public ComplianceExplanation getComplianceExplanation(String dataSubjectId, String dataControllerId) {
        OWLAxiom axiom =  getSubSetAxiom(getDataControllerPolicyClassName(dataControllerId),
                getDataSubjectPolicyClassName(dataSubjectId));

        Reasoner.ReasonerFactory factory =  new Reasoner.ReasonerFactory();
        SatisfiabilityConverter converter = new SatisfiabilityConverter(reasoner.getDataFactory());
        OWLClassExpression unsatClass = converter.convert( axiom );

        // We don't want HermiT to thrown an exception for inconsistent ontologies because then we
        // can't explain the inconsistency. This can be controlled via a configuration setting.
        Configuration configuration=new Configuration();
        configuration.throwInconsistentOntologyException=false;
        // The factory can now be used to obtain an instance of HermiT as an OWLReasoner.
        OWLReasoner reasoner=factory.createReasoner(this.reasoner.getRootOntology(), configuration);
        // Now we instantiate the explanation classes
        BlackBoxExplanation exp=new BlackBoxExplanation(this.reasoner.getRootOntology(), factory, reasoner);
        HSTExplanationGenerator multExplanator=new HSTExplanationGenerator(exp);
        // Now we can get explanations for the unsatisfiability.
        Set<Set<OWLAxiom>> explanations=multExplanator.getExplanations(unsatClass);

        return new ComplianceExplanation(explanations.size() > 0, explanations);
    }

    /**
     * Returns the reasoner that has been initialized with the current ontology.
     * Today this is a HermiT reasoner.
     *
     * @return this.reasoner
     */
    public Reasoner getReasoner() {
        return reasoner;
    }

    /**
     * @return this.manager
     */
    public OWLOntologyManager getManager() {
        return manager;
    }
}
