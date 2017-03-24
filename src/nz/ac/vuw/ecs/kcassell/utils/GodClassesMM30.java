package nz.ac.vuw.ecs.kcassell.utils;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.TypeMetrics;
import net.sourceforge.metrics.persistence.Database;
import net.sourceforge.metrics.persistence.IDatabaseConstants;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

public class GodClassesMM30 implements IDatabaseConstants, Constants {

	private static UtilLogger utilLogger = new UtilLogger(
			"MetricDatabaseLocator");

	private static final String BRULE_ENGINE = "=Jena/<com.hp.hpl.jena.reasoner.rulesys.impl.oldCode{BRuleEngine.java[BRuleEngine";
	private static final String BV_DECOMPOSE = "=wekaSVNTrunk/src\\/main\\/java<weka.classifiers{BVDecompose.java[BVDecompose";
	private static final String BV_DECOMPOSE_SEG_CV_SUB = "=wekaSVNTrunk/src\\/main\\/java<weka.classifiers{BVDecomposeSegCVSub.java[BVDecomposeSegCVSub";
	private static final String CANDIDATE_URI = "=Heritrix/<org.archive.crawler.datamodel{CandidateURI.java[CandidateURI";
	private static final String COMMAND_LINE = "=Jena/<jena.cmdline{CommandLine.java[CommandLine";
	private static final String CRAWL_CONTROLLER = "=Heritrix/<org.archive.crawler.framework{CrawlController.java[CrawlController";
	private static final String DATABASE_UTILS = "=wekaSVNTrunk/src\\/main\\/java<weka.experiment{DatabaseUtils.java[DatabaseUtils";
	private static final String EXPERIMENT = "=wekaSVNTrunk/src\\/main\\/java<weka.experiment{Experiment.java[Experiment";
	private static final String FREECOL_CLIENT = "=FreecolSVNTrunk/src<net.sf.freecol.client{FreeColClient.java[FreeColClient";
	private static final String FREECOL_OBJECT = "=FreecolSVNTrunk/src<net.sf.freecol.common.model{FreeColObject.java[FreeColObject";
	private static final String FREECOL_SERVER = "=FreecolSVNTrunk/src<net.sf.freecol.server{FreeColServer.java[FreeColServer";
	private static final String HERITRIX = "=Heritrix/<org.archive.crawler{Heritrix.java[Heritrix";
	private static final String IMAGE_LIBRARY = "=FreecolSVNTrunk/src<net.sf.freecol.client.gui{ImageLibrary.java[ImageLibrary";
	private static final String LPB_RULE_ENGINE = "=Jena/<com.hp.hpl.jena.reasoner.rulesys.impl{LPBRuleEngine.java[LPBRuleEngine";
	private static final String LP_INTERPRETER = "=Jena/<com.hp.hpl.jena.reasoner.rulesys.impl{LPInterpreter.java[LPInterpreter";
	private static final String N3_JENA_WRITER_COMMON = "=Jena/<com.hp.hpl.jena.n3{N3JenaWriterCommon.java[N3JenaWriterCommon";
	private static final String NEAREST_NEIGHBOR_SEARCH = "=wekaSVNTrunk/src\\/main\\/java<weka.core.neighboursearch{NearestNeighbourSearch.java[NearestNeighbourSearch";
	private static final String NODE_JENA = "=Jena/<com.hp.hpl.jena.graph{Node.java[Node";
	private static final String NODE_WEKA = "=wekaSVNTrunk/src\\/main\\/java<weka.gui.treevisualizer{Node.java[Node";
	private static final String PARSER_BASE = "=Jena/<com.hp.hpl.jena.n3.turtle{ParserBase.java[ParserBase";
	private static final String REG_OPTIMIZER = "=wekaSVNTrunk/src\\/main\\/java<weka.classifiers.functions.supportVector{RegOptimizer.java[RegOptimizer";
	private static final String RESULT_MATRIX = "=wekaSVNTrunk/src\\/main\\/java<weka.experiment{ResultMatrix.java[ResultMatrix";
	private static final String RULE_JENA = "=Jena/<com.hp.hpl.jena.reasoner.rulesys{Rule.java[Rule";
	private static final String RULE_WEKA = "=wekaSVNTrunk/src\\/main\\/java<weka.classifiers.trees.m5{Rule.java[Rule";
	private static final String SCRIPT = "=wekaSVNTrunk/src\\/main\\/java<weka.gui.scripting{Script.java[Script";
	private static final String SETTINGS_HANDLER = "=Heritrix/<org.archive.crawler.settings{SettingsHandler.java[SettingsHandler";
	private static final String SPECIFICATION = "=FreecolSVNTrunk/src<net.sf.freecol.common.model{Specification.java[Specification";
	private static final String TEST_INSTANCES = "=wekaSVNTrunk/src\\/main\\/java<weka.core{TestInstances.java[TestInstances";
	private static final String WORK_QUEUE = "=Heritrix/<org.archive.crawler.frontier{WorkQueue.java[WorkQueue";
	private static final String XML_DOCUMENT = "=wekaSVNTrunk/src\\/main\\/java<weka.core.xml{XMLDocument.java[XMLDocument";

