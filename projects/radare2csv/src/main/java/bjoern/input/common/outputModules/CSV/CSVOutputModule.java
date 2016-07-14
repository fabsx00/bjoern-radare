package bjoern.input.common.outputModules.CSV;

import bjoern.input.common.outputModules.OutputModule;
import bjoern.nodeStore.Node;
import bjoern.nodeStore.NodeKey;
import bjoern.structures.RootNode;
import bjoern.structures.annotations.Flag;
import bjoern.structures.annotations.VariableOrArgument;
import bjoern.structures.edges.DirectedEdge;
import bjoern.structures.edges.EdgeTypes;
import bjoern.structures.interpretations.BasicBlock;
import bjoern.structures.interpretations.Function;
import bjoern.structures.interpretations.FunctionContent;
import bjoern.structures.interpretations.Instruction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVOutputModule implements OutputModule
{

	@Override
	public void initialize(String outputDir)
	{
		CSVWriter.changeOutputDir(outputDir);
	}

	@Override
	public void finish()
	{
		CSVWriter.finish();
	}

	private void writeEdge(DirectedEdge edge)
	{

		String sourceKey = edge.getSourceKey().toString();
		String destKey = edge.getDestKey().toString();
		String label = edge.getType();
		Map<String, Object> properties = new HashMap<>();
		// TODO: add edge properties.
		CSVWriter.addEdge(sourceKey, destKey, properties, label);
	}

	@Override
	public void writeFunction(Function function)
	{
		writeFunctionNodes(function);
		writeReferencesToFunction(function);
		writeFunctionContent(function);
	}

	@Override
	public void writeFlag(Flag flag)
	{
		createRootNodeForNode(flag);
		CSVWriter.addNode(flag);
		attachFlagsToRootNodes(flag);
	}

	@Override
	public void writeCrossReference(DirectedEdge xref)
	{
		writeEdge(xref);
	}

	private void writeFunctionNodes(Function function)
	{
		createRootNodeForNode(function);
		CSVWriter.addNoReplaceNode(function);
	}

	private void writeReferencesToFunction(Function function)
	{
		addEdgeFromRootNode(function, EdgeTypes.INTERPRETATION);
	}

	private void writeFunctionContent(Function function)
	{
		writeArgumentsAndVariables(function);
		writeBasicBlocks(function);
		writeCFGEdges(function);
	}

	private void writeBasicBlock(BasicBlock block)
	{
		createRootNodeForNode(block);
		writeNodeForBasicBlock(block);
		addEdgeFromRootNode(block, EdgeTypes.INTERPRETATION);
		writeInstructions(block);
	}

	private void attachFlagsToRootNodes(Flag flag)
	{
		addEdgeFromRootNode(flag, EdgeTypes.ANNOTATION);
	}

	private void createRootNodeForNode(Node node)
	{
		CSVWriter.addNoReplaceNode(new RootNode(node.getAddress()));
	}

	private void writeArgumentsAndVariables(Function function)
	{
		FunctionContent content = function.getContent();
		List<VariableOrArgument> varsAndArgs = content
				.getVariablesAndArguments();

		for (VariableOrArgument varOrArg : varsAndArgs)
		{
			createRootNodeForNode(varOrArg);
			createNodeForVarOrArg(varOrArg);
			addEdgeFromRootNode(varOrArg, EdgeTypes.ANNOTATION);
		}
	}

	private void createNodeForVarOrArg(VariableOrArgument varOrArg)
	{
		CSVWriter.addNode(varOrArg);
	}

	private void writeBasicBlocks(Function function)
	{
		Collection<BasicBlock> basicBlocks = function.getContent()
				.getBasicBlocks();
		for (BasicBlock block : basicBlocks)
		{
			writeBasicBlock(block);
			writeEdgeFromFunctionToBasicBlock(function, block);
		}
	}

	private void writeEdgeFromFunctionToBasicBlock(Function function, BasicBlock block)
	{
		Map<String, Object> properties = new HashMap<String, Object>();

		String srcId = function.getKey();
		String dstId = block.getKey();

		CSVWriter.addEdge(srcId, dstId, properties, EdgeTypes.IS_FUNCTION_OF);
	}

	private void writeInstructions(BasicBlock block)
	{
		Collection<Instruction> instructions = block.getInstructions();

		for (Instruction instruction : instructions)
		{
			createRootNodeForNode(instruction);
			writeInstruction(instruction);
			addEdgeFromRootNode(instruction, EdgeTypes.INTERPRETATION);

			writeEdgeFromBlockToInstruction(block, instruction);
		}

	}

	private void writeEdgeFromBlockToInstruction(BasicBlock block,
			Instruction instr)
	{
		Map<String, Object> properties = new HashMap<String, Object>();

		String srcId = block.getKey();
		String dstId = instr.getKey();

		CSVWriter.addEdge(srcId, dstId, properties, EdgeTypes.IS_BB_OF);
	}

	private void writeInstruction(Instruction instr)
	{
		CSVWriter.addNode(instr);
	}

	private void writeNodeForBasicBlock(BasicBlock block)
	{
		CSVWriter.addNode(block);
	}

	private void writeCFGEdges(Function function)
	{
		List<DirectedEdge> edges = function.getContent().getEdges();
		for (DirectedEdge edge : edges)
		{

			NodeKey from = edge.getSourceKey();
			NodeKey to = edge.getDestKey();
			String srcId = from.toString();
			String dstId = to.toString();

			Map<String, Object> properties = new HashMap<>();
			String edgeType = edge.getType();
			CSVWriter.addEdge(srcId, dstId, properties, edgeType);
		}
	}

	private void addEdgeFromRootNode(Node node, String type)
	{
		NodeKey srcKey = node.createEpsilonKey();
		NodeKey destKey = node.createKey();

		DirectedEdge newEdge = new DirectedEdge(srcKey, destKey, type);

		writeEdge(newEdge);
	}

}
