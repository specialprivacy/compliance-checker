package com.tenforce.consent_management.compliance;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * There is need for an implementation of an OWLReasonerFactory, the default implemenation is the HermiTReasonerFactory.
 *
 * TODO we now only support the subclass axiom, this should be better abstracted to support any axiom
 */
public class ComplianceChecker {
    // default logger
    private static final Logger log = LoggerFactory.getLogger(ComplianceChecker.class);

    // the ontology manager, this is expected to keep the state of all ontologies
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    // the base ontology
    private OWLOntology baseOntology;

    // the reasoner factory
    private OWLReasonerFactory reasonerFactory;

    /**
     * default constructor
     * This takes a folder name and will load all OWL ontologies in that folder and subfolder
     * and initialize this.reasoner with that set of ontologies.
     *
     * @param folderName the folder with all ontologies that should be initially loaded
     */
    public ComplianceChecker(OWLReasonerFactory reasonerFactory, String folderName) {
        this.reasonerFactory = reasonerFactory;

        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        File folder = new File(folderName);

        try {
            // loading all owl files in that folder into a separate OWLOntology and
            // then adding that ontology to a Set
            ontologies.addAll(this.getAllOntologiesFromFolder(folder,
                    ComplianceChecker.getFolderBlackListForBaseOntology()));
            log.info("[&] compiling all ontologies from folder " + folderName + " to a common base ontology");
            this.baseOntology = this.manager.createOntology(IRI.create("http://base.ontologies.com/base"), ontologies);
        } catch (OWLOntologyCreationException ooce) {
            ooce.printStackTrace();
        }
    }

    /**
     * This is the list of sub folders in the RULES_DIRECTORY that will not be loaded when loading the
     * base ontology. Adding this will allow our file system to be consistent and have all the rules yet
     * still allow us not to load all files. This should be handy when loading policies for instance as
     * for most checks we will only need one data controller and one data subject policy to test for
     * subsumption
     *
     * @return a Set of Strings each of which represents a file that should not be loaded
     */
    private static Set<String> getFolderBlackListForBaseOntology() {
        Set<String> blackList = new HashSet<String>();
        blackList.add("Policies");
        blackList.add(".git");
        return blackList;
    }

    /**
     * Will load every .owl file in the passed folder into the current OWLOntologyManager. This will also work if
     * it gets passed an empty folder, or a folder containing no OWL files. It will however fail if no valid folder
     * is passed. This will be done recursively with respect to sub folders.
     *
     * @param folder the java.util.File object that is of type folder and holds the base set of ontologies to load
     * @param blackList a Set containing all folder names that will be skipped when loading sub folders
     * @return a Set of OWLOntologies one of each found ontology
     * @throws OWLOntologyCreationException if any of the .owl files in the passed folder(s) is not valid OWL
     */
    public Set<OWLOntology> getAllOntologiesFromFolder(File folder, Set<String> blackList) throws OWLOntologyCreationException {
        return this.getAllOntologiesFromFolder(folder, true, blackList);
    }


