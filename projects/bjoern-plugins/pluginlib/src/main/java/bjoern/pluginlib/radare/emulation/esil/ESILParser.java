package bjoern.pluginlib.radare.emulation.esil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import bjoern.pluginlib.structures.Instruction;

public class ESILParser {

	private static final Set<String> POKE_TOKENS =
			new HashSet<String>(Arrays.asList(
					ESILKeyword.POKE.keyword, ESILKeyword.POKE1.keyword,
					ESILKeyword.POKE2.keyword, ESILKeyword.POKE4.keyword,
					ESILKeyword.POKE8.keyword, ESILKeyword.POKE_AST.keyword
			));


	private static final Set<String> PEEK_TOKENS =
			new HashSet<String>(Arrays.asList(

			ESILKeyword.PEEK.keyword, ESILKeyword.PEEK1.keyword,
			ESILKeyword.PEEK2.keyword, ESILKeyword.PEEK4.keyword,
			ESILKeyword.PEEK8.keyword, ESILKeyword.PEEK_AST.keyword));

	private static Set<String> MEM_ACCESS_TOKENS = new HashSet<String>();

	static
	{
		MEM_ACCESS_TOKENS.addAll(POKE_TOKENS);
		MEM_ACCESS_TOKENS.addAll(PEEK_TOKENS);
	}

	public List<String> extractMemoryAccesses(String esilCode, Instruction instr)
	{
		ESILTokenStream stream = new ESILTokenStream(esilCode);

		List<String> retList = new LinkedList<String>();

		int index;
		while((index = stream.skipUntilToken(MEM_ACCESS_TOKENS)) !=
				ESILTokenStream.TOKEN_NOT_FOUND)
		{
			String memoryAccess = createMemoryAccessAt(stream, index, instr);
			if(memoryAccess != null)
				retList.add(memoryAccess);
		}

		return retList;
	}

	private String createMemoryAccessAt(ESILTokenStream stream, int index, Instruction instr)
	{
		String esilCode = stream.getEsilCodeForAccess(index);

		// TODO: make this platform independent

		if(!esilCode.contains("rbp"))
			return null;

		return esilCode;
	}



}
