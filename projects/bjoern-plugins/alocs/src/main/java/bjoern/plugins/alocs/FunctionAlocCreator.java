package bjoern.plugins.alocs;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import bjoern.nodeStore.NodeTypes;
import bjoern.pluginlib.GraphOperations;
import bjoern.pluginlib.Traversals;
import bjoern.pluginlib.radare.emulation.EsilEmulator;
import bjoern.pluginlib.structures.BasicBlock;
import bjoern.pluginlib.structures.Instruction;
import bjoern.pluginlib.structures.Node;
import bjoern.r2interface.Radare;
import bjoern.r2interface.architectures.Architecture;
import bjoern.structures.BjoernNodeProperties;
import bjoern.structures.edges.EdgeTypes;

public class FunctionAlocCreator {

	Map<String,Vertex> registerToVertex = new HashMap<String,Vertex>();
	private Radare radare;
	private OrientGraphNoTx graph;
	Vertex functionVertex;
	EsilEmulator emulator;


	FunctionAlocCreator(Radare radare, OrientGraphNoTx graph) throws IOException
	{
		this.radare = radare;
		this.graph = graph;
		this.emulator = new EsilEmulator(radare);
	}

	public void createAlocsForFunction(Vertex function) throws IOException
	{
		functionVertex = function;

		createAlocsForAllInstructions();
		emulateFirstBasicBlock(function);
	}

	private void createAlocsForAllInstructions() throws IOException
	{
		List<Instruction> instructions = Traversals.functionToInstructions(functionVertex);
		for(Instruction instr : instructions){
			createAlocsForInstruction(instr);
		}
	}

	private void createAlocsForInstruction(Instruction instr) throws IOException
	{
		long address = instr.getAddress();

		List<String> registersRead = radare.getRegistersRead(Long.toUnsignedString(address));
		createAlocsForRegisters(instr, registersRead, EdgeTypes.READ);
		List<String> registersWritten = radare.getRegistersWritten(Long.toUnsignedString(address));
		createAlocsForRegisters(instr, registersWritten, EdgeTypes.WRITE);

	}

	private void createAlocsForRegisters(Instruction instr, List<String> registersRead, String edgeType) throws IOException {
		for(String registerStr : registersRead){

			Vertex registerVertex = registerToVertex.get(registerStr);
			if(registerVertex == null){
				registerVertex = createAloc(registerStr);
			}
			GraphOperations.addEdge(graph, instr, new Node(registerVertex), edgeType);
		}
	}

	private Vertex createAloc(String alocName) throws IOException
	{
		String functionAddr = functionVertex.getProperty("addr");
		String subType = subTypeFromAlocName(alocName);

		Map<String, String> properties = new HashMap<String,String>();
		properties.put(BjoernNodeProperties.ADDR, functionAddr);
		properties.put(BjoernNodeProperties.TYPE, NodeTypes.ALOC);
		properties.put(BjoernNodeProperties.SUBTYPE, subType);
		properties.put(BjoernNodeProperties.NAME, alocName);

		Vertex alocVertex = GraphOperations.addNode(graph, properties);
		registerToVertex.put(alocName, alocVertex);
		linkFunctionAndRegister(alocVertex);
		return alocVertex;
	}

	/**
	 * Determines the subtype by register name, e.g., register, flag, local, ...
	 * @throws IOException
	 * */

	private String subTypeFromAlocName(String alocName) throws IOException
	{
		Architecture architecture = radare.getArchitecture();

		if(alocName.startsWith("$") || architecture.isFlag(alocName))
			return AlocTypes.FLAG;


		return AlocTypes.UNKNOWN;
	}

	private void linkFunctionAndRegister(Vertex alocVertex)
	{
		Node functionNode = new Node(functionVertex);
		Node alocNode = new Node(alocVertex);

		GraphOperations.addEdge(graph, functionNode, alocNode, GraphOperations.ALOC_USE_EDGE);
	}

	private void emulateFirstBasicBlock(Vertex function) throws IOException
	{
		BasicBlock entryBlock;
		try{
			entryBlock = Traversals.functionToEntryBlock(function);

		} catch(RuntimeException ex) {
			System.err.println("Warning: function without entry block");
			return;
		}

		emulator.emulateWithoutCalls(entryBlock.getInstructions());
		long rbp = emulator.getRegisterValue("rbp");
		long rsp = emulator.getRegisterValue("rsp");
	}

}