    /**
     * Will load every .owl file in the passed folder into the current OWLOntologyManager. This will also work if
     * it gets passed an empty folder, or a folder containing no OWL files. It will however fail if no valid folder
     * is passed. This will be done recursively with respect to sub folders.
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
     * @param loadSubFolders if this is set to true the sub folders will also be checked for .owl files
     * @param blackList a set of Strings, all sub folders with names that appear in the blacklist will not be laoded
     * @return a Set of OWLOntologies one of each found ontology
     * @throws OWLOntologyCreationException if any of the .owl files in the passed folder(s) is not valid OWL
     */
    public Set<OWLOntology> getAllOntologiesFromFolder(File folder, boolean loadSubFolders, Set<String> blackList) throws OWLOntologyCreationException {
        blackList.add("Policies");
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
                if(!blackList.contains(subFolder.getName())) {
                    ontologies.addAll(this.getAllOntologiesFromFolder(subFolder, loadSubFolders, blackList));
                }
            }
        }
        return ontologies;
    }

    /**
     * Will load every .owl file in the passed folder into the current OWLOntologyManager. This will also work if
     * it gets passed an empty folder, or a folder containing no OWL files. It will however fail if no valid folder
     * is passed.
     *
     * There is a flag that allows for the recursive loading as well.
     *
     * @param folder the java.util.File object that is of type folder and holds the base set of ontologies to load
     * @param loadSubFolders if this is set to true the sub folders will also be checked for .owl files
     * @return a Set of OWLOntologies one of each found ontology
     * @throws OWLOntologyCreationException if any of the .owl files in the passed folder(s) is not valid OWL
     */
    public Set<OWLOntology> getAllOntologiesFromFolder(File folder, boolean loadSubFolders) throws OWLOntologyCreationException {
        return this.getAllOntologiesFromFolder(folder, loadSubFolders, new HashSet<String>());
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
    public boolean isSubSetOf(String className1, String className2, OWLReasoner reasoner) {
        return reasoner.isEntailed(this.getSubSetAxiom(className1, className2));
    }

    /**
     * returns the name of the class of the data subject policy with id
     * @param id the id of the data subject policy
     * @return the classname of the data subject policy with the passed id
     */
    public static String getDataSubjectPolicyClassName(String id) {
        return com.tenforce.consent_management.config.Configuration.getPolicyClassBase() +
                "DataSubjectPolicy_" + id;
    }

    /**
     * returns the name of the class of the data controller policy with id
     * @param id the id of the data controller policy
     * @return the classname of the data controller policy with the passed id
     */
    public static String getDataControllerPolicyClassName(String id) {
        if(id.startsWith("http")) {
            return id;
        }
        return com.tenforce.consent_management.config.Configuration.getPolicyClassBase() +
                "DataControllerPolicy_" + id;
    }

    /**
     * Returns the filename of the data subject with the passed id their policy
     * @param id the id of the data subject
     * @return the filename of that data subjects policy
     */
    public static String getDataSubjectPolicyFileName(String id) {
        return com.tenforce.consent_management.config.Configuration.getRulesDirectory() +
                "/Policies/DataSubjectPolicies/" + id + ".owl";
    }

    /**
     * Returns the filename of the data controller with the pass id their policy
     * @param id the id of the data controller
     * @return the filename of that data controller's policy
     */
    public static String getDataControllerPolicyFileName(String id) {
        return com.tenforce.consent_management.config.Configuration.getRulesDirectory() +
                "/Policies/DataControllerPolicies/" + id + ".owl";
    }

    /**
     * Returns true if the policy of the data controller is a sub set of the policy of the
     * data subject.
     *
     * @param dataSubjectId the id of the data subject's policy
     * @param dataControllerId the id of the data controller's policy
     * @return true if data controller policy C= data subject policy
     * @throws OWLOntologyCreationException if one of the ontologies involved could not be created
     */
    public boolean hasConsent(String dataSubjectId, String dataControllerId) throws OWLOntologyCreationException {
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        ontologies.add(this.loadOntology(getDataSubjectPolicyFileName(dataSubjectId)));
        ontologies.add(this.loadOntology(getDataControllerPolicyFileName(dataControllerId)));
        return this.hasConsent(dataSubjectId, dataControllerId, ontologies);
    }

    /**
     * Returns true if the class wit the id dataControllerId is a subset of the class with id
     * dataSubjectId. This will be tested against an ontology loaded by combining the set of
     * passed ontologies with the base ontology.
     *
     * @param dataSubjectId the id of the data subject
     * @param dataControllerId the id of the data controller
     * @param ontologies the set of ontologies which is supposed to hold all info wrt to the classes of the data
     *                   controller and data subject's policies
     * @return boolean consent
     * @throws OWLOntologyCreationException if either the data subject's policy is not loadable from the file or the
     *                    combination of the ontologies would result in an contradiction
     */
    public boolean hasConsent(String dataSubjectId, String dataControllerId, Set<OWLOntology> ontologies) throws OWLOntologyCreationException{
        OWLOntology ontology = this.getOntology(ontologies);
        OWLReasoner reasoner = this.reasonerFactory.instantiateReasoner(ontology);

        String dataControllerPolicyClass = ComplianceChecker.getDataControllerPolicyClassName(dataControllerId);
        String dataSubjectPolicyClass =ComplianceChecker.getDataSubjectPolicyClassName(dataSubjectId);
        return this.isSubSetOf(dataControllerPolicyClass, dataSubjectPolicyClass, reasoner);
    }

    /**
     * Returns true if the passed ontology would get consent from the data subject. The other methods expect
     * the policies to be found in the respective [RULES_DIRECTORY]/Policies folders but sometimes it is
     * convenient to be able to test an ontology that has not been fully materialised. This function supports
     * the consent validation functionality without the explicit need to follow the assumptions of the rest
     * of the ComplianceChecker class. There is still the need for the data controller's id as the naming
     * assumption is not cleared by this method. In other words the data controller's policy name should in
     * the passed ontology be consistent with the expectation of the ComplianceChecker. The ComplianceChecker
     * offers a static method for obtaining the data controller's name based in it's id. This method can be
     * invoked in order to obtain that name.
     *
     * @param dataSubjectId the id of the data subject
     * @param dataControllerId the id of the data controller
     * @param dataControllerOntology the ontology that should hold the data controller's policy
     * @return boolean consent
     * @throws OWLOntologyCreationException if either the data subject's policy file is inconsistent or
     *         the combination of the ontologies passed would result in an inconsitent ontology.
     */
    public boolean hasConsent(String dataSubjectId, String dataControllerId, OWLOntology dataControllerOntology) throws OWLOntologyCreationException {
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        ontologies.add(dataControllerOntology);
        ontologies.add(this.loadOntology(getDataSubjectPolicyFileName(dataSubjectId)));
        return this.hasConsent(dataSubjectId, dataControllerId, ontologies);
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
    /**
     * TODO Fix this method, I will remove all HermiT references from this file, hence I cannot
     *      rely on the HermiT method getDataFactory.
     */
    public ComplianceExplanation getComplianceExplanation(String dataSubjectId, String dataControllerId) {
//        OWLAxiom axiom =  getSubSetAxiom(getDataControllerPolicyClassName(dataControllerId),
//                getDataSubjectPolicyClassName(dataSubjectId));
//
//        Reasoner.ReasonerFactory factory =  new Reasoner.ReasonerFactory();
//        SatisfiabilityConverter converter = new SatisfiabilityConverter(reasoner.getDataFactory());
//        OWLClassExpression unsatClass = converter.convert( axiom );
//
//        // We don't want HermiT to thrown an exception for inconsistent ontologies because then we
//        // can't explain the inconsistency. This can be controlled via a configuration setting.
//        Configuration configuration=new Configuration();
//        configuration.throwInconsistentOntologyException=false;
//        // The factory can now be used to obtain an instance of HermiT as an OWLReasoner.
//        OWLReasoner reasoner=factory.createReasoner(this.reasoner.getRootOntology(), configuration);
//        // Now we instantiate the explanation classes
//        BlackBoxExplanation exp=new BlackBoxExplanation(this.reasoner.getRootOntology(), factory, reasoner);
//        HSTExplanationGenerator multExplanator=new HSTExplanationGenerator(exp);
//        // Now we can get explanations for the unsatisfiability.
//        Set<Set<OWLAxiom>> explanations=multExplanator.getExplanations(unsatClass);
//
//        return new ComplianceExplanation(explanations.size() > 0, explanations);
        return null;
    }

    /**
     * Returns the reasoner that has been initialized with the current base ontology
     * and the passed Set of OWLOntologies
     *
     * @param ontologies the set of ontologies that this reasoner needs to be initialized with
     * @return this.reasoner
     */
    public OWLReasoner getReasoner(Set<OWLOntology> ontologies) throws OWLOntologyCreationException {
        return this.reasonerFactory.instantiateReasoner(this.getOntology(ontologies));
    }

    /**
     * Creates a new ontology which is built of the passed set of ontologies and the base ontology
     * @param ontologies a set of ontologies that will be combined together and with the base ontology
     * @return an OWLOntology that is composed of the base ontology and the passed set of ontologies
     * @throws OWLOntologyCreationException
     */
    public OWLOntology getOntology(Set<OWLOntology> ontologies) throws OWLOntologyCreationException {
        // first we create a new set of ontologies extended with the base set
        Set<OWLOntology> extendedOntologies = new HashSet<OWLOntology>(ontologies);
        extendedOntologies.add(this.baseOntology);

        // then we create the ontology the reasoner will use
        return this.manager.createOntology(IRI.create("http://base.ontologies.com/instance"), extendedOntologies);
    }

    /**
     * @return this.manager
     */
    public OWLOntologyManager getManager() {
        return manager;
    }
}