	private static final String BRULE_ENGINE_BETW = "=JenaRBetw/<com.hp.hpl.jena.reasoner.rulesys.impl.oldCode{BRuleEngine.java[BRuleEngine";
	private static final String BV_DECOMPOSE_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.classifiers{BVDecompose.java[BVDecompose";
	private static final String BV_DECOMPOSE_SEG_CV_SUB_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.classifiers{BVDecomposeSegCVSub.java[BVDecomposeSegCVSub";
	private static final String CANDIDATE_URI_BETW = "=HeritrixRBetw/<org.archive.crawler.datamodel{CandidateURI.java[CandidateURI";
	private static final String COMMAND_LINE_BETW = "=JenaRBetw/<jena.cmdline{CommandLine.java[CommandLine";
	private static final String CRAWL_CONTROLLER_BETW = "=HeritrixRBetw/<org.archive.crawler.framework{CrawlController.java[CrawlController";
	private static final String DATABASE_UTILS_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.experiment{DatabaseUtils.java[DatabaseUtils";
	private static final String EXPERIMENT_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.experiment{Experiment.java[Experiment";
	private static final String FREECOL_CLIENT_BETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.client{FreeColClient.java[FreeColClient";
	private static final String FREECOL_OBJECT_BETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.common.model{FreeColObject.java[FreeColObject";
	private static final String FREECOL_SERVER_BETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.server{FreeColServer.java[FreeColServer";
	private static final String HERITRIX_BETW = "=HeritrixRBetw/<org.archive.crawler{Heritrix.java[Heritrix";
	private static final String IMAGE_LIBRARY_BETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.client.gui{ImageLibrary.java[ImageLibrary";
	private static final String LPB_RULE_ENGINE_BETW = "=JenaRBetw/<com.hp.hpl.jena.reasoner.rulesys.impl{LPBRuleEngine.java[LPBRuleEngine";
	private static final String LP_INTERPRETER_BETW = "=JenaRBetw/<com.hp.hpl.jena.reasoner.rulesys.impl{LPInterpreter.java[LPInterpreter";
	private static final String N3_JENA_WRITER_COMMON_BETW = "=JenaRBetw/<com.hp.hpl.jena.n3{N3JenaWriterCommon.java[N3JenaWriterCommon";
	private static final String NEAREST_NEIGHBOR_SEARCH_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.core.neighboursearch{NearestNeighbourSearch.java[NearestNeighbourSearch";
	private static final String NODE_JENA_BETW = "=JenaRBetw/<com.hp.hpl.jena.graph{Node.java[Node";
	private static final String NODE_WEKA_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.gui.treevisualizer{Node.java[Node";
	private static final String PARSER_BASE_BETW = "=JenaRBetw/<com.hp.hpl.jena.n3.turtle{ParserBase.java[ParserBase";
	private static final String REG_OPTIMIZER_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.classifiers.functions.supportVector{RegOptimizer.java[RegOptimizer";
	private static final String RESULT_MATRIX_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.experiment{ResultMatrix.java[ResultMatrix";
	private static final String RULE_JENA_BETW = "=JenaRBetw/<com.hp.hpl.jena.reasoner.rulesys{Rule.java[Rule";
	private static final String RULE_WEKA_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.classifiers.trees.m5{Rule.java[Rule";
	private static final String SCRIPT_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.gui.scripting{Script.java[Script";
	private static final String SETTINGS_HANDLER_BETW = "=HeritrixRBetw/<org.archive.crawler.settings{SettingsHandler.java[SettingsHandler";
	private static final String SPECIFICATION_BETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.common.model{Specification.java[Specification";
	private static final String TEST_INSTANCES_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.core{TestInstances.java[TestInstances";
	private static final String WORK_QUEUE_BETW = "=HeritrixRBetw/<org.archive.crawler.frontier{WorkQueue.java[WorkQueue";
	private static final String XML_DOCUMENT_BETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.core.xml{XMLDocument.java[XMLDocument";

	private static final String BRULE_ENGINE_RBETW = "=JenaRBetw/<com.hp.hpl.jena.reasoner.rulesys.impl.oldCode{BRuleEngineExtract.java[BRuleEngineExtract";
	private static final String BV_DECOMPOSE_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.classifiers{BVDecomposeExtract.java[BVDecomposeExtract";
	private static final String BV_DECOMPOSE_SEG_CV_SUB_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.classifiers{BVDecomposeSegCVSubExtract.java[BVDecomposeSegCVSubExtract";
	private static final String CANDIDATE_URI_RBETW = "=HeritrixRBetw/<org.archive.crawler.datamodel{CandidateURIExtract.java[CandidateURIExtract";
	private static final String COMMAND_LINE_RBETW = "=JenaRBetw/<jena.cmdline{CommandLineExtract.java[CommandLineExtract";
	private static final String CRAWL_CONTROLLER_RBETW = "=HeritrixRBetw/<org.archive.crawler.framework{CrawlControllerExtract.java[CrawlControllerExtract";
	private static final String DATABASE_UTILS_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.experiment{DatabaseUtilsExtract.java[DatabaseUtilsExtract";
	private static final String EXPERIMENT_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.experiment{ExperimentExtract.java[ExperimentExtract";
	private static final String FREECOL_CLIENT_RBETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.client{FreeColClientExtract.java[FreeColClientExtract";
	private static final String FREECOL_OBJECT_RBETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.common.model{FreeColObjectExtract.java[FreeColObjectExtract";
	private static final String FREECOL_SERVER_RBETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.server{FreecolServerExtract.java[FreecolServerExtract";
	// private static final String FREECOL_SERVER_RBETW =
	// "=FreecolSVNTrunkRBetw/src<net.sf.freecol.server{FreeColServerExtract.java[FreeColServerExtract";
	private static final String HERITRIX_RBETW = "=HeritrixRBetw/<org.archive.crawler{HeritrixExtract.java[HeritrixExtract";
	private static final String IMAGE_LIBRARY_RBETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.client.gui{ImageLibraryExtract.java[ImageLibraryExtract";
	private static final String LPB_RULE_ENGINE_RBETW = "=JenaRBetw/<com.hp.hpl.jena.reasoner.rulesys.impl{LPBRuleEngineExtract.java[LPBRuleEngineExtract";
	private static final String LP_INTERPRETER_RBETW = "=JenaRBetw/<com.hp.hpl.jena.reasoner.rulesys.impl{LPInterpreterExtract.java[LPInterpreterExtract";
	private static final String N3_JENA_WRITER_COMMON_RBETW = "=JenaRBetw/<com.hp.hpl.jena.n3{N3JenaWriterCommonExtract.java[N3JenaWriterCommonExtract";
	private static final String NEAREST_NEIGHBOR_SEARCH_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.core.neighboursearch{NearestNeighbourSearchExtract.java[NearestNeighbourSearchExtract";
	private static final String NODE_JENA_RBETW = "=JenaRBetw/<com.hp.hpl.jena.graph{NodeExtract.java[NodeExtract";
	private static final String NODE_WEKA_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.gui.treevisualizer{NodeExtract.java[NodeExtract";
	private static final String PARSER_BASE_RBETW = "=JenaRBetw/<com.hp.hpl.jena.n3.turtle{ParserBaseExtract.java[ParserBaseExtract";
	private static final String REG_OPTIMIZER_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.classifiers.functions.supportVector{RegOptimizerExtract.java[RegOptimizerExtract";
	private static final String RESULT_MATRIX_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.experiment{ResultMatrixExtract.java[ResultMatrixExtract";
	private static final String RULE_JENA_RBETW = "=JenaRBetw/<com.hp.hpl.jena.reasoner.rulesys{RuleExtract.java[RuleExtract";
	private static final String RULE_WEKA_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.classifiers.trees.m5{RuleExtract.java[RuleExtract";
	private static final String SCRIPT_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.gui.scripting{ScriptExtract.java[ScriptExtract";
	private static final String SETTINGS_HANDLER_RBETW = "=HeritrixRBetw/<org.archive.crawler.settings{SettingsHandlerExtract.java[SettingsHandlerExtract";
	private static final String SPECIFICATION_RBETW = "=FreecolSVNTrunkRBetw/src<net.sf.freecol.common.model{SpecificationExtract.java[SpecificationExtract";
	private static final String TEST_INSTANCES_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.core{TestInstancesExtract.java[TestInstancesExtract";
	private static final String WORK_QUEUE_RBETW = "=HeritrixRBetw/<org.archive.crawler.frontier{WorkQueueExtract.java[WorkQueueExtract";
	private static final String XML_DOCUMENT_RBETW = "=wekaSVNTrunkRBetw/src\\/main\\/java<weka.core.xml{XMLDocumentExtract.java[XMLDocumentExtract";

	private static final String BRULE_ENGINE_ECOOP = "=JenaRECOOP/<com.hp.hpl.jena.reasoner.rulesys.impl.oldCode{BRuleEngine.java[BRuleEngine";
	private static final String BV_DECOMPOSE_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.classifiers{BVDecompose.java[BVDecompose";
	private static final String BV_DECOMPOSE_SEG_CV_SUB_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.classifiers{BVDecomposeSegCVSub.java[BVDecomposeSegCVSub";
	private static final String CANDIDATE_URI_ECOOP = "=HeritrixRECOOP/<org.archive.crawler.datamodel{CandidateURI.java[CandidateURI";
	private static final String COMMAND_LINE_ECOOP = "=JenaRECOOP/<jena.cmdline{CommandLine.java[CommandLine";
	private static final String CRAWL_CONTROLLER_ECOOP = "=HeritrixRECOOP/<org.archive.crawler.framework{CrawlController.java[CrawlController";
	private static final String DATABASE_UTILS_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.experiment{DatabaseUtils.java[DatabaseUtils";
	private static final String EXPERIMENT_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.experiment{Experiment.java[Experiment";
	private static final String FREECOL_CLIENT_ECOOP = "=FreecolRECOOP/src<net.sf.freecol.client{FreeColClient.java[FreeColClient";
	private static final String FREECOL_OBJECT_ECOOP = "=FreecolRECOOP/src<net.sf.freecol.common.model{FreeColObject.java[FreeColObject";
	private static final String FREECOL_SERVER_ECOOP = "=FreecolRECOOP/src<net.sf.freecol.server{FreeColServer.java[FreeColServer";
	private static final String HERITRIX_ECOOP = "=HeritrixRECOOP/<org.archive.crawler{Heritrix.java[Heritrix";
	private static final String IMAGE_LIBRARY_ECOOP = "=FreecolRECOOP/src<net.sf.freecol.client.gui{ImageLibrary.java[ImageLibrary";
	private static final String LPB_RULE_ENGINE_ECOOP = "=JenaRECOOP/<com.hp.hpl.jena.reasoner.rulesys.impl{LPBRuleEngine.java[LPBRuleEngine";
	private static final String LP_INTERPRETER_ECOOP = "=JenaRECOOP/<com.hp.hpl.jena.reasoner.rulesys.impl{LPInterpreter.java[LPInterpreter";
	private static final String N3_JENA_WRITER_COMMON_ECOOP = "=JenaRECOOP/<com.hp.hpl.jena.n3{N3JenaWriterCommon.java[N3JenaWriterCommon";
	private static final String NEAREST_NEIGHBOR_SEARCH_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.core.neighboursearch{NearestNeighbourSearch.java[NearestNeighbourSearch";
	private static final String NODE_JENA_ECOOP = "=JenaRECOOP/<com.hp.hpl.jena.graph{Node.java[Node";
	private static final String NODE_WEKA_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.gui.treevisualizer{Node.java[Node";
	private static final String PARSER_BASE_ECOOP = "=JenaRECOOP/<com.hp.hpl.jena.n3.turtle{ParserBase.java[ParserBase";
	private static final String REG_OPTIMIZER_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.classifiers.functions.supportVector{RegOptimizer.java[RegOptimizer";
	private static final String RESULT_MATRIX_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.experiment{ResultMatrix.java[ResultMatrix";
	private static final String RULE_JENA_ECOOP = "=JenaRECOOP/<com.hp.hpl.jena.reasoner.rulesys{Rule.java[Rule";
	private static final String RULE_WEKA_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.classifiers.trees.m5{Rule.java[Rule";
	private static final String SCRIPT_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.gui.scripting{Script.java[Script";
	private static final String SETTINGS_HANDLER_ECOOP = "=HeritrixRECOOP/<org.archive.crawler.settings{SettingsHandler.java[SettingsHandler";
	private static final String SPECIFICATION_ECOOP = "=FreecolRECOOP/src<net.sf.freecol.common.model{Specification.java[Specification";
	private static final String TEST_INSTANCES_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.core{TestInstances.java[TestInstances";
	private static final String WORK_QUEUE_ECOOP = "=HeritrixRECOOP/<org.archive.crawler.frontier{WorkQueue.java[WorkQueue";
	private static final String XML_DOCUMENT_ECOOP = "=wekaRECOOP/src\\/main\\/java<weka.core.xml{XMLDocument.java[XMLDocument";

	private static final String BRULE_ENGINE_RECOOP = "=JenaRECOOP/<com.hp.hpl.jena.reasoner.rulesys.impl.oldCode{BRuleEngineExtract.java[BRuleEngineExtract";
	private static final String BV_DECOMPOSE_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.classifiers{BVDecomposeExtract.java[BVDecomposeExtract";
	private static final String BV_DECOMPOSE_SEG_CV_SUB_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.classifiers{BVDecomposeSegCVSubExtract.java[BVDecomposeSegCVSubExtract";
	private static final String CANDIDATE_URI_RECOOP = "=HeritrixRECOOP/<org.archive.crawler.datamodel{CandidateURIExtract.java[CandidateURIExtract";
	private static final String COMMAND_LINE_RECOOP = "=JenaRECOOP/<jena.cmdline{CommandLineExtract.java[CommandLineExtract";
	private static final String CRAWL_CONTROLLER_RECOOP = "=HeritrixRECOOP/<org.archive.crawler.framework{CrawlControllerExtract.java[CrawlControllerExtract";
	private static final String DATABASE_UTILS_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.experiment{DatabaseUtilKeywords.java[DatabaseUtilKeywords";
	private static final String EXPERIMENT_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.experiment{ExperimentExtract.java[ExperimentExtract";
	private static final String FREECOL_CLIENT_RECOOP = "=FreecolRECOOP/src<net.sf.freecol.client{FreeColClientControllers.java[FreeColClientControllers";
	private static final String FREECOL_OBJECT_RECOOP = "=FreecolRECOOP/src<net.sf.freecol.common.model{FreeColObjectProperties.java[FreeColObjectProperties";
	private static final String FREECOL_SERVER_RECOOP = "=FreecolRECOOP/src<net.sf.freecol.server{FreeColServerControllers.java[FreeColServerControllers";
	private static final String HERITRIX_RECOOP = "=HeritrixRECOOP/<org.archive.crawler{HeritrixAlerts.java[HeritrixAlerts";
	private static final String IMAGE_LIBRARY_RECOOP = "=FreecolRECOOP/src<net.sf.freecol.client.gui{ImageLibraryExtract.java[ImageLibraryExtract";
	private static final String LPB_RULE_ENGINE_RECOOP = "=JenaRECOOP/<com.hp.hpl.jena.reasoner.rulesys.impl{LPBRuleEngineExtract.java[LPBRuleEngineExtract";
	private static final String LP_INTERPRETER_RECOOP = "=JenaRECOOP/<com.hp.hpl.jena.reasoner.rulesys.impl{LPInterpreterExtract.java[LPInterpreterExtract";
	private static final String N3_JENA_WRITER_COMMON_RECOOP = "=JenaRECOOP/<com.hp.hpl.jena.n3{N3JenaWriterCommonExtract.java[N3JenaWriterCommonExtract";
	private static final String NEAREST_NEIGHBOR_SEARCH_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.core.neighboursearch{NearestNeighbourSearchExtract.java[NearestNeighbourSearchExtract";
	private static final String NODE_JENA_RECOOP = "=JenaRECOOP/<com.hp.hpl.jena.graph{NodeExtract.java[NodeExtract";
	private static final String NODE_WEKA_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.gui.treevisualizer{NodeVisibility.java[NodeVisibility";
	private static final String PARSER_BASE_RECOOP = "=JenaRECOOP/<com.hp.hpl.jena.n3.turtle{ParserBaseExtract.java[ParserBaseExtract";
	private static final String REG_OPTIMIZER_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.classifiers.functions.supportVector{RegOptimizerExtract.java[RegOptimizerExtract";
	private static final String RESULT_MATRIX_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.experiment{ResultMatrixExcerpt.java[ResultMatrixExcerpt";
	private static final String RULE_JENA_RECOOP = "=JenaRECOOP/<com.hp.hpl.jena.reasoner.rulesys{RulesExtract.java[RulesExtract";
	private static final String RULE_WEKA_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.classifiers.trees.m5{RuleExtract.java[RuleExtract";
	private static final String SCRIPT_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.gui.scripting{ScriptExtract.java[ScriptExtract";
	private static final String SETTINGS_HANDLER_RECOOP = "=HeritrixRECOOP/<org.archive.crawler.settings{SettingsHandlerExtract.java[SettingsHandlerExtract";
	private static final String SPECIFICATION_RECOOP = "=FreecolRECOOP/src<net.sf.freecol.common.model{SpecificationExcerpt.java[SpecificationExcerpt";
	private static final String TEST_INSTANCES_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.core{TestInstancesExtract.java[TestInstancesExtract";
	private static final String WORK_QUEUE_RECOOP = "=HeritrixRECOOP/<org.archive.crawler.frontier{WorkQueueExtract.java[WorkQueueExtract";
	private static final String XML_DOCUMENT_RECOOP = "=wekaRECOOP/src\\/main\\/java<weka.core.xml{XMLDocumentEvaluator.java[XMLDocumentEvaluator";

	private ArrayList<String> freeColClasses = new ArrayList<String>();
	private ArrayList<String> heritrixClasses = new ArrayList<String>();
	private ArrayList<String> jenaClasses = new ArrayList<String>();
	private ArrayList<String> wekaClasses = new ArrayList<String>();
	private ArrayList<String> allClasses = new ArrayList<String>();

	private ArrayList<String> freeColClassesBetw = new ArrayList<String>();
	private ArrayList<String> heritrixClassesBetw = new ArrayList<String>();
	private ArrayList<String> jenaClassesBetw = new ArrayList<String>();
	private ArrayList<String> wekaClassesBetw = new ArrayList<String>();
	private ArrayList<String> allClassesBetw = new ArrayList<String>();

	private ArrayList<String> freeColClassesRBetw = new ArrayList<String>();
	private ArrayList<String> heritrixClassesRBetw = new ArrayList<String>();
	private ArrayList<String> jenaClassesRBetw = new ArrayList<String>();
	private ArrayList<String> wekaClassesRBetw = new ArrayList<String>();
	private ArrayList<String> allClassesRBetw = new ArrayList<String>();

	private ArrayList<String> freeColClassesECOOP = new ArrayList<String>();
	private ArrayList<String> heritrixClassesECOOP = new ArrayList<String>();
	private ArrayList<String> jenaClassesECOOP = new ArrayList<String>();
	private ArrayList<String> wekaClassesECOOP = new ArrayList<String>();
	private ArrayList<String> allClassesECOOP = new ArrayList<String>();

	private ArrayList<String> freeColClassesRECOOP = new ArrayList<String>();
	private ArrayList<String> heritrixClassesRECOOP = new ArrayList<String>();
	private ArrayList<String> jenaClassesRECOOP = new ArrayList<String>();
	private ArrayList<String> wekaClassesRECOOP = new ArrayList<String>();
	private ArrayList<String> allClassesRECOOP = new ArrayList<String>();

	public GodClassesMM30() {
		freeColClasses.add(FREECOL_CLIENT);
		freeColClasses.add(FREECOL_OBJECT);
		freeColClasses.add(FREECOL_SERVER);
		freeColClasses.add(IMAGE_LIBRARY);
		freeColClasses.add(SPECIFICATION);

		heritrixClasses.add(CANDIDATE_URI);
		heritrixClasses.add(CRAWL_CONTROLLER);
		heritrixClasses.add(HERITRIX);
		heritrixClasses.add(SETTINGS_HANDLER);
		heritrixClasses.add(WORK_QUEUE);

		jenaClasses.add(BRULE_ENGINE);
		jenaClasses.add(COMMAND_LINE);
		jenaClasses.add(LPB_RULE_ENGINE);
		jenaClasses.add(LP_INTERPRETER);
		jenaClasses.add(N3_JENA_WRITER_COMMON);
		jenaClasses.add(NODE_JENA);
		jenaClasses.add(PARSER_BASE);
		jenaClasses.add(RULE_JENA);

		wekaClasses.add(BV_DECOMPOSE);
		wekaClasses.add(BV_DECOMPOSE_SEG_CV_SUB);
		wekaClasses.add(DATABASE_UTILS);
		wekaClasses.add(EXPERIMENT);
		wekaClasses.add(NEAREST_NEIGHBOR_SEARCH);
		wekaClasses.add(NODE_WEKA);
		wekaClasses.add(REG_OPTIMIZER);
		wekaClasses.add(RESULT_MATRIX);
		wekaClasses.add(RULE_WEKA);
		wekaClasses.add(SCRIPT);
		wekaClasses.add(TEST_INSTANCES);
		wekaClasses.add(XML_DOCUMENT);

		allClasses.addAll(freeColClasses);
		allClasses.addAll(heritrixClasses);
		allClasses.addAll(jenaClasses);
		allClasses.addAll(wekaClasses);

		freeColClassesBetw.add(FREECOL_CLIENT_BETW);
		freeColClassesBetw.add(FREECOL_OBJECT_BETW);
		freeColClassesBetw.add(FREECOL_SERVER_BETW);
		freeColClassesBetw.add(IMAGE_LIBRARY_BETW);
		freeColClassesBetw.add(SPECIFICATION_BETW);

		heritrixClassesBetw.add(CANDIDATE_URI_BETW);
		heritrixClassesBetw.add(CRAWL_CONTROLLER_BETW);
		heritrixClassesBetw.add(HERITRIX_BETW);
		heritrixClassesBetw.add(SETTINGS_HANDLER_BETW);
		heritrixClassesBetw.add(WORK_QUEUE_BETW);

		jenaClassesBetw.add(BRULE_ENGINE_BETW);
		jenaClassesBetw.add(COMMAND_LINE_BETW);
		jenaClassesBetw.add(LPB_RULE_ENGINE_BETW);
		jenaClassesBetw.add(LP_INTERPRETER_BETW);
		jenaClassesBetw.add(N3_JENA_WRITER_COMMON_BETW);
		jenaClassesBetw.add(NODE_JENA_BETW);
		jenaClassesBetw.add(PARSER_BASE_BETW);
		jenaClassesBetw.add(RULE_JENA_BETW);

		wekaClassesBetw.add(BV_DECOMPOSE_BETW);
		wekaClassesBetw.add(BV_DECOMPOSE_SEG_CV_SUB_BETW);
		wekaClassesBetw.add(DATABASE_UTILS_BETW);
		wekaClassesBetw.add(EXPERIMENT_BETW);
		wekaClassesBetw.add(NEAREST_NEIGHBOR_SEARCH_BETW);
		wekaClassesBetw.add(NODE_WEKA_BETW);
		wekaClassesBetw.add(REG_OPTIMIZER_BETW);
		wekaClassesBetw.add(RESULT_MATRIX_BETW);
		wekaClassesBetw.add(RULE_WEKA_BETW);
		wekaClassesBetw.add(SCRIPT_BETW);
		wekaClassesBetw.add(TEST_INSTANCES_BETW);
		wekaClassesBetw.add(XML_DOCUMENT_BETW);

		allClassesBetw.addAll(freeColClassesBetw);
		allClassesBetw.addAll(heritrixClassesBetw);
		allClassesBetw.addAll(jenaClassesBetw);
		allClassesBetw.addAll(wekaClassesBetw);

		freeColClassesRBetw.add(FREECOL_CLIENT_RBETW);
		freeColClassesRBetw.add(FREECOL_OBJECT_RBETW);
		freeColClassesRBetw.add(FREECOL_SERVER_RBETW);
		freeColClassesRBetw.add(IMAGE_LIBRARY_RBETW);
		freeColClassesRBetw.add(SPECIFICATION_RBETW);

		freeColClassesECOOP.add(FREECOL_CLIENT_ECOOP);
		freeColClassesECOOP.add(FREECOL_OBJECT_ECOOP);
		freeColClassesECOOP.add(FREECOL_SERVER_ECOOP);
		freeColClassesECOOP.add(IMAGE_LIBRARY_ECOOP);
		freeColClassesECOOP.add(SPECIFICATION_ECOOP);

		freeColClassesRECOOP.add(FREECOL_CLIENT_RECOOP);
		freeColClassesRECOOP.add(FREECOL_OBJECT_RECOOP);
		freeColClassesRECOOP.add(FREECOL_SERVER_RECOOP);
		freeColClassesRECOOP.add(IMAGE_LIBRARY_RECOOP);
		freeColClassesRECOOP.add(SPECIFICATION_RECOOP);

		heritrixClassesRBetw.add(CANDIDATE_URI_RBETW);
		heritrixClassesRBetw.add(CRAWL_CONTROLLER_RBETW);
		heritrixClassesRBetw.add(HERITRIX_RBETW);
		heritrixClassesRBetw.add(SETTINGS_HANDLER_RBETW);
		heritrixClassesRBetw.add(WORK_QUEUE_RBETW);

		heritrixClassesECOOP.add(CANDIDATE_URI_ECOOP);
		heritrixClassesECOOP.add(CRAWL_CONTROLLER_ECOOP);
		heritrixClassesECOOP.add(HERITRIX_ECOOP);
		heritrixClassesECOOP.add(SETTINGS_HANDLER_ECOOP);
		heritrixClassesECOOP.add(WORK_QUEUE_ECOOP);

		heritrixClassesRECOOP.add(CANDIDATE_URI_RECOOP);
		heritrixClassesRECOOP.add(CRAWL_CONTROLLER_RECOOP);
		heritrixClassesRECOOP.add(HERITRIX_RECOOP);
		heritrixClassesRECOOP.add(SETTINGS_HANDLER_RECOOP);
		heritrixClassesRECOOP.add(WORK_QUEUE_RECOOP);

		jenaClassesRBetw.add(BRULE_ENGINE_RBETW);
		jenaClassesRBetw.add(COMMAND_LINE_RBETW);
		jenaClassesRBetw.add(LPB_RULE_ENGINE_RBETW);
		jenaClassesRBetw.add(LP_INTERPRETER_RBETW);
		jenaClassesRBetw.add(N3_JENA_WRITER_COMMON_RBETW);
		jenaClassesRBetw.add(NODE_JENA_RBETW);
		jenaClassesRBetw.add(PARSER_BASE_RBETW);
		jenaClassesRBetw.add(RULE_JENA_RBETW);

		jenaClassesECOOP.add(BRULE_ENGINE_ECOOP);
		jenaClassesECOOP.add(COMMAND_LINE_ECOOP);
		jenaClassesECOOP.add(LPB_RULE_ENGINE_ECOOP);
		jenaClassesECOOP.add(LP_INTERPRETER_ECOOP);
		jenaClassesECOOP.add(N3_JENA_WRITER_COMMON_ECOOP);
		jenaClassesECOOP.add(NODE_JENA_ECOOP);
		jenaClassesECOOP.add(PARSER_BASE_ECOOP);
		jenaClassesECOOP.add(RULE_JENA_ECOOP);

		jenaClassesRECOOP.add(BRULE_ENGINE_RECOOP);
		jenaClassesRECOOP.add(COMMAND_LINE_RECOOP);
		jenaClassesRECOOP.add(LPB_RULE_ENGINE_RECOOP);
		jenaClassesRECOOP.add(LP_INTERPRETER_RECOOP);
		jenaClassesRECOOP.add(N3_JENA_WRITER_COMMON_RECOOP);
		jenaClassesRECOOP.add(NODE_JENA_RECOOP);
		jenaClassesRECOOP.add(PARSER_BASE_RECOOP);
		jenaClassesRECOOP.add(RULE_JENA_RECOOP);

		wekaClassesRBetw.add(BV_DECOMPOSE_RBETW);
		wekaClassesRBetw.add(BV_DECOMPOSE_SEG_CV_SUB_RBETW);
		wekaClassesRBetw.add(DATABASE_UTILS_RBETW);
		wekaClassesRBetw.add(EXPERIMENT_RBETW);
		wekaClassesRBetw.add(NEAREST_NEIGHBOR_SEARCH_RBETW);
		wekaClassesRBetw.add(NODE_WEKA_RBETW);
		wekaClassesRBetw.add(REG_OPTIMIZER_RBETW);
		wekaClassesRBetw.add(RESULT_MATRIX_RBETW);
		wekaClassesRBetw.add(RULE_WEKA_RBETW);
		wekaClassesRBetw.add(SCRIPT_RBETW);
		wekaClassesRBetw.add(TEST_INSTANCES_RBETW);
		wekaClassesRBetw.add(XML_DOCUMENT_RBETW);

		wekaClassesECOOP.add(BV_DECOMPOSE_ECOOP);
		wekaClassesECOOP.add(BV_DECOMPOSE_SEG_CV_SUB_ECOOP);
		wekaClassesECOOP.add(DATABASE_UTILS_ECOOP);
		wekaClassesECOOP.add(EXPERIMENT_ECOOP);
		wekaClassesECOOP.add(NEAREST_NEIGHBOR_SEARCH_ECOOP);
		wekaClassesECOOP.add(NODE_WEKA_ECOOP);
		wekaClassesECOOP.add(REG_OPTIMIZER_ECOOP);
		wekaClassesECOOP.add(RESULT_MATRIX_ECOOP);
		wekaClassesECOOP.add(RULE_WEKA_ECOOP);
		wekaClassesECOOP.add(SCRIPT_ECOOP);
		wekaClassesECOOP.add(TEST_INSTANCES_ECOOP);
		wekaClassesECOOP.add(XML_DOCUMENT_ECOOP);

		wekaClassesRECOOP.add(BV_DECOMPOSE_RECOOP);
		wekaClassesRECOOP.add(BV_DECOMPOSE_SEG_CV_SUB_RECOOP);
		wekaClassesRECOOP.add(DATABASE_UTILS_RECOOP);
		wekaClassesRECOOP.add(EXPERIMENT_RECOOP);
		wekaClassesRECOOP.add(NEAREST_NEIGHBOR_SEARCH_RECOOP);
		wekaClassesRECOOP.add(NODE_WEKA_RECOOP);
		wekaClassesRECOOP.add(REG_OPTIMIZER_RECOOP);
		wekaClassesRECOOP.add(RESULT_MATRIX_RECOOP);
		wekaClassesRECOOP.add(RULE_WEKA_RECOOP);
		wekaClassesRECOOP.add(SCRIPT_RECOOP);
		wekaClassesRECOOP.add(TEST_INSTANCES_RECOOP);
		wekaClassesRECOOP.add(XML_DOCUMENT_RECOOP);

		allClassesRBetw.addAll(freeColClassesRBetw);
		allClassesRBetw.addAll(heritrixClassesRBetw);
		allClassesRBetw.addAll(jenaClassesRBetw);
		allClassesRBetw.addAll(wekaClassesRBetw);

		allClassesECOOP.addAll(freeColClassesECOOP);
		allClassesECOOP.addAll(heritrixClassesECOOP);
		allClassesECOOP.addAll(jenaClassesECOOP);
		allClassesECOOP.addAll(wekaClassesECOOP);

		allClassesRECOOP.addAll(freeColClassesRECOOP);
		allClassesRECOOP.addAll(heritrixClassesRECOOP);
		allClassesRECOOP.addAll(jenaClassesRECOOP);
		allClassesRECOOP.addAll(wekaClassesRECOOP);
	}

	public ArrayList<String> getAllClasses() {
		return allClasses;
	}

	public ArrayList<String> getAllClassesBetw() {
		return allClassesBetw;
	}

	public ArrayList<String> getAllClassesRBetw() {
		return allClassesRBetw;
	}

	public ArrayList<String> getAllClassesECOOP() {
		return allClassesECOOP;
	}

	public ArrayList<String> getAllClassesRECOOP() {
		return allClassesRECOOP;
	}

	public ArrayList<String> getCommandLineClasses() {
		ArrayList<String> classes = new ArrayList<String>();
		classes.add("=JenaAggNhoodSim/<jena.cmdline{CommandLine.java[CommandLine");
		classes.add("=JenaAggNhoodSim75/<jena.cmdline{CommandLine.java[CommandLine");
		classes.add("=JenaAggSimSin/<jena.cmdline{CommandLine.java[CommandLine");
		classes.add("=JenaAggNhoodSim/<jena.cmdline{CommandLineExtract.java[CommandLineExtract");
		classes.add("=JenaAggNhoodSim75/<jena.cmdline{CommandLineExtract.java[CommandLineExtract");
		classes.add("=JenaAggSimSin/<jena.cmdline{CommandLineExtract.java[CommandLineExtract");
		return classes ;
	}

	/**
	 * Collects all the software measurements for classes that match the SQL
	 * query.
	 * 
	 * @param args
	 */
	public Map<String, TypeMetrics> printMetricValues() throws SQLException {
		Map<String, TypeMetrics> metrics = new HashMap<String, TypeMetrics>();
		/*
		 * We are storing the Statement and Prepared statement object references
		 * in an array list for convenience.
		 */
		ArrayList<Statement> statements = new ArrayList<Statement>();
		Statement statement = null;
		ResultSet resultSet = null;
		Database db = new Database();
		Connection connection = null;
		/*
		 * We will be using Statement and PreparedStatement objects for
		 * executing SQL. These objects, as well as Connections and ResultSets,
		 * are resources that should be released explicitly after use, hence the
		 * try-catch-finally pattern used below.
		 */
		try {
			// db.setClientURL("jdbc:derby://localhost:1527/C:/metrics/metrics2DB");
			db.loadDriver();
			connection = db.prepareConnection();

			/*
			 * Creating a statement object that we can use for running various
			 * SQL statements commands against the database.
			 */
			statement = connection.createStatement();
			statements.add(statement);
			PreparedStatement selectStatement =
				getSelectMetricValuesPreparedStatement(connection, statements);
			statements.add(statement);

			ArrayList<String> allClasses = getAllClassesRBetw();

			for (String handle : allClasses) {
				resultSet = selectMetricValues(selectStatement, handle);
				metrics = collectMetricResults(resultSet, metrics);
			}
			printNonCohesionResults(metrics);
			statement.close();
			connection.commit();

			// In embedded mode, an application should shut down the database.
			db.shutDownEmbedded();
		} catch (SQLException sqle) {
			utilLogger.warning("SQLException = " + sqle);
			Database.printSQLException(sqle);
			throw sqle;
		} finally {
			// release all open resources to avoid unnecessary memory usage
			try {
				db.releaseResources(connection, statements, resultSet);
			} catch (Throwable re) {
				utilLogger.warning("caught " + re
						+ " while releasing resources");
			}
		}
		return metrics;
	}

	@SuppressWarnings("unused")
	private void printCohesionResults(Map<String, TypeMetrics> metrics) {
		PrintStream out = System.out;
		DecimalFormat formatter = new DecimalFormat();
		formatter.setGroupingUsed(false);
		formatter.setMaximumFractionDigits(3);

		out.print(HANDLE_FIELD.trim());
		out.print(",");
		out.print(LCOMCK.trim());
		out.print(",");
		out.print(LCOMHS.trim());
		out.print(",");
		out.print(DCD.trim());
		out.print(",");
		out.print(DCI.trim());
		out.print(",");
		out.print(LCC.trim());
		out.print(",");
		out.print(TCC.trim());
		out.print(",");
		out.print("C3V");
		out.print("\n");
		Set<String> keySet = metrics.keySet();
		for (String key : keySet) {
			TypeMetrics typeMetrics = metrics.get(key);
			Map<String, Metric> valueMap = typeMetrics.getValues();
			out.print(key);
			printValue(out, valueMap, LCOMCK, formatter);
			printValue(out, valueMap, LCOMHS, formatter);
			printValue(out, valueMap, DCD, formatter);
			printValue(out, valueMap, DCI, formatter);
			printValue(out, valueMap, LCC, formatter);
			printValue(out, valueMap, TCC, formatter);
			printValue(out, valueMap, "C3V", formatter);
			out.print("\n");
		}
		out.print("*** Done ***\n");
	}

	private void printNonCohesionResults(Map<String, TypeMetrics> metrics) {
		PrintStream out = System.out;
		DecimalFormat formatter = new DecimalFormat();
		formatter.setGroupingUsed(false);
		formatter.setMaximumFractionDigits(3);

		out.print(HANDLE_FIELD.trim());
		out.print(",");
		out.print("NF,");
		out.print("NM,");
		out.print(WMC.trim());
		out.print("\n");
		Set<String> keySet = metrics.keySet();
		for (String key : keySet) {
			TypeMetrics typeMetrics = metrics.get(key);
			Map<String, Metric> valueMap = typeMetrics.getValues();
			out.print(key);
			printValue(out, valueMap, NUM_FIELDS, formatter);
			printValue(out, valueMap, NUM_METHODS, formatter);
			printValue(out, valueMap, WMC, formatter);
			out.print("\n");
		}
		out.print("*** Done ***\n");
	}

	protected void printValue(PrintStream out, Map<String, Metric> valueMap,
			String acronym, NumberFormat formatter) {
		Metric metric = valueMap.get(acronym);
		if (metric != null) {
			double value = metric.getValue();
			// NUM_FIELDS counts instance fields, we want static fields too
			if (NUM_FIELDS.equals(acronym)) {
				Metric metricStatic = valueMap.get(NUM_STAT_FIELDS);
				if (metricStatic != null) {
					value += metricStatic.getValue();
				}
			} else if (NUM_METHODS.equals(acronym)) {
				Metric metricStatic = valueMap.get(NUM_STAT_METHODS);
				if (metricStatic != null) {
					value += metricStatic.getValue();
				}
			}
			String sValue = formatter.format(value);
			out.print(",");
			out.print(sValue);
		} else {
			out.print(",");
		}
	}

	private PreparedStatement getSelectMetricValuesPreparedStatement(
			Connection connection, List<Statement> statements)
			throws SQLException {
		String sqlString = "SELECT * FROM JOOMP.MetricValues WHERE handle = ?";
		PreparedStatement statement = connection.prepareStatement(sqlString);
		return statement;
	}

	private ResultSet selectMetricValues(PreparedStatement selectStatement,
			String handle) {
		ResultSet resultSet = null;
		try {
			selectStatement.setString(1, handle);
			resultSet = selectStatement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultSet;
	}

	/**
	 * Each result will have a single metric value
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, TypeMetrics> collectMetricResults(
			ResultSet resultSet, Map<String, TypeMetrics> allClassMetrics)
			throws SQLException {
		EclipseUtils.prepareWorkspace();

		// Each result will have a single metric value
		while (resultSet.next()) {
			String pref = resultSet.getString(USER_PREFERENCES_FOREIGN_KEY
					.trim());

			if ("5".equals(pref)) {
				String handle = resultSet.getString(HANDLE_FIELD.trim());
				TypeMetrics classMetrics = allClassMetrics.get(handle);
				if (classMetrics == null) {
					classMetrics = new TypeMetrics();
					IJavaElement element = JavaCore.create(handle);
					classMetrics.setJavaElement(element);
					classMetrics.setHandle(handle);
					allClassMetrics.put(handle, classMetrics);
				}
				String metricId = resultSet.getString(ACRONYM_FIELD.trim());
				Double value = resultSet.getDouble(VALUE_FIELD.trim());
				Metric metric = new Metric(metricId, value);
				classMetrics.setValue(metric);
			}
		}
		return allClassMetrics;
	}

}